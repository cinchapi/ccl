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

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;

import com.cinchapi.ccl.grammar.ExpressionSymbol;
import com.cinchapi.ccl.grammar.NavigationKeySymbol;
import com.cinchapi.ccl.grammar.OperatorSymbol;
import com.cinchapi.ccl.grammar.ParenthesisSymbol;
import com.cinchapi.ccl.grammar.PostfixNotationSymbol;
import com.cinchapi.ccl.grammar.ScopeEndSymbol;
import com.cinchapi.ccl.grammar.NavigationKeyStop;
import com.cinchapi.ccl.grammar.ScopeSymbol;
import com.cinchapi.ccl.grammar.Symbol;
import com.cinchapi.ccl.grammar.TemporalKeySymbol;
import com.cinchapi.ccl.grammar.TimestampSymbol;
import com.cinchapi.ccl.grammar.ValueSymbol;
import com.cinchapi.ccl.syntax.AbstractSyntaxTree;
import com.cinchapi.ccl.syntax.AndTree;
import com.cinchapi.ccl.syntax.CommandTree;
import com.cinchapi.ccl.syntax.ConditionTree;
import com.cinchapi.ccl.syntax.ExpressionTree;
import com.cinchapi.ccl.syntax.OrTree;
import com.cinchapi.ccl.syntax.ScopedConditionTree;
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
     * <strong>Goal:</strong> Verify that {@code A.(foo = "A" AND bar = "B")}
     * parses into a {@link ScopedConditionTree} whose
     * {@link ScopedConditionTree#prefix() prefix} is {@code A} and whose
     * {@link ScopedConditionTree#condition() inner} is an {@link AndTree}
     * of leaf expressions.
     */
    @Test
    public void testParseScopedWrappingAnd() {
        String ccl = "A.(foo = \"A\" AND bar = \"B\")";
        Compiler compiler = createCompiler();
        ConditionTree tree = (ConditionTree) compiler.parse(ccl);
        Assert.assertTrue(tree instanceof ScopedConditionTree);
        ScopedConditionTree scoped = (ScopedConditionTree) tree;
        Assert.assertEquals("A", scoped.prefix().key().toString());
        Assert.assertTrue(scoped.prefix() instanceof KeySymbol);
        ConditionTree inner = scoped.condition();
        Assert.assertTrue(inner instanceof AndTree);
        AndTree and = (AndTree) inner;
        Assert.assertTrue(and.left() instanceof ExpressionTree);
        Assert.assertTrue(and.right() instanceof ExpressionTree);
    }

    /**
     * <strong>Goal:</strong> Verify that a single-expression
     * {@code A.(foo = "A")} parses into a {@link ScopedConditionTree}
     * wrapping an {@link ExpressionTree}.
     */
    @Test
    public void testParseScopedWrappingSingleExpression() {
        String ccl = "A.(foo = \"A\")";
        Compiler compiler = createCompiler();
        ConditionTree tree = (ConditionTree) compiler.parse(ccl);
        Assert.assertTrue(tree instanceof ScopedConditionTree);
        Assert.assertTrue(((ScopedConditionTree) tree)
                .condition() instanceof ExpressionTree);
    }

    /**
     * <strong>Goal:</strong> Verify that {@code A.(foo = "X" OR bar = "Y")}
     * parses into a {@link ScopedConditionTree} wrapping an {@link OrTree}.
     */
    @Test
    public void testParseScopedWrappingOr() {
        String ccl = "A.(foo = \"X\" OR bar = \"Y\")";
        Compiler compiler = createCompiler();
        ConditionTree tree = (ConditionTree) compiler.parse(ccl);
        Assert.assertTrue(tree instanceof ScopedConditionTree);
        Assert.assertTrue(((ScopedConditionTree) tree)
                .condition() instanceof OrTree);
    }

    /**
     * <strong>Goal:</strong> Verify that a multi-segment scope prefix
     * (e.g. {@code a.b.(...)}) parses into a {@link ScopedConditionTree}
     * whose prefix is a {@link NavigationKeySymbol} carrying the full
     * dotted path.
     */
    @Test
    public void testParseScopedWithMultiSegmentPrefix() {
        String ccl = "a.b.(foo = \"X\" AND bar = \"Y\")";
        Compiler compiler = createCompiler();
        ConditionTree tree = (ConditionTree) compiler.parse(ccl);
        Assert.assertTrue(tree instanceof ScopedConditionTree);
        ScopedConditionTree scoped = (ScopedConditionTree) tree;
        Assert.assertTrue(scoped.prefix() instanceof NavigationKeySymbol);
        Assert.assertEquals("a.b", scoped.prefix().key().toString());
    }

    /**
     * <strong>Goal:</strong> Verify that a scoped prefix containing a
     * transitive marker (e.g. {@code children*.(...)}) parses into a
     * {@link ScopedConditionTree} whose prefix preserves the {@code *}.
     */
    @Test
    public void testParseScopedWithTransitivePrefix() {
        String ccl = "children*.(name = \"Jeff\")";
        Compiler compiler = createCompiler();
        ConditionTree tree = (ConditionTree) compiler.parse(ccl);
        Assert.assertTrue(tree instanceof ScopedConditionTree);
        ScopedConditionTree scoped = (ScopedConditionTree) tree;
        Assert.assertTrue(scoped.prefix() instanceof NavigationKeySymbol);
        Assert.assertEquals("children*", scoped.prefix().key().toString());
    }

    /**
     * <strong>Goal:</strong> Verify that three-or-more conditions inside a
     * scope parse into a left-associative {@link AndTree} chain, each leaf
     * of which is an {@link ExpressionTree}.
     */
    @Test
    public void testParseScopedWithThreeConditions() {
        String ccl = "a.(foo = \"X\" AND bar = \"Y\" AND baz = \"Z\")";
        Compiler compiler = createCompiler();
        ConditionTree tree = (ConditionTree) compiler.parse(ccl);
        Assert.assertTrue(tree instanceof ScopedConditionTree);
        ConditionTree inner = ((ScopedConditionTree) tree).condition();
        Assert.assertTrue(inner instanceof AndTree);
        AndTree and = (AndTree) inner;
        // AND is left-associative: ((foo AND bar) AND baz)
        Assert.assertTrue(and.right() instanceof ExpressionTree);
        Assert.assertTrue(and.left() instanceof AndTree);
        AndTree leftAnd = (AndTree) and.left();
        Assert.assertTrue(leftAnd.left() instanceof ExpressionTree);
        Assert.assertTrue(leftAnd.right() instanceof ExpressionTree);
    }

    /**
     * <strong>Goal:</strong> Verify that a transitive prefix composes with
     * multiple inner conditions. The pivot is the (potentially unbounded)
     * set of records reachable via the transitive stop.
     */
    @Test
    public void testParseScopedTransitiveWithMultipleConditions() {
        String ccl = "children*.(name = \"Jeff\" AND age > 30 AND active = true)";
        Compiler compiler = createCompiler();
        ConditionTree tree = (ConditionTree) compiler.parse(ccl);
        Assert.assertTrue(tree instanceof ScopedConditionTree);
        ScopedConditionTree scoped = (ScopedConditionTree) tree;
        Assert.assertTrue(scoped.prefix() instanceof NavigationKeySymbol);
        Assert.assertEquals("children*", scoped.prefix().key().toString());
        Assert.assertTrue(scoped.condition() instanceof AndTree);
    }

    /**
     * <strong>Goal:</strong> Verify that a transitive marker on an
     * intermediate segment of a multi-segment prefix is preserved. Such
     * prefixes traverse the transitive segment to exhaustion before
     * continuing to the next segment.
     */
    @Test
    public void testParseScopedMultiSegmentWithInteriorTransitive() {
        String ccl = "a.b*.c.(foo = \"X\" AND bar = \"Y\")";
        Compiler compiler = createCompiler();
        ConditionTree tree = (ConditionTree) compiler.parse(ccl);
        Assert.assertTrue(tree instanceof ScopedConditionTree);
        ScopedConditionTree scoped = (ScopedConditionTree) tree;
        Assert.assertTrue(scoped.prefix() instanceof NavigationKeySymbol);
        Assert.assertEquals("a.b*.c", scoped.prefix().key().toString());
        NavigationKeySymbol nav = (NavigationKeySymbol) scoped.prefix();
        Assert.assertArrayEquals(new String[] { "a", "b*", "c" },
                nav.components());
    }

    /**
     * <strong>Goal:</strong> Verify that a transitive marker on the last
     * segment of a multi-segment prefix parses and is preserved.
     */
    @Test
    public void testParseScopedMultiSegmentWithTerminalTransitive() {
        String ccl = "a.b.children*.(name = \"Jeff\")";
        Compiler compiler = createCompiler();
        ConditionTree tree = (ConditionTree) compiler.parse(ccl);
        Assert.assertTrue(tree instanceof ScopedConditionTree);
        ScopedConditionTree scoped = (ScopedConditionTree) tree;
        Assert.assertEquals("a.b.children*",
                scoped.prefix().key().toString());
    }

    /**
     * <strong>Goal:</strong> Verify that inner conditions inside a scope
     * can themselves be multi-segment navigation keys, resolved relative
     * to the outer pivot. This is the real-world pattern from
     * cinchapi/concourse#533 where the suffix continues navigation past
     * the pivot.
     */
    @Test
    public void testParseScopedWithNavigationKeysInInner() {
        String ccl = "a.(b.c = \"X\" AND d.e.f = \"Y\")";
        Compiler compiler = createCompiler();
        ConditionTree tree = (ConditionTree) compiler.parse(ccl);
        Assert.assertTrue(tree instanceof ScopedConditionTree);
        ScopedConditionTree scoped = (ScopedConditionTree) tree;
        Assert.assertEquals("a", scoped.prefix().key().toString());
        Assert.assertTrue(scoped.condition() instanceof AndTree);
        AndTree and = (AndTree) scoped.condition();
        ExpressionTree left = (ExpressionTree) and.left();
        ExpressionTree right = (ExpressionTree) and.right();
        Assert.assertTrue(
                ((ExpressionSymbol) left.root()).raw().key().equals("b.c"));
        Assert.assertTrue(
                ((ExpressionSymbol) right.root()).raw().key().equals("d.e.f"));
    }

    /**
     * <strong>Goal:</strong> Verify that a scope containing mixed AND/OR
     * with parenthesised subexpression parses correctly, preserving the
     * intended precedence inside the pivot.
     */
    @Test
    public void testParseScopedWithMixedAndOrInside() {
        String ccl = "a.(foo = \"X\" AND (bar = \"Y\" OR baz = \"Z\"))";
        Compiler compiler = createCompiler();
        ConditionTree tree = (ConditionTree) compiler.parse(ccl);
        Assert.assertTrue(tree instanceof ScopedConditionTree);
        ConditionTree inner = ((ScopedConditionTree) tree).condition();
        Assert.assertTrue(inner instanceof AndTree);
        AndTree and = (AndTree) inner;
        Assert.assertTrue(and.left() instanceof ExpressionTree);
        Assert.assertTrue(and.right() instanceof OrTree);
    }

    /**
     * <strong>Goal:</strong> Verify a deeper nesting pattern where an
     * outer multi-segment pivot wraps an inner multi-segment pivot that
     * wraps a multi-condition group — exercises the recursive case for
     * scope production inside scope production.
     */
    @Test
    public void testParseScopedDeeplyNestedWithMultiSegments() {
        String ccl = "a.b.(c.d.(foo = \"X\" AND bar = \"Y\" AND baz = \"Z\"))";
        Compiler compiler = createCompiler();
        ConditionTree tree = (ConditionTree) compiler.parse(ccl);
        Assert.assertTrue(tree instanceof ScopedConditionTree);
        ScopedConditionTree outer = (ScopedConditionTree) tree;
        Assert.assertEquals("a.b", outer.prefix().key().toString());
        Assert.assertTrue(outer.condition() instanceof ScopedConditionTree);
        ScopedConditionTree inner = (ScopedConditionTree) outer.condition();
        Assert.assertEquals("c.d", inner.prefix().key().toString());
        Assert.assertTrue(inner.condition() instanceof AndTree);
    }

    /**
     * <strong>Goal:</strong> Verify that scoped conditions nest — the
     * inner group of a {@link ScopedConditionTree} can itself be a
     * {@link ScopedConditionTree} relative to the outer pivot.
     */
    @Test
    public void testParseScopedNested() {
        String ccl = "a.(b.(foo = \"X\" AND bar = \"Y\"))";
        Compiler compiler = createCompiler();
        ConditionTree tree = (ConditionTree) compiler.parse(ccl);
        Assert.assertTrue(tree instanceof ScopedConditionTree);
        ScopedConditionTree outer = (ScopedConditionTree) tree;
        Assert.assertEquals("a", outer.prefix().key().toString());
        Assert.assertTrue(outer.condition() instanceof ScopedConditionTree);
        ScopedConditionTree inner = (ScopedConditionTree) outer.condition();
        Assert.assertEquals("b", inner.prefix().key().toString());
        Assert.assertTrue(inner.condition() instanceof AndTree);
    }

    /**
     * <strong>Goal:</strong> Verify that a scoped group composes with
     * outer logical connectives, appearing as a child of an enclosing
     * {@link OrTree}.
     */
    @Test
    public void testParseScopedInsideLargerExpression() {
        String ccl = "A.(foo = \"A\" AND bar = \"B\") OR name = \"test\"";
        Compiler compiler = createCompiler();
        ConditionTree tree = (ConditionTree) compiler.parse(ccl);
        Assert.assertTrue(tree instanceof OrTree);
        OrTree or = (OrTree) tree;
        Assert.assertTrue(or.left() instanceof ScopedConditionTree);
        Assert.assertTrue(or.right() instanceof ExpressionTree);
        Assert.assertTrue(((ScopedConditionTree) or.left())
                .condition() instanceof AndTree);
    }

    /**
     * <strong>Goal:</strong> Verify that {@link Compiler#arrange(ConditionTree)
     * arrange} emits a {@link ScopeSymbol} BEGIN marker carrying the
     * prefix and a {@link ScopeEndSymbol#INSTANCE} END marker that
     * bracket the inner postfix so scope-aware consumers can identify the
     * group and its pivot.
     */
    @Test
    public void testArrangeScopedEmitsScopeMarkers() {
        String ccl = "A.(foo = \"X\" AND bar = \"Y\") OR name = \"test\"";
        Compiler compiler = createCompiler();
        ConditionTree tree = (ConditionTree) compiler.parse(ccl);
        Queue<PostfixNotationSymbol> queue = compiler.arrange(tree);
        List<PostfixNotationSymbol> symbols = new ArrayList<>(queue);
        Assert.assertTrue(symbols.get(0) instanceof ScopeSymbol);
        ScopeSymbol begin = (ScopeSymbol) symbols.get(0);
        Assert.assertEquals("A", begin.prefix().key().toString());
        Assert.assertTrue(symbols.get(1) instanceof ExpressionSymbol);
        Assert.assertTrue(symbols.get(2) instanceof ExpressionSymbol);
        Assert.assertEquals(
                com.cinchapi.ccl.grammar.ConjunctionSymbol.AND,
                symbols.get(3));
        Assert.assertEquals(ScopeEndSymbol.INSTANCE, symbols.get(4));
        Assert.assertTrue(symbols.get(5) instanceof ExpressionSymbol);
        Assert.assertEquals(
                com.cinchapi.ccl.grammar.ConjunctionSymbol.OR,
                symbols.get(6));
    }

    /**
     * <strong>Goal:</strong> Verify that
     * {@link Parsing#toPostfixNotation(List) toPostfixNotation} applied
     * to {@link Compiler#tokenize(AbstractSyntaxTree) tokenize} output
     * produces the same postfix queue as
     * {@link Compiler#arrange(ConditionTree) arrange} for a scoped
     * query. Guards against the two paths diverging on
     * {@link ScopeSymbol}/{@link ScopeEndSymbol} bracketing.
     */
    @Test
    public void testToPostfixNotationMatchesArrangeForScoped() {
        String ccl = "A.(foo = \"X\" AND bar = \"Y\") OR name = \"test\"";
        Compiler compiler = createCompiler();
        ConditionTree tree = (ConditionTree) compiler.parse(ccl);
        Assert.assertEquals(compiler.arrange(tree),
                Parsing.toPostfixNotation(compiler.tokenize(tree)));
    }

    /**
     * <strong>Goal:</strong> Verify that {@code prefix.(...)} parses
     * inside a {@code findOrInsert} command when a trailing command-level
     * timestamp and JSON argument are present. Exercises the
     * {@code UnaryExpressionNoTimestamp} branch that was extended with
     * {@link com.cinchapi.ccl.generated.ASTScoped ScopedExpression} —
     * whose job is to keep the closing {@code at "..."} and JSON payload
     * from being swallowed by a trailing {@code RelationalExpression}.
     */
    @Test
    public void testParseScopedInsideFindOrInsertWithCommandTimestamp() {
        String ccl = "findOrInsert friend.(name = \"Jeff\" and age > 30) "
                + "at \"2024-01-01\" \"{'name': 'Jeff', 'age': 31}\"";
        Compiler compiler = createCompiler();
        AbstractSyntaxTree tree = compiler.parse(ccl);
        Assert.assertTrue(tree instanceof CommandTree);
        ConditionTree condition = ((CommandTree) tree).conditionTree();
        Assert.assertTrue(condition instanceof ScopedConditionTree);
        Assert.assertTrue(((ScopedConditionTree) condition)
                .condition() instanceof AndTree);
    }

    /**
     * <strong>Goal:</strong> Verify that
     * {@link StatementAnalysis#keys() analyze(tree).keys()} includes the
     * scope pivot prefix so consumers that key off referenced paths
     * (auth, routing, index selection) see the pivot alongside the inner
     * expression keys.
     */
    @Test
    public void testAnalyzeKeysIncludesScopePivot() {
        String ccl = "friend.(name = \"Jeff\" AND age > 30)";
        Compiler compiler = createCompiler();
        ConditionTree tree = (ConditionTree) compiler.parse(ccl);
        Assert.assertEquals(
                com.google.common.collect.Sets.newHashSet("friend", "name",
                        "age"),
                compiler.analyze(tree).keys());
    }

    /**
     * <strong>Goal:</strong> Verify that
     * {@link StatementAnalysis#keys() analyze(tree).keys()} preserves a
     * multi-segment pivot verbatim (including any transitive markers) so
     * callers can disambiguate the exact navigation path the scope
     * evaluates against.
     */
    @Test
    public void testAnalyzeKeysIncludesMultiSegmentScopePivot() {
        String ccl = "a.b*.c.(foo = \"X\")";
        Compiler compiler = createCompiler();
        ConditionTree tree = (ConditionTree) compiler.parse(ccl);
        Assert.assertTrue(
                compiler.analyze(tree).keys().contains("a.b*.c"));
    }

    /**
     * <strong>Goal:</strong> Verify that
     * {@link StatementAnalysis#keys() analyze(tree).keys()} returns a leaf
     * key without its bracket-timestamp annotation, so consumers index by
     * the bare key regardless of when the read is pinned.
     */
    @Test
    public void testAnalyzeKeysStripsBracketOnLeaf() {
        String ccl = "foo[1700000000] = \"X\"";
        Compiler compiler = createCompiler();
        ConditionTree tree = (ConditionTree) compiler.parse(ccl);
        Assert.assertEquals(
                com.google.common.collect.Sets.newHashSet("foo"),
                compiler.analyze(tree).keys());
    }

    /**
     * <strong>Goal:</strong> Verify that
     * {@link StatementAnalysis#keys() analyze(tree).keys()} strips
     * per-stop bracket annotations from a navigation key while preserving
     * the dotted path and any transitive markers.
     */
    @Test
    public void testAnalyzeKeysStripsBracketsOnNavigation() {
        String ccl = "a[111].b[222].foo[333] = \"X\"";
        Compiler compiler = createCompiler();
        ConditionTree tree = (ConditionTree) compiler.parse(ccl);
        Assert.assertEquals(
                com.google.common.collect.Sets.newHashSet("a.b.foo"),
                compiler.analyze(tree).keys());
    }

    /**
     * <strong>Goal:</strong> Verify that
     * {@link StatementAnalysis#keys() analyze(tree).keys()} strips a
     * bracket annotation from a single-key scope prefix.
     */
    @Test
    public void testAnalyzeKeysStripsBracketOnScopePrefix() {
        String ccl = "A[1700000000].(foo = \"X\")";
        Compiler compiler = createCompiler();
        ConditionTree tree = (ConditionTree) compiler.parse(ccl);
        Assert.assertEquals(
                com.google.common.collect.Sets.newHashSet("A", "foo"),
                compiler.analyze(tree).keys());
    }

    /**
     * <strong>Goal:</strong> Verify that
     * {@link StatementAnalysis#keys() analyze(tree).keys()} strips
     * bracket annotations from a multi-stop scope prefix while preserving
     * the dotted path.
     */
    @Test
    public void testAnalyzeKeysStripsBracketsOnMultiStopScopePrefix() {
        String ccl = "a[111].b[222].(foo = \"X\")";
        Compiler compiler = createCompiler();
        ConditionTree tree = (ConditionTree) compiler.parse(ccl);
        Assert.assertEquals(
                com.google.common.collect.Sets.newHashSet("a.b", "foo"),
                compiler.analyze(tree).keys());
    }

    /**
     * <strong>Goal:</strong> Verify that
     * {@link Parsing#toPostfixNotation(List) toPostfixNotation} throws a
     * {@link SyntaxException} when a {@link ScopeEndSymbol} is encountered
     * with no opening {@link ScopeSymbol} on the stack. Guards the public
     * API against malformed hand-built token streams.
     */
    @Test(expected = SyntaxException.class)
    public void testToPostfixNotationScopeEndWithoutOpen() {
        Compiler compiler = createCompiler();
        List<Symbol> tokens = new ArrayList<>(
                compiler.tokenize(compiler.parse("foo = \"X\"")));
        tokens.add(ScopeEndSymbol.INSTANCE);
        Parsing.toPostfixNotation(tokens);
    }

    /**
     * <strong>Goal:</strong> Verify that
     * {@link Parsing#toPostfixNotation(List) toPostfixNotation} throws a
     * {@link SyntaxException} when a {@link ScopeSymbol} is opened but
     * never closed by a {@link ScopeEndSymbol}.
     */
    @Test(expected = SyntaxException.class)
    public void testToPostfixNotationScopeOpenWithoutEnd() {
        Compiler compiler = createCompiler();
        List<Symbol> tokens = new ArrayList<>(compiler
                .tokenize(compiler.parse("A.(foo = \"X\" AND bar = \"Y\")")));
        // Strip the trailing ScopeEndSymbol to leave the opening
        // ScopeSymbol unmatched on the stack.
        Assert.assertEquals(ScopeEndSymbol.INSTANCE,
                tokens.remove(tokens.size() - 1));
        Parsing.toPostfixNotation(tokens);
    }

    /**
     * <strong>Goal:</strong> Verify that
     * {@link Parsing#toPostfixNotation(List) toPostfixNotation} throws a
     * {@link SyntaxException} &mdash; rather than a
     * {@link ClassCastException} &mdash; when a {@link ParenthesisSymbol}
     * is still on the stack at {@link ScopeEndSymbol}, which indicates
     * the scope bracket closes before an inner paren group does.
     */
    @Test(expected = SyntaxException.class)
    public void testToPostfixNotationScopeEndWithUnmatchedParenOnStack() {
        Compiler compiler = createCompiler();
        // Start from valid tokens for the inner expression so that
        // groupExpressions() can fold key/op/value into an ExpressionSymbol,
        // then wrap with an opening ScopeSymbol and an unmatched LEFT
        // paren before the scope end.
        List<Symbol> inner = new ArrayList<>(
                compiler.tokenize(compiler.parse("foo = \"X\"")));
        List<Symbol> tokens = new ArrayList<>();
        tokens.add(new ScopeSymbol(new KeySymbol("A")));
        tokens.add(ParenthesisSymbol.LEFT);
        tokens.addAll(inner);
        tokens.add(ScopeEndSymbol.INSTANCE);
        Parsing.toPostfixNotation(tokens);
    }

    /**
     * <strong>Goal:</strong> Verify that local
     * {@link Compiler#evaluate(ConditionTree, Multimap,
     * com.cinchapi.common.function.TriFunction) evaluate} throws on a
     * {@link ScopedConditionTree} because a flat local data view cannot
     * honor same-destination semantics at a navigation pivot.
     */
    @Test(expected = UnsupportedOperationException.class)
    public void testEvaluateScopedThrows() {
        String ccl = "friend.(name = \"Jeff\" AND age > 30)";
        Compiler compiler = createCompiler(Convert::stringToJava,
                Convert::stringToOperator);
        TriFunction<Object, Operator, List<Object>, Boolean> evaluator = (value,
                operator, values) -> false;
        ConditionTree tree = (ConditionTree) compiler.parse(ccl);
        compiler.evaluate(tree, ImmutableMultimap.of(), evaluator);
    }

    /**
     * <strong>Goal:</strong> Verify that {@code foo[1700000000] = "X"}
     * parses into a leaf {@link ExpressionTree} whose key is a
     * {@link TemporalKeySymbol} wrapping a {@link KeySymbol} and carrying
     * the bracket-derived {@link TimestampSymbol}.
     */
    @Test
    public void testParseLeafBracketTimestamp() {
        String ccl = "foo[1700000000] = \"X\"";
        Compiler compiler = createCompiler();
        ConditionTree tree = (ConditionTree) compiler.parse(ccl);
        Assert.assertTrue(tree instanceof ExpressionTree);
        ExpressionSymbol expr = (ExpressionSymbol) tree.root();
        Assert.assertTrue(expr.key() instanceof TemporalKeySymbol);
        TemporalKeySymbol temporal = (TemporalKeySymbol) expr.key();
        Assert.assertTrue(temporal.key() instanceof KeySymbol);
        Assert.assertEquals("foo",
                ((KeySymbol) temporal.key()).key().toString());
        Assert.assertEquals(1700000000L,
                temporal.timestamp().timestamp());
    }

    /**
     * <strong>Goal:</strong> Verify that the legacy keyworded bracket
     * forms ({@code foo[at t]}, {@code foo[on t]}, {@code foo[during t]})
     * produce ASTs equal to the canonical keyword-less form
     * ({@code foo[t]}).
     */
    @Test
    public void testParseLeafBracketKeywordEquivalence() {
        Compiler compiler = createCompiler();
        ConditionTree canonical = (ConditionTree) compiler
                .parse("foo[1700000000] = \"X\"");
        for (String keyword : new String[] { "at", "on", "during" }) {
            String ccl = "foo[" + keyword + " 1700000000] = \"X\"";
            ConditionTree tree = (ConditionTree) compiler.parse(ccl);
            Assert.assertEquals("keyword form '" + keyword
                    + "' must equal canonical", canonical, tree);
        }
    }

    /**
     * <strong>Goal:</strong> Verify that an unbracketed key still parses
     * to a plain {@link KeySymbol} rather than a {@link TemporalKeySymbol}.
     */
    @Test
    public void testParseLeafWithoutBracketUnchanged() {
        String ccl = "foo = \"X\"";
        Compiler compiler = createCompiler();
        ConditionTree tree = (ConditionTree) compiler.parse(ccl);
        Assert.assertTrue(tree instanceof ExpressionTree);
        ExpressionSymbol expr = (ExpressionSymbol) tree.root();
        Assert.assertTrue(expr.key() instanceof KeySymbol);
        Assert.assertFalse(expr.key() instanceof TemporalKeySymbol);
    }

    /**
     * <strong>Goal:</strong> Verify that a navigation key with per-stop
     * bracket annotations parses to a {@link NavigationKeySymbol} whose
     * {@link NavigationKeySymbol#stops() stops} carry the per-stop
     * timestamps.
     */
    @Test
    public void testParseNavigationKeyWithPerStopBrackets() {
        String ccl = "a[111].b[222].foo[333] = \"X\"";
        Compiler compiler = createCompiler();
        ConditionTree tree = (ConditionTree) compiler.parse(ccl);
        Assert.assertTrue(tree instanceof ExpressionTree);
        ExpressionSymbol expr = (ExpressionSymbol) tree.root();
        Assert.assertTrue(expr.key() instanceof NavigationKeySymbol);
        NavigationKeySymbol nav = (NavigationKeySymbol) expr.key();
        List<NavigationKeyStop> stops = nav.stops();
        Assert.assertEquals(3, stops.size());
        Assert.assertEquals("a", stops.get(0).key());
        Assert.assertEquals(111L, stops.get(0).timestamp().timestamp());
        Assert.assertEquals("b", stops.get(1).key());
        Assert.assertEquals(222L, stops.get(1).timestamp().timestamp());
        Assert.assertEquals("foo", stops.get(2).key());
        Assert.assertEquals(333L, stops.get(2).timestamp().timestamp());
    }

    /**
     * <strong>Goal:</strong> Verify that a navigation key with a
     * transitive-and-bracketed first stop ({@code a[t]*.foo}) parses
     * with both the transitive marker and the timestamp on the same
     * stop.
     */
    @Test
    public void testParseNavigationKeyTransitiveAndBracketed() {
        String ccl = "a[111]*.foo = \"X\"";
        Compiler compiler = createCompiler();
        ConditionTree tree = (ConditionTree) compiler.parse(ccl);
        ExpressionSymbol expr = (ExpressionSymbol) tree.root();
        Assert.assertTrue(expr.key() instanceof NavigationKeySymbol);
        NavigationKeySymbol nav = (NavigationKeySymbol) expr.key();
        List<NavigationKeyStop> stops = nav.stops();
        Assert.assertEquals(2, stops.size());
        Assert.assertEquals("a", stops.get(0).key());
        Assert.assertTrue(stops.get(0).isTransitive());
        Assert.assertEquals(111L, stops.get(0).timestamp().timestamp());
        Assert.assertEquals("foo", stops.get(1).key());
        Assert.assertFalse(stops.get(1).isTransitive());
        Assert.assertNull(stops.get(1).timestamp());
    }

    /**
     * <strong>Goal:</strong> Verify that an unbracketed navigation key
     * still parses identically to the pre-bracket era.
     */
    @Test
    public void testParseNavigationKeyWithoutBracketsUnchanged() {
        String ccl = "a.b.foo = \"X\"";
        Compiler compiler = createCompiler();
        ConditionTree tree = (ConditionTree) compiler.parse(ccl);
        ExpressionSymbol expr = (ExpressionSymbol) tree.root();
        Assert.assertTrue(expr.key() instanceof NavigationKeySymbol);
        NavigationKeySymbol nav = (NavigationKeySymbol) expr.key();
        for (NavigationKeyStop stop : nav.stops()) {
            Assert.assertNull(stop.timestamp());
        }
    }

    /**
     * <strong>Goal:</strong> Verify that {@code A[t].(foo = "X")} parses
     * into a {@link ScopedConditionTree} whose
     * {@link ScopedConditionTree#prefix() prefix} is a
     * {@link TemporalKeySymbol} wrapping a {@link KeySymbol}.
     */
    @Test
    public void testParseScopedSingleKeyBracketPrefix() {
        String ccl = "A[1700000000].(foo = \"X\")";
        Compiler compiler = createCompiler();
        ConditionTree tree = (ConditionTree) compiler.parse(ccl);
        Assert.assertTrue(tree instanceof ScopedConditionTree);
        ScopedConditionTree scoped = (ScopedConditionTree) tree;
        Assert.assertTrue(scoped.prefix() instanceof TemporalKeySymbol);
        TemporalKeySymbol temporal = (TemporalKeySymbol) scoped.prefix();
        Assert.assertTrue(temporal.key() instanceof KeySymbol);
        Assert.assertEquals("A",
                ((KeySymbol) temporal.key()).key().toString());
        Assert.assertEquals(1700000000L,
                temporal.timestamp().timestamp());
    }

    /**
     * <strong>Goal:</strong> Verify that the legacy keyword forms inside
     * a scope-prefix bracket ({@code A[at t].(...)}) produce ASTs equal
     * to the canonical keyword-less form ({@code A[t].(...)}).
     */
    @Test
    public void testParseScopedSingleKeyBracketKeywordEquivalence() {
        Compiler compiler = createCompiler();
        ConditionTree canonical = (ConditionTree) compiler
                .parse("A[1700000000].(foo = \"X\")");
        for (String keyword : new String[] { "at", "on", "during" }) {
            String ccl = "A[" + keyword + " 1700000000].(foo = \"X\")";
            ConditionTree tree = (ConditionTree) compiler.parse(ccl);
            Assert.assertEquals(canonical, tree);
        }
    }

    /**
     * <strong>Goal:</strong> Verify that a multi-stop scope prefix with
     * per-stop brackets ({@code a[t1].b[t2].(...)}) parses to a
     * {@link NavigationKeySymbol} whose
     * {@link NavigationKeySymbol#stops() stops} carry the per-stop
     * timestamps.
     */
    @Test
    public void testParseScopedMultiStopBracketPrefix() {
        String ccl = "a[111].b[222].(foo = \"X\")";
        Compiler compiler = createCompiler();
        ConditionTree tree = (ConditionTree) compiler.parse(ccl);
        Assert.assertTrue(tree instanceof ScopedConditionTree);
        ScopedConditionTree scoped = (ScopedConditionTree) tree;
        Assert.assertTrue(scoped.prefix() instanceof NavigationKeySymbol);
        NavigationKeySymbol nav = (NavigationKeySymbol) scoped.prefix();
        List<NavigationKeyStop> stops = nav.stops();
        Assert.assertEquals(2, stops.size());
        Assert.assertEquals("a", stops.get(0).key());
        Assert.assertEquals(111L, stops.get(0).timestamp().timestamp());
        Assert.assertEquals("b", stops.get(1).key());
        Assert.assertEquals(222L, stops.get(1).timestamp().timestamp());
    }

    /**
     * <strong>Goal:</strong> Verify that an unbracketed scope prefix
     * still parses to a plain {@link KeySymbol} (single key) or
     * {@link NavigationKeySymbol} (multi-stop), preserving pre-bracket
     * behavior.
     */
    @Test
    public void testParseScopedWithoutBracketsUnchanged() {
        Compiler compiler = createCompiler();
        ConditionTree tree = (ConditionTree) compiler
                .parse("A.(foo = \"X\")");
        ScopedConditionTree scoped = (ScopedConditionTree) tree;
        Assert.assertTrue(scoped.prefix() instanceof KeySymbol);
        Assert.assertFalse(scoped.prefix() instanceof TemporalKeySymbol);
    }

    /**
     * <strong>Goal:</strong> Verify that {@link Compiler#tokenize(AbstractSyntaxTree)}
     * emits a {@link TemporalKeySymbol} directly into the symbol list
     * for a bracket-stamped leaf key.
     */
    @Test
    public void testTokenizeEmitsTemporalKeySymbolForLeafBracket() {
        String ccl = "foo[1700000000] = \"X\"";
        Compiler compiler = createCompiler();
        AbstractSyntaxTree tree = compiler.parse(ccl);
        List<Symbol> symbols = compiler.tokenize(tree);
        boolean found = symbols.stream()
                .anyMatch(s -> s instanceof TemporalKeySymbol);
        Assert.assertTrue(
                "tokenize must emit TemporalKeySymbol for bracket-stamped leaf",
                found);
    }

    /**
     * <strong>Goal:</strong> Verify lossless round-trip
     * (parse -> tokenize -> joined toString -> re-parse) for a
     * bracket-stamped leaf.
     */
    @Test
    public void testRoundTripLeafBracket() {
        assertRoundTripStable("foo[1700000000] = \"X\"");
    }

    /**
     * <strong>Goal:</strong> Verify lossless round-trip for a
     * navigation key with per-stop brackets.
     */
    @Test
    public void testRoundTripNavigationPerStopBrackets() {
        assertRoundTripStable("a[111].b[222].foo[333] = \"X\"");
    }

    /**
     * <strong>Goal:</strong> Verify lossless round-trip for a
     * single-key scope prefix bracket.
     */
    @Test
    public void testRoundTripScopedSingleKeyBracket() {
        assertRoundTripStable("A[111].(foo = \"X\")");
    }

    /**
     * <strong>Goal:</strong> Verify lossless round-trip for a multi-stop
     * scope prefix with per-stop brackets.
     */
    @Test
    public void testRoundTripScopedMultiStopBrackets() {
        assertRoundTripStable("a[111].b[222].(foo = \"X\")");
    }

    /**
     * <strong>Goal:</strong> Verify lossless round-trip for a scope
     * prefix bracket combined with a per-leaf bracket inside the scope.
     */
    @Test
    public void testRoundTripScopedPrefixAndLeafBrackets() {
        assertRoundTripStable("A[111].(foo[222] = \"X\")");
    }

    /**
     * <strong>Goal:</strong> Verify that the legacy trailing-{@code at}
     * form (outside brackets) still round-trips without regression.
     */
    @Test
    public void testRoundTripLegacyTrailingAt() {
        assertRoundTripStable("foo = \"X\" at 1700000000");
    }

    /**
     * Assert that {@code ccl} parses, tokenizes, re-emits, and re-parses
     * to an equal {@link AbstractSyntaxTree}.
     *
     * @param ccl the CCL string
     */
    private void assertRoundTripStable(String ccl) {
        Compiler compiler = createCompiler();
        AbstractSyntaxTree first = compiler.parse(ccl);
        String reemitted = compiler.tokenize(first).stream()
                .map(Symbol::toString)
                .collect(Collectors.joining(" "));
        AbstractSyntaxTree second = compiler.parse(reemitted);
        Assert.assertEquals("round-trip failed for: " + ccl
                + " (re-emitted as: " + reemitted + ")", first, second);
    }

    @Override
    protected Compiler createCompiler(
            Function<String, Object> valueTransformFunction,
            Function<String, Operator> operatorTransformFunction) {
        return Compiler.create(valueTransformFunction,
                operatorTransformFunction);
    }

}
