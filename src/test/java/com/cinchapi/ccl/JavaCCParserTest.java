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

import java.util.function.Function;
import java.util.stream.Collectors;

import com.cinchapi.ccl.grammar.KeySymbol;
import com.cinchapi.ccl.grammar.ValueSymbol;
import org.junit.Assert;
import org.junit.Test;

import com.cinchapi.ccl.grammar.Expression;
import com.cinchapi.ccl.grammar.OperatorSymbol;
import com.cinchapi.ccl.type.Operator;
import com.cinchapi.common.base.Array;
import com.cinchapi.concourse.thrift.TObject;
import com.cinchapi.concourse.util.Convert;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;

/**
 * {@link ParserTest} that uses the {@link JavaCCParser}
 * 
 * @author Jeff Nelson
 */
public class JavaCCParserTest extends AbstractParserTest {

    @Override
    protected Parser createParser(String ccl,
            Function<String, Object> valueTransformFunction,
            Function<String, Operator> operatorTransformFunction) {
        return Parser.create(ccl, valueTransformFunction,
                operatorTransformFunction);
    }

    @Override
    protected Parser createParser(String ccl, Multimap<String, Object> data,
            Function<String, Object> valueTransformFunction,
            Function<String, Operator> operatorTransformFunction) {
        return Parser.create(ccl, data, valueTransformFunction,
                operatorTransformFunction);
    }

    @Test
    public void testParseCclNoSpaces() {
        String ccl = "name=jeff";
        Parser parser = createParser(ccl);
        parser.order();

        Assert.assertEquals(new Expression(new KeySymbol("name"),
                new OperatorSymbol(
                        com.cinchapi.concourse.thrift.Operator.EQUALS),
                new ValueSymbol("jeff")), parser.order().peek());
    }

    @Test
    public void testLocalEvaluationAnd() {
        String ccl = "a > 1 AND b bw 10 15";
        Parser parser = Parser.create(ccl, Convert::stringToJava,
                Convert::stringToOperator, (value, operator, values) -> {
                    TObject tvalue = Convert.javaToThrift(value);
                    TObject[] tvalues = values.stream()
                            .map(Convert::javaToThrift)
                            .collect(Collectors.toList())
                            .toArray(Array.containing());
                    com.cinchapi.concourse.thrift.Operator toperator = Convert
                            .stringToOperator(operator.symbol());
                    return tvalue.is(toperator, tvalues);
                });
        Multimap<String, Object> passes = ImmutableMultimap.of("a", 5, "b", 12,
                "c", 4, "a", -1);
        Assert.assertTrue(parser.evaluate(passes));
        Multimap<String, Object> fails = ImmutableMultimap.of("a", 1, "b", 12,
                "c", 4, "a", -1);
        Assert.assertFalse(parser.evaluate(fails));
        Multimap<String, Object> missing = ImmutableMultimap.of("a", 1, "c", 4,
                "a", -1);
        Assert.assertFalse(parser.evaluate(missing));
    }
    
    @Test
    public void testLocalEvaluationOr() {
        String ccl = "a > 1 OR b bw 10 15";
        Parser parser = Parser.create(ccl, Convert::stringToJava,
                Convert::stringToOperator, (value, operator, values) -> {
                    TObject tvalue = Convert.javaToThrift(value);
                    TObject[] tvalues = values.stream()
                            .map(Convert::javaToThrift)
                            .collect(Collectors.toList())
                            .toArray(Array.containing());
                    com.cinchapi.concourse.thrift.Operator toperator = Convert
                            .stringToOperator(operator.symbol());
                    return tvalue.is(toperator, tvalues);
                });
        Multimap<String, Object> a = ImmutableMultimap.of("a", 5, "b", 12,
                "c", 4, "a", -1);
        Assert.assertTrue(parser.evaluate(a));
        Multimap<String, Object> b = ImmutableMultimap.of("a", 1, "b", 12,
                "c", 4, "a", -1);
        Assert.assertTrue(parser.evaluate(b));
        Multimap<String, Object> c = ImmutableMultimap.of("a", 2, "c", 4,
                "a", -1);
        Assert.assertTrue(parser.evaluate(c));
        Multimap<String, Object> d = ImmutableMultimap.of("a", 1, "c", 4,
                "a", -1);
        Assert.assertFalse(parser.evaluate(d));
    }

}
