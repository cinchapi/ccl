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

import com.cinchapi.ccl.generated.ASTFunction;
import com.cinchapi.ccl.generated.ASTOrder;
import com.cinchapi.ccl.generated.ASTPage;
import com.cinchapi.ccl.generated.Grammar;
import com.cinchapi.ccl.generated.GrammarVisitor;
import com.cinchapi.ccl.syntax.AndTree;
import com.cinchapi.ccl.syntax.ConditionTree;
import com.cinchapi.ccl.syntax.ExpressionTree;
import com.cinchapi.ccl.syntax.FunctionTree;
import com.cinchapi.ccl.syntax.OrTree;
import com.cinchapi.ccl.syntax.OrderTree;
import com.cinchapi.ccl.syntax.PageTree;
import com.cinchapi.ccl.type.Operator;
import com.cinchapi.ccl.generated.ASTAnd;
import com.cinchapi.ccl.generated.ASTExpression;
import com.cinchapi.ccl.generated.ASTOr;
import com.cinchapi.ccl.generated.ASTStart;
import com.cinchapi.ccl.generated.SimpleNode;
import com.cinchapi.concourse.util.Convert;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import org.junit.Test;

import com.cinchapi.ccl.generated.ParseException;

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
    public void testWithNumber() throws UnsupportedEncodingException, ParseException {
        String input = PAGE + " 3";

        InputStream stream = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream,
                PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testWithNumberAndSize() throws UnsupportedEncodingException, ParseException {
        String input = SIZE + " 1 " + PAGE + " 3";

        InputStream stream = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream,
                PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testWithSize() throws UnsupportedEncodingException, ParseException {
        String input = SIZE + " 3";

        InputStream stream = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream,
                PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testWithSizeAndNumber() throws UnsupportedEncodingException, ParseException {
        String input = SIZE + " 1 " + PAGE + " 3";

        InputStream stream = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream,
                PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void validUnaryOperatorWithPage() throws UnsupportedEncodingException, ParseException {
        String ccl = "a = 1 page 1 size 3";
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
        String ccl = "a = 1 " + ORDER + " a " + SIZE+ " 3 " + PAGE + " 1";
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
    public void testIndexFunctionWithTimestamp() throws UnsupportedEncodingException, ParseException {
        String ccl = "avg(age, at today)";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(
                StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    /**
     * Constants
     */
    private static final String PAGE = "page";
    private static final String SIZE = "size";
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
    };

}
