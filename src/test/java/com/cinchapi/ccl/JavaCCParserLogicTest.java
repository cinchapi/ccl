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

import com.cinchapi.ccl.grammar.ConjunctionSymbol;
import com.cinchapi.ccl.grammar.ExpressionSymbol;
import com.cinchapi.ccl.grammar.FunctionKeySymbol;
import com.cinchapi.ccl.grammar.FunctionValueSymbol;
import com.cinchapi.ccl.grammar.OperatorSymbol;
import com.cinchapi.ccl.grammar.PageSymbol;
import com.cinchapi.ccl.grammar.ParenthesisSymbol;
import com.cinchapi.ccl.grammar.PostfixNotationSymbol;
import com.cinchapi.ccl.grammar.ValueSymbol;
import com.cinchapi.ccl.grammar.KeySymbol;
import com.cinchapi.ccl.grammar.Symbol;
import com.cinchapi.ccl.syntax.AbstractSyntaxTree;
import com.cinchapi.ccl.syntax.AndTree;
import com.cinchapi.ccl.syntax.ConjunctionTree;
import com.cinchapi.ccl.syntax.ExpressionTree;
import com.cinchapi.ccl.syntax.OrTree;
import com.cinchapi.ccl.syntax.RootTree;
import com.cinchapi.ccl.type.Operator;
import com.cinchapi.ccl.type.function.IndexFunction;
import com.cinchapi.ccl.type.function.KeyCclFunction;
import com.cinchapi.ccl.type.function.KeyRecordsFunction;
import com.cinchapi.ccl.type.function.ImplicitKeyRecordFunction;
import com.cinchapi.concourse.Tag;
import com.cinchapi.concourse.lang.Criteria;
import com.cinchapi.concourse.util.Convert;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import org.junit.Assert;
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
        expectedTokens.add(new OperatorSymbol(
                PARSER_TRANSFORM_OPERATOR_FUNCTION.apply("=")));
        expectedTokens.add(
                new ValueSymbol(PARSER_TRANSFORM_VALUE_FUNCTION.apply("1")));

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
        expectedTokens.add(new OperatorSymbol(
                PARSER_TRANSFORM_OPERATOR_FUNCTION.apply("><")));
        expectedTokens.add(
                new ValueSymbol(PARSER_TRANSFORM_VALUE_FUNCTION.apply("1")));
        expectedTokens.add(
                new ValueSymbol(PARSER_TRANSFORM_VALUE_FUNCTION.apply("3")));

        // Generate queue
        Parser parser = Parser.create(ccl, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION);
        List<Symbol> tokens = parser.tokenize();

        Assert.assertEquals(expectedTokens, tokens);
    }

    @Test
    public void testSingleNRegexExpressionTokenize() {
        String ccl = "name nregex (?i:%jeff%)";

        // Build expected queue
        List<Object> expectedTokens = Lists.newArrayList();

        expectedTokens.add(new KeySymbol("name"));
        expectedTokens.add(new OperatorSymbol(
                PARSER_TRANSFORM_OPERATOR_FUNCTION.apply("nregex")));
        expectedTokens.add(new ValueSymbol(
                PARSER_TRANSFORM_VALUE_FUNCTION.apply("(?i:%jeff%)")));

        // Generate queue
        Parser parser = Parser.create(ccl, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION);
        List<Symbol> tokens = parser.tokenize();

        Assert.assertEquals(expectedTokens, tokens);
    }

    @Test
    public void testSingleLikeExpressionTokenize() {
        String ccl = "name like (?i:%jeff%)";

        // Build expected queue
        List<Object> expectedTokens = Lists.newArrayList();

        expectedTokens.add(new KeySymbol("name"));
        expectedTokens.add(new OperatorSymbol(
                PARSER_TRANSFORM_OPERATOR_FUNCTION.apply("like")));
        expectedTokens.add(new ValueSymbol(
                PARSER_TRANSFORM_VALUE_FUNCTION.apply("(?i:%jeff%)")));

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
        expectedTokens.add(new OperatorSymbol(
                PARSER_TRANSFORM_OPERATOR_FUNCTION.apply("=")));
        expectedTokens.add(
                new ValueSymbol(PARSER_TRANSFORM_VALUE_FUNCTION.apply("1")));
        expectedTokens.add(ConjunctionSymbol.AND);
        expectedTokens.add(new KeySymbol("b"));
        expectedTokens.add(new OperatorSymbol(
                PARSER_TRANSFORM_OPERATOR_FUNCTION.apply("=")));
        expectedTokens.add(
                new ValueSymbol(PARSER_TRANSFORM_VALUE_FUNCTION.apply("2")));

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
        expectedTokens.add(new OperatorSymbol(
                PARSER_TRANSFORM_OPERATOR_FUNCTION.apply("=")));
        expectedTokens.add(
                new ValueSymbol(PARSER_TRANSFORM_VALUE_FUNCTION.apply("1")));
        expectedTokens.add(ConjunctionSymbol.OR);
        expectedTokens.add(new KeySymbol("b"));
        expectedTokens.add(new OperatorSymbol(
                PARSER_TRANSFORM_OPERATOR_FUNCTION.apply("=")));
        expectedTokens.add(
                new ValueSymbol(PARSER_TRANSFORM_VALUE_FUNCTION.apply("2")));

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
        expectedTokens.add(new OperatorSymbol(
                PARSER_TRANSFORM_OPERATOR_FUNCTION.apply("=")));
        expectedTokens.add(
                new ValueSymbol(PARSER_TRANSFORM_VALUE_FUNCTION.apply("1")));
        expectedTokens.add(ConjunctionSymbol.AND);
        expectedTokens.add(new KeySymbol("b"));
        expectedTokens.add(new OperatorSymbol(
                PARSER_TRANSFORM_OPERATOR_FUNCTION.apply("=")));
        expectedTokens.add(
                new ValueSymbol(PARSER_TRANSFORM_VALUE_FUNCTION.apply("2")));
        expectedTokens.add(ConjunctionSymbol.AND);
        expectedTokens.add(new KeySymbol("c"));
        expectedTokens.add(new OperatorSymbol(
                PARSER_TRANSFORM_OPERATOR_FUNCTION.apply("=")));
        expectedTokens.add(
                new ValueSymbol(PARSER_TRANSFORM_VALUE_FUNCTION.apply("3")));

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
        expectedTokens.add(new OperatorSymbol(
                PARSER_TRANSFORM_OPERATOR_FUNCTION.apply("=")));
        expectedTokens.add(
                new ValueSymbol(PARSER_TRANSFORM_VALUE_FUNCTION.apply("1")));
        expectedTokens.add(ConjunctionSymbol.OR);
        expectedTokens.add(new KeySymbol("b"));
        expectedTokens.add(new OperatorSymbol(
                PARSER_TRANSFORM_OPERATOR_FUNCTION.apply("=")));
        expectedTokens.add(
                new ValueSymbol(PARSER_TRANSFORM_VALUE_FUNCTION.apply("2")));
        expectedTokens.add(ConjunctionSymbol.OR);
        expectedTokens.add(new KeySymbol("c"));
        expectedTokens.add(new OperatorSymbol(
                PARSER_TRANSFORM_OPERATOR_FUNCTION.apply("=")));
        expectedTokens.add(
                new ValueSymbol(PARSER_TRANSFORM_VALUE_FUNCTION.apply("3")));

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
        expectedTokens.add(new OperatorSymbol(
                PARSER_TRANSFORM_OPERATOR_FUNCTION.apply("=")));
        expectedTokens.add(
                new ValueSymbol(PARSER_TRANSFORM_VALUE_FUNCTION.apply("1")));
        expectedTokens.add(ConjunctionSymbol.AND);
        expectedTokens.add(new KeySymbol("b"));
        expectedTokens.add(new OperatorSymbol(
                PARSER_TRANSFORM_OPERATOR_FUNCTION.apply("=")));
        expectedTokens.add(
                new ValueSymbol(PARSER_TRANSFORM_VALUE_FUNCTION.apply("2")));
        expectedTokens.add(ConjunctionSymbol.OR);
        expectedTokens.add(new KeySymbol("c"));
        expectedTokens.add(new OperatorSymbol(
                PARSER_TRANSFORM_OPERATOR_FUNCTION.apply("=")));
        expectedTokens.add(
                new ValueSymbol(PARSER_TRANSFORM_VALUE_FUNCTION.apply("3")));

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
        expectedTokens.add(new OperatorSymbol(
                PARSER_TRANSFORM_OPERATOR_FUNCTION.apply("=")));
        expectedTokens.add(
                new ValueSymbol(PARSER_TRANSFORM_VALUE_FUNCTION.apply("1")));
        expectedTokens.add(ConjunctionSymbol.OR);
        expectedTokens.add(new KeySymbol("b"));
        expectedTokens.add(new OperatorSymbol(
                PARSER_TRANSFORM_OPERATOR_FUNCTION.apply("=")));
        expectedTokens.add(
                new ValueSymbol(PARSER_TRANSFORM_VALUE_FUNCTION.apply("2")));
        expectedTokens.add(ConjunctionSymbol.AND);
        expectedTokens.add(new KeySymbol("c"));
        expectedTokens.add(new OperatorSymbol(
                PARSER_TRANSFORM_OPERATOR_FUNCTION.apply("=")));
        expectedTokens.add(
                new ValueSymbol(PARSER_TRANSFORM_VALUE_FUNCTION.apply("3")));

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
        expectedTokens.add(new OperatorSymbol(
                PARSER_TRANSFORM_OPERATOR_FUNCTION.apply("=")));
        expectedTokens.add(
                new ValueSymbol(PARSER_TRANSFORM_VALUE_FUNCTION.apply("1")));
        expectedTokens.add(ConjunctionSymbol.AND);
        expectedTokens.add(ParenthesisSymbol.LEFT);
        expectedTokens.add(new KeySymbol("b"));
        expectedTokens.add(new OperatorSymbol(
                PARSER_TRANSFORM_OPERATOR_FUNCTION.apply("=")));
        expectedTokens.add(
                new ValueSymbol(PARSER_TRANSFORM_VALUE_FUNCTION.apply("2")));
        expectedTokens.add(ConjunctionSymbol.OR);
        expectedTokens.add(new KeySymbol("c"));
        expectedTokens.add(new OperatorSymbol(
                PARSER_TRANSFORM_OPERATOR_FUNCTION.apply("=")));
        expectedTokens.add(
                new ValueSymbol(PARSER_TRANSFORM_VALUE_FUNCTION.apply("3")));
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
        ExpressionSymbol expression = ExpressionSymbol.create(key, operator,
                value);
        expectedOrder.add(expression);

        key = new KeySymbol("b");
        operator = new OperatorSymbol(
                PARSER_TRANSFORM_OPERATOR_FUNCTION.apply("="));
        value = new ValueSymbol(PARSER_TRANSFORM_VALUE_FUNCTION.apply("2"));
        expression = ExpressionSymbol.create(key, operator, value);
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
        ExpressionSymbol expression = ExpressionSymbol.create(key, operator,
                value);
        expectedOrder.add(expression);

        key = new KeySymbol("b");
        operator = new OperatorSymbol(
                PARSER_TRANSFORM_OPERATOR_FUNCTION.apply("="));
        value = new ValueSymbol(PARSER_TRANSFORM_VALUE_FUNCTION.apply("2"));
        expression = ExpressionSymbol.create(key, operator, value);
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
        ExpressionSymbol expression = ExpressionSymbol.create(key, operator,
                value);
        expectedOrder.add(expression);

        key = new KeySymbol("b");
        operator = new OperatorSymbol(
                PARSER_TRANSFORM_OPERATOR_FUNCTION.apply("="));
        value = new ValueSymbol(PARSER_TRANSFORM_VALUE_FUNCTION.apply("2"));
        expression = ExpressionSymbol.create(key, operator, value);
        expectedOrder.add(expression);

        expectedOrder.add(ConjunctionSymbol.AND);

        key = new KeySymbol("c");
        operator = new OperatorSymbol(
                PARSER_TRANSFORM_OPERATOR_FUNCTION.apply("="));
        value = new ValueSymbol(PARSER_TRANSFORM_VALUE_FUNCTION.apply("3"));
        expression = ExpressionSymbol.create(key, operator, value);
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
        ExpressionSymbol expression = ExpressionSymbol.create(key, operator,
                value);
        expectedOrder.add(expression);

        key = new KeySymbol("b");
        operator = new OperatorSymbol(
                PARSER_TRANSFORM_OPERATOR_FUNCTION.apply("="));
        value = new ValueSymbol(PARSER_TRANSFORM_VALUE_FUNCTION.apply("2"));
        expression = ExpressionSymbol.create(key, operator, value);
        expectedOrder.add(expression);

        expectedOrder.add(ConjunctionSymbol.OR);

        key = new KeySymbol("c");
        operator = new OperatorSymbol(
                PARSER_TRANSFORM_OPERATOR_FUNCTION.apply("="));
        value = new ValueSymbol(PARSER_TRANSFORM_VALUE_FUNCTION.apply("3"));
        expression = ExpressionSymbol.create(key, operator, value);
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
        ExpressionSymbol expression = ExpressionSymbol.create(key, operator,
                value);
        expectedOrder.add(expression);

        key = new KeySymbol("b");
        operator = new OperatorSymbol(
                PARSER_TRANSFORM_OPERATOR_FUNCTION.apply("="));
        value = new ValueSymbol(PARSER_TRANSFORM_VALUE_FUNCTION.apply("2"));
        expression = ExpressionSymbol.create(key, operator, value);
        expectedOrder.add(expression);

        expectedOrder.add(ConjunctionSymbol.AND);

        key = new KeySymbol("c");
        operator = new OperatorSymbol(
                PARSER_TRANSFORM_OPERATOR_FUNCTION.apply("="));
        value = new ValueSymbol(PARSER_TRANSFORM_VALUE_FUNCTION.apply("3"));
        expression = ExpressionSymbol.create(key, operator, value);
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
        ExpressionSymbol expression = ExpressionSymbol.create(key, operator,
                value);
        expectedOrder.add(expression);

        key = new KeySymbol("b");
        operator = new OperatorSymbol(
                PARSER_TRANSFORM_OPERATOR_FUNCTION.apply("="));
        value = new ValueSymbol(PARSER_TRANSFORM_VALUE_FUNCTION.apply("2"));
        expression = ExpressionSymbol.create(key, operator, value);
        expectedOrder.add(expression);

        key = new KeySymbol("c");
        operator = new OperatorSymbol(
                PARSER_TRANSFORM_OPERATOR_FUNCTION.apply("="));
        value = new ValueSymbol(PARSER_TRANSFORM_VALUE_FUNCTION.apply("3"));
        expression = ExpressionSymbol.create(key, operator, value);
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
        ExpressionSymbol expression = ExpressionSymbol.create(key, operator,
                value);
        expectedOrder.add(expression);

        key = new KeySymbol("b");
        operator = new OperatorSymbol(
                PARSER_TRANSFORM_OPERATOR_FUNCTION.apply("="));
        value = new ValueSymbol(PARSER_TRANSFORM_VALUE_FUNCTION.apply("2"));
        expression = ExpressionSymbol.create(key, operator, value);
        expectedOrder.add(expression);

        key = new KeySymbol("c");
        operator = new OperatorSymbol(
                PARSER_TRANSFORM_OPERATOR_FUNCTION.apply("="));
        value = new ValueSymbol(PARSER_TRANSFORM_VALUE_FUNCTION.apply("3"));
        expression = ExpressionSymbol.create(key, operator, value);
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
        Assert.assertTrue(tree instanceof RootTree);
        Assert.assertTrue(((RootTree) tree).parseTree() != null);
        RootTree rootNode = (RootTree) tree;

        Assert.assertTrue(rootNode.parseTree() instanceof ExpressionTree);
        ExpressionSymbol expression = (ExpressionSymbol) rootNode.parseTree()
                .root();
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
        Assert.assertTrue(tree instanceof RootTree);
        Assert.assertTrue(((RootTree) tree).parseTree() != null);
        RootTree rootNode = (RootTree) tree;

        Assert.assertTrue(rootNode.parseTree() instanceof ExpressionTree);
        ExpressionSymbol expression = (ExpressionSymbol) rootNode.parseTree()
                .root();
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
        Assert.assertTrue(tree instanceof RootTree);
        RootTree rootNode = (RootTree) tree;

        Assert.assertTrue(rootNode.parseTree() != null);
        Assert.assertTrue(rootNode.parseTree() instanceof AndTree);
        ConjunctionTree andNode = (ConjunctionTree) rootNode.parseTree();

        // Left node
        Assert.assertTrue(andNode.left() instanceof ExpressionTree);
        ExpressionSymbol leftExpression = (ExpressionSymbol) (andNode.left())
                .root();
        Assert.assertEquals("a", leftExpression.key().toString());
        Assert.assertEquals("=", leftExpression.operator().toString());
        Assert.assertEquals("1", leftExpression.values().get(0).toString());

        // Right node
        Assert.assertTrue(andNode.right() instanceof ExpressionTree);
        ExpressionSymbol rightExpression = (ExpressionSymbol) (andNode.right())
                .root();
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
        Assert.assertTrue(tree instanceof RootTree);
        RootTree rootNode = (RootTree) tree;

        Assert.assertTrue(rootNode.parseTree() != null);
        Assert.assertTrue(rootNode.parseTree() instanceof OrTree);
        ConjunctionTree orNode = (ConjunctionTree) rootNode.parseTree();

        // Left node
        Assert.assertTrue(orNode.left() instanceof ExpressionTree);
        ExpressionSymbol leftExpression = (ExpressionSymbol) (orNode.left())
                .root();
        Assert.assertEquals("a", leftExpression.key().toString());
        Assert.assertEquals("=", leftExpression.operator().toString());
        Assert.assertEquals("1", leftExpression.values().get(0).toString());

        // Right node
        Assert.assertTrue(orNode.left() instanceof ExpressionTree);
        ExpressionSymbol rightExpression = (ExpressionSymbol) (orNode.right())
                .root();
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
        Assert.assertTrue(tree instanceof RootTree);
        RootTree rootNode = (RootTree) tree;

        Assert.assertTrue(rootNode.parseTree() != null);
        Assert.assertTrue(rootNode.parseTree() instanceof AndTree);
        ConjunctionTree andNode = (ConjunctionTree) rootNode.parseTree();

        // left node
        Assert.assertTrue(andNode.left() instanceof AndTree);
        ConjunctionTree leftNode = (ConjunctionTree) andNode.left();

        // right node
        Assert.assertTrue(andNode.right() instanceof ExpressionTree);
        ExpressionSymbol rightExpression = (ExpressionSymbol) (andNode.right())
                .root();
        Assert.assertEquals("c", rightExpression.key().toString());
        Assert.assertEquals("=", rightExpression.operator().toString());
        Assert.assertEquals("3", rightExpression.values().get(0).toString());

        // Left left node
        Assert.assertTrue(leftNode.left() instanceof ExpressionTree);
        ExpressionSymbol leftLeftExpression = (ExpressionSymbol) (leftNode
                .left()).root();
        Assert.assertEquals("a", leftLeftExpression.key().toString());
        Assert.assertEquals("=", leftLeftExpression.operator().toString());
        Assert.assertEquals("1", leftLeftExpression.values().get(0).toString());

        // Left right node
        Assert.assertTrue(leftNode.right() instanceof ExpressionTree);
        ExpressionSymbol rightRightExpression = (ExpressionSymbol) (leftNode
                .right()).root();
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
        Assert.assertTrue(tree instanceof RootTree);
        RootTree rootNode = (RootTree) tree;

        Assert.assertTrue(rootNode.parseTree() != null);
        Assert.assertTrue(rootNode.parseTree() instanceof OrTree);
        ConjunctionTree orNode = (ConjunctionTree) rootNode.parseTree();

        // left node
        Assert.assertTrue(orNode.left() instanceof OrTree);
        ConjunctionTree leftNode = (ConjunctionTree) orNode.left();

        // right node
        Assert.assertTrue(orNode.right() instanceof ExpressionTree);
        ExpressionSymbol rightExpression = (ExpressionSymbol) (orNode.right())
                .root();
        Assert.assertEquals("c", rightExpression.key().toString());
        Assert.assertEquals("=", rightExpression.operator().toString());
        Assert.assertEquals("3", rightExpression.values().get(0).toString());

        // Left left node
        Assert.assertTrue(leftNode.left() instanceof ExpressionTree);
        ExpressionSymbol leftLeftExpression = (ExpressionSymbol) (leftNode
                .left()).root();
        Assert.assertEquals("a", leftLeftExpression.key().toString());
        Assert.assertEquals("=", leftLeftExpression.operator().toString());
        Assert.assertEquals("1", leftLeftExpression.values().get(0).toString());

        // Left right node
        Assert.assertTrue(leftNode.right() instanceof ExpressionTree);
        ExpressionSymbol leftRightExpression = (ExpressionSymbol) (leftNode
                .right()).root();
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
        Assert.assertTrue(tree instanceof RootTree);
        RootTree rootNode = (RootTree) tree;

        Assert.assertTrue(rootNode.parseTree() != null);
        Assert.assertTrue(rootNode.parseTree() instanceof OrTree);
        ConjunctionTree orNode = (ConjunctionTree) rootNode.parseTree();

        // left node
        Assert.assertTrue(orNode.left() instanceof AndTree);
        ConjunctionTree leftNode = (ConjunctionTree) orNode.left();

        // right node
        Assert.assertTrue(orNode.right() instanceof ExpressionTree);
        ExpressionSymbol rightExpression = (ExpressionSymbol) (orNode.right())
                .root();
        Assert.assertEquals("c", rightExpression.key().toString());
        Assert.assertEquals("=", rightExpression.operator().toString());
        Assert.assertEquals("3", rightExpression.values().get(0).toString());

        // Left left node
        Assert.assertTrue(leftNode.left() instanceof ExpressionTree);
        ExpressionSymbol leftLeftExpression = (ExpressionSymbol) (leftNode
                .left()).root();
        Assert.assertEquals("a", leftLeftExpression.key().toString());
        Assert.assertEquals("=", leftLeftExpression.operator().toString());
        Assert.assertEquals("1", leftLeftExpression.values().get(0).toString());

        // Left right node
        Assert.assertTrue(leftNode.right() instanceof ExpressionTree);
        ExpressionSymbol leftRightExpression = (ExpressionSymbol) (leftNode
                .right()).root();
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
        Assert.assertTrue(tree instanceof RootTree);
        RootTree rootNode = (RootTree) tree;

        Assert.assertTrue(rootNode.parseTree() != null);
        Assert.assertTrue(rootNode.parseTree() instanceof OrTree);
        ConjunctionTree orNode = (ConjunctionTree) rootNode.parseTree();

        // Right node
        Assert.assertTrue(orNode.right() instanceof AndTree);
        ConjunctionTree rightNode = (ConjunctionTree) orNode.right();

        // right node
        Assert.assertTrue(orNode.left() instanceof ExpressionTree);
        ExpressionSymbol leftExpression = (ExpressionSymbol) (orNode.left())
                .root();
        Assert.assertEquals("a", leftExpression.key().toString());
        Assert.assertEquals("=", leftExpression.operator().toString());
        Assert.assertEquals("1", leftExpression.values().get(0).toString());

        // Right left node
        Assert.assertTrue(rightNode.left() instanceof ExpressionTree);
        ExpressionSymbol rightLeftExpression = (ExpressionSymbol) (rightNode
                .left()).root();
        Assert.assertEquals("b", rightLeftExpression.key().toString());
        Assert.assertEquals("=", rightLeftExpression.operator().toString());
        Assert.assertEquals("2",
                rightLeftExpression.values().get(0).toString());

        // Right right node
        Assert.assertTrue(rightNode.right() instanceof ExpressionTree);
        ExpressionSymbol rightRightExpression = (ExpressionSymbol) (rightNode
                .right()).root();
        Assert.assertEquals("c", rightRightExpression.key().toString());
        Assert.assertEquals("=", rightRightExpression.operator().toString());
        Assert.assertEquals("3",
                rightRightExpression.values().get(0).toString());
    }

    @Test
    public void testDisjunctionParenthesizedConjunctionAbstractSyntaxTree() {
        String ccl = "a = 1 and (b = 2 or c = 3)";

        Parser parser = Parser.create(ccl, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION);

        AbstractSyntaxTree tree = parser.parse();

        // Root node
        Assert.assertTrue(tree instanceof RootTree);
        RootTree rootNode = (RootTree) tree;

        Assert.assertTrue(rootNode.parseTree() != null);
        Assert.assertTrue(rootNode.parseTree() instanceof AndTree);
        ConjunctionTree andNode = (ConjunctionTree) rootNode.parseTree();

        // Left node
        Assert.assertTrue(andNode.left() instanceof ExpressionTree);
        ExpressionSymbol leftExpression = (ExpressionSymbol) (andNode.left())
                .root();
        Assert.assertEquals("a", leftExpression.key().toString());
        Assert.assertEquals("=", leftExpression.operator().toString());
        Assert.assertEquals("1", leftExpression.values().get(0).toString());

        // Right node
        Assert.assertTrue(andNode.right() instanceof OrTree);
        ConjunctionTree rightNode = (ConjunctionTree) andNode.right();

        // Right left node
        Assert.assertTrue(rightNode.left() instanceof ExpressionTree);
        ExpressionSymbol rightLeftExpression = (ExpressionSymbol) (rightNode
                .left()).root();
        Assert.assertEquals("b", rightLeftExpression.key().toString());
        Assert.assertEquals("=", rightLeftExpression.operator().toString());
        Assert.assertEquals("2",
                rightLeftExpression.values().get(0).toString());

        // Right right node
        Assert.assertTrue(rightNode.right() instanceof ExpressionTree);
        ExpressionSymbol leftRightExpression = (ExpressionSymbol) (rightNode
                .right()).root();
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
        Assert.assertTrue(tree instanceof RootTree);
        Assert.assertTrue(((RootTree) tree).parseTree() != null);
        RootTree rootNode = (RootTree) tree;

        Assert.assertTrue(rootNode.parseTree() instanceof ExpressionTree);
        ExpressionSymbol expression = (ExpressionSymbol) rootNode.parseTree()
                .root();
        Assert.assertEquals("name", expression.key().toString());
        Assert.assertEquals("=", expression.operator().toString());
        Assert.assertEquals("\"Lebron James\"",
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
        Assert.assertTrue(tree instanceof RootTree);
        Assert.assertTrue(((RootTree) tree).parseTree() != null);
        RootTree rootNode = (RootTree) tree;

        Assert.assertTrue(rootNode.parseTree() instanceof ExpressionTree);
        ExpressionSymbol expression = (ExpressionSymbol) rootNode.parseTree()
                .root();
        Assert.assertEquals("name", expression.key().toString());
        Assert.assertEquals("=", expression.operator().toString());
        Assert.assertEquals("$name", expression.values().get(0).toString());
    }

    @Test
    public void testDoubleQuotedValue() {
        String ccl = "name = \"Javier Lores\"";

        // Generate tree
        Parser parser = Parser.create(ccl, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION);
        AbstractSyntaxTree tree = parser.parse();

        // Root node
        Assert.assertTrue(tree instanceof RootTree);
        Assert.assertTrue(((RootTree) tree).parseTree() != null);
        RootTree rootNode = (RootTree) tree;

        Assert.assertTrue(rootNode.parseTree() instanceof ExpressionTree);
        ExpressionSymbol expression = (ExpressionSymbol) rootNode.parseTree()
                .root();
        Assert.assertEquals("name", expression.key().toString());
        Assert.assertEquals("=", expression.operator().toString());
        Assert.assertEquals("\"Javier Lores\"",
                expression.values().get(0).toString());
    }

    @Test
    public void testDoubleRightAndLeftQuotedValue() {
        String ccl = "name = “Javier Lores”";

        // Generate tree
        Parser parser = Parser.create(ccl, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION);
        AbstractSyntaxTree tree = parser.parse();

        // Root node
        Assert.assertTrue(tree instanceof RootTree);
        Assert.assertTrue(((RootTree) tree).parseTree() != null);
        RootTree rootNode = (RootTree) tree;

        Assert.assertTrue(rootNode.parseTree() instanceof ExpressionTree);
        ExpressionSymbol expression = (ExpressionSymbol) rootNode.parseTree()
                .root();
        Assert.assertEquals("name", expression.key().toString());
        Assert.assertEquals("=", expression.operator().toString());
        Assert.assertEquals("\"Javier Lores\"",
                expression.values().get(0).toString());
    }

    @Test
    public void testQuotedValueWithinQuotedString() {
        String ccl = "name = \"Javier \\\"Lores\"";

        // Generate tree
        Parser parser = Parser.create(ccl, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION);
        AbstractSyntaxTree tree = parser.parse();

        // Root node
        Assert.assertTrue(tree instanceof RootTree);
        Assert.assertTrue(((RootTree) tree).parseTree() != null);
        RootTree rootNode = (RootTree) tree;

        Assert.assertTrue(rootNode.parseTree() instanceof ExpressionTree);
        ExpressionSymbol expression = (ExpressionSymbol) rootNode.parseTree()
                .root();
        Assert.assertEquals("name", expression.key().toString());
        Assert.assertEquals("=", expression.operator().toString());
        Assert.assertEquals("'Javier \"Lores'",
                expression.values().get(0).toString());
    }

    @Test
    public void testNonQuoteEscapedValueWithinQuoteString() {
        String ccl = "name = \"Javier \\\"\\@Lores\"";

        // Generate tree
        Parser parser = Parser.create(ccl, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION);
        AbstractSyntaxTree tree = parser.parse();

        // Root node
        Assert.assertTrue(tree instanceof RootTree);
        Assert.assertTrue(((RootTree) tree).parseTree() != null);
        RootTree rootNode = (RootTree) tree;

        Assert.assertTrue(rootNode.parseTree() instanceof ExpressionTree);
        ExpressionSymbol expression = (ExpressionSymbol) rootNode.parseTree()
                .root();
        Assert.assertEquals("name", expression.key().toString());
        Assert.assertEquals("=", expression.operator().toString());
        Assert.assertEquals("'Javier \"\\@Lores'",
                expression.values().get(0).toString());
    }

    @Test
    public void validEscapedLocalResolution() {
        String ccl = "name = \\$name";

        // Generate tree
        Parser parser = Parser.create(ccl, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION);
        AbstractSyntaxTree tree = parser.parse();

        // Root node
        Assert.assertTrue(tree instanceof RootTree);
        Assert.assertTrue(((RootTree) tree).parseTree() != null);
        RootTree rootNode = (RootTree) tree;

        Assert.assertTrue(rootNode.parseTree() instanceof ExpressionTree);
        ExpressionSymbol expression = (ExpressionSymbol) rootNode.parseTree()
                .root();
        Assert.assertEquals("name", expression.key().toString());
        Assert.assertEquals("=", expression.operator().toString());
        Assert.assertEquals("$name", expression.values().get(0).toString());
    }

    @Test
    public void validEscapedImplicitLink() {
        String ccl = "name = \\@name";

        // Generate tree
        Parser parser = Parser.create(ccl, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION);
        AbstractSyntaxTree tree = parser.parse();

        // Root node
        Assert.assertTrue(tree instanceof RootTree);
        Assert.assertTrue(((RootTree) tree).parseTree() != null);
        RootTree rootNode = (RootTree) tree;

        Assert.assertTrue(rootNode.parseTree() instanceof ExpressionTree);
        ExpressionSymbol expression = (ExpressionSymbol) rootNode.parseTree()
                .root();
        Assert.assertEquals("name", expression.key().toString());
        Assert.assertEquals("=", expression.operator().toString());
        Assert.assertEquals("@name", expression.values().get(0).toString());
    }

    @Test
    public void validLink() {
        String ccl = "name -> 30";

        // Generate tree
        Parser parser = Parser.create(ccl, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION);
        AbstractSyntaxTree tree = parser.parse();

        // Root node
        Assert.assertTrue(tree instanceof RootTree);
        Assert.assertTrue(((RootTree) tree).parseTree() != null);
        RootTree rootNode = (RootTree) tree;

        Assert.assertTrue(rootNode.parseTree() instanceof ExpressionTree);
        ExpressionSymbol expression = (ExpressionSymbol) rootNode.parseTree()
                .root();
        Assert.assertEquals("name", expression.key().toString());
        Assert.assertEquals("LINKS_TO", expression.operator().toString());
        Assert.assertEquals("30", expression.values().get(0).toString());
    }

    @Test
    public void validOperatorEnum() {
        String ccl = "name LINKS_TO 30";

        // Generate tree
        Parser parser = Parser.create(ccl, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION);
        AbstractSyntaxTree tree = parser.parse();

        // Root node
        Assert.assertTrue(tree instanceof RootTree);
        Assert.assertTrue(((RootTree) tree).parseTree() != null);
        RootTree rootNode = (RootTree) tree;

        Assert.assertTrue(rootNode.parseTree() instanceof ExpressionTree);
        ExpressionSymbol expression = (ExpressionSymbol) rootNode.parseTree()
                .root();
        Assert.assertEquals("name", expression.key().toString());
        Assert.assertEquals("LINKS_TO", expression.operator().toString());
        Assert.assertEquals("30", expression.values().get(0).toString());
    }

    @Test
    public void testNavigationKey() {
        String ccl = "mother.children = 3";

        // Generate tree
        Parser parser = Parser.create(ccl, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION);
        AbstractSyntaxTree tree = parser.parse();

        // Root node
        Assert.assertTrue(tree instanceof RootTree);
        Assert.assertTrue(((RootTree) tree).parseTree() != null);
        RootTree rootNode = (RootTree) tree;

        Assert.assertTrue(rootNode.parseTree() instanceof ExpressionTree);
        ExpressionSymbol expression = (ExpressionSymbol) rootNode.parseTree()
                .root();
        Assert.assertEquals("mother.children", expression.key().toString());
        Assert.assertEquals("=", expression.operator().toString());
        Assert.assertEquals("3", expression.values().get(0).toString());
    }

    @Test
    public void testLongNavigationKey() {
        String ccl = "mother.mother.siblings = 3";

        // Generate tree
        Parser parser = Parser.create(ccl, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION);
        AbstractSyntaxTree tree = parser.parse();

        // Root node
        Assert.assertTrue(tree instanceof RootTree);
        Assert.assertTrue(((RootTree) tree).parseTree() != null);
        RootTree rootNode = (RootTree) tree;

        Assert.assertTrue(rootNode.parseTree() instanceof ExpressionTree);
        ExpressionSymbol expression = (ExpressionSymbol) rootNode.parseTree()
                .root();
        Assert.assertEquals("mother.mother.siblings",
                expression.key().toString());
        Assert.assertEquals("=", expression.operator().toString());
        Assert.assertEquals("3", expression.values().get(0).toString());
    }

    @Test
    public void testPeriodSeparatedValue() {
        String ccl = "mother = a.b.c";

        // Generate tree
        Parser parser = Parser.create(ccl, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION);
        AbstractSyntaxTree tree = parser.parse();

        // Root node
        Assert.assertTrue(tree instanceof RootTree);
        Assert.assertTrue(((RootTree) tree).parseTree() != null);
        RootTree rootNode = (RootTree) tree;

        Assert.assertTrue(rootNode.parseTree() instanceof ExpressionTree);
        ExpressionSymbol expression = (ExpressionSymbol) rootNode.parseTree()
                .root();
        Assert.assertEquals("mother", expression.key().toString());
        Assert.assertEquals("=", expression.operator().toString());
        Assert.assertEquals("a.b.c", expression.values().get(0).toString());
    }

    @Test
    public void testImplicitRecordFunctionAsEvaluationKey() {
        String ccl = "friends | avg > 3";

        // Generate tree
        Parser parser = Parser.create(ccl, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION);
        AbstractSyntaxTree tree = parser.parse();

        // Root node
        Assert.assertTrue(tree instanceof RootTree);
        Assert.assertTrue(((RootTree) tree).parseTree() != null);
        RootTree rootNode = (RootTree) tree;

        Assert.assertTrue(rootNode.parseTree() instanceof ExpressionTree);
        ExpressionSymbol expression = (ExpressionSymbol) rootNode.parseTree()
                .root();
        Assert.assertTrue(expression.key() instanceof FunctionKeySymbol);
        FunctionKeySymbol symbol = expression.key();
        Assert.assertEquals("avg", symbol.key().operation());
        Assert.assertEquals("friends", symbol.key().key());
        Assert.assertEquals(">", expression.operator().toString());
        Assert.assertEquals("3", expression.values().get(0).toString());
    }

    @Test
    public void testImplicitIndexFunctionAsEvaluationValue() {
        String ccl = "age > avg(age)";

        // Generate tree
        Parser parser = Parser.create(ccl, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION);
        AbstractSyntaxTree tree = parser.parse();

        // Root node
        Assert.assertTrue(tree instanceof RootTree);
        Assert.assertTrue(((RootTree) tree).parseTree() != null);
        RootTree rootNode = (RootTree) tree;

        Assert.assertTrue(rootNode.parseTree() instanceof ExpressionTree);
        ExpressionSymbol expression = (ExpressionSymbol) rootNode.parseTree()
                .root();
        Assert.assertEquals("age", expression.key().toString());
        Assert.assertEquals(">", expression.operator().toString());
        Assert.assertTrue(
                expression.values().get(0) instanceof FunctionValueSymbol);
        Assert.assertEquals("avg",
                ((IndexFunction) expression.values().get(0).value())
                        .operation());
        Assert.assertEquals("age",
                ((IndexFunction) expression.values().get(0).value()).key());
    }

    @Test
    public void testExplicitFunctionWithSingleRecordAsEvaluationValue() {
        String ccl = "age > avg(age, 1)";

        // Generate tree
        Parser parser = Parser.create(ccl, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION);
        AbstractSyntaxTree tree = parser.parse();

        // Root node
        Assert.assertTrue(tree instanceof RootTree);
        Assert.assertTrue(((RootTree) tree).parseTree() != null);
        RootTree rootNode = (RootTree) tree;

        Assert.assertTrue(rootNode.parseTree() instanceof ExpressionTree);
        ExpressionSymbol expression = (ExpressionSymbol) rootNode.parseTree()
                .root();
        Assert.assertEquals("age", expression.key().toString());
        Assert.assertEquals(">", expression.operator().toString());
        Assert.assertTrue(
                expression.values().get(0) instanceof FunctionValueSymbol);
        Assert.assertEquals("avg",
                ((KeyRecordsFunction) expression.values().get(0).value())
                        .operation());
        Assert.assertEquals("age",
                ((KeyRecordsFunction) expression.values().get(0).value())
                        .key());
        Assert.assertEquals(1, ((List<String>) ((KeyRecordsFunction) expression
                .values().get(0).value()).source()).size());
        Assert.assertEquals("1",
                ((List<String>) ((KeyRecordsFunction) expression.values().get(0)
                        .value()).source()).get(0));
    }

    @Test
    public void testExplicitFunctionWithBetween() {
        String ccl = "age bw avg(age) 1000";

        // Generate tree
        Parser parser = Parser.create(ccl, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION);
        AbstractSyntaxTree tree = parser.parse();

        // Root node
        Assert.assertTrue(tree instanceof RootTree);
        Assert.assertTrue(((RootTree) tree).parseTree() != null);
        RootTree rootNode = (RootTree) tree;

        Assert.assertTrue(rootNode.parseTree() instanceof ExpressionTree);
        ExpressionSymbol expression = (ExpressionSymbol) rootNode.parseTree()
                .root();
        Assert.assertEquals("age", expression.key().toString());
        Assert.assertEquals("><", expression.operator().toString());
        Assert.assertTrue(
                expression.values().get(0) instanceof FunctionValueSymbol);
        Assert.assertEquals("avg",
                ((IndexFunction) expression.values().get(0).value())
                        .operation());
        Assert.assertEquals("age",
                ((IndexFunction) expression.values().get(0).value()).key());

        Assert.assertEquals("1000", expression.values().get(1).toString());
    }

    @Test
    public void testExplicitFunctionWithBetweenCCL() {
        String ccl = "age bw avg(age, age > 10) 1000";

        // Generate tree
        Parser parser = Parser.create(ccl, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION);
        AbstractSyntaxTree tree = parser.parse();

        // Root node
        Assert.assertTrue(tree instanceof RootTree);
        Assert.assertTrue(((RootTree) tree).parseTree() != null);
        RootTree rootNode = (RootTree) tree;

        Assert.assertTrue(rootNode.parseTree() instanceof ExpressionTree);
        ExpressionSymbol expression = (ExpressionSymbol) rootNode.parseTree()
                .root();
        Assert.assertEquals("age", expression.key().toString());
        Assert.assertEquals("><", expression.operator().toString());
        Assert.assertTrue(
                expression.values().get(0) instanceof FunctionValueSymbol);
        KeyCclFunction function = (KeyCclFunction) expression.values().get(0)
                .value();
        Assert.assertEquals("avg", function.operation());
        Assert.assertEquals("age", function.key());

        Assert.assertTrue(function.source() instanceof RootTree);
        Assert.assertTrue(((RootTree) function.source()).parseTree() != null);
        RootTree functionRoot = (RootTree) function.source();

        Assert.assertTrue(functionRoot.parseTree() instanceof ExpressionTree);
        ExpressionTree t = (ExpressionTree) functionRoot.parseTree();
        ExpressionSymbol root = (ExpressionSymbol) t.root();
        Assert.assertEquals("age", root.key().key());
        Assert.assertEquals(">", root.operator().operator().symbol());
        Assert.assertEquals(10, root.values().get(0).value());

        Assert.assertEquals("1000", expression.values().get(1).toString());
    }

    @Test
    public void testExplicitFunctionWithMultipleRecordsAsEvaluationValue() {
        String ccl = "age > avg(age, 1, 2)";

        // Generate tree
        Parser parser = Parser.create(ccl, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION);
        AbstractSyntaxTree tree = parser.parse();

        // Root node
        Assert.assertTrue(tree instanceof RootTree);
        Assert.assertTrue(((RootTree) tree).parseTree() != null);
        RootTree rootNode = (RootTree) tree;

        Assert.assertTrue(rootNode.parseTree() instanceof ExpressionTree);
        ExpressionSymbol expression = (ExpressionSymbol) rootNode.parseTree()
                .root();
        Assert.assertEquals("age", expression.key().toString());
        Assert.assertEquals(">", expression.operator().toString());
        Assert.assertTrue(
                expression.values().get(0) instanceof FunctionValueSymbol);
        Assert.assertEquals("avg",
                ((KeyRecordsFunction) expression.values().get(0).value())
                        .operation());
        Assert.assertEquals("age",
                ((KeyRecordsFunction) expression.values().get(0).value())
                        .key());
        Assert.assertEquals(2, ((List<String>) ((KeyRecordsFunction) expression
                .values().get(0).value()).source()).size());
        Assert.assertEquals("1",
                ((List<String>) ((KeyRecordsFunction) expression.values().get(0)
                        .value()).source()).get(0));
        Assert.assertEquals("2",
                ((List<String>) ((KeyRecordsFunction) expression.values().get(0)
                        .value()).source()).get(1));
    }

    @Test
    public void testExplicitFunctionWithCCLAsEvaluationValue() {
        String ccl = "age > avg(age, age < 30)";

        // Generate tree
        Parser parser = Parser.create(ccl, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION);
        AbstractSyntaxTree tree = parser.parse();

        // Root node
        Assert.assertTrue(tree instanceof RootTree);
        Assert.assertTrue(((RootTree) tree).parseTree() != null);
        RootTree rootNode = (RootTree) tree;

        Assert.assertTrue(rootNode.parseTree() instanceof ExpressionTree);
        ExpressionSymbol expression = (ExpressionSymbol) rootNode.parseTree()
                .root();
        Assert.assertEquals("age", expression.key().toString());
        Assert.assertEquals(">", expression.operator().toString());

        Assert.assertTrue(
                expression.values().get(0) instanceof FunctionValueSymbol);
        KeyCclFunction function = (KeyCclFunction) expression.values().get(0)
                .value();
        Assert.assertEquals("avg", function.operation());
        Assert.assertEquals("age", function.key());

        Assert.assertTrue(function.source() instanceof RootTree);
        Assert.assertTrue(((RootTree) function.source()).parseTree() != null);
        RootTree functionRoot = (RootTree) function.source();

        Assert.assertTrue(functionRoot.parseTree() instanceof ExpressionTree);
        ExpressionTree t = (ExpressionTree) functionRoot.parseTree();
        ExpressionSymbol root = (ExpressionSymbol) t.root();
        Assert.assertEquals("age", root.key().key());
        Assert.assertEquals("<", root.operator().operator().symbol());
        Assert.assertEquals(30, root.values().get(0).value());
    }

    @Test
    public void validImplicitRecordFunctionAsEvaluationKeyAndExplicitFunctionWithCCLAsEvaluationValue() {
        String ccl = "age | avg > avg(age, age < 30)";

        // Generate tree
        Parser parser = Parser.create(ccl, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION);
        AbstractSyntaxTree tree = parser.parse();

        // Root node
        Assert.assertTrue(tree instanceof RootTree);
        Assert.assertTrue(((RootTree) tree).parseTree() != null);
        RootTree rootNode = (RootTree) tree;

        Assert.assertTrue(rootNode.parseTree() instanceof ExpressionTree);
        ExpressionSymbol expression = (ExpressionSymbol) rootNode.parseTree()
                .root();
        Assert.assertTrue(expression.key() instanceof FunctionKeySymbol);
        Assert.assertEquals("avg",
                ((ImplicitKeyRecordFunction) expression.key().key())
                        .operation());
        Assert.assertEquals(">", expression.operator().toString());

        Assert.assertTrue(
                expression.values().get(0) instanceof FunctionValueSymbol);
        KeyCclFunction function = (KeyCclFunction) expression.values().get(0)
                .value();
        Assert.assertEquals("avg", function.operation());
        Assert.assertEquals("age", function.key());

        Assert.assertTrue(function.source() instanceof RootTree);
        Assert.assertTrue(((RootTree) function.source()).parseTree() != null);
        RootTree functionRoot = (RootTree) function.source();

        Assert.assertTrue(functionRoot.parseTree() instanceof ExpressionTree);
        ExpressionTree t = (ExpressionTree) functionRoot.parseTree();
        ExpressionSymbol root = (ExpressionSymbol) t.root();
        Assert.assertEquals("age", root.key().key());
        Assert.assertEquals("<", root.operator().operator().symbol());
        Assert.assertEquals(30, root.values().get(0).value());
    }

    @Test
    public void testPageWithNumber() {
        String input = PAGE + " 3";

        // Build expected list
        List<Object> expectedTokens = Lists.newArrayList();

        expectedTokens.add(new PageSymbol(String.valueOf(3), null));

        // Generate list
        Parser parser = Parser.create(input, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION);
        List<Symbol> tokens = parser.tokenize();

        Assert.assertEquals(expectedTokens, tokens);
    }

    @Test
    public void testPageWithNumberAndSize() {
        String input = SIZE + " 1 " + PAGE + " 3";

        // Build expected list
        List<Object> expectedTokens = Lists.newArrayList();

        expectedTokens
                .add(new PageSymbol(String.valueOf(3), String.valueOf(1)));

        // Generate list
        Parser parser = Parser.create(input, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION);
        List<Symbol> tokens = parser.tokenize();

        Assert.assertEquals(expectedTokens, tokens);
    }

    @Test
    public void testPageWithSize() {
        String input = SIZE + " 3";

        // Build expected list
        List<Object> expectedTokens = Lists.newArrayList();

        expectedTokens.add(new PageSymbol(null, String.valueOf(3)));

        // Generate list
        Parser parser = Parser.create(input, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION);
        List<Symbol> tokens = parser.tokenize();

        Assert.assertEquals(expectedTokens, tokens);
    }

    @Test
    public void testPageWithSizeAndNumber() {
        String input = SIZE + " 1 " + PAGE + " 3";

        // Build expected list
        List<Object> expectedTokens = Lists.newArrayList();

        expectedTokens
                .add(new PageSymbol(String.valueOf(3), String.valueOf(1)));

        // Generate list
        Parser parser = Parser.create(input, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION);
        List<Symbol> tokens = parser.tokenize();

        Assert.assertEquals(expectedTokens, tokens);
    }

    @Test
    public void testPageWithSizeAndNumberQueue() {
        String input = SIZE + " 1 " + PAGE + " 3";

        // Build expected queue
        Queue<PostfixNotationSymbol> expectedOrder = new LinkedList<>();

        expectedOrder.add(new PageSymbol(String.valueOf(3), String.valueOf(1)));

        // Generate queue
        Parser parser = Parser.create(input, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION);
        Queue<PostfixNotationSymbol> order = parser.order();

        Assert.assertEquals(expectedOrder, order);
    }

    @Test
    public void testPageWithSizeAndNumberAST() {
        String input = SIZE + " 1 " + PAGE + " 3";

        // Generate queue
        Parser parser = Parser.create(input, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION);
        AbstractSyntaxTree tree = parser.parse();

        // Root node
        Assert.assertTrue(tree instanceof RootTree);
        Assert.assertTrue(((RootTree) tree).pageTree() != null);

        PageSymbol page = (PageSymbol) ((RootTree) tree).pageTree().root();
        Assert.assertEquals(2, page.page().offset());
        Assert.assertEquals(1, page.page().limit());
    }

    @Test
    public void testSingleExpressionTokenizeWithPage() {
        String ccl = "a = 1 " + SIZE + " 3 " + PAGE + " 1 ";

        // Build expected queue
        List<Object> expectedTokens = Lists.newArrayList();

        expectedTokens.add(new KeySymbol("a"));
        expectedTokens.add(new OperatorSymbol(
                PARSER_TRANSFORM_OPERATOR_FUNCTION.apply("=")));
        expectedTokens.add(
                new ValueSymbol(PARSER_TRANSFORM_VALUE_FUNCTION.apply("1")));
        expectedTokens.add(new PageSymbol("1", "3"));

        // Generate queue
        Parser parser = Parser.create(ccl, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION);
        List<Symbol> tokens = parser.tokenize();

        Assert.assertEquals(expectedTokens, tokens);
    }

    @Test
    public void testSingleExpressionASTWithPage() {
        String ccl = "a = 1 " + SIZE + " 1 " + PAGE + " 3 ";

        // Generate queue
        Parser parser = Parser.create(ccl, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION);
        AbstractSyntaxTree tree = parser.parse();

        // Root node
        Assert.assertTrue(tree instanceof RootTree);
        Assert.assertTrue(
                ((RootTree) tree).parseTree() instanceof ExpressionTree);
        ExpressionSymbol expression = (ExpressionSymbol) (((RootTree) tree)
                .parseTree().root());
        Assert.assertEquals("a", expression.key().toString());
        Assert.assertEquals("=", expression.operator().toString());
        Assert.assertEquals("1", expression.values().get(0).toString());

        // Page Node
        Assert.assertTrue(((RootTree) tree).pageTree() != null);
        PageSymbol page = (PageSymbol) ((RootTree) tree).pageTree().root();
        Assert.assertEquals(2, page.page().offset());
        Assert.assertEquals(1, page.page().limit());
    }

    @Test
    public void testJsonReservedIdentifier() {
        String ccl = "$id$ != 40";

        // Generate tree
        Parser parser = Parser.create(ccl, PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION);
        AbstractSyntaxTree tree = parser.parse();

        // Root node
        Assert.assertTrue(tree instanceof RootTree);
        Assert.assertTrue(((RootTree) tree).parseTree() != null);
        RootTree rootNode = (RootTree) tree;

        Assert.assertTrue(rootNode.parseTree() instanceof ExpressionTree);
        ExpressionSymbol expression = (ExpressionSymbol) rootNode.parseTree()
                .root();
        Assert.assertEquals("$id$", expression.key().toString());
        Assert.assertEquals("!=", expression.operator().toString());
        Assert.assertEquals("40", expression.values().get(0).toString());
    }

    @Test
    public void testReproIX5A() {
        Criteria criteria = Criteria.where()
                .group(Criteria.where().key("_")
                        .operator(com.cinchapi.concourse.thrift.Operator.EQUALS)
                        .value("org.internx.model.data.user.Student"))
                .and()
                .group(Criteria.where()
                        .group(Criteria.where().key("group").operator(
                                com.cinchapi.concourse.thrift.Operator.LIKE)
                                .value("%Accounting And Business/management%"))
                        .or()
                        .group(Criteria.where().key("major").operator(
                                com.cinchapi.concourse.thrift.Operator.LIKE)
                                .value("%accounting and business/management%")));

        // Generate tree
        Parser parser = Parser.create(criteria.ccl(),
                PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION);
        parser.tokenize().forEach(token -> {
            if(token instanceof ValueSymbol) {
                Assert.assertEquals(String.class,
                        ((ValueSymbol) token).value().getClass());
            }
        });
    }

    @Test
    public void testReproIX5B() {
        Criteria criteria = Criteria.where()
                .group(Criteria.where().key("_")
                        .operator(com.cinchapi.concourse.thrift.Operator.EQUALS)
                        .value(Tag
                                .create("org.internx.model.data.user.Student")))
                .and()
                .group(Criteria.where().group(Criteria.where().key("group")
                        .operator(com.cinchapi.concourse.thrift.Operator.EQUALS)
                        .value(Tag
                                .create("Accounting And Business/management")))
                        .or()
                        .group(Criteria.where().key("major").operator(
                                com.cinchapi.concourse.thrift.Operator.EQUALS)
                                .value(Tag.create(
                                        "accounting and business/management"))));

        // Generate tree
        Parser parser = Parser.create(criteria.ccl(),
                PARSER_TRANSFORM_VALUE_FUNCTION,
                PARSER_TRANSFORM_OPERATOR_FUNCTION);
        parser.tokenize().forEach(token -> {
            if(token instanceof ValueSymbol) {
                Assert.assertEquals(Tag.class,
                        ((ValueSymbol) token).value().getClass());
            }
        });
    }

    // String constants
    static final String PAGE = "page";
    static final String SIZE = "size";

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
