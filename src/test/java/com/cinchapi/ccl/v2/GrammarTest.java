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
package com.cinchapi.ccl.v2;

import com.cinchapi.ccl.Parser;
import com.cinchapi.ccl.type.Operator;
import com.cinchapi.concourse.util.Convert;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import org.junit.Test;

import com.cinchapi.ccl.v2.generated.Grammar;
import com.cinchapi.ccl.v2.generated.ParseException;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.function.Function;

/**
 * Tests for {@link Grammar}
 */
public class GrammarTest {

    @Test
    public void validUnaryOperatorSingleWordValueExpression() throws UnsupportedEncodingException, ParseException {
        String ccl = "a = 1";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(
                StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION, PARSER_TRANSFORM_OPERATOR_FUNCTION);
        grammar.Start();
    }

    @Test
    public void validUnaryOperatorMultiWordValueExpression() throws UnsupportedEncodingException, ParseException {
        String ccl = "a = 1 2";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(
                StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION, PARSER_TRANSFORM_OPERATOR_FUNCTION);
        grammar.Start();
    }

    @Test
    public void validBinaryOperatorExpression() throws UnsupportedEncodingException, ParseException {
        String ccl = "a >< 1 3";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(
                StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION, PARSER_TRANSFORM_OPERATOR_FUNCTION);
        grammar.Start();
    }

    @Test
    public void validExplicitFunctionAsEvaluationKey() throws UnsupportedEncodingException, ParseException {
        String ccl = "count(friends, ?) > 3";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(
                StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION, PARSER_TRANSFORM_OPERATOR_FUNCTION);
        grammar.Start();
    }

    @Test
    public void validImplicitFunctionAsEvaluationValue() throws UnsupportedEncodingException, ParseException {
        String ccl = "age > avg(age, ?)";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(
                StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION, PARSER_TRANSFORM_OPERATOR_FUNCTION);
        grammar.Start();
    }

    @Test
    public void validExplicitFunctionWithSingleRecordAsEvaluationValue() throws UnsupportedEncodingException, ParseException {
        String ccl = "age > avg(age, 1)";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(
                StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION, PARSER_TRANSFORM_OPERATOR_FUNCTION);
        grammar.Start();
    }

    @Test
    public void validExplicitFunctionWithMultpleRecordsAsEvaluationValue() throws UnsupportedEncodingException, ParseException {
        String ccl = "age > avg(age, 1, 2)";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(
                StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION, PARSER_TRANSFORM_OPERATOR_FUNCTION);
        grammar.Start();
    }

    @Test
    public void validExplicitFunctionWithCCLAsEvaluationValue() throws UnsupportedEncodingException, ParseException {
        String ccl = "age > avg(age, age < 30)";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(
                StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION, PARSER_TRANSFORM_OPERATOR_FUNCTION);
        grammar.Start();
    }

    @Test
    public void validExplicitFunctionAsEvalutionKeyAndExplicitFunctionWithCCLAsEvaluationValue() throws UnsupportedEncodingException, ParseException {
        String ccl = "count(friends, ?) > avg(age, age < 30)";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(
                StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION, PARSER_TRANSFORM_OPERATOR_FUNCTION);
        grammar.Start();
    }

    @Test (expected = ParseException.class)
    public void missingSecondOperandBinaryOperator() throws UnsupportedEncodingException, ParseException {
        String ccl = "a >< 1";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(
                StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION, PARSER_TRANSFORM_OPERATOR_FUNCTION);
        grammar.Start();
    }

    @Test (expected = ParseException.class)
    public void tooManyOperandsBinaryOperator() throws UnsupportedEncodingException, ParseException {
        String ccl = "a >< 1 2 3";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(
                StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION, PARSER_TRANSFORM_OPERATOR_FUNCTION);
        grammar.Start();
    }

    @Test
    public void validSingleWordTimestamp() throws UnsupportedEncodingException, ParseException {
        String ccl = "a = 1 at today";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(
                StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION, PARSER_TRANSFORM_OPERATOR_FUNCTION);
        grammar.Start();
    }

    @Test
    public void validJsonReservedIdentifier() throws UnsupportedEncodingException, ParseException {
        String ccl = "$id$ != 40";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(
                StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream);
        grammar.Start();
    }

    @Test
    public void validMultiwordTimestamp() throws UnsupportedEncodingException, ParseException {
        String ccl = "a = 1 on last christmas";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(
                StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION, PARSER_TRANSFORM_OPERATOR_FUNCTION);
        grammar.Start();
    }

    @Test (expected = ParseException.class)
    public void missingTimestamp() throws UnsupportedEncodingException, ParseException {
        String ccl = "a = 1 at";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(
                StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION, PARSER_TRANSFORM_OPERATOR_FUNCTION);
        grammar.Start();
    }

    @Test
    public void validQuotedValue() throws UnsupportedEncodingException, ParseException {
        String ccl = "name = \"name\"";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(
                StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION, PARSER_TRANSFORM_OPERATOR_FUNCTION);
        grammar.Start();
    }

