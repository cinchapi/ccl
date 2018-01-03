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

import com.cinchapi.ccl.JavaCCParser;
import com.cinchapi.ccl.Parser;
import com.cinchapi.ccl.grammar.BaseValueSymbol;
import com.cinchapi.ccl.grammar.ConjunctionSymbol;
import com.cinchapi.ccl.grammar.ExplicitFunction;
import com.cinchapi.ccl.grammar.ExplicitRecordsFunction;
import com.cinchapi.ccl.grammar.Expression;
import com.cinchapi.ccl.grammar.FunctionKeySymbol;
import com.cinchapi.ccl.grammar.FunctionValueSymbol;
import com.cinchapi.ccl.grammar.ImplicitFunction;
import com.cinchapi.ccl.grammar.KeySymbol;
import com.cinchapi.ccl.grammar.OperatorSymbol;
import com.cinchapi.ccl.grammar.ParenthesisSymbol;
import com.cinchapi.ccl.grammar.PostfixNotationSymbol;
import com.cinchapi.ccl.grammar.Symbol;
import com.cinchapi.ccl.grammar.ValueSymbol;
import com.cinchapi.ccl.syntax.AbstractSyntaxTree;
import com.cinchapi.ccl.syntax.AndTree;
import com.cinchapi.ccl.syntax.ConjunctionTree;
import com.cinchapi.ccl.syntax.ExpressionTree;
import com.cinchapi.ccl.syntax.OrTree;
import com.cinchapi.ccl.type.Operator;
import com.cinchapi.ccl.v2.generated.Grammar;
import com.cinchapi.ccl.v2.generated.ParseException;
import com.cinchapi.concourse.util.Convert;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.function.Function;

/**
 * Tests for {@link JavaCCParser}.
 *
 * These tests include utput tests (postfix, abstract
 * syntax tree, tokens)
 */
public class JavaCCParserLogicTest {

    @Test
    public void testSingleExpressionTokenize() {
        String ccl = "a = 1";

        // Build expected queue
        List<Object> expectedTokens = Lists.newArrayList();

        expectedTokens.add(new KeySymbol("a"));
        expectedTokens.add(new OperatorSymbol(PARSER_TRANSFORM_OPERATOR_FUNCTION.apply("=")));
        expectedTokens.add(new ValueSymbol(PARSER_TRANSFORM_VALUE_FUNCTION.apply("1")));

        // Generate queue
        Parser parser = Parser.create(ccl, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION);
        List<Symbol> tokens = parser.tokenize();

        Assert.assertEquals(expectedTokens, tokens);
    }

    @Test
    public void testSingleConjunctionTokenize() {
        String ccl = "a = 1 and b = 2";

        // Build expected queue
        List<Symbol> expectedTokens = Lists.newArrayList();

        expectedTokens.add(new KeySymbol("a"));
        expectedTokens.add(new OperatorSymbol(PARSER_TRANSFORM_OPERATOR_FUNCTION.apply("=")));
        expectedTokens.add(new ValueSymbol(PARSER_TRANSFORM_VALUE_FUNCTION.apply("1")));
        expectedTokens.add(ConjunctionSymbol.AND);
        expectedTokens.add(new KeySymbol("b"));
        expectedTokens.add(new OperatorSymbol(PARSER_TRANSFORM_OPERATOR_FUNCTION.apply("=")));
        expectedTokens.add(new ValueSymbol(PARSER_TRANSFORM_VALUE_FUNCTION.apply("2")));

        // Generate queue
        Parser parser = Parser.create(ccl, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION);
        List<Symbol> tokens = parser.tokenize();

        Assert.assertEquals(expectedTokens, tokens);
    }

    @Test
    public void testSingleDisjunctionTokenize() {
        String ccl = "a = 1 or b = 2";

        // Build expected queue
        List<Symbol> expectedTokens = Lists.newArrayList();

        expectedTokens.add(new KeySymbol("a"));
        expectedTokens.add(new OperatorSymbol(PARSER_TRANSFORM_OPERATOR_FUNCTION.apply("=")));
        expectedTokens.add(new ValueSymbol(PARSER_TRANSFORM_VALUE_FUNCTION.apply("1")));
        expectedTokens.add(ConjunctionSymbol.OR);
        expectedTokens.add(new KeySymbol("b"));
        expectedTokens.add(new OperatorSymbol(PARSER_TRANSFORM_OPERATOR_FUNCTION.apply("=")));
        expectedTokens.add(new ValueSymbol(PARSER_TRANSFORM_VALUE_FUNCTION.apply("2")));

        // Generate queue
        Parser parser = Parser.create(ccl, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION);
        List<Symbol> tokens = parser.tokenize();

        Assert.assertEquals(expectedTokens, tokens);
    }

    @Test
    public void testDoubleConjunctionTokenize() {
        String ccl = "a = 1 and b = 2 and c = 3";

        // Build expected queue
        List<Symbol> expectedTokens = Lists.newArrayList();

        expectedTokens.add(new KeySymbol("a"));
        expectedTokens.add(new OperatorSymbol(PARSER_TRANSFORM_OPERATOR_FUNCTION.apply("=")));
        expectedTokens.add(new ValueSymbol(PARSER_TRANSFORM_VALUE_FUNCTION.apply("1")));
        expectedTokens.add(ConjunctionSymbol.AND);
        expectedTokens.add(new KeySymbol("b"));
        expectedTokens.add(new OperatorSymbol(PARSER_TRANSFORM_OPERATOR_FUNCTION.apply("=")));
        expectedTokens.add(new ValueSymbol(PARSER_TRANSFORM_VALUE_FUNCTION.apply("2")));
        expectedTokens.add(ConjunctionSymbol.AND);
        expectedTokens.add(new KeySymbol("c"));
        expectedTokens.add(new OperatorSymbol(PARSER_TRANSFORM_OPERATOR_FUNCTION.apply("=")));
        expectedTokens.add(new ValueSymbol(PARSER_TRANSFORM_VALUE_FUNCTION.apply("3")));

        // Generate queue
        Parser parser = Parser.create(ccl, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION);
        List<Symbol> tokens = parser.tokenize();

        Assert.assertEquals(expectedTokens, tokens);
    }

