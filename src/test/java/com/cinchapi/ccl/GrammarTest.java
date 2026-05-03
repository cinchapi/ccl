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

import com.cinchapi.ccl.generated.*;
import com.cinchapi.ccl.syntax.*;
import com.cinchapi.ccl.type.Operator;
import com.cinchapi.concourse.util.Convert;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import org.junit.Test;

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
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void validUnaryOperatorMultiWordValueExpression() throws UnsupportedEncodingException, ParseException {
        String ccl = "a = 1 2";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(
                StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void validBinaryOperatorExpression() throws UnsupportedEncodingException, ParseException {
        String ccl = "a >< 1 3";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(
                StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void validImplicitContextFunctionAsEvaluationKey() throws UnsupportedEncodingException, ParseException {
        String ccl = "friends | avg > 3";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(
                StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void validImplicitIndexFunctionAsEvaluationValue() throws UnsupportedEncodingException, ParseException {
        String ccl = "age > avg(age)";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(
                StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void validExplicitFunctionWithSingleRecordAsEvaluationValue() throws UnsupportedEncodingException, ParseException {
        String ccl = "age > avg(age, 1)";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(
                StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void validExplicitFunctionWithBetween() throws UnsupportedEncodingException, ParseException {
        String ccl = "age bw avg(age) 1000";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(
                StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void validExplicitFunctionWithBetweenCCL() throws UnsupportedEncodingException, ParseException {
        String ccl = "age bw avg(age, age > 10) 1000";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(
                StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void validExplicitFunctionWithMultipleRecordsAsEvaluationValue() throws UnsupportedEncodingException, ParseException {
        String ccl = "age > avg(age, 1, 2)";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(
                StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void validExplicitFunctionWithCCLAsEvaluationValue() throws UnsupportedEncodingException, ParseException {
        String ccl = "age > avg(age, age < 30)";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(
                StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void validImplicitRecordFunctionAsEvaluationKeyAndExplicitFunctionWithCCLAsEvaluationValue() throws UnsupportedEncodingException, ParseException {
        String ccl = "age | avg > avg(age, age < 30)";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(
                StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test (expected = ParseException.class)
    public void missingSecondOperandBinaryOperator() throws UnsupportedEncodingException, ParseException {
        String ccl = "a >< 1";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(
                StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test (expected = ParseException.class)
    public void tooManyOperandsBinaryOperator() throws UnsupportedEncodingException, ParseException {
        String ccl = "a >< 1 2 3";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(
                StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void validSingleWordTimestamp() throws UnsupportedEncodingException, ParseException {
        String ccl = "a = 1 at today";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(
                StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void validJsonReservedIdentifier() throws UnsupportedEncodingException, ParseException {
        String ccl = "$id$ != 40";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(
                StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void validMultiwordTimestamp() throws UnsupportedEncodingException, ParseException {
        String ccl = "a = 1 on last christmas";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(
                StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test (expected = ParseException.class)
    public void missingTimestamp() throws UnsupportedEncodingException, ParseException {
        String ccl = "a = 1 at";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(
                StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void validQuotedValue() throws UnsupportedEncodingException, ParseException {
        String ccl = "name = \"name\"";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(
                StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void singleLeftAndRightQuotation() throws UnsupportedEncodingException, ParseException {
        String ccl = "name = ‘name’";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void validQuotedValueWithQuote() throws UnsupportedEncodingException, ParseException {
        String ccl = "name = \"wood\\\"ford\"";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(
                StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void validLocalResolution() throws UnsupportedEncodingException, ParseException {
        String ccl = "name = $name";
        Multimap<String, Object> data = LinkedHashMultimap.create();
        data.put("name", "Lebron James");
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(
                StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, data, visitor);
        grammar.generateAST();
    }

    @Test
    public void validEscapedLocalResolution() throws UnsupportedEncodingException, ParseException {
        String ccl = "name = \\$name";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(
                StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void validImplicitLink() throws UnsupportedEncodingException, ParseException {
        String ccl = "name = @name";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(
                StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void validImplicitEscapedLink() throws UnsupportedEncodingException, ParseException {
        String ccl = "name = \\@name";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(
                StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void validLink() throws UnsupportedEncodingException, ParseException {
        String ccl = "a -> 1";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void validConjunctionExpression() throws UnsupportedEncodingException, ParseException {
        String ccl = "a = 1 and b = 2";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void validDisjunctionExpression() throws UnsupportedEncodingException, ParseException {
        String ccl = "a = 1 or b = 2";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void validParenthesizedExpression() throws UnsupportedEncodingException, ParseException {
        String ccl = "a = 1 or (b = 2 and c = 3)";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void operatorEnum() throws UnsupportedEncodingException, ParseException {
        String ccl = "a LINKS_TO 1";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void validNavigationKeyAsEvaluationKey() throws UnsupportedEncodingException, ParseException {
        String ccl = "a.b = 3";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void validLongNavigationKeyAsEvaluationKey() throws UnsupportedEncodingException, ParseException {
        String ccl = "a.b.c.d = 3";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void validPeriodSeparatedValue() throws UnsupportedEncodingException, ParseException {
        String ccl = "a = a.b.c";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void validRegex() throws UnsupportedEncodingException, ParseException {
        String ccl = "name nregex (?i:%jeff%)";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void validLikeWithSecondParenthesizedExpression() throws UnsupportedEncodingException, ParseException {
        String ccl = "(a = b) and (name like (?i:%jeff%))";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void validLike() throws UnsupportedEncodingException, ParseException {
        String ccl = "name like (?i:%jeff%)";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void validNavigationKeyWithContainsOperator() throws UnsupportedEncodingException, ParseException {
        String ccl = "a.b contains 'foo'";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void validLongNavigationKeyWithContainsOperator() throws UnsupportedEncodingException, ParseException {
        String ccl = "a.b.c.d contains 'foo'";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void validNavigationKeyWithNotContainsOperator() throws UnsupportedEncodingException, ParseException {
        String ccl = "a.b not_contains 'foo'";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void validSearchMatchWord() throws UnsupportedEncodingException, ParseException {
        String ccl = "major search_match 'business administration'";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void validSearchMatchContains() throws UnsupportedEncodingException, ParseException {
        String ccl = "major contains 'business administration'";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void validSearchExcludeWord() throws UnsupportedEncodingException, ParseException {
        String ccl = "name search_exclude jeff";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void validSearchMatchOp() throws UnsupportedEncodingException, ParseException {
        String ccl = "major ~ 'business administration'";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void validSearchExcludeNotContains() throws UnsupportedEncodingException, ParseException {
        String ccl = "major not_contains 'business administration'";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void validSearchExcludeOP() throws UnsupportedEncodingException, ParseException {
        String ccl = "name !~ jeff";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test (expected = ParseException.class)
    public void invalidSearchExcludeOP() throws UnsupportedEncodingException, ParseException {
        String ccl = "name !~ jeff at last week";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test (expected = ParseException.class)
    public void invalidSearchExcludeWord() throws UnsupportedEncodingException, ParseException {
        String ccl = "name search_exclude jeff at last week";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test (expected = ParseException.class)
    public void missingKeyExpression() throws UnsupportedEncodingException, ParseException {
        String ccl = "= 1";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(
                StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test (expected = ParseException.class)
    public void missingValueExpression() throws UnsupportedEncodingException, ParseException {
        String ccl = "a =";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(
                StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test (expected = ParseException.class)
    public void missingOperatorExpression() throws UnsupportedEncodingException, ParseException {
        String ccl = "a 1";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(
                StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test (expected = ParseException.class)
    public void missingSecondExpression() throws UnsupportedEncodingException, ParseException {
        String ccl = "a = 1 and";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(
                StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test (expected = ParseException.class)
    public void missingFirstExpression() throws UnsupportedEncodingException, ParseException {
        String ccl = "and a = 1";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(
                StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test (expected = ParseException.class)
    public void invalidLink() throws UnsupportedEncodingException, ParseException {
        String ccl = "a LINKS_TO b";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void validBracketTimestamp() throws UnsupportedEncodingException, ParseException {
        String ccl = "name[1700000000] = jeff";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void validBracketTimestampWithKeyword() throws UnsupportedEncodingException, ParseException {
        String ccl = "name[at 1700000000] = jeff";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test (expected = Exception.class)
    public void invalidEmptyBracket() throws UnsupportedEncodingException, ParseException {
        String ccl = "name[] = jeff";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test (expected = Exception.class)
    public void invalidUnclosedBracket() throws UnsupportedEncodingException, ParseException {
        String ccl = "name[1700000000 = jeff";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test (expected = Exception.class)
    public void invalidWhitespaceOnlyBracket() throws UnsupportedEncodingException, ParseException {
        String ccl = "name[ ] = jeff";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testWithOffset() throws UnsupportedEncodingException, ParseException {
        String input = OFFSET + " 3";

        InputStream stream = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream,
                PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testWithOffsetAndLimit() throws UnsupportedEncodingException, ParseException {
        String input = OFFSET + " 3 " + LIMIT + " 1";

        InputStream stream = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream,
                PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testWithLimit() throws UnsupportedEncodingException, ParseException {
        String input = LIMIT + " 3";

        InputStream stream = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream,
                PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testWithLimitAndOffset() throws UnsupportedEncodingException, ParseException {
        String input = LIMIT + " 1 " + OFFSET + " 3";

        InputStream stream = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream,
                PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void validUnaryOperatorWithPage() throws UnsupportedEncodingException, ParseException {
        String ccl = "a = 1 " + OFFSET + " 1 " + LIMIT + " 3";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(
                StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test(expected = ParseException.class)
    public void invalidPageNumberSyntax() throws UnsupportedEncodingException, ParseException {
        String ccl = "page 2";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(
                StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test(expected = ParseException.class)
    public void invalidPageSizeSyntax() throws UnsupportedEncodingException, ParseException {
        String ccl = "size 10";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(
                StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test(expected = ParseException.class)
    public void invalidPageNumberAndSizeSyntax() throws UnsupportedEncodingException, ParseException {
        String ccl = "page 2 size 10";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(
                StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test(expected = ParseException.class)
    public void invalidSizeAndPageNumberSyntax() throws UnsupportedEncodingException, ParseException {
        String ccl = "size 10 page 2";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(
                StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testKey() throws UnsupportedEncodingException, ParseException {
        String input = ORDER + " age";

        InputStream stream = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testKeyAscendingSymbol() throws UnsupportedEncodingException, ParseException {
        String input = ORDER + " < age";

        InputStream stream = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testKeyAscendingWord() throws UnsupportedEncodingException, ParseException {
        String input = ORDER + " age ASC";

        InputStream stream = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testKeyDescendingSymbol() throws UnsupportedEncodingException, ParseException {
        String input = ORDER + " > age";

        InputStream stream = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testKeyDescendingWord() throws UnsupportedEncodingException, ParseException {
        String input = ORDER + " age DESC";

        InputStream stream = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testKeyWithNumberTimestamp() throws UnsupportedEncodingException, ParseException {
        String input = ORDER + " age at " + String.valueOf(122L);

        InputStream stream = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testKeyWithStringTimestamp() throws UnsupportedEncodingException, ParseException {
        String input = ORDER + " age at \"1992-10-02\"";

        InputStream stream = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testKeyWithNumberTimestampAscending() throws UnsupportedEncodingException, ParseException {
        String input = ORDER + "  > age at " + String.valueOf(122L);

        InputStream stream = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testKeyWithStringTimestampAscending() throws UnsupportedEncodingException, ParseException {
        String input = ORDER + " < age at \"1992-10-02\"";

        InputStream stream = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test(expected = ParseException.class)
    public void testKeyWithStringTimestampAscendingException() throws UnsupportedEncodingException, ParseException {
        String input = ORDER + " < age as of \"1992-10-02\"";

        InputStream stream = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testKeyWithNumberTimestampDescending() throws UnsupportedEncodingException, ParseException {
        String input = ORDER + " > age at " + String.valueOf(122L);

        InputStream stream = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testKeyWithStringTimestampDescending() throws UnsupportedEncodingException, ParseException {
        String input = ORDER + " > age at \"1992-10-02\"";

        InputStream stream = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testMultipleKeys() throws UnsupportedEncodingException, ParseException {
        String input = ORDER + " age, salary";

        InputStream stream = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testMultipleKeysWithDirectional() throws UnsupportedEncodingException, ParseException {
        String input = ORDER + " < age, > salary";

        InputStream stream = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void validUnaryOperatorWithOrder() throws UnsupportedEncodingException, ParseException {
        String ccl = "a = 1 " + ORDER + " a";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(
                StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void validUnaryOperatorWithOrderAndPage() throws UnsupportedEncodingException, ParseException {
        String ccl = "a = 1 " + ORDER + " a " + LIMIT + " 3 " + OFFSET + " 1";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(
                StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testKeyMultiRecordsFunction() throws UnsupportedEncodingException, ParseException {
        String ccl = "avg(age, 1,2,3,5,11)";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(
                StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testKeyMultiRecordsFunctionWithTimestamp() throws UnsupportedEncodingException, ParseException {
        String ccl = "avg(age, [1,2,3,5,11], at today)";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(
                StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testKeyConditionFunctionWithTimestamp() throws UnsupportedEncodingException, ParseException {
        String ccl = "avg(age, age > 30, at today)";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(
                StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testIndexFunctionWithTimestamp2() throws UnsupportedEncodingException, ParseException {
        String ccl = "avg(age, at today)";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(
                StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testOrderByTimestamp() throws UnsupportedEncodingException, ParseException {
        String ccl = "" + ORDER + " > age at 1992-10-02";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(
                StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testKeyMultiRecordsFunctionWithTimestamp2() throws UnsupportedEncodingException, ParseException {
        String ccl = "avg(age, [1,2,3,5,11], at 1992-10-02)";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(
                StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testImplicitKeyRecordFunction() throws UnsupportedEncodingException, ParseException {
        String ccl = "age | avg";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(
                StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testKeyCclFunctionWithTimestamp() throws UnsupportedEncodingException, ParseException {
        String ccl = "avg(age, age > 3, at 1992-10-02)";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(
                StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testImplicitKeyRecordFunctionTokenize() throws UnsupportedEncodingException, ParseException {
        String ccl = "age | avg";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(
                StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testIndexFunctionWithTimestamp() throws UnsupportedEncodingException, ParseException {
        String ccl = "avg(age, at 1992-10-02)";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(
                StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testAbortCommand() throws UnsupportedEncodingException, ParseException {
        String ccl = "abort";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(
                StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testAddSingleRecordCommand() throws UnsupportedEncodingException, ParseException {
        String ccl = "add name as \"John Doe\"";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(
                StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testAddSpecificRecordCommand() throws UnsupportedEncodingException, ParseException {
        String ccl = "add name as \"John Doe\" in 1";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(
                StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testAddMultipleRecordsCommand() throws UnsupportedEncodingException, ParseException {
        String ccl = "add name as \"John Doe\" in [1, 2, 3]";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(
                StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test(expected = ParseException.class)
    public void testInvalidAddCommandMissingValue() throws UnsupportedEncodingException, ParseException {
        String ccl = "add name as";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(
                StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test(expected = ParseException.class)
    public void testInvalidAddCommandMissingAs() throws UnsupportedEncodingException, ParseException {
        String ccl = "add name \"John Doe\"";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(
                StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test(expected = ParseException.class)
    public void testInvalidAddCommandMalformedRecordList() throws UnsupportedEncodingException, ParseException {
        String ccl = "add name as \"John Doe\" in [1, 2,]";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(
                StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testAddNumericValueCommand() throws UnsupportedEncodingException, ParseException {
        String ccl = "add age as 25";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(
                StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testAddCompoundValueCommand() throws UnsupportedEncodingException, ParseException {
        String ccl = "add full_name as \"John Q. Doe\"";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(
                StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testBrowseSingleKeyCommand() throws UnsupportedEncodingException, ParseException {
        String ccl = "browse [name]";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(
                StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testBrowseMultipleKeysCommand() throws UnsupportedEncodingException, ParseException {
        String ccl = "browse [name, age, location]";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(
                StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testBrowseSingleKeyWithTimestampCommand() throws UnsupportedEncodingException, ParseException {
        String ccl = "browse [name] at \"2024-01-01\"";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(
                StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testBrowseMultipleKeysWithTimestampCommand() throws UnsupportedEncodingException, ParseException {
        String ccl = "browse [name, age] at \"2024-01-01\"";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(
                StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testBrowseWithNaturalLanguageTimestamp() throws UnsupportedEncodingException, ParseException {
        String ccl = "browse [name] at \"two weeks ago\"";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(
                StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testBrowseWithMicrosTimestamp() throws UnsupportedEncodingException, ParseException {
        String ccl = "browse [name] at 1704067200000000"; // Unix timestamp in microseconds
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(
                StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testBrowseSingleKeyWithoutBrackets() throws UnsupportedEncodingException, ParseException {
        String ccl = "browse name";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(
                StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test(expected = ParseException.class)
    public void testInvalidBrowseCommandEmptyKeyList() throws UnsupportedEncodingException, ParseException {
        String ccl = "browse []";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(
                StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test(expected = ParseException.class)
    public void testInvalidBrowseCommandMalformedKeyList() throws UnsupportedEncodingException, ParseException {
        String ccl = "browse [name,,age]";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(
                StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test(expected = ParseException.class)
    public void testInvalidBrowseCommandMalformedTimestamp() throws UnsupportedEncodingException, ParseException {
        String ccl = "browse [name] at at \"2024-01-01\"";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(
                StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testChronicleBasicCommand() throws UnsupportedEncodingException, ParseException {
        String ccl = "chronicle name in 1";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testChronicleWithRange() throws UnsupportedEncodingException, ParseException {
        String ccl = "chronicle name in 1 from \"2024-01-01\" to \"2024-02-01\"";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testChronicleWithNaturalLanguageRange() throws UnsupportedEncodingException, ParseException {
        String ccl = "chronicle name in 1 from \"two weeks ago\" to \"today\"";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testClearSingleRecordCommand() throws UnsupportedEncodingException, ParseException {
        String ccl = "clear 1";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testClearMultipleRecordsCommand() throws UnsupportedEncodingException, ParseException {
        String ccl = "clear [1, 2, 3]";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testClearSingleKeyFromRecord() throws UnsupportedEncodingException, ParseException {
        String ccl = "clear name from 1";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testClearMultipleKeysFromRecord() throws UnsupportedEncodingException, ParseException {
        String ccl = "clear [name, age] from 1";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testClearKeysFromMultipleRecords() throws UnsupportedEncodingException, ParseException {
        String ccl = "clear [name, age] from [1, 2, 3]";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testClearSingleKeyFromMultipleRecords() throws UnsupportedEncodingException, ParseException {
        String ccl = "clear name from [1, 2, 3]";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test(expected = ParseException.class)
    public void testInvalidChronicleNoRecord() throws UnsupportedEncodingException, ParseException {
        String ccl = "chronicle name";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test(expected = ParseException.class)
    public void testInvalidClearNoTarget() throws UnsupportedEncodingException, ParseException {
        String ccl = "clear";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testCommitCommand() throws UnsupportedEncodingException, ParseException {
        String ccl = "commit";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test(expected = ParseException.class)
    public void testInvalidCommitWithArguments() throws UnsupportedEncodingException, ParseException {
        String ccl = "commit 1";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test(expected = ParseException.class)
    public void testInvalidCommitWithTimestamp() throws UnsupportedEncodingException, ParseException {
        String ccl = "commit at \"now\"";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testDescribeBasic() throws UnsupportedEncodingException, ParseException {
        String ccl = "describe";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testDescribeSingleRecord() throws UnsupportedEncodingException, ParseException {
        String ccl = "describe 1";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testDescribeMultipleRecords() throws UnsupportedEncodingException, ParseException {
        String ccl = "describe [1, 2, 3]";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testDescribeWithTimestamp() throws UnsupportedEncodingException, ParseException {
        String ccl = "describe 1 at \"2024-01-01\"";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testDescribeMultipleRecordsWithTimestamp() throws UnsupportedEncodingException, ParseException {
        String ccl = "describe [1, 2, 3] at \"2024-01-01\"";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testDiffSingleRecordWithRange() throws UnsupportedEncodingException, ParseException {
        String ccl = "diff 1 from \"2024-01-01\" to \"2024-02-01\"";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testDiffKeyRecordWithRange() throws UnsupportedEncodingException, ParseException {
        String ccl = "diff name in 1 from \"2024-01-01\" to \"2024-02-01\"";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testDiffKeyWithRange() throws UnsupportedEncodingException, ParseException {
        String ccl = "diff name from \"2024-01-01\" to \"2024-02-01\"";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test(expected = ParseException.class)
    public void testInvalidDiffNoTimestamp() throws UnsupportedEncodingException, ParseException {
        String ccl = "diff 1";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test(expected = ParseException.class)
    public void testInvalidExitWithParameters() throws UnsupportedEncodingException, ParseException {
        String ccl = "exit now";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testBasicFind() throws UnsupportedEncodingException, ParseException {
        String ccl = "find name = \"John\"";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testFindWithOrder() throws UnsupportedEncodingException, ParseException {
        String ccl = "find name = \"John\" " + ORDER + " age";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testFindWithOrderAndPage() throws UnsupportedEncodingException, ParseException {
        String ccl = "find name = \"John\" " + ORDER + " age " + LIMIT + " 10";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testFindWithTimestamp() throws UnsupportedEncodingException, ParseException {
        String ccl = "find name = \"John\" at \"2024-01-01\"";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testFindWithComplexOperator() throws UnsupportedEncodingException, ParseException {
        String ccl = "find age >< 25 35";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testFindWithComplexCriteria() throws UnsupportedEncodingException, ParseException {
        String ccl = "find (age > 25 and name = \"John\") or (age < 20 and city = \"NYC\")";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testFindWithRegex() throws UnsupportedEncodingException, ParseException {
        String ccl = "find name regex (?i:john.*)";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testFindWithMultipleOrderCriteria() throws UnsupportedEncodingException, ParseException {
        String ccl = "find age > 25 " + ORDER + " name ASC, age DESC";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testFindWithFunction() throws UnsupportedEncodingException, ParseException {
        String ccl = "find age > avg(age)";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test(expected = ParseException.class)
    public void testInvalidFindIncompleteCriteria() throws UnsupportedEncodingException, ParseException {
        String ccl = "find name";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test(expected = ParseException.class)
    public void testInvalidFindOperator() throws UnsupportedEncodingException, ParseException {
        String ccl = "find age >>>> 25";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test(expected = IllegalStateException.class)
    public void testInvalidFindMalformedTimestamp() throws UnsupportedEncodingException, ParseException {
        String ccl = "find age > 25 at invalid-timestamp";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testGetSingleKeyRecord() throws UnsupportedEncodingException, ParseException {
        String ccl = "get name from 1";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testGetMultipleKeysFromRecord() throws UnsupportedEncodingException, ParseException {
        String ccl = "get [name, age] from 1";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testGetKeysFromRecordsWithTimestamp() throws UnsupportedEncodingException, ParseException {
        String ccl = "get [name, age] from [1, 2, 3] at \"2024-01-01\"";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testGetKeysWithCriteria() throws UnsupportedEncodingException, ParseException {
        String ccl = "get [name, age] where age > 25";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testGetWithCriteriaAndOrder() throws UnsupportedEncodingException, ParseException {
        String ccl = "get [name, age] where age > 25 " + ORDER + " age DESC";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testGetWithCriteriaAndPagination() throws UnsupportedEncodingException, ParseException {
        String ccl = "get [name, age] where age > 25 " + OFFSET + " 10";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testGetWithComplexCriteria() throws UnsupportedEncodingException, ParseException {
        String ccl = "get [name, age, city] where (age > 25 and city = \"NYC\") or (age < 20 and city = \"SF\")";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testGetWithTimestampAndOrder() throws UnsupportedEncodingException, ParseException {
        String ccl = "get [name, age] where age > 25 at \"2024-01-01\" " + ORDER + " age ASC";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test(expected = ParseException.class)
    public void testInvalidGetNoKeys() throws UnsupportedEncodingException, ParseException {
        String ccl = "get";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test(expected = ParseException.class)
    public void testInvalidGetMalformedKeys() throws UnsupportedEncodingException, ParseException {
        String ccl = "get [name,,age] from 1";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test(expected = ParseException.class)
    public void testInvalidGetMissingFrom() throws UnsupportedEncodingException, ParseException {
        String ccl = "get [name, age] 1";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testBasicInsert() throws UnsupportedEncodingException, ParseException {
        String ccl = "insert \"{'name': 'John', 'age': 25}\"";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testInsertIntoSpecificRecord() throws UnsupportedEncodingException, ParseException {
        String ccl = "insert \"{'name': 'John', 'age': 25}\" in 1";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testInsertIntoMultipleRecords() throws UnsupportedEncodingException, ParseException {
        String ccl = "insert \"{'name': 'John', 'age': 25}\" in [1, 2, 3]";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testInsertComplexJson() throws UnsupportedEncodingException, ParseException {
        String ccl = "insert \"{'name': 'John', 'age': 25, 'skills': ['java', 'python'], 'active': true}\"";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testInsertArrayOfObjects() throws UnsupportedEncodingException, ParseException {
        String ccl = "insert \"[{'name': 'John', 'age': 25}, {'name': 'Jane', 'age': 30}]\"";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testInventory() throws UnsupportedEncodingException, ParseException {
        String ccl = "inventory";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test(expected = ParseException.class)
    public void testInvalidInsertMalformedJson() throws UnsupportedEncodingException, ParseException {
        String ccl = "insert \"{name: John, age: 25\"";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test(expected = ParseException.class)
    public void testInvalidInsertNoJson() throws UnsupportedEncodingException, ParseException {
        String ccl = "insert in 1";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test(expected = ParseException.class)
    public void testInvalidInsertMalformedRecordList() throws UnsupportedEncodingException, ParseException {
        String ccl = "insert \"{'name': 'John'}\" in [1,,2]";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testNavigateSingleKey() throws UnsupportedEncodingException, ParseException {
        String ccl = "navigate [user.name] from 1";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testNavigateMultipleKeys() throws UnsupportedEncodingException, ParseException {
        String ccl = "navigate [user.name, user.profile.age] from [1, 2, 3]";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testNavigateWithCriteria() throws UnsupportedEncodingException, ParseException {
        String ccl = "navigate [user.friends] where age > 25";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testNavigateWithTimestamp() throws UnsupportedEncodingException, ParseException {
        String ccl = "navigate [user.profile] from 1 at \"2024-01-01\"";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testNavigateComplexPath() throws UnsupportedEncodingException, ParseException {
        String ccl = "navigate [user.profile.friends.contacts.email] from 1";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test(expected = ParseException.class)
    public void testInvalidNavigateNoKeys() throws UnsupportedEncodingException, ParseException {
        String ccl = "navigate from 1";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test(expected = ParseException.class)
    public void testInvalidNavigateMalformedPath() throws UnsupportedEncodingException, ParseException {
        String ccl = "navigate [user..name] from 1";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test(expected = ParseException.class)
    public void testInvalidNavigateEmptyKey() throws UnsupportedEncodingException, ParseException {
        String ccl = "navigate [] from 1";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testRemoveSingleValue() throws UnsupportedEncodingException, ParseException {
        String ccl = "remove name as \"John\" from 1";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testRemoveFromMultipleRecords() throws UnsupportedEncodingException, ParseException {
        String ccl = "remove name as \"John\" from [1, 2, 3]";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test(expected = ParseException.class)
    public void testRemoveWithoutValueFromSingleRecordFails() throws UnsupportedEncodingException, ParseException {
        String ccl = "remove name from 1";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test(expected = ParseException.class)
    public void testRemoveWithoutValueFromBracketedRecordsFails() throws UnsupportedEncodingException, ParseException {
        String ccl = "remove name from [1, 2]";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testRevertSingleKey() throws UnsupportedEncodingException, ParseException {
        String ccl = "revert [name] in 1 at \"2024-01-01\"";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testRevertMultipleKeys() throws UnsupportedEncodingException, ParseException {
        String ccl = "revert [name, age] in [1, 2, 3] at \"2024-01-01\"";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testAuditBasic() throws UnsupportedEncodingException, ParseException {
        String ccl = "audit 1";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testAuditWithTimeRange() throws UnsupportedEncodingException, ParseException {
        String ccl = "audit name in 1 from \"2024-01-01\" to \"2024-02-01\"";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testSearchCommand() throws UnsupportedEncodingException, ParseException {
        String ccl = "search name for \"John\"";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testSelectBasic() throws UnsupportedEncodingException, ParseException {
        String ccl = "select [name, age] from 1";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testSelectWithCriteria() throws UnsupportedEncodingException, ParseException {
        String ccl = "select [name, age] where age > 25";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testSelectWithTimestampAndOrder() throws UnsupportedEncodingException, ParseException {
        String ccl = "select [name, age] where age > 25 at \"2024-01-01\" " + ORDER + " age DESC";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test(expected = ParseException.class)
    public void testInvalidRevertNoTimestamp() throws UnsupportedEncodingException, ParseException {
        String ccl = "revert [name] in 1";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test(expected = ParseException.class)
    public void testInvalidSelectMissingKeys() throws UnsupportedEncodingException, ParseException {
        String ccl = "select from 1";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testSelectSingleRecordNoKeys() throws UnsupportedEncodingException, ParseException {
        String ccl = "select 1";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testSelectRecordCollectionNoKeys() throws UnsupportedEncodingException, ParseException {
        String ccl = "select [1, 2, 3]";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testSelectSingleRecordWithTimestamp() throws UnsupportedEncodingException, ParseException {
        String ccl = "select 1 at \"yesterday\"";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testSelectWhereNoKeys() throws UnsupportedEncodingException, ParseException {
        String ccl = "select where age > 25";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testGetFromRecordNoKeys() throws UnsupportedEncodingException, ParseException {
        String ccl = "get from 1";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testGetFromRecordsNoKeys() throws UnsupportedEncodingException, ParseException {
        String ccl = "get from [1, 2]";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testGetWhereNoKeys() throws UnsupportedEncodingException, ParseException {
        String ccl = "get where age > 25";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testGetSingleKeyWhere() throws UnsupportedEncodingException, ParseException {
        String ccl = "get name where age > 30";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testAddBracketlessRecordCollection() throws UnsupportedEncodingException, ParseException {
        String ccl = "add name as \"jeff\" in 1, 2, 3";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testSelectBracketlessKeyCollection() throws UnsupportedEncodingException, ParseException {
        String ccl = "select name, age from 1";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testSelectBracketlessRecordCollectionNoKeys() throws UnsupportedEncodingException, ParseException {
        String ccl = "select 1, 2, 3";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testGetBracketlessKeyCollection() throws UnsupportedEncodingException, ParseException {
        String ccl = "get name, age from 1";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testGetBracketlessKeyCollectionWhere() throws UnsupportedEncodingException, ParseException {
        String ccl = "get name, age where age > 30";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testClearBracketlessKeyCollection() throws UnsupportedEncodingException, ParseException {
        String ccl = "clear name, age from 1";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testClearBracketlessRecordCollection() throws UnsupportedEncodingException, ParseException {
        String ccl = "clear 1, 2, 3";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testBrowseBracketlessKeyCollection() throws UnsupportedEncodingException, ParseException {
        String ccl = "browse name, age";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testSetBracketlessRecordCollection() throws UnsupportedEncodingException, ParseException {
        String ccl = "set name as \"jeff\" in 1, 2, 3";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testRemoveBracketlessRecordCollection() throws UnsupportedEncodingException, ParseException {
        String ccl = "remove name as \"jeff\" from 1, 2, 3";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test(expected = ParseException.class)
    public void testRemoveWithoutValueFromBracketlessRecordsFails() throws UnsupportedEncodingException, ParseException {
        String ccl = "remove name from 1, 2";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testInsertBracketlessRecordCollection() throws UnsupportedEncodingException, ParseException {
        String ccl = "insert \"{'name': 'jeff'}\" into 1, 2, 3";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testLinkBracketlessRecordCollection() throws UnsupportedEncodingException, ParseException {
        String ccl = "link friends from 1 to 2, 3, 4";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testNavigateBracketlessKeyCollection() throws UnsupportedEncodingException, ParseException {
        String ccl = "navigate name, age from 1";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testNavigateBracketlessRecordCollection() throws UnsupportedEncodingException, ParseException {
        String ccl = "navigate name from 1, 2, 3";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testCalculateBracketlessRecordCollection() throws UnsupportedEncodingException, ParseException {
        String ccl = "calculate count name from 1, 2, 3";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testRevertBracketlessKeyCollection() throws UnsupportedEncodingException, ParseException {
        String ccl = "revert name, age in 1 at \"yesterday\"";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testRevertBracketlessRecordCollection() throws UnsupportedEncodingException, ParseException {
        String ccl = "revert name in 1, 2, 3 at \"yesterday\"";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testReconcileBracketlessValueCollection() throws UnsupportedEncodingException, ParseException {
        String ccl = "reconcile name in 1 with \"jeff\", \"bob\"";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testStageCommand() throws UnsupportedEncodingException, ParseException {
        String ccl = "stage";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test(expected = ParseException.class)
    public void testInvalidStageWithArguments() throws UnsupportedEncodingException, ParseException {
        String ccl = "stage data";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testUnlinkCommand() throws UnsupportedEncodingException, ParseException {
        String ccl = "unlink friends from 1 to 2";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test(expected = ParseException.class)
    public void testInvalidUnlinkMultipleDestinations() throws UnsupportedEncodingException, ParseException {
        String ccl = "unlink friends from 1 to [2, 3, 4]";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testVerifyCommand() throws UnsupportedEncodingException, ParseException {
        String ccl = "verify name as \"John\" in 1";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testVerifyWithTimestamp() throws UnsupportedEncodingException, ParseException {
        String ccl = "verify name as \"John\" in 1 at \"2024-01-01\"";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testVerifyAndSwapCommand() throws UnsupportedEncodingException, ParseException {
        String ccl = "verifyAndSwap name as \"John\" in 1 with \"Jane\"";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testVerifyOrSetCommand() throws UnsupportedEncodingException, ParseException {
        String ccl = "verifyOrSet name as \"John\" in 1";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test(expected = ParseException.class)
    public void testInvalidUnlinkMissingDestination() throws UnsupportedEncodingException, ParseException {
        String ccl = "unlink friends from 1";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test(expected = ParseException.class)
    public void testInvalidVerifyNoValue() throws UnsupportedEncodingException, ParseException {
        String ccl = "verify name in 1";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test(expected = ParseException.class)
    public void testInvalidVerifyAndSwapMissingReplacement() throws UnsupportedEncodingException, ParseException {
        String ccl = "verifyAndSwap name as \"John\" in 1";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testTraceBasicSingleRecord() throws UnsupportedEncodingException, ParseException {
        String ccl = "trace 1";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testTraceMultipleRecords() throws UnsupportedEncodingException, ParseException {
        String ccl = "trace [1, 2, 3]";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testTraceSingleRecordWithTimestamp() throws UnsupportedEncodingException, ParseException {
        String ccl = "trace 1 at \"2024-01-01\"";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testTraceMultipleRecordsWithTimestamp() throws UnsupportedEncodingException, ParseException {
        String ccl = "trace [1, 2, 3] at \"2024-01-01\"";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testTraceWithNaturalLanguageTimestamp() throws UnsupportedEncodingException, ParseException {
        String ccl = "trace 1 at \"two weeks ago\"";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test(expected = ParseException.class)
    public void testInvalidTraceNoRecord() throws UnsupportedEncodingException, ParseException {
        String ccl = "trace";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test(expected = ParseException.class)
    public void testInvalidTraceMalformedRecordList() throws UnsupportedEncodingException, ParseException {
        String ccl = "trace [1,,2]";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testFindOrAddBasic() throws UnsupportedEncodingException, ParseException {
        String ccl = "findOrAdd name as \"John\"";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testFindOrAddNumericValue() throws UnsupportedEncodingException, ParseException {
        String ccl = "findOrAdd age as 25";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testFindOrInsertWithCriteria() throws UnsupportedEncodingException, ParseException {
        String ccl = "findOrInsert age > 25 \"{'name': 'John', 'age': 30}\"";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testFindOrInsertWithComplexCriteria() throws UnsupportedEncodingException, ParseException {
        String ccl = "findOrInsert (age > 25 and city = \"NYC\") \"{'name': 'John', 'age': 30, 'city': 'NYC'}\"";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testFindOrInsertWithTimestamp() throws UnsupportedEncodingException, ParseException {
        String ccl = "findOrInsert age > 25 at \"2024-01-01\" \"{'name': 'John', 'age': 30}\"";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testFindOrInsertWithParenthesizedTimestampAndCommandTimestamp() throws UnsupportedEncodingException, ParseException {
        String ccl = "findOrInsert (age > 25 at \"yesterday\") at \"2024-01-01\" \"{'name': 'John'}\"";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    // === Bare record GET tests ===

    @Test
    public void testGetSingleRecordNoKeys() throws UnsupportedEncodingException, ParseException {
        String ccl = "get 1";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testGetRecordCollectionNoKeys() throws UnsupportedEncodingException, ParseException {
        String ccl = "get [1, 2, 3]";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testGetBracketlessRecordCollectionNoKeys() throws UnsupportedEncodingException, ParseException {
        String ccl = "get 1, 2, 3";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    // === SET command tests ===

    @Test
    public void testSetSingleRecordCommand() throws UnsupportedEncodingException, ParseException {
        String ccl = "set name as \"Jeff\" in 1";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testSetMultipleRecordsCommand() throws UnsupportedEncodingException, ParseException {
        String ccl = "set name as \"Jeff\" in [1, 2, 3]";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    // === LINK command tests ===

    @Test
    public void testLinkSingleDestinationCommand() throws UnsupportedEncodingException, ParseException {
        String ccl = "link friends from 1 to 2";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testLinkMultipleDestinationsCommand() throws UnsupportedEncodingException, ParseException {
        String ccl = "link friends from 1 to [2, 3, 4]";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    // === UNLINK command tests ===

    @Test
    public void testUnlinkSingleDestinationCommand() throws UnsupportedEncodingException, ParseException {
        String ccl = "unlink friends from 1 to 2";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    // === JSONIFY command tests ===

    @Test
    public void testJsonifySingleRecordCommand() throws UnsupportedEncodingException, ParseException {
        String ccl = "jsonify 1";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testJsonifyMultipleRecordsCommand() throws UnsupportedEncodingException, ParseException {
        String ccl = "jsonify [1, 2, 3]";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testJsonifyWithoutIdentifierCommand() throws UnsupportedEncodingException, ParseException {
        String ccl = "jsonify 1 without $id$";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testJsonifyWithTimestampCommand() throws UnsupportedEncodingException, ParseException {
        String ccl = "jsonify 1 at \"December 30, 1987\"";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testJsonifyWithoutIdentifierAndTimestampCommand() throws UnsupportedEncodingException, ParseException {
        String ccl = "jsonify 1 without $id$ at \"December 30, 1987\"";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    // === RECONCILE command tests ===

    @Test
    public void testReconcileCommand() throws UnsupportedEncodingException, ParseException {
        String ccl = "reconcile name in 1 with [\"Jeff\", \"Bob\"]";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    // === NAVIGATE single key tests ===

    @Test
    public void testNavigateSingleKeyFromRecordCommand() throws UnsupportedEncodingException, ParseException {
        String ccl = "navigate name from 1";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    // === PING command tests ===

    @Test
    public void testPingNoArgsCommand() throws UnsupportedEncodingException, ParseException {
        String ccl = "ping";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test(expected = ParseException.class)
    public void testInvalidPingSingleRecordCommand() throws UnsupportedEncodingException, ParseException {
        String ccl = "ping 1";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test(expected = ParseException.class)
    public void testInvalidPingMultipleRecordsCommand() throws UnsupportedEncodingException, ParseException {
        String ccl = "ping [1, 2, 3]";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    // === HOLDS command tests ===

    @Test
    public void testHoldsSingleRecordCommand() throws UnsupportedEncodingException, ParseException {
        String ccl = "holds 1";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testHoldsMultipleRecordsCommand() throws UnsupportedEncodingException, ParseException {
        String ccl = "holds [1, 2, 3]";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test(expected = ParseException.class)
    public void testInvalidHoldsNoRecordCommand() throws UnsupportedEncodingException, ParseException {
        String ccl = "holds";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    // === CONSOLIDATE command tests ===

    @Test
    public void testConsolidateTwoRecordsCommand() throws UnsupportedEncodingException, ParseException {
        String ccl = "consolidate 1 2";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testConsolidateMultipleRecordsCommand() throws UnsupportedEncodingException, ParseException {
        String ccl = "consolidate 1 [2, 3, 4]";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test(expected = ParseException.class)
    public void testInvalidConsolidateSingleRecordCommand() throws UnsupportedEncodingException, ParseException {
        String ccl = "consolidate 1";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    // === CALCULATE command tests ===

    @Test
    public void testCalculateSumCommand() throws UnsupportedEncodingException, ParseException {
        String ccl = "calculate sum age";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testCalculateAverageWithRecordsCommand() throws UnsupportedEncodingException, ParseException {
        String ccl = "calculate average age in [1, 2, 3]";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testCalculateCountWithConditionCommand() throws UnsupportedEncodingException, ParseException {
        String ccl = "calculate count name where name = \"Jeff\"";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testCalculateMinWithTimestampCommand() throws UnsupportedEncodingException, ParseException {
        String ccl = "calculate min age at \"December 30, 1987\"";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testCalculateMaxWithRecordsAndTimestampCommand() throws UnsupportedEncodingException, ParseException {
        String ccl = "calculate max score in [1, 2] at \"December 30, 1987\"";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test(expected = ParseException.class)
    public void testInvalidCalculateMissingKeyCommand() throws UnsupportedEncodingException, ParseException {
        String ccl = "calculate sum";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testCalculateSumSingleRecordCommand() throws UnsupportedEncodingException, ParseException {
        String ccl = "calculate sum age in 1";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testCalculateSumSingleRecordWithTimestampCommand() throws UnsupportedEncodingException, ParseException {
        String ccl = "calculate sum age in 1 at \"December 30, 1987\"";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test(expected = Exception.class)
    public void testInvalidCalculateBadFunctionCommand() throws UnsupportedEncodingException, ParseException {
        String ccl = "calculate xyzgarbage age";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    // === NAVIGATE single key with WHERE test ===

    @Test
    public void testNavigateSingleKeyWhereCommand() throws UnsupportedEncodingException, ParseException {
        String ccl = "navigate name where name = \"Jeff\"";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testNavigateWhereWithTimestampCommand() throws UnsupportedEncodingException, ParseException {
        String ccl = "navigate [user.friends] where age > 25 at \"December 30, 1987\"";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testCalculateCountWithConditionAndTimestampCommand() throws UnsupportedEncodingException, ParseException {
        String ccl = "calculate count name where name = \"Jeff\" at \"December 30, 1987\"";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    // === DESCRIBE timestamp-only tests ===

    @Test
    public void testDescribeTimestampOnlyCommand() throws UnsupportedEncodingException, ParseException {
        String ccl = "describe at \"December 30, 1987\"";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testDescribeNoArgsCommand() throws UnsupportedEncodingException, ParseException {
        String ccl = "describe";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    // === Preposition alias tests ===

    // Write/Put: in, to, within

    @Test
    public void testAddWithToPreposition() throws UnsupportedEncodingException, ParseException {
        String ccl = "add name as \"John Doe\" to 1";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testAddWithWithinPreposition() throws UnsupportedEncodingException, ParseException {
        String ccl = "add name as \"John Doe\" within 1";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testAddMultipleRecordsWithToPreposition() throws UnsupportedEncodingException, ParseException {
        String ccl = "add name as \"John Doe\" to [1, 2, 3]";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testSetWithWithinPreposition() throws UnsupportedEncodingException, ParseException {
        String ccl = "set name as \"Jeff\" within 1";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testInsertWithToPreposition() throws UnsupportedEncodingException, ParseException {
        String ccl = "insert \"{'name': 'John', 'age': 25}\" to 1";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testInsertWithWithinPreposition() throws UnsupportedEncodingException, ParseException {
        String ccl = "insert \"{'name': 'John', 'age': 25}\" within [1, 2, 3]";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    // Read/Extract: from, in, within

    @Test
    public void testSelectWithInPreposition() throws UnsupportedEncodingException, ParseException {
        String ccl = "select name in 1";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testSelectWithWithinPreposition() throws UnsupportedEncodingException, ParseException {
        String ccl = "select [name, age] within [1, 2, 3]";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testGetWithInPreposition() throws UnsupportedEncodingException, ParseException {
        String ccl = "get name in 1";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testGetWithWithinPreposition() throws UnsupportedEncodingException, ParseException {
        String ccl = "get [name, age] within [1, 2, 3]";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testNavigateWithInPreposition() throws UnsupportedEncodingException, ParseException {
        String ccl = "navigate [user.name] in 1";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testNavigateWithWithinPreposition() throws UnsupportedEncodingException, ParseException {
        String ccl = "navigate [user.name] within [1, 2]";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testCalculateWithFromPreposition() throws UnsupportedEncodingException, ParseException {
        String ccl = "calculate average score from 1";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testCalculateWithWithinPreposition() throws UnsupportedEncodingException, ParseException {
        String ccl = "calculate sum name within [1, 2, 3]";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    // Remove: from, in, within

    @Test
    public void testRemoveWithWithinPreposition() throws UnsupportedEncodingException, ParseException {
        String ccl = "remove name as \"John\" within 1";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testClearWithInPreposition() throws UnsupportedEncodingException, ParseException {
        String ccl = "clear name in 1";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testClearWithWithinPreposition() throws UnsupportedEncodingException, ParseException {
        String ccl = "clear [name, age] within [1, 2, 3]";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    // Inspect: in, within

    @Test
    public void testVerifyWithWithinPreposition() throws UnsupportedEncodingException, ParseException {
        String ccl = "verify name as \"John\" within 1";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testVerifyAndSwapWithWithinPreposition() throws UnsupportedEncodingException, ParseException {
        String ccl = "verifyAndSwap name as \"John\" within 1 with \"Jane\"";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testVerifyOrSetWithWithinPreposition() throws UnsupportedEncodingException, ParseException {
        String ccl = "verifyOrSet name as \"John\" within 1";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testChronicleWithWithinPreposition() throws UnsupportedEncodingException, ParseException {
        String ccl = "chronicle name within 1";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testChronicleWithWithinPrepositionAndRange() throws UnsupportedEncodingException, ParseException {
        String ccl = "chronicle name within 1 from \"2024-01-01\" to \"2024-02-01\"";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testDiffWithWithinPreposition() throws UnsupportedEncodingException, ParseException {
        String ccl = "diff name within 1 from \"2024-01-01\" to \"2024-02-01\"";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test(expected = ParseException.class)
    public void testInvalidChronicleLegacySyntax() throws UnsupportedEncodingException, ParseException {
        String ccl = "chronicle name in 1 at \"2024-01-01\" at \"2024-02-01\"";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test(expected = ParseException.class)
    public void testInvalidDiffSingleRecordLegacySyntax() throws UnsupportedEncodingException, ParseException {
        String ccl = "diff 1 at \"2024-01-01\" at \"2024-02-01\"";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test(expected = ParseException.class)
    public void testInvalidDiffKeyRecordLegacySyntax() throws UnsupportedEncodingException, ParseException {
        String ccl = "diff name in 1 at \"2024-01-01\" at \"2024-02-01\"";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test(expected = ParseException.class)
    public void testInvalidDiffKeyLegacySyntax() throws UnsupportedEncodingException, ParseException {
        String ccl = "diff name at \"2024-01-01\" at \"2024-02-01\"";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testAuditWithWithinPreposition() throws UnsupportedEncodingException, ParseException {
        String ccl = "audit name within 1";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testAuditWithWithinPrepositionAndRange() throws UnsupportedEncodingException, ParseException {
        String ccl = "audit name within 1 from \"2024-01-01\" to \"2024-02-01\"";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testChronicleWithSingleTimestamp() throws UnsupportedEncodingException, ParseException {
        String ccl = "chronicle name in 1 from 100";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testChronicleWithSingleTimestampNaturalLanguage() throws UnsupportedEncodingException, ParseException {
        String ccl = "chronicle name in 1 from \"two weeks ago\"";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testAuditRecordWithSingleTimestamp() throws UnsupportedEncodingException, ParseException {
        String ccl = "audit 1 from 100";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testAuditKeyRecordWithSingleTimestamp() throws UnsupportedEncodingException, ParseException {
        String ccl = "audit name in 1 from 100";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testDiffRecordWithSingleTimestamp() throws UnsupportedEncodingException, ParseException {
        String ccl = "diff 1 from 100";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testDiffKeyRecordWithSingleTimestamp() throws UnsupportedEncodingException, ParseException {
        String ccl = "diff name in 1 from 100";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testDiffKeyWithSingleTimestamp() throws UnsupportedEncodingException, ParseException {
        String ccl = "diff name from 100";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test(expected = ParseException.class)
    public void testInvalidAuditLegacySyntax() throws UnsupportedEncodingException, ParseException {
        String ccl = "audit name in 1 at \"2024-01-01\" at \"2024-02-01\"";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testRevertWithWithinPreposition() throws UnsupportedEncodingException, ParseException {
        String ccl = "revert [name] within 1 at \"2024-01-01\"";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testReconcileWithWithinPreposition() throws UnsupportedEncodingException, ParseException {
        String ccl = "reconcile name within 1 with [\"Jeff\", \"Bob\"]";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    // -- Edge case tests --

    @Test(expected = ParseException.class)
    public void testSelectInPrepositionWithInTimestamp() throws UnsupportedEncodingException, ParseException {
        String ccl = "select name in 1 in \"yesterday\"";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test(expected = ParseException.class)
    public void testGetInPrepositionWithInTimestamp() throws UnsupportedEncodingException, ParseException {
        String ccl = "get name in 1 in \"yesterday\"";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test(expected = ParseException.class)
    public void testBrowseWithInTimestampCommand() throws UnsupportedEncodingException, ParseException {
        String ccl = "browse [name] in \"2024-01-01\"";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(
                StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test(expected = ParseException.class)
    public void testFindOrInsertWithInTimestampCommand() throws UnsupportedEncodingException, ParseException {
        String ccl = "find_or_insert name = jeff in \"2024-01-01\" \"{\\\"name\\\":\\\"Jeff\\\"}\"";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(
                StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test(expected = ParseException.class)
    public void testIndexFunctionWithInTimestampFails() throws UnsupportedEncodingException, ParseException {
        String ccl = "avg(age, in 1992-10-02)";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(
                StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testInsertWithIntoPreposition() throws UnsupportedEncodingException, ParseException {
        String ccl = "insert \"{\\\"name\\\": \\\"Jeff\\\"}\" into 1";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testClearWithFromPreposition() throws UnsupportedEncodingException, ParseException {
        String ccl = "clear name from 1";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testCalculateAvgAlias() throws UnsupportedEncodingException, ParseException {
        String ccl = "calculate avg score from 1";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testSelectWithinPrepositionWithAtTimestamp() throws UnsupportedEncodingException, ParseException {
        String ccl = "select name within 1 at \"yesterday\"";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    /**
     * Constants
     */
    private static final String LIMIT = "limit";
    private static final String OFFSET = "offset";
    private static final String ORDER = "ORDER BY";

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
    public final Function<String, Operator> PARSER_TRANSFORM_OPERATOR_FUNCTION = operator -> Convert
            .stringToOperator(operator);

    public final GrammarVisitor visitor = new GrammarVisitor() {
        @Override
        public Object visit(SimpleNode node, Object data) {
            System.out.println(node + ": acceptor not unimplemented in subclass?");
            data = node.childrenAccept(this, data);
            return data;
        }

        @Override
        public Object visit(ASTStart node, Object data) {
            data = node.jjtGetChild(0).jjtAccept(this, data);
            return data;
        }

        @Override
        public Object visit(ASTOr node, Object data) {
            ConditionTree left = (ConditionTree) node.jjtGetChild(0).jjtAccept(this, data);
            ConditionTree right =(ConditionTree) node.jjtGetChild(1).jjtAccept(this, data);
            return new OrTree(left, right);
        }

        @Override
        public Object visit(ASTAnd node, Object data) {
            ConditionTree left = (ConditionTree) node.jjtGetChild(0).jjtAccept(this, data);
            ConditionTree right =(ConditionTree) node.jjtGetChild(1).jjtAccept(this, data);
            return new AndTree(left, right);
        }

        @Override
        public Object visit(ASTScoped node, Object data) {
            ConditionTree inner = (ConditionTree) node.jjtGetChild(0).jjtAccept(this, data);
            return new ScopedConditionTree(node.prefix(), inner);
        }

        @Override
        public Object visit(ASTExpression node, Object data) {
            return new ExpressionTree(node);
        }

        @Override public Object visit(ASTOrder node, Object data) {
            return new OrderTree(node.order());
        }

        @Override
        public Object visit(ASTPage node, Object data) {
            return new PageTree(node.page());
        }

        @Override
        public Object visit(ASTFunction node, Object data) {
            return new FunctionTree(node.function());
        }

        @Override
        public Object visit(ASTCommand node, Object data) {
            return new CommandTree(node.command(), null, null, null);
        }
    };

}
