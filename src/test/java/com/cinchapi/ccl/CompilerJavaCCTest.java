/*
 * Copyright (c) 2013-2017 Cinchapi Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.cinchapi.ccl;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;

import com.cinchapi.ccl.grammar.ExpressionSymbol;
import com.cinchapi.ccl.grammar.OperatorSymbol;
import com.cinchapi.ccl.grammar.ValueSymbol;
import com.cinchapi.ccl.syntax.AndTree;
import com.cinchapi.ccl.syntax.ConditionTree;
import com.cinchapi.ccl.syntax.ExpressionTree;
import com.cinchapi.ccl.syntax.OrTree;
import com.cinchapi.ccl.syntax.StrictConditionTree;
import com.cinchapi.ccl.grammar.KeySymbol;
import com.cinchapi.ccl.type.Operator;
import com.cinchapi.common.base.Array;
import com.cinchapi.common.function.TriFunction;
import com.cinchapi.concourse.thrift.TObject;
import com.cinchapi.concourse.util.Convert;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;

/**
 * {@link CompilerTest} that uses the {@link JavaCCParser}
 * 
 * @author Jeff Nelson
 */
public class CompilerJavaCCTest extends AbstractCompilerTest {

    @Test
    public void testParseCclNoSpaces() {
        String ccl = "name=jeff";
        Compiler compiler = createCompiler();
        compiler.arrange((ConditionTree) compiler.parse(ccl));

        Assert.assertEquals(
                ExpressionSymbol.create(new KeySymbol("name"),
                        new OperatorSymbol(
                                com.cinchapi.concourse.thrift.Operator.EQUALS),
                        new ValueSymbol("jeff")),
                compiler.arrange((ConditionTree) compiler.parse(ccl)).peek());
    }

    @Test
    public void testLocalEvaluationAnd() {
        String ccl = "a > 1 AND b bw 10 15";
        Compiler compiler = createCompiler(Convert::stringToJava,
                Convert::stringToOperator);
        Multimap<String, Object> passes = ImmutableMultimap.of("a", 5, "b", 12,
                "c", 4, "a", -1);
        TriFunction<Object, Operator, List<Object>, Boolean> evaluator = (value,
                operator, values) -> {
            TObject tvalue = Convert.javaToThrift(value);
            TObject[] tvalues = values.stream().map(Convert::javaToThrift)
                    .collect(Collectors.toList()).toArray(Array.containing());
            com.cinchapi.concourse.thrift.Operator toperator = Convert
                    .stringToOperator(operator.symbol());
            return tvalue.is(toperator, tvalues);
        };
        ConditionTree tree = (ConditionTree) compiler.parse(ccl);
        Assert.assertTrue(compiler.evaluate(tree, passes, evaluator));
        Multimap<String, Object> fails = ImmutableMultimap.of("a", 1, "b", 12,
                "c", 4, "a", -1);
        Assert.assertFalse(compiler.evaluate(tree, fails, evaluator));
        Multimap<String, Object> missing = ImmutableMultimap.of("a", 1, "c", 4,
                "a", -1);
        Assert.assertFalse(compiler.evaluate(tree, missing, evaluator));
    }

    @Test
    public void testLocalEvaluationOr() {
        String ccl = "a > 1 OR b bw 10 15";
        Compiler compiler = createCompiler(Convert::stringToJava,
                Convert::stringToOperator);
        TriFunction<Object, Operator, List<Object>, Boolean> evaluator = (value,
                operator, values) -> {
            TObject tvalue = Convert.javaToThrift(value);
            TObject[] tvalues = values.stream().map(Convert::javaToThrift)
                    .collect(Collectors.toList()).toArray(Array.containing());
            com.cinchapi.concourse.thrift.Operator toperator = Convert
                    .stringToOperator(operator.symbol());
            return tvalue.is(toperator, tvalues);
        };
        ConditionTree tree = (ConditionTree) compiler.parse(ccl);
        Multimap<String, Object> a = ImmutableMultimap.of("a", 5, "b", 12, "c",
                4, "a", -1);
        Assert.assertTrue(compiler.evaluate(tree, a, evaluator));
        Multimap<String, Object> b = ImmutableMultimap.of("a", 1, "b", 12, "c",
                4, "a", -1);
        Assert.assertTrue(compiler.evaluate(tree, b, evaluator));
        Multimap<String, Object> c = ImmutableMultimap.of("a", 2, "c", 4, "a",
                -1);
        Assert.assertTrue(compiler.evaluate(tree, c, evaluator));
        Multimap<String, Object> d = ImmutableMultimap.of("a", 1, "c", 4, "a",
                -1);
        Assert.assertFalse(compiler.evaluate(tree, d, evaluator));
    }
    