    @Test
    public void testDoubleDisjunctionTokenize() {
        String ccl = "a = 1 or b = 2 or c = 3";

        // Build expected queue
        List<Symbol> expectedTokens = Lists.newArrayList();

        expectedTokens.add(new KeySymbol("a"));
        expectedTokens.add(new OperatorSymbol(PARSER_TRANSFORM_OPERATOR_FUNCTION.apply("=")));
        expectedTokens.add(new ValueSymbol(PARSER_TRANSFORM_VALUE_FUNCTION.apply("1")));
        expectedTokens.add(ConjunctionSymbol.OR);
        expectedTokens.add(new KeySymbol("b"));
        expectedTokens.add(new OperatorSymbol(PARSER_TRANSFORM_OPERATOR_FUNCTION.apply("=")));
        expectedTokens.add(new ValueSymbol(PARSER_TRANSFORM_VALUE_FUNCTION.apply("2")));
        expectedTokens.add(ConjunctionSymbol.OR);
        expectedTokens.add(new KeySymbol("c"));
        expectedTokens.add(new OperatorSymbol(PARSER_TRANSFORM_OPERATOR_FUNCTION.apply("=")));
        expectedTokens.add(new ValueSymbol(PARSER_TRANSFORM_VALUE_FUNCTION.apply("3")));

        // Generate queue
        Parser parser = Parser.create(ccl, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION);
        List<Symbol> tokens = parser.tokenize();

        Assert.assertEquals(expectedTokens, tokens);
    }

    @Test
    public void testConjunctionDisjunctionTokenize() {
        String ccl = "a = 1 and b = 2 or c = 3";

        // Build expected queue
        List<Symbol> expectedTokens = Lists.newArrayList();

        expectedTokens.add(new KeySymbol("a"));
        expectedTokens.add(new OperatorSymbol(PARSER_TRANSFORM_OPERATOR_FUNCTION.apply("=")));
        expectedTokens.add(new ValueSymbol(PARSER_TRANSFORM_VALUE_FUNCTION.apply("1")));
        expectedTokens.add(ConjunctionSymbol.AND);
        expectedTokens.add(new KeySymbol("b"));
        expectedTokens.add(new OperatorSymbol(PARSER_TRANSFORM_OPERATOR_FUNCTION.apply("=")));
        expectedTokens.add(new ValueSymbol(PARSER_TRANSFORM_VALUE_FUNCTION.apply("2")));
        expectedTokens.add(ConjunctionSymbol.OR);
        expectedTokens.add(new KeySymbol("c"));
        expectedTokens.add(new OperatorSymbol(PARSER_TRANSFORM_OPERATOR_FUNCTION.apply("=")));
        expectedTokens.add(new ValueSymbol(PARSER_TRANSFORM_VALUE_FUNCTION.apply("3")));

        // Generate queue
        Parser parser = Parser.create(ccl, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION);
        List<Symbol> tokens = parser.tokenize();

        Assert.assertEquals(expectedTokens, tokens);
    }

    @Test
    public void testDisjunctionConjunctionTokenize() {
        String ccl = "a = 1 or b = 2 and c = 3";

        // Build expected queue
        List<Symbol> expectedTokens = Lists.newArrayList();

        expectedTokens.add(new KeySymbol("a"));
        expectedTokens.add(new OperatorSymbol(PARSER_TRANSFORM_OPERATOR_FUNCTION.apply("=")));
        expectedTokens.add(new ValueSymbol(PARSER_TRANSFORM_VALUE_FUNCTION.apply("1")));
        expectedTokens.add(ConjunctionSymbol.OR);
        expectedTokens.add(new KeySymbol("b"));
        expectedTokens.add(new OperatorSymbol(PARSER_TRANSFORM_OPERATOR_FUNCTION.apply("=")));
        expectedTokens.add(new ValueSymbol(PARSER_TRANSFORM_VALUE_FUNCTION.apply("2")));
        expectedTokens.add(ConjunctionSymbol.AND);
        expectedTokens.add(new KeySymbol("c"));
        expectedTokens.add(new OperatorSymbol(PARSER_TRANSFORM_OPERATOR_FUNCTION.apply("=")));
        expectedTokens.add(new ValueSymbol(PARSER_TRANSFORM_VALUE_FUNCTION.apply("3")));

        // Generate queue
        Parser parser = Parser.create(ccl, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION);
        List<Symbol> tokens = parser.tokenize();

        Assert.assertEquals(expectedTokens, tokens);
    }

