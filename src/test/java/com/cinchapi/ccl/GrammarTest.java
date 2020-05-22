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

import com.cinchapi.ccl.generated.ASTOrder;
import com.cinchapi.ccl.generated.Grammar;
import com.cinchapi.ccl.generated.GrammarVisitor;
import com.cinchapi.ccl.generated.SimpleNode;
import com.cinchapi.ccl.syntax.AbstractSyntaxTree;
import com.cinchapi.ccl.syntax.AndTree;
import com.cinchapi.ccl.syntax.ExpressionTree;
import com.cinchapi.ccl.syntax.OrTree;
import com.cinchapi.ccl.syntax.OrderTree;
import com.cinchapi.ccl.type.Operator;
import com.cinchapi.ccl.generated.ASTAnd;
import com.cinchapi.ccl.generated.ASTExpression;
import com.cinchapi.ccl.generated.ASTOr;
import com.cinchapi.ccl.generated.ASTStart;
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
    public void validLike() throws UnsupportedEncodingException, ParseException {
        String ccl = "name like (?i:%jeff%)";
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
    public void testKey() throws UnsupportedEncodingException, ParseException {
        String input = "order age";

        InputStream stream = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testKeyAscendingSymbol() throws UnsupportedEncodingException, ParseException {
        String input = "order < age";

        InputStream stream = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testKeyAscendingWord() throws UnsupportedEncodingException, ParseException {
        String input = "order age ASC";

        InputStream stream = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testKeyDescendingSymbol() throws UnsupportedEncodingException, ParseException {
        String input = "order > age";

        InputStream stream = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testKeyDescendingWord() throws UnsupportedEncodingException, ParseException {
        String input = "order age DESC";

        InputStream stream = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testKeyWithNumberTimestamp() throws UnsupportedEncodingException, ParseException {
        String input = "order age @ " + String.valueOf(122L);

        InputStream stream = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testKeyWithStringTimestamp() throws UnsupportedEncodingException, ParseException {
        String input = "order age @ \"1992-10-02\"";

        InputStream stream = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testKeyWithStringAndFormatTimestamp() throws UnsupportedEncodingException, ParseException {
        String input = "order age @ \"1992-10-02\" | \"yyyy-mm-dd\"";

        InputStream stream = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testKeyWithNumberTimestampAscending() throws UnsupportedEncodingException, ParseException {
        String input = "order  > age @ " + String.valueOf(122L);

        InputStream stream = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testKeyWithStringTimestampAscending() throws UnsupportedEncodingException, ParseException {
        String input = "order < age @ \"1992-10-02\"";

        InputStream stream = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testKeyWithStringAndFormatTimestampAscending() throws UnsupportedEncodingException, ParseException {
        String input = "order < age @ \"1992-10-02\" | \"yyyy-mm-dd\"";

        InputStream stream = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testKeyWithNumberTimestampDescending() throws UnsupportedEncodingException, ParseException {
        String input = "order > age @ " + String.valueOf(122L);

        InputStream stream = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testKeyWithStringTimestampDescending() throws UnsupportedEncodingException, ParseException {
        String input = "order > age @ \"1992-10-02\"";

        InputStream stream = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testKeyWithStringAndFormatTimestampDescending() throws UnsupportedEncodingException, ParseException {
        String input = "order > age @ \"1992-10-02\" | \"yyyy-mm-dd\"";

        InputStream stream = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testMultipleKeys() throws UnsupportedEncodingException, ParseException {
        String input = "order age salary";

        InputStream stream = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void testMultipleKeysWithDirectional() throws UnsupportedEncodingException, ParseException {
        String input = "order < age > salary";

        InputStream stream = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
    }

    @Test
    public void validUnaryOperatorWithOrder() throws UnsupportedEncodingException, ParseException {
        String ccl = "a = 1 order a";
        InputStream stream = new ByteArrayInputStream(ccl.getBytes(
                StandardCharsets.UTF_8.name()));
        Grammar grammar = new Grammar(stream, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION, visitor);
        grammar.generateAST();
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
            AbstractSyntaxTree left = (AbstractSyntaxTree) node.jjtGetChild(0).jjtAccept(this, data);
            AbstractSyntaxTree right =(AbstractSyntaxTree) node.jjtGetChild(1).jjtAccept(this, data);
            return new OrTree(left, right);
        }

        @Override
        public Object visit(ASTAnd node, Object data) {
            AbstractSyntaxTree left = (AbstractSyntaxTree) node.jjtGetChild(0).jjtAccept(this, data);
            AbstractSyntaxTree right =(AbstractSyntaxTree) node.jjtGetChild(1).jjtAccept(this, data);
            return new AndTree(left, right);
        }

        @Override
        public Object visit(ASTExpression node, Object data) {
            return new ExpressionTree(node);
        }

        @Override public Object visit(ASTOrder node, Object data) {
            return new OrderTree(node.order());
        }
    };

}