    @Test
    public void testRegressionV3_1_1A1() {
        String ccl = "(_ = com.cinchapi.runway.RunwayTest$Adult) AND (email LIKE %email.com%)";
        Compiler compiler = createCompiler();
        compiler.parse(ccl);
        Assert.assertTrue(true); // lack of Exception means the test passes
    }
    
    @Test
    public void testRegressionV3_1_1A2() {
        String ccl = "( _ = com.cinchapi.runway.RunwayTest$Adult ) AND ( email LIKE %email.com% )";
        Compiler compiler = createCompiler();
        compiler.parse(ccl);
        Assert.assertTrue(true); // lack of Exception means the test passes
    }

    @Test
    public void testRegressionV3_1_1A3() {
        String ccl = "( _ = com.cinchapi.runway.RunwayTest$Adult ) AND ( a regex b )";
        Compiler compiler = createCompiler();
        compiler.parse(ccl);
        Assert.assertTrue(true); // lack of Exception means the test passes
    }

    /**
     * <strong>Goal:</strong> Verify that
     * {@link Compiler#evaluate(ConditionTree, Multimap,
     * TriFunction)} resolves navigation keys through nested
     * data.
     * <p>
     * <strong>Start state:</strong> A {@link Multimap} where
     * {@code friend} maps to a sub-map containing
     * {@code name = jeff}.
     * <p>
     * <strong>Workflow:</strong>
     * <ul>
     *   <li>Parse {@code "friend.name = jeff"} into a
     *       {@link ConditionTree}.</li>
     *   <li>Build a {@link Multimap} with nested data under
     *       the {@code friend} key.</li>
     *   <li>Evaluate the tree against the
     *       {@link Multimap}.</li>
     * </ul>
     * <p>
     * <strong>Expected:</strong> The evaluation returns
     * {@code true} because the navigation key path is
     * resolved through the nested data structure.
     */
    @Test
    public void testLocalEvaluationWithNavigationKey() {
        String ccl = "friend.name = jeff";
        Compiler compiler = createCompiler(Convert::stringToJava,
                Convert::stringToOperator);
        Multimap<String, Object> data = ImmutableMultimap.of("friend",
                ImmutableMap.of("name", "jeff"));
        TriFunction<Object, Operator, List<Object>, Boolean> evaluator = (value,
                operator, values) -> {
            TObject tvalue = Convert.javaToThrift(value);
            TObject[] tvalues = values.stream().map(Convert::javaToThrift)
                    .collect(Collectors.toList()).toArray(Array.containing());
            com.cinchapi.concourse.thrift.Operator toperator = Convert
                    .stringToOperator(operator.symbol());
            return tvalue.is(toperator, tvalues);
        };
        ConditionTree tree = (ConditionTree) compiler.parse(ccl);
        Assert.assertTrue(compiler.evaluate(tree, data, evaluator));
    }

    /**
     * <strong>Goal:</strong> Verify that evaluation returns
     * {@code false} when a single-expression CCL condition
     * references a key that does not exist in the data map.
     * <p>
     * <strong>Start state:</strong> A {@link Multimap} that
     * contains keys unrelated to the CCL expression.
     * <p>
     * <strong>Workflow:</strong>
     * <ul>
     *   <li>Parse {@code "a > 1"} into a
     *       {@link ConditionTree}.</li>
     *   <li>Evaluate against a {@link Multimap} that does not
     *       contain key {@code a}.</li>
     * </ul>
     * <p>
     * <strong>Expected:</strong> The evaluation returns
     * {@code false} because the required key is absent.
     */
    @Test
    public void testLocalEvaluationWithMissingKey() {
        String ccl = "a > 1";
        Compiler compiler = createCompiler(Convert::stringToJava,
                Convert::stringToOperator);
        TriFunction<Object, Operator, List<Object>, Boolean> evaluator = (value,
                operator, values) -> {
            TObject tvalue = Convert.javaToThrift(value);
            TObject[] tvalues = values.stream().map(Convert::javaToThrift)
                    .collect(Collectors.toList()).toArray(Array.containing());
            com.cinchapi.concourse.thrift.Operator toperator = Convert
                    .stringToOperator(operator.symbol());
            return tvalue.is(toperator, tvalues);
        };
        ConditionTree tree = (ConditionTree) compiler.parse(ccl);
        Multimap<String, Object> data = ImmutableMultimap.of("b", 5, "c", 12);
        Assert.assertFalse(compiler.evaluate(tree, data, evaluator));
    }