    @Test
    public void testDisjunctionParenthesizedConjunctionTokenize() {
        String ccl = "a = 1 or (b = 2 and c = 3)";

        // Build expected queue
        List<Symbol> expectedTokens = Lists.newArrayList();

        expectedTokens.add(new KeySymbol("a"));
        expectedTokens.add(new OperatorSymbol(PARSER_TRANSFORM_OPERATOR_FUNCTION.apply("=")));
        expectedTokens.add(new ValueSymbol(PARSER_TRANSFORM_VALUE_FUNCTION.apply("1")));
        expectedTokens.add(ConjunctionSymbol.OR);
        expectedTokens.add(ParenthesisSymbol.LEFT);
        expectedTokens.add(new KeySymbol("b"));
        expectedTokens.add(new OperatorSymbol(PARSER_TRANSFORM_OPERATOR_FUNCTION.apply("=")));
        expectedTokens.add(new ValueSymbol(PARSER_TRANSFORM_VALUE_FUNCTION.apply("2")));
        expectedTokens.add(ConjunctionSymbol.AND);
        expectedTokens.add(new KeySymbol("c"));
        expectedTokens.add(new OperatorSymbol(PARSER_TRANSFORM_OPERATOR_FUNCTION.apply("=")));
        expectedTokens.add(new ValueSymbol(PARSER_TRANSFORM_VALUE_FUNCTION.apply("3")));
        expectedTokens.add(ParenthesisSymbol.RIGHT);

        // Generate queue
        Parser parser = Parser.create(ccl, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION);
        List<Symbol> tokens = parser.tokenize();

        Assert.assertEquals(expectedTokens, tokens);
    }

    @Test
    public void testSingleConjunctionPostFix() {
        String ccl = "a = 1 and b = 2";

        // Build expected queue
        Queue<PostfixNotationSymbol> expectedOrder = new LinkedList<>();

        KeySymbol key = new KeySymbol("a");
        OperatorSymbol operator = new OperatorSymbol(
                PARSER_TRANSFORM_OPERATOR_FUNCTION.apply("="));
        ValueSymbol value = new ValueSymbol(
                PARSER_TRANSFORM_VALUE_FUNCTION.apply("1"));
        Expression expression = new Expression(key, operator, value);
        expectedOrder.add(expression);

        key = new KeySymbol("b");
        operator = new OperatorSymbol(
                PARSER_TRANSFORM_OPERATOR_FUNCTION.apply("="));
        value = new ValueSymbol(PARSER_TRANSFORM_VALUE_FUNCTION.apply("2"));
        expression = new Expression(key, operator, value);
        expectedOrder.add(expression);

        expectedOrder.add(ConjunctionSymbol.AND);

        // Generate queue
        Parser parser = Parser.create(ccl, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION);
        Queue<PostfixNotationSymbol> order = parser.order();

        Assert.assertEquals(expectedOrder, order);
    }

    @Test
    public void testSingleDisjunctionPostFix() {
        String ccl = "a = 1 or b = 2";
        // Build expected queue
        Queue<PostfixNotationSymbol> expectedOrder = new LinkedList<>();

        KeySymbol key = new KeySymbol("a");
        OperatorSymbol operator = new OperatorSymbol(
                PARSER_TRANSFORM_OPERATOR_FUNCTION.apply("="));
        ValueSymbol value = new ValueSymbol(
                PARSER_TRANSFORM_VALUE_FUNCTION.apply("1"));
        Expression expression = new Expression(key, operator, value);
        expectedOrder.add(expression);

        key = new KeySymbol("b");
        operator = new OperatorSymbol(
                PARSER_TRANSFORM_OPERATOR_FUNCTION.apply("="));
        value = new ValueSymbol(PARSER_TRANSFORM_VALUE_FUNCTION.apply("2"));
        expression = new Expression(key, operator, value);
        expectedOrder.add(expression);

        expectedOrder.add(ConjunctionSymbol.OR);

        // Generate queue
        Parser parser = Parser.create(ccl, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION);
        Queue<PostfixNotationSymbol> order = parser.order();

        Assert.assertEquals(expectedOrder, order);
    }

    @Test
    public void testDoubleConjunctionPostFix() {
        String ccl = "a = 1 and b = 2 and c = 3";

        // Build expected queue
        Queue<PostfixNotationSymbol> expectedOrder = new LinkedList<>();

        KeySymbol key = new KeySymbol("a");
        OperatorSymbol operator = new OperatorSymbol(
                PARSER_TRANSFORM_OPERATOR_FUNCTION.apply("="));
        ValueSymbol value = new ValueSymbol(
                PARSER_TRANSFORM_VALUE_FUNCTION.apply("1"));
        Expression expression = new Expression(key, operator, value);
        expectedOrder.add(expression);

        key = new KeySymbol("b");
        operator = new OperatorSymbol(
                PARSER_TRANSFORM_OPERATOR_FUNCTION.apply("="));
        value = new ValueSymbol(PARSER_TRANSFORM_VALUE_FUNCTION.apply("2"));
        expression = new Expression(key, operator, value);
        expectedOrder.add(expression);

        key = new KeySymbol("c");
        operator = new OperatorSymbol(
                PARSER_TRANSFORM_OPERATOR_FUNCTION.apply("="));
        value = new ValueSymbol(PARSER_TRANSFORM_VALUE_FUNCTION.apply("3"));
        expression = new Expression(key, operator, value);
        expectedOrder.add(expression);

        expectedOrder.add(ConjunctionSymbol.AND);
        expectedOrder.add(ConjunctionSymbol.AND);

        // Generate queue
        Parser parser = Parser.create(ccl, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION);
        Queue<PostfixNotationSymbol> order = parser.order();

        Assert.assertEquals(expectedOrder, order);
    }

