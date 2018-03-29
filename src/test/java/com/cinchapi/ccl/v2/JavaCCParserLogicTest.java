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
import com.cinchapi.ccl.grammar.ConjunctionSymbol;
import com.cinchapi.ccl.grammar.Expression;
import com.cinchapi.ccl.grammar.KeySymbol;
import com.cinchapi.ccl.grammar.OperatorSymbol;
import com.cinchapi.ccl.grammar.ParenthesisSymbol;
import com.cinchapi.ccl.grammar.PostfixNotationSymbol;
import com.cinchapi.ccl.grammar.Symbol;
import com.cinchapi.ccl.grammar.ValueSymbol;
import com.cinchapi.ccl.syntax.AbstractSyntaxTree;
import com.cinchapi.ccl.syntax.BaseAndTree;
import com.cinchapi.ccl.syntax.BaseConjunctionTree;
import com.cinchapi.ccl.syntax.BaseExpressionTree;
import com.cinchapi.ccl.v1.ExpressionTree;
import com.cinchapi.ccl.syntax.BaseOrTree;
import com.cinchapi.ccl.type.Operator;
import com.cinchapi.concourse.util.Convert;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

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
    public void testSingleBinaryExpressionTokenize() {
        String ccl = "a >< 1 3";

        // Build expected queue
        List<Object> expectedTokens = Lists.newArrayList();

        expectedTokens.add(new KeySymbol("a"));
        expectedTokens.add(new OperatorSymbol(PARSER_TRANSFORM_OPERATOR_FUNCTION.apply("><")));
        expectedTokens.add(new ValueSymbol(PARSER_TRANSFORM_VALUE_FUNCTION.apply("1")));
        expectedTokens.add(new ValueSymbol(PARSER_TRANSFORM_VALUE_FUNCTION.apply("3")));

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
        String ccl = "a = 1 and (b = 2 or c = 3)";

        // Build expected queue
        List<Symbol> expectedTokens = Lists.newArrayList();

        expectedTokens.add(new KeySymbol("a"));
        expectedTokens.add(new OperatorSymbol(PARSER_TRANSFORM_OPERATOR_FUNCTION.apply("=")));
        expectedTokens.add(new ValueSymbol(PARSER_TRANSFORM_VALUE_FUNCTION.apply("1")));
        expectedTokens.add(ConjunctionSymbol.AND);
        expectedTokens.add(ParenthesisSymbol.LEFT);
        expectedTokens.add(new KeySymbol("b"));
        expectedTokens.add(new OperatorSymbol(PARSER_TRANSFORM_OPERATOR_FUNCTION.apply("=")));
        expectedTokens.add(new ValueSymbol(PARSER_TRANSFORM_VALUE_FUNCTION.apply("2")));
        expectedTokens.add(ConjunctionSymbol.OR);
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

        expectedOrder.add(ConjunctionSymbol.AND);

        key = new KeySymbol("c");
        operator = new OperatorSymbol(
                PARSER_TRANSFORM_OPERATOR_FUNCTION.apply("="));
        value = new ValueSymbol(PARSER_TRANSFORM_VALUE_FUNCTION.apply("3"));
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
        Assert.assertTrue(tree instanceof BaseExpressionTree);
        Expression expression = (Expression) tree.root();
        Assert.assertEquals("a", expression.key().toString());
        Assert.assertEquals("=", expression.operator().toString());
        Assert.assertEquals("1", expression.values().get(0).toString());
    }

    @Test
    public void testSingleBinaryExpressionAbstractSyntaxTree() {
        String ccl = "a >< 1 2";

        // Generate tree
        Parser parser = Parser.create(ccl, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION);
        AbstractSyntaxTree tree = parser.parse();

        // Root node
        Assert.assertTrue(tree instanceof BaseExpressionTree);
        Expression expression = (Expression) tree.root();
        Assert.assertEquals("a", expression.key().toString());
        Assert.assertEquals("><", expression.operator().toString());
        Assert.assertEquals("1", expression.values().get(0).toString());
        Assert.assertEquals("2", expression.values().get(1).toString());
    }

    @Test
    public void testSingleConjunctionAbstractSyntaxTree() {
        String ccl = "a = 1 and b = 2";

        Parser parser = Parser.create(ccl, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION);

        AbstractSyntaxTree tree = parser.parse();

        // Root node
        Assert.assertTrue(tree instanceof BaseAndTree);
        BaseConjunctionTree rootNode = (BaseConjunctionTree) tree;

        // Left node
        Assert.assertTrue(rootNode.left() instanceof BaseExpressionTree);
        Expression leftExpression = (Expression) (rootNode.left()).root();
        Assert.assertEquals("a", leftExpression.key().toString());
        Assert.assertEquals("=", leftExpression.operator().toString());
        Assert.assertEquals("1", leftExpression.values().get(0).toString());

        // Right node
        Assert.assertTrue(rootNode.right() instanceof BaseExpressionTree);
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
        Assert.assertTrue(tree instanceof BaseOrTree);
        BaseConjunctionTree rootNode = (BaseConjunctionTree) tree;

        // Left node
        Assert.assertTrue(rootNode.left() instanceof BaseExpressionTree);
        Expression leftExpression = (Expression) (rootNode.left()).root();
        Assert.assertEquals("a", leftExpression.key().toString());
        Assert.assertEquals("=", leftExpression.operator().toString());
        Assert.assertEquals("1", leftExpression.values().get(0).toString());

        // Right node
        Assert.assertTrue(rootNode.left() instanceof BaseExpressionTree);
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
        Assert.assertTrue(tree instanceof BaseAndTree);
        BaseConjunctionTree rootNode = (BaseConjunctionTree) tree;

        // left node
        Assert.assertTrue(rootNode.left() instanceof BaseAndTree);
        BaseConjunctionTree leftNode = (BaseConjunctionTree) rootNode.left();

        // right node
        Assert.assertTrue(rootNode.right() instanceof BaseExpressionTree);
        Expression rightExpression = (Expression) (rootNode.right()).root();
        Assert.assertEquals("c", rightExpression.key().toString());
        Assert.assertEquals("=", rightExpression.operator().toString());
        Assert.assertEquals("3", rightExpression.values().get(0).toString());

        // Left left node
        Assert.assertTrue(leftNode.left() instanceof BaseExpressionTree);
        Expression leftLeftExpression = (Expression) (leftNode.left()).root();
        Assert.assertEquals("a", leftLeftExpression.key().toString());
        Assert.assertEquals("=", leftLeftExpression.operator().toString());
        Assert.assertEquals("1", leftLeftExpression.values().get(0).toString());

        // Left right node
        Assert.assertTrue(leftNode.right() instanceof BaseExpressionTree);
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
        Assert.assertTrue(tree instanceof BaseOrTree);
        BaseConjunctionTree rootNode = (BaseConjunctionTree) tree;

        // left node
        Assert.assertTrue(rootNode.left() instanceof BaseOrTree);
        BaseConjunctionTree leftNode = (BaseConjunctionTree) rootNode.left();

        // right node
        Assert.assertTrue(rootNode.right() instanceof BaseExpressionTree);
        Expression rightExpression = (Expression) (rootNode.right()).root();
        Assert.assertEquals("c", rightExpression.key().toString());
        Assert.assertEquals("=", rightExpression.operator().toString());
        Assert.assertEquals("3", rightExpression.values().get(0).toString());

        // Left left node
        Assert.assertTrue(leftNode.left() instanceof BaseExpressionTree);
        Expression leftLeftExpression = (Expression) (leftNode.left()).root();
        Assert.assertEquals("a", leftLeftExpression.key().toString());
        Assert.assertEquals("=", leftLeftExpression.operator().toString());
        Assert.assertEquals("1", leftLeftExpression.values().get(0).toString());

        // Left right node
        Assert.assertTrue(leftNode.right() instanceof BaseExpressionTree);
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
        Assert.assertTrue(tree instanceof BaseOrTree);
        BaseConjunctionTree rootNode = (BaseConjunctionTree) tree;

        // left node
        Assert.assertTrue(rootNode.left() instanceof BaseAndTree);
        BaseConjunctionTree leftNode = (BaseConjunctionTree) rootNode.left();

        // right node
        Assert.assertTrue(rootNode.right() instanceof BaseExpressionTree);
        Expression rightExpression = (Expression) (rootNode.right()).root();
        Assert.assertEquals("c", rightExpression.key().toString());
        Assert.assertEquals("=", rightExpression.operator().toString());
        Assert.assertEquals("3", rightExpression.values().get(0).toString());

        // Left left node
        Assert.assertTrue(leftNode.left() instanceof BaseExpressionTree);
        Expression leftLeftExpression = (Expression) (leftNode.left()).root();
        Assert.assertEquals("a", leftLeftExpression.key().toString());
        Assert.assertEquals("=", leftLeftExpression.operator().toString());
        Assert.assertEquals("1", leftLeftExpression.values().get(0).toString());

        // Left right node
        Assert.assertTrue(leftNode.right() instanceof BaseExpressionTree);
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
        Assert.assertTrue(tree instanceof BaseOrTree);
        BaseConjunctionTree rootNode = (BaseConjunctionTree) tree;

        // Right node
        Assert.assertTrue(rootNode.right() instanceof BaseAndTree);
        BaseConjunctionTree rightNode = (BaseConjunctionTree) rootNode.right();

        // right node
        Assert.assertTrue(rootNode.left() instanceof BaseExpressionTree);
        Expression leftExpression = (Expression) (rootNode.left()).root();
        Assert.assertEquals("a", leftExpression.key().toString());
        Assert.assertEquals("=", leftExpression.operator().toString());
        Assert.assertEquals("1", leftExpression.values().get(0).toString());

        // Right left node
        Assert.assertTrue(rightNode.left() instanceof BaseExpressionTree);
        Expression rightLeftExpression = (Expression) (rightNode.left()).root();
        Assert.assertEquals("b", rightLeftExpression.key().toString());
        Assert.assertEquals("=", rightLeftExpression.operator().toString());
        Assert.assertEquals("2", rightLeftExpression.values().get(0).toString());

        // Right right node
        Assert.assertTrue(rightNode.right() instanceof BaseExpressionTree);
        Expression rightRightExpression = (Expression) (rightNode.right()).root();
        Assert.assertEquals("c", rightRightExpression.key().toString());
        Assert.assertEquals("=", rightRightExpression.operator().toString());
        Assert.assertEquals("3", rightRightExpression.values().get(0).toString());
    }

    @Test
    public void testDisjunctionParenthesizedConjunctionAbstractSyntaxTree() {
        String ccl = "a = 1 and (b = 2 or c = 3)";

        long start = System.nanoTime();

        Parser parser = Parser.create(ccl, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION);

        long end = System.nanoTime();
        System.out.println(end-start / 1000000);


        AbstractSyntaxTree tree = parser.parse();

        // Root node
        Assert.assertTrue(tree instanceof BaseAndTree);
        BaseConjunctionTree rootNode = (BaseConjunctionTree) tree;

        // Left node
        Assert.assertTrue(rootNode.left() instanceof BaseExpressionTree);
        Expression leftExpression = (Expression) (rootNode.left()).root();
        Assert.assertEquals("a", leftExpression.key().toString());
        Assert.assertEquals("=", leftExpression.operator().toString());
        Assert.assertEquals("1", leftExpression.values().get(0).toString());

        // Right node
        Assert.assertTrue(rootNode.right() instanceof BaseOrTree);
        BaseConjunctionTree rightNode = (BaseConjunctionTree) rootNode.right();

        // Right left node
        Assert.assertTrue(rightNode.left() instanceof BaseExpressionTree);
        Expression rightLeftExpression = (Expression) (rightNode.left()).root();
        Assert.assertEquals("b", rightLeftExpression.key().toString());
        Assert.assertEquals("=", rightLeftExpression.operator().toString());
        Assert.assertEquals("2",
                rightLeftExpression.values().get(0).toString());

        // Right right node
        Assert.assertTrue(rightNode.right() instanceof BaseExpressionTree);
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
        Assert.assertTrue(tree instanceof BaseExpressionTree);
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
        Assert.assertTrue(tree instanceof BaseExpressionTree);
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
        Assert.assertTrue(tree instanceof BaseExpressionTree);
        Expression expression = (Expression) tree.root();
        Assert.assertEquals("name", expression.key().toString());
        Assert.assertEquals("=", expression.operator().toString());
        Assert.assertEquals("\"Javier Lores\"",
                expression.values().get(0).toString());
    }

    @Test
    public void testJsonReservedIdentifier() {
        String ccl = "$id$ != 40";

        // Generate tree
        Parser parser = Parser.create(ccl,
                PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION);
        AbstractSyntaxTree tree = parser.parse();

        // Root node
        Assert.assertTrue(tree instanceof BaseExpressionTree);
        Expression expression = (Expression) tree.root();
        Assert.assertEquals("$id$", expression.key().toString());
        Assert.assertEquals("!=", expression.operator().toString());
        Assert.assertEquals("40", expression.values().get(0).toString());
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