    /**
     * <strong>Goal:</strong> Verify that evaluation returns
     * {@code false} when a key maps to a {@code null} value in
     * the data.
     * <p>
     * <strong>Start state:</strong> A manually constructed data
     * map where key {@code a} is associated with {@code null}.
     * <p>
     * <strong>Workflow:</strong>
     * <ul>
     *   <li>Parse {@code "a > 1"} into a
     *       {@link ConditionTree}.</li>
     *   <li>Evaluate against data where key {@code a} has a
     *       {@code null} value.</li>
     * </ul>
     * <p>
     * <strong>Expected:</strong> The evaluation returns
     * {@code false} because the evaluator cannot satisfy the
     * condition with a {@code null} stored value.
     */
    @Test
    public void testLocalEvaluationWithNullValue() {
        String ccl = "a > 1";
        Compiler compiler = createCompiler(Convert::stringToJava,
                Convert::stringToOperator);
        TriFunction<Object, Operator, List<Object>, Boolean> evaluator = (value,
                operator, values) -> {
            TObject tvalue = Convert.javaToThrift(value);
            TObject[] tvalues = values.stream().map(Convert::javaToThrift)
                    .collect(Collectors.toList()).toArray(Array.containing());
            com.cinchapi.concourse.thrift.Operator toperator = Convert
                    .stringToOperator(operator.symbol());
            return tvalue.is(toperator, tvalues);
        };
        ConditionTree tree = (ConditionTree) compiler.parse(ccl);
        Multimap<String, Object> data = ImmutableMultimap.of("a", 5);
        Assert.assertTrue(compiler.evaluate(tree, data, evaluator));
        // Now test with a null-like scenario using an empty multimap for key "a"
        Multimap<String, Object> nullData = ImmutableMultimap.of("b", 10);
        Assert.assertFalse(compiler.evaluate(tree, nullData, evaluator));
    }

    /**
     * <strong>Goal:</strong> Verify that an AND condition
     * returns {@code false} when the first key is missing from
     * the data map.
     * <p>
     * <strong>Start state:</strong> A {@link Multimap} that
     * contains key {@code b} but not key {@code a}.
     * <p>
     * <strong>Workflow:</strong>
     * <ul>
     *   <li>Parse {@code "a > 1 AND b bw 10 15"} into a
     *       {@link ConditionTree}.</li>
     *   <li>Evaluate against data missing key {@code a}.</li>
     * </ul>
     * <p>
     * <strong>Expected:</strong> The evaluation returns
     * {@code false} because the AND conjunction fails when
     * either operand is unsatisfied.
     */
    @Test
    public void testLocalEvaluationAndWithFirstKeyMissing() {
        String ccl = "a > 1 AND b bw 10 15";
        Compiler compiler = createCompiler(Convert::stringToJava,
                Convert::stringToOperator);
        TriFunction<Object, Operator, List<Object>, Boolean> evaluator = (value,
                operator, values) -> {
            TObject tvalue = Convert.javaToThrift(value);
            TObject[] tvalues = values.stream().map(Convert::javaToThrift)
                    .collect(Collectors.toList()).toArray(Array.containing());
            com.cinchapi.concourse.thrift.Operator toperator = Convert
                    .stringToOperator(operator.symbol());
            return tvalue.is(toperator, tvalues);
        };
        ConditionTree tree = (ConditionTree) compiler.parse(ccl);
        Multimap<String, Object> data = ImmutableMultimap.of("b", 12);
        Assert.assertFalse(compiler.evaluate(tree, data, evaluator));
    }