    @Test
    public void testDoubleDisjunctionPostFix() {
        String ccl = "a = 1 or b = 2 or c = 3";

        // Build expected queue
        Queue<PostfixNotationSymbol> expectedOrder = new LinkedList<>();

        KeySymbol key = new KeySymbol("a");
        OperatorSymbol operator = new OperatorSymbol(
                PARSER_TRANSFORM_OPERATOR_FUNCTION.apply("="));
        ValueSymbol value = new ValueSymbol(
                PARSER_TRANSFORM_VALUE_FUNCTION.apply("1"));
        Expression expression = new Expression(key, operator, value);
        expectedOrder.add(expression);

        key = new KeySymbol("b");
        operator = new OperatorSymbol(
                PARSER_TRANSFORM_OPERATOR_FUNCTION.apply("="));
        value = new ValueSymbol(PARSER_TRANSFORM_VALUE_FUNCTION.apply("2"));
        expression = new Expression(key, operator, value);
        expectedOrder.add(expression);

        expectedOrder.add(ConjunctionSymbol.OR);

        key = new KeySymbol("c");
        operator = new OperatorSymbol(
                PARSER_TRANSFORM_OPERATOR_FUNCTION.apply("="));
        value = new ValueSymbol(PARSER_TRANSFORM_VALUE_FUNCTION.apply("3"));
        expression = new Expression(key, operator, value);
        expectedOrder.add(expression);

        expectedOrder.add(ConjunctionSymbol.OR);

        // Generate queue
        Parser parser = Parser.create(ccl, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION);
        Queue<PostfixNotationSymbol> order = parser.order();

        Assert.assertEquals(expectedOrder, order);
    }

    @Test
    public void testConjunctionDisjunctionPostFix() {
        String ccl = "a = 1 and b = 2 or c = 3";

        // Build expected queue
        Queue<PostfixNotationSymbol> expectedOrder = new LinkedList<>();

        KeySymbol key = new KeySymbol("a");
        OperatorSymbol operator = new OperatorSymbol(
                PARSER_TRANSFORM_OPERATOR_FUNCTION.apply("="));
        ValueSymbol value = new ValueSymbol(
                PARSER_TRANSFORM_VALUE_FUNCTION.apply("1"));
        Expression expression = new Expression(key, operator, value);
        expectedOrder.add(expression);

        key = new KeySymbol("b");
        operator = new OperatorSymbol(
                PARSER_TRANSFORM_OPERATOR_FUNCTION.apply("="));
        value = new ValueSymbol(PARSER_TRANSFORM_VALUE_FUNCTION.apply("2"));
        expression = new Expression(key, operator, value);
        expectedOrder.add(expression);

        expectedOrder.add(ConjunctionSymbol.AND);

        key = new KeySymbol("c");
        operator = new OperatorSymbol(
                PARSER_TRANSFORM_OPERATOR_FUNCTION.apply("="));
        value = new ValueSymbol(PARSER_TRANSFORM_VALUE_FUNCTION.apply("3"));
        expression = new Expression(key, operator, value);
        expectedOrder.add(expression);

        expectedOrder.add(ConjunctionSymbol.OR);

        // Generate queue
        Parser parser = Parser.create(ccl, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION);
        Queue<PostfixNotationSymbol> order = parser.order();

        Assert.assertEquals(expectedOrder, order);
    }

    @Test
    public void testDisjunctionConjunctionPostFix() {
        String ccl = "a = 1 or b = 2 and c = 3";

        // Build expected queue
        Queue<PostfixNotationSymbol> expectedOrder = new LinkedList<>();

        KeySymbol key = new KeySymbol("a");
        OperatorSymbol operator = new OperatorSymbol(
                PARSER_TRANSFORM_OPERATOR_FUNCTION.apply("="));
        ValueSymbol value = new ValueSymbol(
                PARSER_TRANSFORM_VALUE_FUNCTION.apply("1"));
        Expression expression = new Expression(key, operator, value);
        expectedOrder.add(expression);

        key = new KeySymbol("b");
        operator = new OperatorSymbol(
                PARSER_TRANSFORM_OPERATOR_FUNCTION.apply("="));
        value = new ValueSymbol(PARSER_TRANSFORM_VALUE_FUNCTION.apply("2"));
        expression = new Expression(key, operator, value);
        expectedOrder.add(expression);

        key = new KeySymbol("c");
        operator = new OperatorSymbol(
                PARSER_TRANSFORM_OPERATOR_FUNCTION.apply("="));
        value = new ValueSymbol(PARSER_TRANSFORM_VALUE_FUNCTION.apply("3"));
        expression = new Expression(key, operator, value);
        expectedOrder.add(expression);

        expectedOrder.add(ConjunctionSymbol.AND);
        expectedOrder.add(ConjunctionSymbol.OR);

        // Generate queue
        Parser parser = Parser.create(ccl, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION);
        Queue<PostfixNotationSymbol> order = parser.order();

        Assert.assertEquals(expectedOrder, order);
    }

    @Test
    public void testDisjunctionParenthesizedConjunctionPostFix() {
        String ccl = "a = 1 or (b = 2 and c = 3)";

        // Build expected queue
        Queue<PostfixNotationSymbol> expectedOrder = new LinkedList<>();

        KeySymbol key = new KeySymbol("a");
        OperatorSymbol operator = new OperatorSymbol(
                PARSER_TRANSFORM_OPERATOR_FUNCTION.apply("="));
        ValueSymbol value = new ValueSymbol(
                PARSER_TRANSFORM_VALUE_FUNCTION.apply("1"));
        Expression expression = new Expression(key, operator, value);
        expectedOrder.add(expression);

        key = new KeySymbol("b");
        operator = new OperatorSymbol(
                PARSER_TRANSFORM_OPERATOR_FUNCTION.apply("="));
        value = new ValueSymbol(PARSER_TRANSFORM_VALUE_FUNCTION.apply("2"));
        expression = new Expression(key, operator, value);
        expectedOrder.add(expression);

        key = new KeySymbol("c");
        operator = new OperatorSymbol(
                PARSER_TRANSFORM_OPERATOR_FUNCTION.apply("="));
        value = new ValueSymbol(PARSER_TRANSFORM_VALUE_FUNCTION.apply("3"));
        expression = new Expression(key, operator, value);
        expectedOrder.add(expression);

        expectedOrder.add(ConjunctionSymbol.AND);
        expectedOrder.add(ConjunctionSymbol.OR);

        // Generate queue
        Parser parser = Parser.create(ccl, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION);
        Queue<PostfixNotationSymbol> order = parser.order();

        Assert.assertEquals(expectedOrder, order);
    }

