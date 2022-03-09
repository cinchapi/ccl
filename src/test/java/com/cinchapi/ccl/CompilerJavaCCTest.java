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
import com.cinchapi.ccl.syntax.ConditionTree;
import com.cinchapi.ccl.grammar.KeySymbol;
import com.cinchapi.ccl.type.Operator;
import com.cinchapi.common.base.Array;
import com.cinchapi.common.function.TriFunction;
import com.cinchapi.concourse.thrift.TObject;
import com.cinchapi.concourse.util.Convert;
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

    @Override
    protected Compiler createCompiler(
            Function<String, Object> valueTransformFunction,
            Function<String, Operator> operatorTransformFunction) {
        return Compiler.create(valueTransformFunction,
                operatorTransformFunction);
    }

}