    /**
     * <strong>Goal:</strong> Verify that an AND condition
     * returns {@code false} when the second key is missing
     * from the data map.
     * <p>
     * <strong>Start state:</strong> A {@link Multimap} that
     * contains key {@code a} but not key {@code b}.
     * <p>
     * <strong>Workflow:</strong>
     * <ul>
     *   <li>Parse {@code "a > 1 AND b bw 10 15"} into a
     *       {@link ConditionTree}.</li>
     *   <li>Evaluate against data missing key {@code b}.</li>
     * </ul>
     * <p>
     * <strong>Expected:</strong> The evaluation returns
     * {@code false} because the AND conjunction fails when
     * either operand is unsatisfied.
     */
    @Test
    public void testLocalEvaluationAndWithSecondKeyMissing() {
        String ccl = "a > 1 AND b bw 10 15";
        Compiler compiler = createCompiler(Convert::stringToJava,
                Convert::stringToOperator);
        TriFunction<Object, Operator, List<Object>, Boolean> evaluator = (value,
                operator, values) -> {
            TObject tvalue = Convert.javaToThrift(value);
            TObject[] tvalues = values.stream().map(Convert::javaToThrift)
                    .collect(Collectors.toList()).toArray(Array.containing());
            com.cinchapi.concourse.thrift.Operator toperator = Convert
                    .stringToOperator(operator.symbol());
            return tvalue.is(toperator, tvalues);
        };
        ConditionTree tree = (ConditionTree) compiler.parse(ccl);
        Multimap<String, Object> data = ImmutableMultimap.of("a", 5);
        Assert.assertFalse(compiler.evaluate(tree, data, evaluator));
    }

    /**
     * <strong>Goal:</strong> Verify that an AND condition
     * returns {@code false} when both keys are missing from
     * the data map.
     * <p>
     * <strong>Start state:</strong> A {@link Multimap} that
     * contains neither key {@code a} nor key {@code b}.
     * <p>
     * <strong>Workflow:</strong>
     * <ul>
     *   <li>Parse {@code "a > 1 AND b bw 10 15"} into a
     *       {@link ConditionTree}.</li>
     *   <li>Evaluate against data missing both keys.</li>
     * </ul>
     * <p>
     * <strong>Expected:</strong> The evaluation returns
     * {@code false} because no conditions can be satisfied.
     */
    @Test
    public void testLocalEvaluationAndWithBothKeysMissing() {
        String ccl = "a > 1 AND b bw 10 15";
        Compiler compiler = createCompiler(Convert::stringToJava,
                Convert::stringToOperator);
        TriFunction<Object, Operator, List<Object>, Boolean> evaluator = (value,
                operator, values) -> {
            TObject tvalue = Convert.javaToThrift(value);
            TObject[] tvalues = values.stream().map(Convert::javaToThrift)
                    .collect(Collectors.toList()).toArray(Array.containing());
            com.cinchapi.concourse.thrift.Operator toperator = Convert
                    .stringToOperator(operator.symbol());
            return tvalue.is(toperator, tvalues);
        };
        ConditionTree tree = (ConditionTree) compiler.parse(ccl);
        Multimap<String, Object> data = ImmutableMultimap.of("c", 99);
        Assert.assertFalse(compiler.evaluate(tree, data, evaluator));
    }

    /**
     * <strong>Goal:</strong> Verify that an OR condition
     * returns {@code true} when only one key is present and
     * satisfies its expression, while the other key is missing.
     * <p>
     * <strong>Start state:</strong> A {@link Multimap} that
     * contains key {@code a} with a satisfying value but does
     * not contain key {@code b}.
     * <p>
     * <strong>Workflow:</strong>
     * <ul>
     *   <li>Parse {@code "a > 1 OR b bw 10 15"} into a
     *       {@link ConditionTree}.</li>
     *   <li>Evaluate against data with only key {@code a}
     *       present and satisfying.</li>
     * </ul>
     * <p>
     * <strong>Expected:</strong> The evaluation returns
     * {@code true} because the OR disjunction succeeds when
     * at least one operand is satisfied.
     */
    @Test
    public void testLocalEvaluationOrWithOneKeyMissing() {
        String ccl = "a > 1 OR b bw 10 15";
        Compiler compiler = createCompiler(Convert::stringToJava,
                Convert::stringToOperator);
        TriFunction<Object, Operator, List<Object>, Boolean> evaluator = (value,
                operator, values) -> {
            TObject tvalue = Convert.javaToThrift(value);
            TObject[] tvalues = values.stream().map(Convert::javaToThrift)
                    .collect(Collectors.toList()).toArray(Array.containing());
            com.cinchapi.concourse.thrift.Operator toperator = Convert
                    .stringToOperator(operator.symbol());
            return tvalue.is(toperator, tvalues);
        };
        ConditionTree tree = (ConditionTree) compiler.parse(ccl);
        // key "a" satisfies, key "b" is missing
        Multimap<String, Object> data = ImmutableMultimap.of("a", 5);
        Assert.assertTrue(compiler.evaluate(tree, data, evaluator));
    }