    @Test
    public void testSingleExpressionAbstractSyntaxTree() {
        String ccl = "a = 1";

        // Generate tree
        Parser parser = Parser.create(ccl, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION);
        AbstractSyntaxTree tree = parser.parse();

        // Root node
        Assert.assertTrue(tree instanceof ExpressionTree);
        Expression expression = (Expression) tree.root();
        Assert.assertEquals("a", expression.key().toString());
        Assert.assertEquals("=", expression.operator().toString());
        Assert.assertEquals("1", expression.values().get(0).toString());
    }

    @Test
    public void testSingleConjunctionAbstractSyntaxTree() {
        String ccl = "a = 1 and b = 2";

        Parser parser = Parser.create(ccl, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION);

        AbstractSyntaxTree tree = parser.parse();

        // Root node
        Assert.assertTrue(tree instanceof AndTree);
        ConjunctionTree rootNode = (ConjunctionTree) tree;

        // Left node
        Assert.assertTrue(rootNode.left() instanceof ExpressionTree);
        Expression leftExpression = (Expression) (rootNode.left()).root();
        Assert.assertEquals("a", leftExpression.key().toString());
        Assert.assertEquals("=", leftExpression.operator().toString());
        Assert.assertEquals("1", leftExpression.values().get(0).toString());

        // Right node
        Assert.assertTrue(rootNode.right() instanceof ExpressionTree);
        Expression rightExpression = (Expression) (rootNode.right()).root();
        Assert.assertEquals("b", rightExpression.key().toString());
        Assert.assertEquals("=", rightExpression.operator().toString());
        Assert.assertEquals("2", rightExpression.values().get(0).toString());
    }

    @Test
    public void testSingleDisjunctionAbstractSyntaxTree() {
        String ccl = "a = 1 or b = 2";

        Parser parser = Parser.create(ccl, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION);

        AbstractSyntaxTree tree = parser.parse();

        // Root node
        Assert.assertTrue(tree instanceof OrTree);
        ConjunctionTree rootNode = (ConjunctionTree) tree;

        // Left node
        Assert.assertTrue(rootNode.left() instanceof ExpressionTree);
        Expression leftExpression = (Expression) (rootNode.left()).root();
        Assert.assertEquals("a", leftExpression.key().toString());
        Assert.assertEquals("=", leftExpression.operator().toString());
        Assert.assertEquals("1", leftExpression.values().get(0).toString());

        // Right node
        Assert.assertTrue(rootNode.left() instanceof ExpressionTree);
        Expression rightExpression = (Expression) (rootNode.right()).root();
        Assert.assertEquals("b", rightExpression.key().toString());
        Assert.assertEquals("=", rightExpression.operator().toString());
        Assert.assertEquals("2", rightExpression.values().get(0).toString());
    }

    @Test
    public void testDoubleConjunctionAbstractSyntaxTree() {
        String ccl = "a = 1 and b = 2 and c = 3";

        Parser parser = Parser.create(ccl, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION);

        AbstractSyntaxTree tree = parser.parse();

        // Root node
        Assert.assertTrue(tree instanceof AndTree);
        ConjunctionTree rootNode = (ConjunctionTree) tree;

        // left node
        Assert.assertTrue(rootNode.left() instanceof AndTree);
        ConjunctionTree leftNode = (ConjunctionTree) rootNode.left();

        // right node
        Assert.assertTrue(rootNode.right() instanceof ExpressionTree);
        Expression rightExpression = (Expression) (rootNode.right()).root();
        Assert.assertEquals("c", rightExpression.key().toString());
        Assert.assertEquals("=", rightExpression.operator().toString());
        Assert.assertEquals("3", rightExpression.values().get(0).toString());

        // Left left node
        Assert.assertTrue(leftNode.left() instanceof ExpressionTree);
        Expression leftLeftExpression = (Expression) (leftNode.left()).root();
        Assert.assertEquals("a", leftLeftExpression.key().toString());
        Assert.assertEquals("=", leftLeftExpression.operator().toString());
        Assert.assertEquals("1", leftLeftExpression.values().get(0).toString());

        // Left right node
        Assert.assertTrue(leftNode.right() instanceof ExpressionTree);
        Expression rightRightExpression = (Expression) (leftNode.right())
                .root();
        Assert.assertEquals("b", rightRightExpression.key().toString());
        Assert.assertEquals("=", rightRightExpression.operator().toString());
        Assert.assertEquals("2",
                rightRightExpression.values().get(0).toString());
    }