    @Test
    public void doubleLeftAndRightQuotation() throws UnsupportedEncodingException, ParseException {
        String ccl = "name = “name”";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8));
        Grammar grammar = new Grammar(stream);
        grammar.Start();
    }

    @Test
    public void singleLeftAndRightQuotation() throws UnsupportedEncodingException, ParseException {
        String ccl = "name = ‘name’";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8));
        Grammar grammar = new Grammar(stream);
        grammar.Start();
    }

    @Test
    public void validQuotedValueWithQuote() throws UnsupportedEncodingException, ParseException {
        String ccl = "name = \"wood\\\"ford\"";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(
                StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream);
        grammar.Start();
    }

    @Test
    public void validLocalResolution() throws UnsupportedEncodingException, ParseException {
        String ccl = "name = $name";
        Multimap<String, Object> data = LinkedHashMultimap.create();
        data.put("name", "Lebron James");
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(
                StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION, PARSER_TRANSFORM_OPERATOR_FUNCTION, data);
        grammar.Start();
    }

    @Test
    public void validEscapedLocalResolution() throws UnsupportedEncodingException, ParseException {
        String ccl = "name = \\$name";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(
                StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION, PARSER_TRANSFORM_OPERATOR_FUNCTION);
        grammar.Start();
    }

    @Test
    public void validImplicitLink() throws UnsupportedEncodingException, ParseException {
        String ccl = "name = @name";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(
                StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION, PARSER_TRANSFORM_OPERATOR_FUNCTION);
        grammar.Start();
    }

    @Test
    public void validImplicitEscapedLink() throws UnsupportedEncodingException, ParseException {
        String ccl = "name = \\@name";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(
                StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream);
        grammar.Start();
    }

    @Test
    public void validLink() throws UnsupportedEncodingException, ParseException {
        String ccl = "a -> 1";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream);
        grammar.Start();
    }

    @Test
    public void validConjunctionExpression() throws UnsupportedEncodingException, ParseException {
        String ccl = "a = 1 and b = 2";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION, PARSER_TRANSFORM_OPERATOR_FUNCTION);
        grammar.Start();
    }

    @Test
    public void validDisjunctionExpression() throws UnsupportedEncodingException, ParseException {
        String ccl = "a = 1 or b = 2";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION, PARSER_TRANSFORM_OPERATOR_FUNCTION);
        grammar.Start();
    }

    @Test
    public void operatorEnum() throws UnsupportedEncodingException, ParseException {
        String ccl = "a LINKS_TO 1";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream);
        grammar.Start();
    }

    @Test
    public void validNavigationKeyAsEvaluationKey() throws UnsupportedEncodingException, ParseException {
        String ccl = "a.b = 3";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream);
        grammar.Start();
    }

    @Test
    public void validLongNavigationKeyAsEvaluationKey() throws UnsupportedEncodingException, ParseException {
        String ccl = "a.b.c.d = 3";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream);
        grammar.Start();
    }

    @Test
    public void validPeriodSeparatedValue() throws UnsupportedEncodingException, ParseException {
        String ccl = "a = a.b.c";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream);
        grammar.Start();
    }

    @Test (expected = ParseException.class)
    public void missingKeyExpression() throws UnsupportedEncodingException, ParseException {
        String ccl = "= 1";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(
                StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION, PARSER_TRANSFORM_OPERATOR_FUNCTION);
        grammar.Start();
    }

    @Test (expected = ParseException.class)
    public void missingValueExpression() throws UnsupportedEncodingException, ParseException {
        String ccl = "a =";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(
                StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION, PARSER_TRANSFORM_OPERATOR_FUNCTION);
        grammar.Start();
    }

    @Test (expected = ParseException.class)
    public void missingOperatorExpression() throws UnsupportedEncodingException, ParseException {
        String ccl = "a 1";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(
                StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION, PARSER_TRANSFORM_OPERATOR_FUNCTION);
        grammar.Start();
    }

    @Test (expected = ParseException.class)
    public void missingSecondExpression() throws UnsupportedEncodingException, ParseException {
        String ccl = "a = 1 and";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(
                StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION, PARSER_TRANSFORM_OPERATOR_FUNCTION);
        grammar.Start();
    }

    @Test (expected = ParseException.class)
    public void missingFirstExpression() throws UnsupportedEncodingException, ParseException {
        String ccl = "and a = 1";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(
                StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION, PARSER_TRANSFORM_OPERATOR_FUNCTION);
        grammar.Start();
    }


    @Test (expected = ParseException.class)
    public void invalidLink() throws UnsupportedEncodingException, ParseException {
        String ccl = "a LINKS_TO b";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream);
        grammar.Start();
    }

    /**
     * The canonical function to transform strings to java values in a
     * {@link Parser}.
     */
    public final Function<String, Object> PARSER_TRANSFORM_VALUE_FUNCTION = value -> Convert
            .stringToJava(value);

    /**
     * The canonical function to transform strings to operators in a
     * {@link Parser}.
     */
    public final Function<String, Operator> PARSER_TRANSFORM_OPERATOR_FUNCTION = operator -> Convert.stringToOperator(operator);
}