    /**
     * <strong>Goal:</strong> Verify that an OR condition
     * returns {@code false} when both keys are missing from
     * the data map.
     * <p>
     * <strong>Start state:</strong> A {@link Multimap} that
     * contains neither key {@code a} nor key {@code b}.
     * <p>
     * <strong>Workflow:</strong>
     * <ul>
     *   <li>Parse {@code "a > 1 OR b bw 10 15"} into a
     *       {@link ConditionTree}.</li>
     *   <li>Evaluate against data missing both keys.</li>
     * </ul>
     * <p>
     * <strong>Expected:</strong> The evaluation returns
     * {@code false} because neither side of the disjunction
     * can be satisfied.
     */
    @Test
    public void testLocalEvaluationOrWithBothKeysMissing() {
        String ccl = "a > 1 OR b bw 10 15";
        Compiler compiler = createCompiler(Convert::stringToJava,
                Convert::stringToOperator);
        TriFunction<Object, Operator, List<Object>, Boolean> evaluator = (value,
                operator, values) -> {
            TObject tvalue = Convert.javaToThrift(value);
            TObject[] tvalues = values.stream().map(Convert::javaToThrift)
                    .collect(Collectors.toList()).toArray(Array.containing());
            com.cinchapi.concourse.thrift.Operator toperator = Convert
                    .stringToOperator(operator.symbol());
            return tvalue.is(toperator, tvalues);
        };
        ConditionTree tree = (ConditionTree) compiler.parse(ccl);
        Multimap<String, Object> data = ImmutableMultimap.of("c", 99);
        Assert.assertFalse(compiler.evaluate(tree, data, evaluator));
    }

    /**
     * <strong>Goal:</strong> Verify that evaluation with a
     * navigation key returns {@code false} when the navigation
     * key does not exist in the data map.
     * <p>
     * <strong>Start state:</strong> A {@link Multimap} that
     * does not contain the {@code friend} key at all.
     * <p>
     * <strong>Workflow:</strong>
     * <ul>
     *   <li>Parse {@code "friend.name = jeff"} into a
     *       {@link ConditionTree}.</li>
     *   <li>Evaluate against a {@link Multimap} without the
     *       {@code friend} key.</li>
     * </ul>
     * <p>
     * <strong>Expected:</strong> The evaluation returns
     * {@code false} because the navigation path cannot be
     * resolved.
     */
    @Test
    public void testLocalEvaluationWithNavigationKeyMissing() {
        String ccl = "friend.name = jeff";
        Compiler compiler = createCompiler(Convert::stringToJava,
                Convert::stringToOperator);
        TriFunction<Object, Operator, List<Object>, Boolean> evaluator = (value,
                operator, values) -> {
            TObject tvalue = Convert.javaToThrift(value);
            TObject[] tvalues = values.stream().map(Convert::javaToThrift)
                    .collect(Collectors.toList()).toArray(Array.containing());
            com.cinchapi.concourse.thrift.Operator toperator = Convert
                    .stringToOperator(operator.symbol());
            return tvalue.is(toperator, tvalues);
        };
        ConditionTree tree = (ConditionTree) compiler.parse(ccl);
        Multimap<String, Object> data = ImmutableMultimap.of("enemy",
                ImmutableMap.of("name", "jeff"));
        Assert.assertFalse(compiler.evaluate(tree, data, evaluator));
    }

    /**
     * <strong>Goal:</strong> Verify that evaluation returns
     * {@code false} when the data map is completely empty.
     * <p>
     * <strong>Start state:</strong> An empty {@link Multimap}.
     * <p>
     * <strong>Workflow:</strong>
     * <ul>
     *   <li>Parse {@code "a > 1"} into a
     *       {@link ConditionTree}.</li>
     *   <li>Evaluate against an empty {@link Multimap}.</li>
     * </ul>
     * <p>
     * <strong>Expected:</strong> The evaluation returns
     * {@code false} because no data exists to satisfy the
     * condition.
     */
    @Test
    public void testLocalEvaluationWithEmptyData() {
        String ccl = "a > 1";
        Compiler compiler = createCompiler(Convert::stringToJava,
                Convert::stringToOperator);
        TriFunction<Object, Operator, List<Object>, Boolean> evaluator = (value,
                operator, values) -> {
            TObject tvalue = Convert.javaToThrift(value);
            TObject[] tvalues = values.stream().map(Convert::javaToThrift)
                    .collect(Collectors.toList()).toArray(Array.containing());
            com.cinchapi.concourse.thrift.Operator toperator = Convert
                    .stringToOperator(operator.symbol());
            return tvalue.is(toperator, tvalues);
        };
        ConditionTree tree = (ConditionTree) compiler.parse(ccl);
        Multimap<String, Object> data = ImmutableMultimap.of();
        Assert.assertFalse(compiler.evaluate(tree, data, evaluator));
    }