    @Test
    public void testDoubleDisjunctionAbstractSyntaxTree() {
        String ccl = "a = 1 or b = 2 or c = 3";

        Parser parser = Parser.create(ccl, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION);

        AbstractSyntaxTree tree = parser.parse();

        // Root node
        Assert.assertTrue(tree instanceof OrTree);
        ConjunctionTree rootNode = (ConjunctionTree) tree;

        // left node
        Assert.assertTrue(rootNode.left() instanceof OrTree);
        ConjunctionTree leftNode = (ConjunctionTree) rootNode.left();

        // right node
        Assert.assertTrue(rootNode.right() instanceof ExpressionTree);
        Expression rightExpression = (Expression) (rootNode.right()).root();
        Assert.assertEquals("c", rightExpression.key().toString());
        Assert.assertEquals("=", rightExpression.operator().toString());
        Assert.assertEquals("3", rightExpression.values().get(0).toString());

        // Left left node
        Assert.assertTrue(leftNode.left() instanceof ExpressionTree);
        Expression leftLeftExpression = (Expression) (leftNode.left()).root();
        Assert.assertEquals("a", leftLeftExpression.key().toString());
        Assert.assertEquals("=", leftLeftExpression.operator().toString());
        Assert.assertEquals("1", leftLeftExpression.values().get(0).toString());

        // Left right node
        Assert.assertTrue(leftNode.right() instanceof ExpressionTree);
        Expression leftRightExpression = (Expression) (leftNode.right()).root();
        Assert.assertEquals("b", leftRightExpression.key().toString());
        Assert.assertEquals("=", leftRightExpression.operator().toString());
        Assert.assertEquals("2",
                leftRightExpression.values().get(0).toString());
    }

    @Test
    public void testConjunctionDisjunctionAbstractSyntaxTree() {
        String ccl = "a = 1 and b = 2 or c = 3";

        Parser parser = Parser.create(ccl, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION);

        AbstractSyntaxTree tree = parser.parse();

        // Root node
        Assert.assertTrue(tree instanceof OrTree);
        ConjunctionTree rootNode = (ConjunctionTree) tree;

        // left node
        Assert.assertTrue(rootNode.left() instanceof AndTree);
        ConjunctionTree leftNode = (ConjunctionTree) rootNode.left();

        // right node
        Assert.assertTrue(rootNode.right() instanceof ExpressionTree);
        Expression rightExpression = (Expression) (rootNode.right()).root();
        Assert.assertEquals("c", rightExpression.key().toString());
        Assert.assertEquals("=", rightExpression.operator().toString());
        Assert.assertEquals("3", rightExpression.values().get(0).toString());

        // Left left node
        Assert.assertTrue(leftNode.left() instanceof ExpressionTree);
        Expression leftLeftExpression = (Expression) (leftNode.left()).root();
        Assert.assertEquals("a", leftLeftExpression.key().toString());
        Assert.assertEquals("=", leftLeftExpression.operator().toString());
        Assert.assertEquals("1", leftLeftExpression.values().get(0).toString());

        // Left right node
        Assert.assertTrue(leftNode.right() instanceof ExpressionTree);
        Expression leftRightExpression = (Expression) (leftNode.right()).root();
        Assert.assertEquals("b", leftRightExpression.key().toString());
        Assert.assertEquals("=", leftRightExpression.operator().toString());
        Assert.assertEquals("2",
                leftRightExpression.values().get(0).toString());
    }

    @Test
    public void testDisjunctionConjunctionAbstractSyntaxTree() {
        String ccl = "a = 1 or b = 2 and c = 3";

        Parser parser = Parser.create(ccl, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION);

        AbstractSyntaxTree tree = parser.parse();

        // Root node
        Assert.assertTrue(tree instanceof OrTree);
        ConjunctionTree rootNode = (ConjunctionTree) tree;

        // Right node
        Assert.assertTrue(rootNode.right() instanceof AndTree);
        ConjunctionTree rightNode = (ConjunctionTree) rootNode.right();

        // right node
        Assert.assertTrue(rootNode.left() instanceof ExpressionTree);
        Expression leftExpression = (Expression) (rootNode.left()).root();
        Assert.assertEquals("a", leftExpression.key().toString());
        Assert.assertEquals("=", leftExpression.operator().toString());
        Assert.assertEquals("1", leftExpression.values().get(0).toString());

        // Right left node
        Assert.assertTrue(rightNode.left() instanceof ExpressionTree);
        Expression rightLeftExpression = (Expression) (rightNode.left()).root();
        Assert.assertEquals("b", rightLeftExpression.key().toString());
        Assert.assertEquals("=", rightLeftExpression.operator().toString());
        Assert.assertEquals("2", rightLeftExpression.values().get(0).toString());

        // Right right node
        Assert.assertTrue(rightNode.right() instanceof ExpressionTree);
        Expression rightRightExpression = (Expression) (rightNode.right()).root();
        Assert.assertEquals("c", rightRightExpression.key().toString());
        Assert.assertEquals("=", rightRightExpression.operator().toString());
        Assert.assertEquals("3", rightRightExpression.values().get(0).toString());
    }

    @Test
    public void testDisjunctionParenthesizedConjunctionAbstractSyntaxTree() {
        String ccl = "a = 1 or (b = 2 and c = 3)";

        Parser parser = Parser.create(ccl, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION);

        AbstractSyntaxTree tree = parser.parse();

        // Root node
        Assert.assertTrue(tree instanceof OrTree);
        ConjunctionTree rootNode = (ConjunctionTree) tree;

        // Left node
        Assert.assertTrue(rootNode.left() instanceof ExpressionTree);
        Expression leftExpression = (Expression) (rootNode.left()).root();
        Assert.assertEquals("a", leftExpression.key().toString());
        Assert.assertEquals("=", leftExpression.operator().toString());
        Assert.assertEquals("1", leftExpression.values().get(0).toString());

        // Right node
        Assert.assertTrue(rootNode.right() instanceof AndTree);
        ConjunctionTree rightNode = (ConjunctionTree) rootNode.right();

        // Right left node
        Assert.assertTrue(rightNode.left() instanceof ExpressionTree);
        Expression rightLeftExpression = (Expression) (rightNode.left()).root();
        Assert.assertEquals("b", rightLeftExpression.key().toString());
        Assert.assertEquals("=", rightLeftExpression.operator().toString());
        Assert.assertEquals("2",
                rightLeftExpression.values().get(0).toString());

        // Right right node
        Assert.assertTrue(rightNode.right() instanceof ExpressionTree);
        Expression leftRightExpression = (Expression) (rightNode.right())
                .root();
        Assert.assertEquals("c", leftRightExpression.key().toString());
        Assert.assertEquals("=", leftRightExpression.operator().toString());
        Assert.assertEquals("3",
                leftRightExpression.values().get(0).toString());
    }