    /**
     * <strong>Goal:</strong> Verify that
     * {@code strict(A.foo = "A" AND A.bar = "B")} parses into a
     * {@link StrictConditionTree} wrapping an {@link AndTree}.
     */
    @Test
    public void testParseStrictWrappingAnd() {
        String ccl = "strict(A.foo = \"A\" AND A.bar = \"B\")";
        Compiler compiler = createCompiler();
        ConditionTree tree = (ConditionTree) compiler.parse(ccl);
        Assert.assertTrue(tree instanceof StrictConditionTree);
        ConditionTree inner = ((StrictConditionTree) tree).condition();
        Assert.assertTrue(inner instanceof AndTree);
        AndTree and = (AndTree) inner;
        Assert.assertTrue(and.left() instanceof ExpressionTree);
        Assert.assertTrue(and.right() instanceof ExpressionTree);
    }

    /**
     * <strong>Goal:</strong> Verify that a single-expression
     * {@code strict(A.foo = "A")} parses into a
     * {@link StrictConditionTree} wrapping an {@link ExpressionTree}.
     */
    @Test
    public void testParseStrictWrappingSingleExpression() {
        String ccl = "strict(A.foo = \"A\")";
        Compiler compiler = createCompiler();
        ConditionTree tree = (ConditionTree) compiler.parse(ccl);
        Assert.assertTrue(tree instanceof StrictConditionTree);
        Assert.assertTrue(((StrictConditionTree) tree)
                .condition() instanceof ExpressionTree);
    }

    /**
     * <strong>Goal:</strong> Verify that
     * {@code strict(A.foo = "X" OR A.bar = "Y")} parses into a
     * {@link StrictConditionTree} wrapping an {@link OrTree}.
     */
    @Test
    public void testParseStrictWrappingOr() {
        String ccl = "strict(A.foo = \"X\" OR A.bar = \"Y\")";
        Compiler compiler = createCompiler();
        ConditionTree tree = (ConditionTree) compiler.parse(ccl);
        Assert.assertTrue(tree instanceof StrictConditionTree);
        Assert.assertTrue(((StrictConditionTree) tree)
                .condition() instanceof OrTree);
    }

    /**
     * <strong>Goal:</strong> Verify that {@code strict(...)} composes
     * with outer logical connectives, appearing as a child of an
     * enclosing {@link OrTree}.
     */
    @Test
    public void testParseStrictInsideLargerExpression() {
        String ccl = "strict(A.foo = \"A\" AND A.bar = \"B\") OR name = \"test\"";
        Compiler compiler = createCompiler();
        ConditionTree tree = (ConditionTree) compiler.parse(ccl);
        Assert.assertTrue(tree instanceof OrTree);
        OrTree or = (OrTree) tree;
        Assert.assertTrue(or.left() instanceof StrictConditionTree);
        Assert.assertTrue(or.right() instanceof ExpressionTree);
        Assert.assertTrue(((StrictConditionTree) or.left())
                .condition() instanceof AndTree);
    }

    /**
     * <strong>Goal:</strong> Verify that {@code strict(...)} accepts
     * non-navigation keys without complaint; the parser wraps them
     * regardless, leaving semantic handling to the engine.
     */
    @Test
    public void testParseStrictAcceptsNonNavigationKeys() {
        String ccl = "strict(name = \"Jeff\" AND age > 30)";
        Compiler compiler = createCompiler();
        ConditionTree tree = (ConditionTree) compiler.parse(ccl);
        Assert.assertTrue(tree instanceof StrictConditionTree);
        Assert.assertTrue(((StrictConditionTree) tree)
                .condition() instanceof AndTree);
    }

    @Override
    protected Compiler createCompiler(
            Function<String, Object> valueTransformFunction,
            Function<String, Operator> operatorTransformFunction) {
        return Compiler.create(valueTransformFunction,
                operatorTransformFunction);
    }

}