    @Test
    public void testParseCclLocalReferences() {
        String ccl = "name = $name";
        Multimap<String, Object> data = LinkedHashMultimap.create();
        data.put("name", "Lebron James");
        data.put("age", 30);
        data.put("team", "Cleveland Cavaliers");

        // Generate tree
        Parser parser = Parser.create(ccl, data,
                PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION);
        AbstractSyntaxTree tree = parser.parse();

        // Root node
        Assert.assertTrue(tree instanceof ExpressionTree);
        Expression expression = (Expression) tree.root();
        Assert.assertEquals("name", expression.key().toString());
        Assert.assertEquals("=", expression.operator().toString());
        Assert.assertEquals("Lebron James",
                expression.values().get(0).toString());
    }

    @Test
    public void testEscapedCclLocalReferences() {
        String ccl = "name = \\$name";
        Multimap<String, Object> data = LinkedHashMultimap.create();
        data.put("name", "Lebron James");
        data.put("age", 30);
        data.put("team", "Cleveland Cavaliers");

        // Generate tree
        Parser parser = Parser.create(ccl, data,
                PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION);
        AbstractSyntaxTree tree = parser.parse();

        // Root node
        Assert.assertTrue(tree instanceof ExpressionTree);
        Expression expression = (Expression) tree.root();
        Assert.assertEquals("name", expression.key().toString());
        Assert.assertEquals("=", expression.operator().toString());
        Assert.assertEquals("$name",
                expression.values().get(0).toString());
    }
    @Test
    @Ignore
    public void testQuotedValue() {
        String ccl = "name = \"Javier Lores\"";

        // Generate tree
        Parser parser = Parser.create(ccl,
                PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION);
        AbstractSyntaxTree tree = parser.parse();

        // Root node
        Assert.assertTrue(tree instanceof ExpressionTree);
        Expression expression = (Expression) tree.root();
        Assert.assertEquals("name", expression.key().toString());
        Assert.assertEquals("=", expression.operator().toString());
        Assert.assertEquals("\"Javier Lores\"",
                expression.values().get(0).toString());
    }

    @Test
    public void testExplicitFunctionAsEvaluationKey() {
        String ccl = "count(friends, ?) > 3";

        // Generate tree
        Parser parser = Parser.create(ccl,
                PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION);
        AbstractSyntaxTree tree = parser.parse();

        // Root node
        Assert.assertTrue(tree instanceof ExpressionTree);
        Expression expression = (Expression) tree.root();
        Assert.assertTrue(expression.key() instanceof FunctionKeySymbol);
        Assert.assertEquals("count", ((ImplicitFunction) expression.key().key()).function());
        Assert.assertEquals("friends", ((ImplicitFunction) expression.key().key()).key());
        Assert.assertEquals(">", expression.operator().toString());
        Assert.assertEquals("3", expression.values().get(0).toString());
    }

    @Test
    public void testImplicitFunctionAsEvaluationValue() {
        String ccl = "age > avg(age, ?)";

        // Generate tree
        Parser parser = Parser.create(ccl,
                PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION);
        AbstractSyntaxTree tree = parser.parse();

        // Root node
        Assert.assertTrue(tree instanceof ExpressionTree);
        Expression expression = (Expression) tree.root();
        Assert.assertEquals("age", expression.key().toString());
        Assert.assertEquals(">", expression.operator().toString());
        Assert.assertTrue(expression.values().get(0) instanceof FunctionValueSymbol);
        Assert.assertEquals("avg", ((ImplicitFunction) expression.values().get(0).value()).function());
        Assert.assertEquals("age", ((ImplicitFunction) expression.values().get(0).value()).key());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testExplicitFunctionWithSingleRecordAsEvaluationValue() {
        String ccl = "age > avg(age, 1)";

        // Generate tree
        Parser parser = Parser.create(ccl,
                PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION);
        AbstractSyntaxTree tree = parser.parse();

        // Root node
        Assert.assertTrue(tree instanceof ExpressionTree);
        Expression expression = (Expression) tree.root();
        Assert.assertEquals("age", expression.key().toString());
        Assert.assertEquals(">", expression.operator().toString());
        Assert.assertTrue(expression.values().get(0) instanceof FunctionValueSymbol);
        Assert.assertEquals("avg", ((ExplicitFunction) expression.values().get(0).value()).function());
        Assert.assertEquals("age", ((ExplicitFunction) expression.values().get(0).value()).key());
        Assert.assertEquals(1, ((List<String>) ((ExplicitFunction) expression.values().get(0).value()).value()).size());
        Assert.assertEquals("1", ((List<String>) ((ExplicitFunction) expression.values().get(0).value()).value()).get(0));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testExplicitFunctionWithMultpleRecordsAsEvaluationValue() {
        String ccl = "age > avg(age, 1, 2)";

        // Generate tree
        Parser parser = Parser.create(ccl,
                PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION);
        AbstractSyntaxTree tree = parser.parse();

        // Root node
        Assert.assertTrue(tree instanceof ExpressionTree);
        Expression expression = (Expression) tree.root();
        Assert.assertEquals("age", expression.key().toString());
        Assert.assertEquals(">", expression.operator().toString());
        Assert.assertTrue(expression.values().get(0) instanceof FunctionValueSymbol);
        Assert.assertEquals("avg", ((ExplicitFunction) expression.values().get(0).value()).function());
        Assert.assertEquals("age", ((ExplicitFunction) expression.values().get(0).value()).key());
        Assert.assertEquals(2, ((List<String>) ((ExplicitFunction) expression.values().get(0).value()).value()).size());
        Assert.assertEquals("1", ((List<String>) ((ExplicitFunction) expression.values().get(0).value()).value()).get(0));
        Assert.assertEquals("2", ((List<String>) ((ExplicitFunction) expression.values().get(0).value()).value()).get(1));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testExplicitFunctionWithCCLAsEvaluationValue() {
        String ccl = "age > avg(age, age < 30)";

        // Generate tree
        Parser parser = Parser.create(ccl,
                PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION);
        AbstractSyntaxTree tree = parser.parse();

        // Root node
        Assert.assertTrue(tree instanceof ExpressionTree);
        Expression expression = (Expression) tree.root();
        Assert.assertEquals("age", expression.key().toString());
        Assert.assertEquals(">", expression.operator().toString());
        Assert.assertTrue(expression.values().get(0) instanceof FunctionValueSymbol);
        Assert.assertEquals("avg", ((ExplicitFunction) expression.values().get(0).value()).function());
        Assert.assertEquals("age", ((ExplicitFunction) expression.values().get(0).value()).key());
        Assert.assertEquals(3, ((List<Symbol>) ((ExplicitFunction) expression.values().get(0).value()).value()).size());
        Assert.assertTrue(((List<Symbol>) ((ExplicitFunction) expression.values().get(0).value()).value()).get(0) instanceof KeySymbol);
        Assert.assertEquals("age", ((List<Symbol>) ((ExplicitFunction) expression.values().get(0).value()).value()).get(0).toString());
        Assert.assertTrue(((List<Symbol>) ((ExplicitFunction) expression.values().get(0).value()).value()).get(1) instanceof OperatorSymbol);
        Assert.assertEquals("<", ((List<Symbol>) ((ExplicitFunction) expression.values().get(0).value()).value()).get(1).toString());
        Assert.assertTrue(((List<Symbol>) ((ExplicitFunction) expression.values().get(0).value()).value()).get(2) instanceof ValueSymbol);
        Assert.assertEquals("30", ((List<Symbol>) ((ExplicitFunction) expression.values().get(0).value()).value()).get(2).toString());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void validExplicitFunctionAsEvalutionKeyAndExplicitFunctionWithCCLAsEvaluationValue() {
        String ccl = "count(friends, ?) > avg(age, age < 30)";

        // Generate tree
        Parser parser = Parser.create(ccl,
                PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION);
        AbstractSyntaxTree tree = parser.parse();

        // Root node
        Assert.assertTrue(tree instanceof ExpressionTree);
        Expression expression = (Expression) tree.root();
        Assert.assertTrue(expression.key() instanceof FunctionKeySymbol);
        Assert.assertEquals("count", ((ImplicitFunction) expression.key().key()).function());
        Assert.assertEquals("friends", ((ImplicitFunction) expression.key().key()).key());
        Assert.assertEquals(">", expression.operator().toString());
        Assert.assertTrue(expression.values().get(0) instanceof FunctionValueSymbol);
        Assert.assertEquals("avg", ((ExplicitFunction) expression.values().get(0).value()).function());
        Assert.assertEquals("age", ((ExplicitFunction) expression.values().get(0).value()).key());
        Assert.assertEquals(3, ((List<Symbol>) ((ExplicitFunction) expression.values().get(0).value()).value()).size());
        Assert.assertTrue(((List<Symbol>) ((ExplicitFunction) expression.values().get(0).value()).value()).get(0) instanceof KeySymbol);
        Assert.assertEquals("age", ((List<Symbol>) ((ExplicitFunction) expression.values().get(0).value()).value()).get(0).toString());
        Assert.assertTrue(((List<Symbol>) ((ExplicitFunction) expression.values().get(0).value()).value()).get(1) instanceof OperatorSymbol);
        Assert.assertEquals("<", ((List<Symbol>) ((ExplicitFunction) expression.values().get(0).value()).value()).get(1).toString());
        Assert.assertTrue(((List<Symbol>) ((ExplicitFunction) expression.values().get(0).value()).value()).get(2) instanceof ValueSymbol);
        Assert.assertEquals("30", ((List<Symbol>) ((ExplicitFunction) expression.values().get(0).value()).value()).get(2).toString());
    }

    /**
     * The canonical function to transform strings to java values in a
     * {@link Parser}.
     */
    public final Function<String, Object> PARSER_TRANSFORM_VALUE_FUNCTION = value -> Convert.stringToJava(value);

    /**
     * The canonical function to transform strings to operators in a
     * {@link Parser}.
     */
    public final Function<String, Operator> PARSER_TRANSFORM_OPERATOR_FUNCTION = operator -> Convert.stringToOperator(operator);

    /**
     *
     */
    @SuppressWarnings("unused")
    private void printPreOrder(AbstractSyntaxTree tree) {
        System.out.println(tree.root());
        for (AbstractSyntaxTree child : tree.children()) {
            printPreOrder(child);
        }
    }
}
