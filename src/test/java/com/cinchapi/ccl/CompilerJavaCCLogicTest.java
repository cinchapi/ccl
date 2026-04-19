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
import com.cinchapi.ccl.grammar.DirectionSymbol;
import com.cinchapi.ccl.grammar.ExpressionSymbol;
import com.cinchapi.ccl.grammar.FunctionKeySymbol;
import com.cinchapi.ccl.grammar.FunctionTokenSymbol;
import com.cinchapi.ccl.grammar.FunctionValueSymbol;
import com.cinchapi.ccl.grammar.OperatorSymbol;
import com.cinchapi.ccl.grammar.OrderComponentSymbol;
import com.cinchapi.ccl.grammar.OrderSymbol;
import com.cinchapi.ccl.grammar.PageSymbol;
import com.cinchapi.ccl.grammar.ParenthesisSymbol;
import com.cinchapi.ccl.grammar.PostfixNotationSymbol;
import com.cinchapi.ccl.grammar.ValueSymbol;
import com.cinchapi.ccl.grammar.KeySymbol;
import com.cinchapi.ccl.grammar.KeyTokenSymbol;
import com.cinchapi.ccl.grammar.NavigationKeyStop;
import com.cinchapi.ccl.grammar.NavigationKeySymbol;
import com.cinchapi.ccl.grammar.Symbol;
import com.cinchapi.ccl.grammar.TimestampSymbol;
import com.cinchapi.ccl.grammar.command.*;
import com.cinchapi.ccl.syntax.*;
import com.cinchapi.ccl.type.Operator;
import com.cinchapi.ccl.type.function.IndexFunction;
import com.cinchapi.ccl.type.function.KeyConditionFunction;
import com.cinchapi.ccl.type.function.KeyRecordsFunction;
import com.cinchapi.ccl.util.NaturalLanguage;
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
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * Tests for {@link JavaCCParser}.
 *
 * These tests include utput tests (postfix, abstract
 * syntax tree, tokens)
 */
public class CompilerJavaCCLogicTest {

    @Test
    public void testSingleExpressionTokenize() {
        String ccl = "a = 1";

        // Build expected queue
        List<Object> expectedTokens = Lists.newArrayList();

        expectedTokens.add(new KeySymbol("a"));
        expectedTokens.add(new OperatorSymbol(
                COMPILER_PARSE_OPERATOR_FUNCTION.apply("=")));
        expectedTokens
                .add(new ValueSymbol(COMPILER_PARSE_VALUE_FUNCTION.apply("1")));

        // Generate queue
        Compiler compiler = Compiler.create(COMPILER_PARSE_VALUE_FUNCTION,
                COMPILER_PARSE_OPERATOR_FUNCTION);
        AbstractSyntaxTree ast = compiler.parse(ccl);
        List<Symbol> tokens = compiler.tokenize(ast);

        Assert.assertEquals(expectedTokens, tokens);
    }

    @Test
    public void testSingleBinaryExpressionTokenize() {
        String ccl = "a >< 1 3";

        // Build expected queue
        List<Object> expectedTokens = Lists.newArrayList();

        expectedTokens.add(new KeySymbol("a"));
        expectedTokens.add(new OperatorSymbol(
                COMPILER_PARSE_OPERATOR_FUNCTION.apply("><")));
        expectedTokens
                .add(new ValueSymbol(COMPILER_PARSE_VALUE_FUNCTION.apply("1")));
        expectedTokens
                .add(new ValueSymbol(COMPILER_PARSE_VALUE_FUNCTION.apply("3")));

        // Generate queue
        Compiler compiler = Compiler.create(COMPILER_PARSE_VALUE_FUNCTION,
                COMPILER_PARSE_OPERATOR_FUNCTION);
        AbstractSyntaxTree ast = compiler.parse(ccl);
        List<Symbol> tokens = compiler.tokenize(ast);

        Assert.assertEquals(expectedTokens, tokens);
    }

    @Test
    public void testSingleNRegexExpressionTokenize() {
        String ccl = "name nregex (?i:%jeff%)";

        // Build expected queue
        List<Object> expectedTokens = Lists.newArrayList();

        expectedTokens.add(new KeySymbol("name"));
        expectedTokens.add(new OperatorSymbol(
                COMPILER_PARSE_OPERATOR_FUNCTION.apply("nregex")));
        expectedTokens.add(new ValueSymbol(
                COMPILER_PARSE_VALUE_FUNCTION.apply("(?i:%jeff%)")));

        // Generate queue
        Compiler compiler = Compiler.create(COMPILER_PARSE_VALUE_FUNCTION,
                COMPILER_PARSE_OPERATOR_FUNCTION);
        AbstractSyntaxTree ast = compiler.parse(ccl);
        List<Symbol> tokens = compiler.tokenize(ast);

        Assert.assertEquals(expectedTokens, tokens);
    }

    @Test
    public void testSingleLikeExpressionTokenize() {
        String ccl = "name like (?i:%jeff%)";

        // Build expected queue
        List<Object> expectedTokens = Lists.newArrayList();

        expectedTokens.add(new KeySymbol("name"));
        expectedTokens.add(new OperatorSymbol(
                COMPILER_PARSE_OPERATOR_FUNCTION.apply("like")));
        expectedTokens.add(new ValueSymbol(
                COMPILER_PARSE_VALUE_FUNCTION.apply("(?i:%jeff%)")));

        // Generate queue
        Compiler compiler = Compiler.create(COMPILER_PARSE_VALUE_FUNCTION,
                COMPILER_PARSE_OPERATOR_FUNCTION);
        AbstractSyntaxTree ast = compiler.parse(ccl);
        List<Symbol> tokens = compiler.tokenize(ast);

        Assert.assertEquals(expectedTokens, tokens);
    }

    @Test
    public void testSingleConjunctionTokenize() {
        String ccl = "a = 1 and b = 2";

        // Build expected queue
        List<Symbol> expectedTokens = Lists.newArrayList();

        expectedTokens.add(new KeySymbol("a"));
        expectedTokens.add(new OperatorSymbol(
                COMPILER_PARSE_OPERATOR_FUNCTION.apply("=")));
        expectedTokens
                .add(new ValueSymbol(COMPILER_PARSE_VALUE_FUNCTION.apply("1")));
        expectedTokens.add(ConjunctionSymbol.AND);
        expectedTokens.add(new KeySymbol("b"));
        expectedTokens.add(new OperatorSymbol(
                COMPILER_PARSE_OPERATOR_FUNCTION.apply("=")));
        expectedTokens
                .add(new ValueSymbol(COMPILER_PARSE_VALUE_FUNCTION.apply("2")));

        // Generate queue
        Compiler compiler = Compiler.create(COMPILER_PARSE_VALUE_FUNCTION,
                COMPILER_PARSE_OPERATOR_FUNCTION);
        AbstractSyntaxTree ast = compiler.parse(ccl);
        List<Symbol> tokens = compiler.tokenize(ast);

        Assert.assertEquals(expectedTokens, tokens);
    }

    @Test
    public void testSingleDisjunctionTokenize() {
        String ccl = "a = 1 or b = 2";

        // Build expected queue
        List<Symbol> expectedTokens = Lists.newArrayList();

        expectedTokens.add(new KeySymbol("a"));
        expectedTokens.add(new OperatorSymbol(
                COMPILER_PARSE_OPERATOR_FUNCTION.apply("=")));
        expectedTokens
                .add(new ValueSymbol(COMPILER_PARSE_VALUE_FUNCTION.apply("1")));
        expectedTokens.add(ConjunctionSymbol.OR);
        expectedTokens.add(new KeySymbol("b"));
        expectedTokens.add(new OperatorSymbol(
                COMPILER_PARSE_OPERATOR_FUNCTION.apply("=")));
        expectedTokens
                .add(new ValueSymbol(COMPILER_PARSE_VALUE_FUNCTION.apply("2")));

        // Generate queue
        Compiler compiler = Compiler.create(COMPILER_PARSE_VALUE_FUNCTION,
                COMPILER_PARSE_OPERATOR_FUNCTION);
        AbstractSyntaxTree ast = compiler.parse(ccl);
        List<Symbol> tokens = compiler.tokenize(ast);

        Assert.assertEquals(expectedTokens, tokens);
    }

    @Test
    public void testDoubleConjunctionTokenize() {
        String ccl = "a = 1 and b = 2 and c = 3";

        // Build expected queue
        List<Symbol> expectedTokens = Lists.newArrayList();

        expectedTokens.add(new KeySymbol("a"));
        expectedTokens.add(new OperatorSymbol(
                COMPILER_PARSE_OPERATOR_FUNCTION.apply("=")));
        expectedTokens
                .add(new ValueSymbol(COMPILER_PARSE_VALUE_FUNCTION.apply("1")));
        expectedTokens.add(ConjunctionSymbol.AND);
        expectedTokens.add(new KeySymbol("b"));
        expectedTokens.add(new OperatorSymbol(
                COMPILER_PARSE_OPERATOR_FUNCTION.apply("=")));
        expectedTokens
                .add(new ValueSymbol(COMPILER_PARSE_VALUE_FUNCTION.apply("2")));
        expectedTokens.add(ConjunctionSymbol.AND);
        expectedTokens.add(new KeySymbol("c"));
        expectedTokens.add(new OperatorSymbol(
                COMPILER_PARSE_OPERATOR_FUNCTION.apply("=")));
        expectedTokens
                .add(new ValueSymbol(COMPILER_PARSE_VALUE_FUNCTION.apply("3")));

        // Generate queue
        Compiler compiler = Compiler.create(COMPILER_PARSE_VALUE_FUNCTION,
                COMPILER_PARSE_OPERATOR_FUNCTION);
        AbstractSyntaxTree ast = compiler.parse(ccl);
        List<Symbol> tokens = compiler.tokenize(ast);

        Assert.assertEquals(expectedTokens, tokens);
    }

    @Test
    public void testDoubleDisjunctionTokenize() {
        String ccl = "a = 1 or b = 2 or c = 3";

        // Build expected queue
        List<Symbol> expectedTokens = Lists.newArrayList();

        expectedTokens.add(new KeySymbol("a"));
        expectedTokens.add(new OperatorSymbol(
                COMPILER_PARSE_OPERATOR_FUNCTION.apply("=")));
        expectedTokens
                .add(new ValueSymbol(COMPILER_PARSE_VALUE_FUNCTION.apply("1")));
        expectedTokens.add(ConjunctionSymbol.OR);
        expectedTokens.add(new KeySymbol("b"));
        expectedTokens.add(new OperatorSymbol(
                COMPILER_PARSE_OPERATOR_FUNCTION.apply("=")));
        expectedTokens
                .add(new ValueSymbol(COMPILER_PARSE_VALUE_FUNCTION.apply("2")));
        expectedTokens.add(ConjunctionSymbol.OR);
        expectedTokens.add(new KeySymbol("c"));
        expectedTokens.add(new OperatorSymbol(
                COMPILER_PARSE_OPERATOR_FUNCTION.apply("=")));
        expectedTokens
                .add(new ValueSymbol(COMPILER_PARSE_VALUE_FUNCTION.apply("3")));

        // Generate queue
        Compiler compiler = Compiler.create(COMPILER_PARSE_VALUE_FUNCTION,
                COMPILER_PARSE_OPERATOR_FUNCTION);
        AbstractSyntaxTree ast = compiler.parse(ccl);
        List<Symbol> tokens = compiler.tokenize(ast);

        Assert.assertEquals(expectedTokens, tokens);
    }

    @Test
    public void testConjunctionDisjunctionTokenize() {
        String ccl = "a = 1 and b = 2 or c = 3";

        // Build expected queue
        List<Symbol> expectedTokens = Lists.newArrayList();

        expectedTokens.add(new KeySymbol("a"));
        expectedTokens.add(new OperatorSymbol(
                COMPILER_PARSE_OPERATOR_FUNCTION.apply("=")));
        expectedTokens
                .add(new ValueSymbol(COMPILER_PARSE_VALUE_FUNCTION.apply("1")));
        expectedTokens.add(ConjunctionSymbol.AND);
        expectedTokens.add(new KeySymbol("b"));
        expectedTokens.add(new OperatorSymbol(
                COMPILER_PARSE_OPERATOR_FUNCTION.apply("=")));
        expectedTokens
                .add(new ValueSymbol(COMPILER_PARSE_VALUE_FUNCTION.apply("2")));
        expectedTokens.add(ConjunctionSymbol.OR);
        expectedTokens.add(new KeySymbol("c"));
        expectedTokens.add(new OperatorSymbol(
                COMPILER_PARSE_OPERATOR_FUNCTION.apply("=")));
        expectedTokens
                .add(new ValueSymbol(COMPILER_PARSE_VALUE_FUNCTION.apply("3")));

        // Generate queue
        Compiler compiler = Compiler.create(COMPILER_PARSE_VALUE_FUNCTION,
                COMPILER_PARSE_OPERATOR_FUNCTION);
        AbstractSyntaxTree ast = compiler.parse(ccl);
        List<Symbol> tokens = compiler.tokenize(ast);

        Assert.assertEquals(expectedTokens, tokens);
    }

    @Test
    public void testDisjunctionConjunctionTokenize() {
        String ccl = "a = 1 or b = 2 and c = 3";

        // Build expected queue
        List<Symbol> expectedTokens = Lists.newArrayList();

        expectedTokens.add(new KeySymbol("a"));
        expectedTokens.add(new OperatorSymbol(
                COMPILER_PARSE_OPERATOR_FUNCTION.apply("=")));
        expectedTokens
                .add(new ValueSymbol(COMPILER_PARSE_VALUE_FUNCTION.apply("1")));
        expectedTokens.add(ConjunctionSymbol.OR);
        expectedTokens.add(new KeySymbol("b"));
        expectedTokens.add(new OperatorSymbol(
                COMPILER_PARSE_OPERATOR_FUNCTION.apply("=")));
        expectedTokens
                .add(new ValueSymbol(COMPILER_PARSE_VALUE_FUNCTION.apply("2")));
        expectedTokens.add(ConjunctionSymbol.AND);
        expectedTokens.add(new KeySymbol("c"));
        expectedTokens.add(new OperatorSymbol(
                COMPILER_PARSE_OPERATOR_FUNCTION.apply("=")));
        expectedTokens
                .add(new ValueSymbol(COMPILER_PARSE_VALUE_FUNCTION.apply("3")));

        // Generate queue
        Compiler compiler = Compiler.create(COMPILER_PARSE_VALUE_FUNCTION,
                COMPILER_PARSE_OPERATOR_FUNCTION);
        AbstractSyntaxTree ast = compiler.parse(ccl);
        List<Symbol> tokens = compiler.tokenize(ast);

        Assert.assertEquals(expectedTokens, tokens);
    }

    @Test
    public void testDisjunctionParenthesizedConjunctionTokenize() {
        String ccl = "a = 1 and (b = 2 or c = 3)";

        // Build expected queue
        List<Symbol> expectedTokens = Lists.newArrayList();

        expectedTokens.add(new KeySymbol("a"));
        expectedTokens.add(new OperatorSymbol(
                COMPILER_PARSE_OPERATOR_FUNCTION.apply("=")));
        expectedTokens
                .add(new ValueSymbol(COMPILER_PARSE_VALUE_FUNCTION.apply("1")));
        expectedTokens.add(ConjunctionSymbol.AND);
        expectedTokens.add(ParenthesisSymbol.LEFT);
        expectedTokens.add(new KeySymbol("b"));
        expectedTokens.add(new OperatorSymbol(
                COMPILER_PARSE_OPERATOR_FUNCTION.apply("=")));
        expectedTokens
                .add(new ValueSymbol(COMPILER_PARSE_VALUE_FUNCTION.apply("2")));
        expectedTokens.add(ConjunctionSymbol.OR);
        expectedTokens.add(new KeySymbol("c"));
        expectedTokens.add(new OperatorSymbol(
                COMPILER_PARSE_OPERATOR_FUNCTION.apply("=")));
        expectedTokens
                .add(new ValueSymbol(COMPILER_PARSE_VALUE_FUNCTION.apply("3")));
        expectedTokens.add(ParenthesisSymbol.RIGHT);

        // Generate queue
        Compiler compiler = Compiler.create(COMPILER_PARSE_VALUE_FUNCTION,
                COMPILER_PARSE_OPERATOR_FUNCTION);
        AbstractSyntaxTree ast = compiler.parse(ccl);
        List<Symbol> tokens = compiler.tokenize(ast);

        Assert.assertEquals(expectedTokens, tokens);
    }

    @Test
    public void testSingleConjunctionPostFix() {
        String ccl = "a = 1 and b = 2";

        // Build expected queue
        Queue<PostfixNotationSymbol> expectedOrder = new LinkedList<>();

        KeySymbol key = new KeySymbol("a");
        OperatorSymbol operator = new OperatorSymbol(
                COMPILER_PARSE_OPERATOR_FUNCTION.apply("="));
        ValueSymbol value = new ValueSymbol(
                COMPILER_PARSE_VALUE_FUNCTION.apply("1"));
        ExpressionSymbol expression = ExpressionSymbol.create(key, operator,
                value);
        expectedOrder.add(expression);

        key = new KeySymbol("b");
        operator = new OperatorSymbol(
                COMPILER_PARSE_OPERATOR_FUNCTION.apply("="));
        value = new ValueSymbol(COMPILER_PARSE_VALUE_FUNCTION.apply("2"));
        expression = ExpressionSymbol.create(key, operator, value);
        expectedOrder.add(expression);

        expectedOrder.add(ConjunctionSymbol.AND);

        // Generate queue
        Compiler compiler = Compiler.create(COMPILER_PARSE_VALUE_FUNCTION,
                COMPILER_PARSE_OPERATOR_FUNCTION);
        AbstractSyntaxTree ast = compiler.parse(ccl);
        ConditionTree tree = (ConditionTree) ast;
        Queue<PostfixNotationSymbol> order = compiler.arrange(tree);
        Assert.assertEquals(expectedOrder, order);
    }

    @Test
    public void testSingleDisjunctionPostFix() {
        String ccl = "a = 1 or b = 2";
        // Build expected queue
        Queue<PostfixNotationSymbol> expectedOrder = new LinkedList<>();

        KeySymbol key = new KeySymbol("a");
        OperatorSymbol operator = new OperatorSymbol(
                COMPILER_PARSE_OPERATOR_FUNCTION.apply("="));
        ValueSymbol value = new ValueSymbol(
                COMPILER_PARSE_VALUE_FUNCTION.apply("1"));
        ExpressionSymbol expression = ExpressionSymbol.create(key, operator,
                value);
        expectedOrder.add(expression);

        key = new KeySymbol("b");
        operator = new OperatorSymbol(
                COMPILER_PARSE_OPERATOR_FUNCTION.apply("="));
        value = new ValueSymbol(COMPILER_PARSE_VALUE_FUNCTION.apply("2"));
        expression = ExpressionSymbol.create(key, operator, value);
        expectedOrder.add(expression);

        expectedOrder.add(ConjunctionSymbol.OR);

        // Generate queue
        Compiler compiler = Compiler.create(COMPILER_PARSE_VALUE_FUNCTION,
                COMPILER_PARSE_OPERATOR_FUNCTION);
        AbstractSyntaxTree ast = compiler.parse(ccl);
        Queue<PostfixNotationSymbol> order = compiler
                .arrange((ConditionTree) ast);

        Assert.assertEquals(expectedOrder, order);
    }

    @Test
    public void testDoubleConjunctionPostFix() {
        String ccl = "a = 1 and b = 2 and c = 3";

        // Build expected queue
        Queue<PostfixNotationSymbol> expectedOrder = new LinkedList<>();

        KeySymbol key = new KeySymbol("a");
        OperatorSymbol operator = new OperatorSymbol(
                COMPILER_PARSE_OPERATOR_FUNCTION.apply("="));
        ValueSymbol value = new ValueSymbol(
                COMPILER_PARSE_VALUE_FUNCTION.apply("1"));
        ExpressionSymbol expression = ExpressionSymbol.create(key, operator,
                value);
        expectedOrder.add(expression);

        key = new KeySymbol("b");
        operator = new OperatorSymbol(
                COMPILER_PARSE_OPERATOR_FUNCTION.apply("="));
        value = new ValueSymbol(COMPILER_PARSE_VALUE_FUNCTION.apply("2"));
        expression = ExpressionSymbol.create(key, operator, value);
        expectedOrder.add(expression);

        expectedOrder.add(ConjunctionSymbol.AND);

        key = new KeySymbol("c");
        operator = new OperatorSymbol(
                COMPILER_PARSE_OPERATOR_FUNCTION.apply("="));
        value = new ValueSymbol(COMPILER_PARSE_VALUE_FUNCTION.apply("3"));
        expression = ExpressionSymbol.create(key, operator, value);
        expectedOrder.add(expression);

        expectedOrder.add(ConjunctionSymbol.AND);

        // Generate queue
        Compiler compiler = Compiler.create(COMPILER_PARSE_VALUE_FUNCTION,
                COMPILER_PARSE_OPERATOR_FUNCTION);
        AbstractSyntaxTree ast = compiler.parse(ccl);
        ConditionTree tree = (ConditionTree) ast;
        Queue<PostfixNotationSymbol> order = compiler.arrange(tree);

        Assert.assertEquals(expectedOrder, order);
    }

    @Test
    public void testDoubleDisjunctionPostFix() {
        String ccl = "a = 1 or b = 2 or c = 3";

        // Build expected queue
        Queue<PostfixNotationSymbol> expectedOrder = new LinkedList<>();

        KeySymbol key = new KeySymbol("a");
        OperatorSymbol operator = new OperatorSymbol(
                COMPILER_PARSE_OPERATOR_FUNCTION.apply("="));
        ValueSymbol value = new ValueSymbol(
                COMPILER_PARSE_VALUE_FUNCTION.apply("1"));
        ExpressionSymbol expression = ExpressionSymbol.create(key, operator,
                value);
        expectedOrder.add(expression);

        key = new KeySymbol("b");
        operator = new OperatorSymbol(
                COMPILER_PARSE_OPERATOR_FUNCTION.apply("="));
        value = new ValueSymbol(COMPILER_PARSE_VALUE_FUNCTION.apply("2"));
        expression = ExpressionSymbol.create(key, operator, value);
        expectedOrder.add(expression);

        expectedOrder.add(ConjunctionSymbol.OR);

        key = new KeySymbol("c");
        operator = new OperatorSymbol(
                COMPILER_PARSE_OPERATOR_FUNCTION.apply("="));
        value = new ValueSymbol(COMPILER_PARSE_VALUE_FUNCTION.apply("3"));
        expression = ExpressionSymbol.create(key, operator, value);
        expectedOrder.add(expression);

        expectedOrder.add(ConjunctionSymbol.OR);

        // Generate queue
        Compiler compiler = Compiler.create(COMPILER_PARSE_VALUE_FUNCTION,
                COMPILER_PARSE_OPERATOR_FUNCTION);
        AbstractSyntaxTree ast = compiler.parse(ccl);
        ConditionTree tree = (ConditionTree) ast;
        Queue<PostfixNotationSymbol> order = compiler.arrange(tree);

        Assert.assertEquals(expectedOrder, order);
    }

    @Test
    public void testConjunctionDisjunctionPostFix() {
        String ccl = "a = 1 and b = 2 or c = 3";

        // Build expected queue
        Queue<PostfixNotationSymbol> expectedOrder = new LinkedList<>();

        KeySymbol key = new KeySymbol("a");
        OperatorSymbol operator = new OperatorSymbol(
                COMPILER_PARSE_OPERATOR_FUNCTION.apply("="));
        ValueSymbol value = new ValueSymbol(
                COMPILER_PARSE_VALUE_FUNCTION.apply("1"));
        ExpressionSymbol expression = ExpressionSymbol.create(key, operator,
                value);
        expectedOrder.add(expression);

        key = new KeySymbol("b");
        operator = new OperatorSymbol(
                COMPILER_PARSE_OPERATOR_FUNCTION.apply("="));
        value = new ValueSymbol(COMPILER_PARSE_VALUE_FUNCTION.apply("2"));
        expression = ExpressionSymbol.create(key, operator, value);
        expectedOrder.add(expression);

        expectedOrder.add(ConjunctionSymbol.AND);

        key = new KeySymbol("c");
        operator = new OperatorSymbol(
                COMPILER_PARSE_OPERATOR_FUNCTION.apply("="));
        value = new ValueSymbol(COMPILER_PARSE_VALUE_FUNCTION.apply("3"));
        expression = ExpressionSymbol.create(key, operator, value);
        expectedOrder.add(expression);

        expectedOrder.add(ConjunctionSymbol.OR);

        // Generate queue
        Compiler compiler = Compiler.create(COMPILER_PARSE_VALUE_FUNCTION,
                COMPILER_PARSE_OPERATOR_FUNCTION);
        AbstractSyntaxTree ast = compiler.parse(ccl);
        ConditionTree tree = (ConditionTree) ast;
        Queue<PostfixNotationSymbol> order = compiler.arrange(tree);

        Assert.assertEquals(expectedOrder, order);
    }

    @Test
    public void testDisjunctionConjunctionPostFix() {
        String ccl = "a = 1 or b = 2 and c = 3";

        // Build expected queue
        Queue<PostfixNotationSymbol> expectedOrder = new LinkedList<>();

        KeySymbol key = new KeySymbol("a");
        OperatorSymbol operator = new OperatorSymbol(
                COMPILER_PARSE_OPERATOR_FUNCTION.apply("="));
        ValueSymbol value = new ValueSymbol(
                COMPILER_PARSE_VALUE_FUNCTION.apply("1"));
        ExpressionSymbol expression = ExpressionSymbol.create(key, operator,
                value);
        expectedOrder.add(expression);

        key = new KeySymbol("b");
        operator = new OperatorSymbol(
                COMPILER_PARSE_OPERATOR_FUNCTION.apply("="));
        value = new ValueSymbol(COMPILER_PARSE_VALUE_FUNCTION.apply("2"));
        expression = ExpressionSymbol.create(key, operator, value);
        expectedOrder.add(expression);

        key = new KeySymbol("c");
        operator = new OperatorSymbol(
                COMPILER_PARSE_OPERATOR_FUNCTION.apply("="));
        value = new ValueSymbol(COMPILER_PARSE_VALUE_FUNCTION.apply("3"));
        expression = ExpressionSymbol.create(key, operator, value);
        expectedOrder.add(expression);

        expectedOrder.add(ConjunctionSymbol.AND);
        expectedOrder.add(ConjunctionSymbol.OR);

        // Generate queue
        Compiler compiler = Compiler.create(COMPILER_PARSE_VALUE_FUNCTION,
                COMPILER_PARSE_OPERATOR_FUNCTION);
        AbstractSyntaxTree ast = compiler.parse(ccl);
        ConditionTree tree = (ConditionTree) ast;
        Queue<PostfixNotationSymbol> order = compiler.arrange(tree);

        Assert.assertEquals(expectedOrder, order);
    }

    @Test
    public void testDisjunctionParenthesizedConjunctionPostFix() {
        String ccl = "a = 1 or (b = 2 and c = 3)";

        // Build expected queue
        Queue<PostfixNotationSymbol> expectedOrder = new LinkedList<>();

        KeySymbol key = new KeySymbol("a");
        OperatorSymbol operator = new OperatorSymbol(
                COMPILER_PARSE_OPERATOR_FUNCTION.apply("="));
        ValueSymbol value = new ValueSymbol(
                COMPILER_PARSE_VALUE_FUNCTION.apply("1"));
        ExpressionSymbol expression = ExpressionSymbol.create(key, operator,
                value);
        expectedOrder.add(expression);

        key = new KeySymbol("b");
        operator = new OperatorSymbol(
                COMPILER_PARSE_OPERATOR_FUNCTION.apply("="));
        value = new ValueSymbol(COMPILER_PARSE_VALUE_FUNCTION.apply("2"));
        expression = ExpressionSymbol.create(key, operator, value);
        expectedOrder.add(expression);

        key = new KeySymbol("c");
        operator = new OperatorSymbol(
                COMPILER_PARSE_OPERATOR_FUNCTION.apply("="));
        value = new ValueSymbol(COMPILER_PARSE_VALUE_FUNCTION.apply("3"));
        expression = ExpressionSymbol.create(key, operator, value);
        expectedOrder.add(expression);

        expectedOrder.add(ConjunctionSymbol.AND);
        expectedOrder.add(ConjunctionSymbol.OR);

        // Generate queue
        Compiler compiler = Compiler.create(COMPILER_PARSE_VALUE_FUNCTION,
                COMPILER_PARSE_OPERATOR_FUNCTION);
        AbstractSyntaxTree ast = compiler.parse(ccl);
        ConditionTree tree = (ConditionTree) ast;
        Queue<PostfixNotationSymbol> order = compiler.arrange(tree);

        Assert.assertEquals(expectedOrder, order);
    }

    @Test
    public void testSingleExpressionAbstractSyntaxTree() {
        String ccl = "a = 1";

        // Generate tree
        Compiler compiler = Compiler.create(COMPILER_PARSE_VALUE_FUNCTION,
                COMPILER_PARSE_OPERATOR_FUNCTION);
        AbstractSyntaxTree tree = compiler.parse(ccl);

        // Root node
        Assert.assertTrue(tree instanceof ExpressionTree);
        ExpressionSymbol expression = (ExpressionSymbol) tree.root();
        Assert.assertEquals("a", expression.key().toString());
        Assert.assertEquals("=", expression.operator().toString());
        Assert.assertEquals("1", expression.values().get(0).toString());
    }

    @Test
    public void testSingleBinaryExpressionAbstractSyntaxTree() {
        String ccl = "a >< 1 2";

        // Generate tree
        Compiler compiler = Compiler.create(COMPILER_PARSE_VALUE_FUNCTION,
                COMPILER_PARSE_OPERATOR_FUNCTION);
        AbstractSyntaxTree tree = compiler.parse(ccl);

        // Root node
        Assert.assertTrue(tree instanceof ExpressionTree);
        ExpressionSymbol expression = (ExpressionSymbol) tree.root();
        Assert.assertEquals("a", expression.key().toString());
        Assert.assertEquals("><", expression.operator().toString());
        Assert.assertEquals("1", expression.values().get(0).toString());
        Assert.assertEquals("2", expression.values().get(1).toString());
    }

    @Test
    public void testSingleConjunctionAbstractSyntaxTree() {
        String ccl = "a = 1 and b = 2";

        Compiler compiler = Compiler.create(COMPILER_PARSE_VALUE_FUNCTION,
                COMPILER_PARSE_OPERATOR_FUNCTION);

        AbstractSyntaxTree tree = compiler.parse(ccl);

        // Root node
        Assert.assertTrue(tree instanceof ConjunctionTree);
        ExpressionSymbol rightExpression = (ExpressionSymbol) (((ConjunctionTree) tree)
                .right()).root();
        Assert.assertEquals("b", rightExpression.key().toString());
        Assert.assertEquals("=", rightExpression.operator().toString());
        Assert.assertEquals("2", rightExpression.values().get(0).toString());
    }

    @Test
    public void testSingleDisjunctionAbstractSyntaxTree() {
        String ccl = "a = 1 or b = 2";

        Compiler compiler = Compiler.create(COMPILER_PARSE_VALUE_FUNCTION,
                COMPILER_PARSE_OPERATOR_FUNCTION);

        AbstractSyntaxTree tree = compiler.parse(ccl);

        // Root node
        Assert.assertTrue(tree instanceof OrTree);
        ConjunctionTree rootNode = (ConjunctionTree) tree;

        // Left node
        Assert.assertTrue(rootNode.left() instanceof ExpressionTree);
        ExpressionSymbol leftExpression = (ExpressionSymbol) (rootNode.left())
                .root();
        Assert.assertEquals("a", leftExpression.key().toString());
        Assert.assertEquals("=", leftExpression.operator().toString());
        Assert.assertEquals("1", leftExpression.values().get(0).toString());

        // Right node
        Assert.assertTrue(rootNode.left() instanceof ExpressionTree);
        ExpressionSymbol rightExpression = (ExpressionSymbol) (rootNode.right())
                .root();
        Assert.assertEquals("b", rightExpression.key().toString());
        Assert.assertEquals("=", rightExpression.operator().toString());
        Assert.assertEquals("2", rightExpression.values().get(0).toString());
    }

    @Test
    public void testDoubleConjunctionAbstractSyntaxTree() {
        String ccl = "a = 1 and b = 2 and c = 3";

        Compiler compiler = Compiler.create(COMPILER_PARSE_VALUE_FUNCTION,
                COMPILER_PARSE_OPERATOR_FUNCTION);

        AbstractSyntaxTree tree = compiler.parse(ccl);

        // Root node
        Assert.assertTrue(tree instanceof AndTree);
        ConjunctionTree rootNode = (ConjunctionTree) tree;

        // left node
        Assert.assertTrue(rootNode.left() instanceof AndTree);
        ConjunctionTree leftNode = (ConjunctionTree) rootNode.left();

        // right node
        Assert.assertTrue(rootNode.right() instanceof ExpressionTree);
        ExpressionSymbol rightExpression = (ExpressionSymbol) (rootNode.right())
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

        Compiler compiler = Compiler.create(COMPILER_PARSE_VALUE_FUNCTION,
                COMPILER_PARSE_OPERATOR_FUNCTION);

        AbstractSyntaxTree tree = compiler.parse(ccl);

        // Root node
        Assert.assertTrue(tree instanceof OrTree);
        ConjunctionTree rootNode = (ConjunctionTree) tree;

        // left node
        Assert.assertTrue(rootNode.left() instanceof OrTree);
        ConjunctionTree leftNode = (ConjunctionTree) rootNode.left();

        // right node
        Assert.assertTrue(rootNode.right() instanceof ExpressionTree);
        ExpressionSymbol rightExpression = (ExpressionSymbol) (rootNode.right())
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

        Compiler compiler = Compiler.create(COMPILER_PARSE_VALUE_FUNCTION,
                COMPILER_PARSE_OPERATOR_FUNCTION);

        AbstractSyntaxTree tree = compiler.parse(ccl);

        // Root node
        Assert.assertTrue(tree instanceof OrTree);
        ConjunctionTree rootNode = (ConjunctionTree) tree;

        // left node
        Assert.assertTrue(rootNode.left() instanceof AndTree);
        ConjunctionTree leftNode = (ConjunctionTree) rootNode.left();

        // right node
        Assert.assertTrue(rootNode.right() instanceof ExpressionTree);
        ExpressionSymbol rightExpression = (ExpressionSymbol) (rootNode.right())
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

        Compiler compiler = Compiler.create(COMPILER_PARSE_VALUE_FUNCTION,
                COMPILER_PARSE_OPERATOR_FUNCTION);

        AbstractSyntaxTree tree = compiler.parse(ccl);

        // Root node
        Assert.assertTrue(tree instanceof OrTree);
        ConjunctionTree rootNode = (ConjunctionTree) tree;

        // Right node
        Assert.assertTrue(rootNode.right() instanceof AndTree);
        ConjunctionTree rightNode = (ConjunctionTree) rootNode.right();

        // right node
        Assert.assertTrue(rootNode.left() instanceof ExpressionTree);
        ExpressionSymbol leftExpression = (ExpressionSymbol) (rootNode.left())
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

        Compiler compiler = Compiler.create(COMPILER_PARSE_VALUE_FUNCTION,
                COMPILER_PARSE_OPERATOR_FUNCTION);

        AbstractSyntaxTree ast = compiler.parse(ccl);

        // Root node
        Assert.assertTrue(ast instanceof ConditionTree);
        Assert.assertTrue(ast instanceof AndTree);
        ConjunctionTree tree = (ConjunctionTree) ast;

        // Left node
        Assert.assertTrue(tree.left() instanceof ExpressionTree);
        ExpressionSymbol leftExpression = (ExpressionSymbol) (tree.left())
                .root();
        Assert.assertEquals("a", leftExpression.key().toString());
        Assert.assertEquals("=", leftExpression.operator().toString());
        Assert.assertEquals("1", leftExpression.values().get(0).toString());

        // Right node
        Assert.assertTrue(tree.right() instanceof OrTree);
        ConjunctionTree rightNode = (ConjunctionTree) tree.right();

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
        Compiler compiler = Compiler.create(COMPILER_PARSE_VALUE_FUNCTION,
                COMPILER_PARSE_OPERATOR_FUNCTION);
        AbstractSyntaxTree tree = compiler.parse(ccl, data);

        // Root node
        Assert.assertTrue(tree instanceof ExpressionTree);
        ExpressionSymbol expression = (ExpressionSymbol) tree.root();
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
        Compiler compiler = Compiler.create(COMPILER_PARSE_VALUE_FUNCTION,
                COMPILER_PARSE_OPERATOR_FUNCTION);
        AbstractSyntaxTree tree = compiler.parse(ccl, data);

        // Root node
        Assert.assertTrue(tree instanceof ExpressionTree);
        ExpressionSymbol expression = (ExpressionSymbol) tree.root();
        Assert.assertEquals("name", expression.key().toString());
        Assert.assertEquals("=", expression.operator().toString());
        Assert.assertEquals("$name", expression.values().get(0).toString());
    }

    @Test
    public void testDoubleQuotedValue() {
        String ccl = "name = \"Javier Lores\"";

        // Generate tree
        Compiler compiler = Compiler.create(COMPILER_PARSE_VALUE_FUNCTION,
                COMPILER_PARSE_OPERATOR_FUNCTION);
        AbstractSyntaxTree tree = compiler.parse(ccl);

        // Root node
        Assert.assertTrue(tree instanceof ExpressionTree);
        ExpressionSymbol expression = (ExpressionSymbol) tree.root();
        Assert.assertEquals("name", expression.key().toString());
        Assert.assertEquals("=", expression.operator().toString());
        Assert.assertEquals("\"Javier Lores\"",
                expression.values().get(0).toString());
    }

    @Test
    public void testDoubleRightAndLeftQuotedValue() {
        String ccl = "name = “Javier Lores”";

        // Generate tree
        Compiler compiler = Compiler.create(COMPILER_PARSE_VALUE_FUNCTION,
                COMPILER_PARSE_OPERATOR_FUNCTION);
        AbstractSyntaxTree tree = compiler.parse(ccl);

        // Root node
        Assert.assertTrue(tree instanceof ExpressionTree);
        ExpressionSymbol expression = (ExpressionSymbol) tree.root();
        Assert.assertEquals("name", expression.key().toString());
        Assert.assertEquals("=", expression.operator().toString());
        Assert.assertEquals("\"Javier Lores\"",
                expression.values().get(0).toString());
    }

    @Test
    public void testQuotedValueWithinQuotedString() {
        String ccl = "name = \"Javier \\\"Lores\"";

        // Generate tree
        Compiler compiler = Compiler.create(COMPILER_PARSE_VALUE_FUNCTION,
                COMPILER_PARSE_OPERATOR_FUNCTION);
        AbstractSyntaxTree tree = compiler.parse(ccl);

        // Root node
        Assert.assertTrue(tree instanceof ExpressionTree);
        ExpressionSymbol expression = (ExpressionSymbol) tree.root();
        Assert.assertEquals("name", expression.key().toString());
        Assert.assertEquals("=", expression.operator().toString());
        Assert.assertEquals("'Javier \"Lores'",
                expression.values().get(0).toString());
    }

    @Test
    public void testNonQuoteEscapedValueWithinQuoteString() {
        String ccl = "name = \"Javier \\\"\\@Lores\"";

        // Generate tree
        Compiler compiler = Compiler.create(COMPILER_PARSE_VALUE_FUNCTION,
                COMPILER_PARSE_OPERATOR_FUNCTION);
        AbstractSyntaxTree tree = compiler.parse(ccl);

        // Root node
        Assert.assertTrue(tree instanceof ExpressionTree);
        ExpressionSymbol expression = (ExpressionSymbol) tree.root();
        Assert.assertEquals("name", expression.key().toString());
        Assert.assertEquals("=", expression.operator().toString());
        Assert.assertEquals("'Javier \"\\@Lores'",
                expression.values().get(0).toString());
    }

    @Test
    public void validEscapedLocalResolution() {
        String ccl = "name = \\$name";

        // Generate tree
        Compiler compiler = Compiler.create(COMPILER_PARSE_VALUE_FUNCTION,
                COMPILER_PARSE_OPERATOR_FUNCTION);
        AbstractSyntaxTree tree = compiler.parse(ccl);

        // Root node
        Assert.assertTrue(tree instanceof ExpressionTree);
        ExpressionSymbol expression = (ExpressionSymbol) tree.root();
        Assert.assertEquals("name", expression.key().toString());
        Assert.assertEquals("=", expression.operator().toString());
        Assert.assertEquals("$name", expression.values().get(0).toString());
    }

    @Test
    public void validEscapedImplicitLink() {
        String ccl = "name = \\@name";

        // Generate tree
        Compiler compiler = Compiler.create(COMPILER_PARSE_VALUE_FUNCTION,
                COMPILER_PARSE_OPERATOR_FUNCTION);
        AbstractSyntaxTree tree = compiler.parse(ccl);

        // Root node
        Assert.assertTrue(tree instanceof ExpressionTree);
        ExpressionSymbol expression = (ExpressionSymbol) tree.root();
        Assert.assertEquals("name", expression.key().toString());
        Assert.assertEquals("=", expression.operator().toString());
        Assert.assertEquals("@name", expression.values().get(0).toString());
    }

    @Test
    public void testValidLink() {
        String ccl = "name -> 30";

        // Generate tree
        Compiler compiler = Compiler.create(COMPILER_PARSE_VALUE_FUNCTION,
                COMPILER_PARSE_OPERATOR_FUNCTION);
        AbstractSyntaxTree tree = compiler.parse(ccl);

        // Root node
        Assert.assertTrue(tree instanceof ExpressionTree);
        ExpressionSymbol expression = (ExpressionSymbol) tree.root();
        Assert.assertEquals("name", expression.key().toString());
        Assert.assertEquals("LINKS_TO", expression.operator().toString());
        Assert.assertEquals("30", expression.values().get(0).toString());
    }

    @Test
    public void validOperatorEnum() {
        String ccl = "name LINKS_TO 30";

        // Generate tree
        Compiler compiler = Compiler.create(COMPILER_PARSE_VALUE_FUNCTION,
                COMPILER_PARSE_OPERATOR_FUNCTION);
        AbstractSyntaxTree tree = compiler.parse(ccl);

        // Root node
        Assert.assertTrue(tree instanceof ExpressionTree);
        ExpressionSymbol expression = (ExpressionSymbol) tree.root();
        Assert.assertEquals("name", expression.key().toString());
        Assert.assertEquals("LINKS_TO", expression.operator().toString());
        Assert.assertEquals("30", expression.values().get(0).toString());
    }

    @Test
    public void testNavigationKey() {
        String ccl = "mother.children = 3";

        // Generate tree
        Compiler compiler = Compiler.create(COMPILER_PARSE_VALUE_FUNCTION,
                COMPILER_PARSE_OPERATOR_FUNCTION);
        AbstractSyntaxTree tree = compiler.parse(ccl);

        // Root node
        Assert.assertTrue(tree instanceof ExpressionTree);
        ExpressionSymbol expression = (ExpressionSymbol) tree.root();
        Assert.assertEquals("mother.children", expression.key().toString());
        Assert.assertEquals("=", expression.operator().toString());
        Assert.assertEquals("3", expression.values().get(0).toString());
    }

    @Test
    public void testLongNavigationKey() {
        String ccl = "mother.mother.siblings = 3";

        // Generate tree
        Compiler compiler = Compiler.create(COMPILER_PARSE_VALUE_FUNCTION,
                COMPILER_PARSE_OPERATOR_FUNCTION);
        AbstractSyntaxTree tree = compiler.parse(ccl);

        // Root node
        Assert.assertTrue(tree instanceof ExpressionTree);
        ExpressionSymbol expression = (ExpressionSymbol) tree.root();
        Assert.assertEquals("mother.mother.siblings",
                expression.key().toString());
        Assert.assertEquals("=", expression.operator().toString());
        Assert.assertEquals("3", expression.values().get(0).toString());
    }

    @Test
    public void testNavigationKeyWithContainsOperator() {
        String ccl = "mother.children contains 'foo'";

        // Generate tree
        Compiler compiler = Compiler.create(COMPILER_PARSE_VALUE_FUNCTION,
                COMPILER_PARSE_OPERATOR_FUNCTION);
        AbstractSyntaxTree tree = compiler.parse(ccl);

        // Root node
        Assert.assertTrue(tree instanceof ExpressionTree);
        ExpressionSymbol expression = (ExpressionSymbol) tree.root();
        Assert.assertEquals("mother.children", expression.key().toString());
        Assert.assertEquals("CONTAINS", expression.operator().toString());
        Assert.assertEquals("foo", expression.values().get(0).toString());
    }

    @Test
    public void testLongNavigationKeyWithContainsOperator() {
        String ccl = "mother.mother.siblings contains 'bar'";

        // Generate tree
        Compiler compiler = Compiler.create(COMPILER_PARSE_VALUE_FUNCTION,
                COMPILER_PARSE_OPERATOR_FUNCTION);
        AbstractSyntaxTree tree = compiler.parse(ccl);

        // Root node
        Assert.assertTrue(tree instanceof ExpressionTree);
        ExpressionSymbol expression = (ExpressionSymbol) tree.root();
        Assert.assertEquals("mother.mother.siblings",
                expression.key().toString());
        Assert.assertEquals("CONTAINS", expression.operator().toString());
        Assert.assertEquals("bar", expression.values().get(0).toString());
    }

    @Test
    public void testNavigationKeyWithNotContainsOperator() {
        String ccl = "mother.children not_contains 'foo'";

        // Generate tree
        Compiler compiler = Compiler.create(COMPILER_PARSE_VALUE_FUNCTION,
                COMPILER_PARSE_OPERATOR_FUNCTION);
        AbstractSyntaxTree tree = compiler.parse(ccl);

        // Root node
        Assert.assertTrue(tree instanceof ExpressionTree);
        ExpressionSymbol expression = (ExpressionSymbol) tree.root();
        Assert.assertEquals("mother.children", expression.key().toString());
        Assert.assertEquals("NOT_CONTAINS", expression.operator().toString());
        Assert.assertEquals("foo", expression.values().get(0).toString());
    }

    @Test
    public void testTransitiveNavigationKeyAtStart() {
        String ccl = "children*.name = foo";

        // Generate tree
        Compiler compiler = Compiler.create(COMPILER_PARSE_VALUE_FUNCTION,
                COMPILER_PARSE_OPERATOR_FUNCTION);
        AbstractSyntaxTree tree = compiler.parse(ccl);

        // Root node
        Assert.assertTrue(tree instanceof ExpressionTree);
        ExpressionSymbol expression = (ExpressionSymbol) tree.root();
        Assert.assertTrue(expression.key() instanceof NavigationKeySymbol);
        Assert.assertEquals("children*.name", expression.key().toString());
        Assert.assertEquals("=", expression.operator().toString());
        Assert.assertEquals("foo", expression.values().get(0).toString());
    }

    @Test
    public void testTransitiveNavigationKeyInMiddle() {
        String ccl = "a.b*.c = bar";

        // Generate tree
        Compiler compiler = Compiler.create(COMPILER_PARSE_VALUE_FUNCTION,
                COMPILER_PARSE_OPERATOR_FUNCTION);
        AbstractSyntaxTree tree = compiler.parse(ccl);

        // Root node
        Assert.assertTrue(tree instanceof ExpressionTree);
        ExpressionSymbol expression = (ExpressionSymbol) tree.root();
        Assert.assertTrue(expression.key() instanceof NavigationKeySymbol);
        Assert.assertEquals("a.b*.c", expression.key().toString());
        Assert.assertEquals("=", expression.operator().toString());
        Assert.assertEquals("bar", expression.values().get(0).toString());
    }

    @Test
    public void testTransitiveNavigationKeyAtEnd() {
        String ccl = "a.b.c* = baz";

        // Generate tree
        Compiler compiler = Compiler.create(COMPILER_PARSE_VALUE_FUNCTION,
                COMPILER_PARSE_OPERATOR_FUNCTION);
        AbstractSyntaxTree tree = compiler.parse(ccl);

        // Root node
        Assert.assertTrue(tree instanceof ExpressionTree);
        ExpressionSymbol expression = (ExpressionSymbol) tree.root();
        Assert.assertTrue(expression.key() instanceof NavigationKeySymbol);
        Assert.assertEquals("a.b.c*", expression.key().toString());
        Assert.assertEquals("=", expression.operator().toString());
        Assert.assertEquals("baz", expression.values().get(0).toString());
    }

    @Test
    public void testMultipleTransitiveNavigationKeys() {
        String ccl = "a.b*.c.d*.e = foo";

        // Generate tree
        Compiler compiler = Compiler.create(COMPILER_PARSE_VALUE_FUNCTION,
                COMPILER_PARSE_OPERATOR_FUNCTION);
        AbstractSyntaxTree tree = compiler.parse(ccl);

        // Root node
        Assert.assertTrue(tree instanceof ExpressionTree);
        ExpressionSymbol expression = (ExpressionSymbol) tree.root();
        Assert.assertTrue(expression.key() instanceof NavigationKeySymbol);
        Assert.assertEquals("a.b*.c.d*.e", expression.key().toString());
        Assert.assertEquals("=", expression.operator().toString());
        Assert.assertEquals("foo", expression.values().get(0).toString());
    }

    @Test
    public void testTransitiveNavigationKeyComponents() {
        String ccl = "children*.name = foo";

        // Generate tree
        Compiler compiler = Compiler.create(COMPILER_PARSE_VALUE_FUNCTION,
                COMPILER_PARSE_OPERATOR_FUNCTION);
        AbstractSyntaxTree tree = compiler.parse(ccl);

        // Root node
        ExpressionSymbol expression = (ExpressionSymbol) tree.root();
        NavigationKeySymbol key = (NavigationKeySymbol) expression.key();
        Assert.assertArrayEquals(new String[] { "children*", "name" },
                key.components());
    }

    @Test
    public void testTransitiveNavigationKeyComponentsMultiple() {
        String ccl = "a.b*.c.d*.e = foo";

        // Generate tree
        Compiler compiler = Compiler.create(COMPILER_PARSE_VALUE_FUNCTION,
                COMPILER_PARSE_OPERATOR_FUNCTION);
        AbstractSyntaxTree tree = compiler.parse(ccl);

        // Root node
        ExpressionSymbol expression = (ExpressionSymbol) tree.root();
        NavigationKeySymbol key = (NavigationKeySymbol) expression.key();
        Assert.assertArrayEquals(new String[] { "a", "b*", "c", "d*", "e" },
                key.components());
    }

    @Test
    public void testTransitiveNavigationKeyStops() {
        String ccl = "children*.name = foo";

        // Generate tree
        Compiler compiler = Compiler.create(COMPILER_PARSE_VALUE_FUNCTION,
                COMPILER_PARSE_OPERATOR_FUNCTION);
        AbstractSyntaxTree tree = compiler.parse(ccl);

        // Root node
        ExpressionSymbol expression = (ExpressionSymbol) tree.root();
        NavigationKeySymbol key = (NavigationKeySymbol) expression.key();
        List<NavigationKeyStop> stops = key.stops();
        Assert.assertEquals(2, stops.size());
        Assert.assertEquals(NavigationKeyStop.parse("children*"),
                stops.get(0));
        Assert.assertEquals(NavigationKeyStop.parse("name"), stops.get(1));
    }

    @Test
    public void testTransitiveNavigationKeyStopsMultiple() {
        String ccl = "a.b*.c.d*.e = foo";

        // Generate tree
        Compiler compiler = Compiler.create(COMPILER_PARSE_VALUE_FUNCTION,
                COMPILER_PARSE_OPERATOR_FUNCTION);
        AbstractSyntaxTree tree = compiler.parse(ccl);

        // Root node
        ExpressionSymbol expression = (ExpressionSymbol) tree.root();
        NavigationKeySymbol key = (NavigationKeySymbol) expression.key();
        List<NavigationKeyStop> stops = key.stops();
        Assert.assertEquals(5, stops.size());
        Assert.assertEquals(NavigationKeyStop.parse("a"), stops.get(0));
        Assert.assertEquals(NavigationKeyStop.parse("b*"), stops.get(1));
        Assert.assertEquals(NavigationKeyStop.parse("c"), stops.get(2));
        Assert.assertEquals(NavigationKeyStop.parse("d*"), stops.get(3));
        Assert.assertEquals(NavigationKeyStop.parse("e"), stops.get(4));
    }

    @Test
    public void testNonTransitiveNavigationKeyStops() {
        // Regression: a plain (non-transitive) navigation key should still
        // yield stops(), with all stops marked as not transitive.
        String ccl = "mother.children = 3";

        // Generate tree
        Compiler compiler = Compiler.create(COMPILER_PARSE_VALUE_FUNCTION,
                COMPILER_PARSE_OPERATOR_FUNCTION);
        AbstractSyntaxTree tree = compiler.parse(ccl);

        // Root node
        ExpressionSymbol expression = (ExpressionSymbol) tree.root();
        NavigationKeySymbol key = (NavigationKeySymbol) expression.key();
        List<NavigationKeyStop> stops = key.stops();
        Assert.assertEquals(2, stops.size());
        Assert.assertEquals(NavigationKeyStop.parse("mother"),
                stops.get(0));
        Assert.assertEquals(NavigationKeyStop.parse("children"),
                stops.get(1));
    }

    @Test
    public void testStandaloneTransitiveKeyIsANavigationKey() {
        String ccl = "children* = foo";

        Compiler compiler = Compiler.create(COMPILER_PARSE_VALUE_FUNCTION,
                COMPILER_PARSE_OPERATOR_FUNCTION);
        AbstractSyntaxTree tree = compiler.parse(ccl);

        Assert.assertTrue(tree instanceof ExpressionTree);
        ExpressionSymbol expression = (ExpressionSymbol) tree.root();
        Assert.assertTrue(expression.key() instanceof NavigationKeySymbol);
        Assert.assertEquals("children*", expression.key().toString());
        Assert.assertEquals("=", expression.operator().toString());
        Assert.assertEquals("foo", expression.values().get(0).toString());

        NavigationKeySymbol key = (NavigationKeySymbol) expression.key();
        Assert.assertArrayEquals(new String[] { "children*" },
                key.components());
        List<NavigationKeyStop> stops = key.stops();
        Assert.assertEquals(1, stops.size());
        Assert.assertEquals(NavigationKeyStop.parse("children*"),
                stops.get(0));
        Assert.assertTrue(stops.get(0).isTransitive());
        Assert.assertEquals("children", stops.get(0).key());
    }

    @Test
    public void testPeriodSeparatedValue() {
        String ccl = "mother = a.b.c";

        // Generate tree
        Compiler compiler = Compiler.create(COMPILER_PARSE_VALUE_FUNCTION,
                COMPILER_PARSE_OPERATOR_FUNCTION);
        AbstractSyntaxTree tree = compiler.parse(ccl);

        // Root node
        Assert.assertTrue(tree instanceof ExpressionTree);
        ExpressionSymbol expression = (ExpressionSymbol) tree.root();
        Assert.assertEquals("mother", expression.key().toString());
        Assert.assertEquals("=", expression.operator().toString());
        Assert.assertEquals("a.b.c", expression.values().get(0).toString());
    }

    @Test
    public void testPeriodSeparatedValueWithAsterisk() {
        // Regression: the transitive-navigation change makes a dotted value
        // containing `*` tokenize as PERIOD_SEPARATED_STRING instead of
        // NON_ALPHANUMERIC_AND_ALPHANUMERIC. The resulting value string must be
        // identical so callers that inspect expression.values() see no change.
        String ccl = "mother = a.b*.c";

        // Generate tree
        Compiler compiler = Compiler.create(COMPILER_PARSE_VALUE_FUNCTION,
                COMPILER_PARSE_OPERATOR_FUNCTION);
        AbstractSyntaxTree tree = compiler.parse(ccl);

        // Root node
        Assert.assertTrue(tree instanceof ExpressionTree);
        ExpressionSymbol expression = (ExpressionSymbol) tree.root();
        Assert.assertEquals("mother", expression.key().toString());
        Assert.assertEquals("=", expression.operator().toString());
        Assert.assertEquals("a.b*.c", expression.values().get(0).toString());
    }

    @Test
    public void testTransitiveNavigationKeyWithFunctionKey() {
        // A transitive navigation key piped to an aggregation function must
        // tokenize as PERIOD_SEPARATED_STRING and feed KeyFunction() as the
        // raw key (with the `*` preserved in the FunctionKeySymbol).
        String ccl = "children*.name | avg > 3";

        // Generate tree
        Compiler compiler = Compiler.create(COMPILER_PARSE_VALUE_FUNCTION,
                COMPILER_PARSE_OPERATOR_FUNCTION);
        AbstractSyntaxTree tree = compiler.parse(ccl);

        // Root node
        Assert.assertTrue(tree instanceof ExpressionTree);
        ExpressionSymbol expression = (ExpressionSymbol) tree.root();
        Assert.assertTrue(expression.key() instanceof FunctionKeySymbol);
        FunctionKeySymbol symbol = expression.key();
        Assert.assertEquals("avg", symbol.key().operation());
        Assert.assertEquals("children*.name", symbol.key().key());
        Assert.assertEquals(">", expression.operator().toString());
        Assert.assertEquals("3", expression.values().get(0).toString());
    }

    @Test
    public void testTransitiveNavigationKeyWithIndexFunctionValue() {
        // A transitive navigation key used inside an index function value
        // (e.g. `avg(children*.name)`) must preserve the `*` on the key.
        String ccl = "age > avg(children*.name)";

        // Generate tree
        Compiler compiler = Compiler.create(COMPILER_PARSE_VALUE_FUNCTION,
                COMPILER_PARSE_OPERATOR_FUNCTION);
        AbstractSyntaxTree tree = compiler.parse(ccl);

        // Root node
        Assert.assertTrue(tree instanceof ExpressionTree);
        ExpressionSymbol expression = (ExpressionSymbol) tree.root();
        Assert.assertEquals("age", expression.key().toString());
        Assert.assertEquals(">", expression.operator().toString());
        Assert.assertTrue(
                expression.values().get(0) instanceof FunctionValueSymbol);
        IndexFunction function = (IndexFunction) expression.values().get(0)
                .value();
        Assert.assertEquals("avg", function.operation());
        Assert.assertEquals("children*.name", function.key());
    }

    @Test
    public void testTransitiveNavigationKeyWithKeyRecordsFunctionValue() {
        // A transitive navigation key used inside a KeyRecords function value
        // (e.g. `avg(children*.name, 1)`) must preserve the `*` on the key.
        String ccl = "age > avg(children*.name, 1)";

        // Generate tree
        Compiler compiler = Compiler.create(COMPILER_PARSE_VALUE_FUNCTION,
                COMPILER_PARSE_OPERATOR_FUNCTION);
        AbstractSyntaxTree tree = compiler.parse(ccl);

        // Root node
        Assert.assertTrue(tree instanceof ExpressionTree);
        ExpressionSymbol expression = (ExpressionSymbol) tree.root();
        Assert.assertEquals("age", expression.key().toString());
        Assert.assertEquals(">", expression.operator().toString());
        Assert.assertTrue(
                expression.values().get(0) instanceof FunctionValueSymbol);
        KeyRecordsFunction function = (KeyRecordsFunction) expression.values()
                .get(0).value();
        Assert.assertEquals("avg", function.operation());
        Assert.assertEquals("children*.name", function.key());
        Assert.assertEquals(1, ((List<Long>) function.source()).size());
        Assert.assertEquals((long) 1,
                (long) ((List<Long>) function.source()).get(0));
    }

    @Test
    public void testTransitiveNavigationKeyWithKeyConditionFunctionValue() {
        // A transitive navigation key used inside a KeyCondition function
        // value (e.g. `avg(children*.name, age > 30)`) must preserve the `*`
        // on the key.
        String ccl = "age > avg(children*.name, age > 30)";

        // Generate tree
        Compiler compiler = Compiler.create(COMPILER_PARSE_VALUE_FUNCTION,
                COMPILER_PARSE_OPERATOR_FUNCTION);
        AbstractSyntaxTree tree = compiler.parse(ccl);

        // Root node
        Assert.assertTrue(tree instanceof ExpressionTree);
        ExpressionSymbol expression = (ExpressionSymbol) tree.root();
        Assert.assertEquals("age", expression.key().toString());
        Assert.assertEquals(">", expression.operator().toString());
        Assert.assertTrue(
                expression.values().get(0) instanceof FunctionValueSymbol);
        KeyConditionFunction function = (KeyConditionFunction) expression
                .values().get(0).value();
        Assert.assertEquals("avg", function.operation());
        Assert.assertEquals("children*.name", function.key());
        Assert.assertTrue(function.source() instanceof ExpressionTree);
        ExpressionSymbol inner = (ExpressionSymbol) ((AbstractSyntaxTree) function
                .source()).root();
        Assert.assertEquals("age", inner.key().toString());
        Assert.assertEquals(">", inner.operator().toString());
        Assert.assertEquals("30", inner.values().get(0).toString());
    }

    @Test
    public void testImplicitRecordFunctionAsEvaluationKey() {
        String ccl = "friends | avg > 3";

        // Generate tree
        Compiler compiler = Compiler.create(COMPILER_PARSE_VALUE_FUNCTION,
                COMPILER_PARSE_OPERATOR_FUNCTION);
        AbstractSyntaxTree tree = compiler.parse(ccl);

        // Root node
        Assert.assertTrue(tree instanceof ExpressionTree);
        ExpressionSymbol expression = (ExpressionSymbol) tree.root();
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
        Compiler compiler = Compiler.create(COMPILER_PARSE_VALUE_FUNCTION,
                COMPILER_PARSE_OPERATOR_FUNCTION);
        AbstractSyntaxTree tree = compiler.parse(ccl);

        // Root node
        Assert.assertTrue(tree instanceof ExpressionTree);
        ExpressionSymbol expression = (ExpressionSymbol) tree.root();
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
        Compiler compiler = Compiler.create(COMPILER_PARSE_VALUE_FUNCTION,
                COMPILER_PARSE_OPERATOR_FUNCTION);
        AbstractSyntaxTree tree = compiler.parse(ccl);

        // Root node
        Assert.assertTrue(tree instanceof ExpressionTree);
        ExpressionSymbol expression = (ExpressionSymbol) tree.root();
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
        Assert.assertEquals(1, ((List<Long>) ((KeyRecordsFunction) expression
                .values().get(0).value()).source()).size());
        Assert.assertEquals((long) 1,
                (long) ((List<Long>) ((KeyRecordsFunction) expression.values()
                        .get(0).value()).source()).get(0));
    }

    @Test
    public void testExplicitFunctionWithBetween() {
        String ccl = "age bw avg(age) 1000";

        // Generate tree
        Compiler compiler = Compiler.create(COMPILER_PARSE_VALUE_FUNCTION,
                COMPILER_PARSE_OPERATOR_FUNCTION);
        AbstractSyntaxTree tree = compiler.parse(ccl);

        // Root node
        Assert.assertTrue(tree instanceof ExpressionTree);
        ExpressionSymbol expression = (ExpressionSymbol) tree.root();
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
        Compiler compiler = Compiler.create(COMPILER_PARSE_VALUE_FUNCTION,
                COMPILER_PARSE_OPERATOR_FUNCTION);
        AbstractSyntaxTree tree = compiler.parse(ccl);

        // Root node
        Assert.assertTrue(tree instanceof ExpressionTree);
        ExpressionSymbol expression = (ExpressionSymbol) tree.root();
        Assert.assertEquals("age", expression.key().toString());
        Assert.assertEquals("><", expression.operator().toString());
        Assert.assertTrue(
                expression.values().get(0) instanceof FunctionValueSymbol);
        Assert.assertEquals("avg",
                ((KeyConditionFunction) expression.values().get(0).value())
                        .operation());
        Assert.assertEquals("age",
                ((KeyConditionFunction) expression.values().get(0).value())
                        .key());

        Assert.assertTrue(
                (((KeyConditionFunction) expression.values().get(0).value())
                        .source()) instanceof ExpressionTree);
        Assert.assertEquals("age",
                ((ExpressionSymbol) ((AbstractSyntaxTree) ((KeyConditionFunction) expression
                        .values().get(0).value()).source()).root()).key()
                                .toString());
        Assert.assertEquals(">",
                ((ExpressionSymbol) ((AbstractSyntaxTree) ((KeyConditionFunction) expression
                        .values().get(0).value()).source()).root()).operator()
                                .toString());
        Assert.assertEquals("10",
                ((ExpressionSymbol) ((AbstractSyntaxTree) ((KeyConditionFunction) expression
                        .values().get(0).value()).source()).root()).values()
                                .get(0).toString());

        Assert.assertEquals("1000", expression.values().get(1).toString());
    }

    @Test
    public void testExplicitFunctionWithMultipleRecordsAsEvaluationValue() {
        String ccl = "age > avg(age, 1, 2)";

        // Generate tree
        Compiler compiler = Compiler.create(COMPILER_PARSE_VALUE_FUNCTION,
                COMPILER_PARSE_OPERATOR_FUNCTION);
        AbstractSyntaxTree tree = compiler.parse(ccl);

        // Root node
        Assert.assertTrue(tree instanceof ExpressionTree);
        ExpressionSymbol expression = (ExpressionSymbol) tree.root();
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
        Assert.assertEquals(2, ((List<Long>) ((KeyRecordsFunction) expression
                .values().get(0).value()).source()).size());
        Assert.assertEquals((long) 1,
                (long) ((List<Long>) ((KeyRecordsFunction) expression.values()
                        .get(0).value()).source()).get(0));
        Assert.assertEquals((long) 2,
                (long) ((List<Long>) ((KeyRecordsFunction) expression.values()
                        .get(0).value()).source()).get(1));
    }

    @Test
    public void testExplicitFunctionWithCCLAsEvaluationValue() {
        String ccl = "age > avg(age, age < 30)";

        // Generate tree
        Compiler compiler = Compiler.create(COMPILER_PARSE_VALUE_FUNCTION,
                COMPILER_PARSE_OPERATOR_FUNCTION);
        AbstractSyntaxTree tree = compiler.parse(ccl);

        // Root node
        Assert.assertTrue(tree instanceof ExpressionTree);
        ExpressionSymbol expression = (ExpressionSymbol) tree.root();
        Assert.assertEquals("age", expression.key().toString());
        Assert.assertEquals(">", expression.operator().toString());
        Assert.assertTrue(
                expression.values().get(0) instanceof FunctionValueSymbol);
        Assert.assertEquals("avg",
                ((KeyConditionFunction) expression.values().get(0).value())
                        .operation());
        Assert.assertEquals("age",
                ((KeyConditionFunction) expression.values().get(0).value())
                        .key());

        Assert.assertTrue(
                (((KeyConditionFunction) expression.values().get(0).value())
                        .source()) instanceof ExpressionTree);
        Assert.assertEquals("age",
                ((ExpressionSymbol) ((AbstractSyntaxTree) ((KeyConditionFunction) expression
                        .values().get(0).value()).source()).root()).key()
                                .toString());
        Assert.assertEquals("<",
                ((ExpressionSymbol) ((AbstractSyntaxTree) ((KeyConditionFunction) expression
                        .values().get(0).value()).source()).root()).operator()
                                .toString());
        Assert.assertEquals("30",
                ((ExpressionSymbol) ((AbstractSyntaxTree) ((KeyConditionFunction) expression
                        .values().get(0).value()).source()).root()).values()
                                .get(0).toString());
    }

    @Test
    public void testValidImplicitRecordFunctionAsEvaluationKeyAndExplicitFunctionWithCCLAsEvaluationValue() {
        String ccl = "age | avg > avg(age, age < 30)";

        // Generate tree
        Compiler compiler = Compiler.create(COMPILER_PARSE_VALUE_FUNCTION,
                COMPILER_PARSE_OPERATOR_FUNCTION);
        AbstractSyntaxTree tree = compiler.parse(ccl);

        // Root node
        Assert.assertTrue(tree instanceof ExpressionTree);
        ExpressionSymbol expression = (ExpressionSymbol) tree.root();
        Assert.assertTrue(expression.key() instanceof FunctionKeySymbol);
        Assert.assertEquals("avg",
                ((ImplicitKeyRecordFunction) expression.key().key())
                        .operation());
        Assert.assertEquals(">", expression.operator().toString());
        Assert.assertTrue(
                expression.values().get(0) instanceof FunctionValueSymbol);
        Assert.assertEquals("avg",
                ((KeyConditionFunction) expression.values().get(0).value())
                        .operation());
        Assert.assertEquals("age",
                ((KeyConditionFunction) expression.values().get(0).value())
                        .key());

        Assert.assertTrue(
                (((KeyConditionFunction) expression.values().get(0).value())
                        .source()) instanceof ExpressionTree);
        Assert.assertEquals("age",
                ((ExpressionSymbol) ((AbstractSyntaxTree) ((KeyConditionFunction) expression
                        .values().get(0).value()).source()).root()).key()
                                .toString());
        Assert.assertEquals("<",
                ((ExpressionSymbol) ((AbstractSyntaxTree) ((KeyConditionFunction) expression
                        .values().get(0).value()).source()).root()).operator()
                                .toString());
        Assert.assertEquals("30",
                ((ExpressionSymbol) ((AbstractSyntaxTree) ((KeyConditionFunction) expression
                        .values().get(0).value()).source()).root()).values()
                                .get(0).toString());
    }

    @Test
    public void testPageWithOffset() {
        String input = OFFSET + " 3";

        // Build expected list
        List<Object> expectedTokens = Lists.newArrayList();

        expectedTokens.add(PageSymbol.fromSkip(3));

        // Generate list
        Compiler compiler = Compiler.create(COMPILER_PARSE_VALUE_FUNCTION,
                COMPILER_PARSE_OPERATOR_FUNCTION);
        AbstractSyntaxTree ast = compiler.parse(input);
        List<Symbol> tokens = compiler.tokenize(ast);

        Assert.assertEquals(expectedTokens, tokens);
    }

    @Test
    public void testPageWithOffsetAndLimit() {
        String input = OFFSET + " 3 " + LIMIT + " 1";

        // Build expected list
        List<Object> expectedTokens = Lists.newArrayList();

        expectedTokens.add(PageSymbol.fromSkipLimit(3, 1));

        // Generate list
        Compiler compiler = Compiler.create(COMPILER_PARSE_VALUE_FUNCTION,
                COMPILER_PARSE_OPERATOR_FUNCTION);
        AbstractSyntaxTree ast = compiler.parse(input);
        List<Symbol> tokens = compiler.tokenize(ast);

        Assert.assertEquals(expectedTokens, tokens);
    }

    @Test
    public void testPageWithLimit() {
        String input = LIMIT + " 3";

        // Build expected list
        List<Object> expectedTokens = Lists.newArrayList();

        expectedTokens.add(PageSymbol.fromLimit(3));

        // Generate list
        Compiler compiler = Compiler.create(COMPILER_PARSE_VALUE_FUNCTION,
                COMPILER_PARSE_OPERATOR_FUNCTION);
        AbstractSyntaxTree ast = compiler.parse(input);
        List<Symbol> tokens = compiler.tokenize(ast);

        Assert.assertEquals(expectedTokens, tokens);
    }

    @Test
    public void testPageWithLimitAndOffset() {
        String input = LIMIT + " 1 " + OFFSET + " 3";

        // Build expected list
        List<Object> expectedTokens = Lists.newArrayList();

        expectedTokens.add(PageSymbol.fromSkipLimit(3, 1));

        // Generate list
        Compiler compiler = Compiler.create(COMPILER_PARSE_VALUE_FUNCTION,
                COMPILER_PARSE_OPERATOR_FUNCTION);
        AbstractSyntaxTree ast = compiler.parse(input);
        List<Symbol> tokens = compiler.tokenize(ast);

        Assert.assertEquals(expectedTokens, tokens);
    }

    @Test
    public void testPageWithLimitAndOffsetAST() {
        String input = LIMIT + " 1 " + OFFSET + " 3";

        // Generate queue
        Compiler compiler = Compiler.create(COMPILER_PARSE_VALUE_FUNCTION,
                COMPILER_PARSE_OPERATOR_FUNCTION);
        AbstractSyntaxTree tree = compiler.parse(input);

        // Root node
        Assert.assertTrue(tree instanceof PageTree);

        PageSymbol page = (PageSymbol) tree.root();
        Assert.assertEquals(3, page.offset());
        Assert.assertEquals(Integer.valueOf(1), page.limit());
    }

    @Test
    public void testPageWithOffsetAST() {
        String input = OFFSET + " 3";

        Compiler compiler = Compiler.create(COMPILER_PARSE_VALUE_FUNCTION,
                COMPILER_PARSE_OPERATOR_FUNCTION);
        AbstractSyntaxTree tree = compiler.parse(input);

        Assert.assertTrue(tree instanceof PageTree);
        PageSymbol page = (PageSymbol) tree.root();
        Assert.assertEquals(3, page.offset());
        Assert.assertNull(page.limit());
    }

    @Test
    public void testPageSymbolFactoriesAndFormatting() {
        PageSymbol skip = PageSymbol.fromSkip(10);
        PageSymbol limit = PageSymbol.fromLimit(10);
        PageSymbol skipLimit = PageSymbol.fromSkipLimit(10, 5);

        Assert.assertNull(skip.limit());
        Assert.assertEquals("skip 10", skip.toString());
        Assert.assertEquals(Integer.valueOf(10), limit.limit());
        Assert.assertEquals("limit 10", limit.toString());
        Assert.assertEquals(Integer.valueOf(5), skipLimit.limit());
        Assert.assertEquals("skip 10 limit 5", skipLimit.toString());
        Assert.assertEquals(PageSymbol.fromSkip(10), skip);
        Assert.assertEquals(PageSymbol.fromSkip(10).hashCode(), skip.hashCode());
    }

    @Test
    public void testSingleExpressionTokenizeWithPage() {
        String ccl = "a = 1 " + LIMIT + " 3 " + OFFSET + " 1 ";

        // Build expected queue
        List<Object> expectedTokens = Lists.newArrayList();

        expectedTokens.add(new KeySymbol("a"));
        expectedTokens.add(new OperatorSymbol(
                COMPILER_PARSE_OPERATOR_FUNCTION.apply("=")));
        expectedTokens
                .add(new ValueSymbol(COMPILER_PARSE_VALUE_FUNCTION.apply("1")));
        expectedTokens.add(PageSymbol.fromSkipLimit(1, 3));

        // Generate queue
        Compiler compiler = Compiler.create(COMPILER_PARSE_VALUE_FUNCTION,
                COMPILER_PARSE_OPERATOR_FUNCTION);
        AbstractSyntaxTree ast = compiler.parse(ccl);
        List<Symbol> tokens = compiler.tokenize(ast);

        Assert.assertEquals(expectedTokens, tokens);
    }

    @Test
    public void testSingleExpressionASTWithPage() {
        String ccl = "a = 1 " + LIMIT + " 1 " + OFFSET + " 3 ";

        // Generate queue
        Compiler compiler = Compiler.create(COMPILER_PARSE_VALUE_FUNCTION,
                COMPILER_PARSE_OPERATOR_FUNCTION);
        AbstractSyntaxTree tree = compiler.parse(ccl);

        // Root node
        Assert.assertTrue(tree instanceof CommandTree);
        CommandTree rootNode = (CommandTree) tree;

        Assert.assertTrue(rootNode.conditionTree() != null);
        ConditionTree conditionTree = rootNode.conditionTree();

        Assert.assertTrue(conditionTree instanceof ExpressionTree);
        ExpressionSymbol expression = (ExpressionSymbol) conditionTree.root();
        Assert.assertEquals("a", expression.key().toString());
        Assert.assertEquals("=", expression.operator().toString());
        Assert.assertEquals("1", expression.values().get(0).toString());

        // Page Node
        Assert.assertTrue(((CommandTree) tree).pageTree() != null);
        PageSymbol page = (PageSymbol) ((CommandTree) tree).pageTree().root();
        Assert.assertEquals(3, page.offset());
        Assert.assertEquals(Integer.valueOf(1), page.limit());
    }

    @Test
    public void testJsonReservedIdentifier() {
        String ccl = "$id$ != 40";

        // Generate tree
        Compiler compiler = Compiler.create(COMPILER_PARSE_VALUE_FUNCTION,
                COMPILER_PARSE_OPERATOR_FUNCTION);
        AbstractSyntaxTree tree = compiler.parse(ccl);

        // Root node
        Assert.assertTrue(tree instanceof ExpressionTree);
        ExpressionSymbol expression = (ExpressionSymbol) tree.root();
        Assert.assertEquals("$id$", expression.key().toString());
        Assert.assertEquals("!=", expression.operator().toString());
        Assert.assertEquals("40", expression.values().get(0).toString());
    }

    @Test
    public void testOrderKey() {
        String input = ORDER + " age";

        // Build expected list
        List<Object> expectedTokens = Lists.newArrayList();

        OrderSymbol order = new OrderSymbol();
        order.add(new OrderComponentSymbol(new KeySymbol("age"),
                DirectionSymbol.ASCENDING));
        expectedTokens.add(order);

        // Generate list
        Compiler compiler = Compiler.create(COMPILER_PARSE_VALUE_FUNCTION,
                COMPILER_PARSE_OPERATOR_FUNCTION);
        AbstractSyntaxTree ast = compiler.parse(input);
        List<Symbol> tokens = compiler.tokenize(ast);

        Assert.assertEquals(expectedTokens, tokens);
    }

    @Test
    public void testOrderKeyAscendingSymbol() {
        String input = ORDER + " < age";

        // Build expected list
        List<Object> expectedTokens = Lists.newArrayList();

        OrderSymbol order = new OrderSymbol();
        order.add(new OrderComponentSymbol(new KeySymbol("age"),
                DirectionSymbol.ASCENDING));
        expectedTokens.add(order);

        // Generate list
        Compiler compiler = Compiler.create(COMPILER_PARSE_VALUE_FUNCTION,
                COMPILER_PARSE_OPERATOR_FUNCTION);
        AbstractSyntaxTree ast = compiler.parse(input);
        List<Symbol> tokens = compiler.tokenize(ast);

        Assert.assertEquals(expectedTokens, tokens);
    }

    @Test
    public void testOrderKeyAscendingWord() {
        String input = ORDER + " age asc";

        // Build expected list
        List<Object> expectedTokens = Lists.newArrayList();

        OrderSymbol order = new OrderSymbol();
        order.add(new OrderComponentSymbol(new KeySymbol("age"),
                DirectionSymbol.ASCENDING));
        expectedTokens.add(order);

        // Generate list
        Compiler compiler = Compiler.create(COMPILER_PARSE_VALUE_FUNCTION,
                COMPILER_PARSE_OPERATOR_FUNCTION);
        AbstractSyntaxTree ast = compiler.parse(input);
        List<Symbol> tokens = compiler.tokenize(ast);

        Assert.assertEquals(expectedTokens, tokens);
    }

    @Test
    public void testOrderKeyDescendingSymbol() {
        String input = ORDER + " > age";

        // Build expected list
        List<Object> expectedTokens = Lists.newArrayList();

        OrderSymbol order = new OrderSymbol();
        order.add(new OrderComponentSymbol(new KeySymbol("age"),
                DirectionSymbol.DESCENDING));
        expectedTokens.add(order);

        // Generate list
        Compiler compiler = Compiler.create(COMPILER_PARSE_VALUE_FUNCTION,
                COMPILER_PARSE_OPERATOR_FUNCTION);
        AbstractSyntaxTree ast = compiler.parse(input);
        List<Symbol> tokens = compiler.tokenize(ast);

        Assert.assertEquals(expectedTokens, tokens);
    }

    @Test
    public void testOrderKeyDescendingWord() {
        String input = ORDER + " age desc";

        // Build expected list
        List<Object> expectedTokens = Lists.newArrayList();

        OrderSymbol order = new OrderSymbol();
        order.add(new OrderComponentSymbol(new KeySymbol("age"),
                DirectionSymbol.DESCENDING));
        expectedTokens.add(order);

        // Generate list
        Compiler compiler = Compiler.create(COMPILER_PARSE_VALUE_FUNCTION,
                COMPILER_PARSE_OPERATOR_FUNCTION);
        AbstractSyntaxTree ast = compiler.parse(input);
        List<Symbol> tokens = compiler.tokenize(ast);

        Assert.assertEquals(expectedTokens, tokens);
    }

    @Test
    public void testOrderKeyWithNumberTimestamp() {
        String input = ORDER + " age at " + String.valueOf(122L);

        // Build expected list
        List<Object> expectedTokens = Lists.newArrayList();

        OrderSymbol order = new OrderSymbol();
        order.add(new OrderComponentSymbol(new KeySymbol("age"),
                new TimestampSymbol(122L), DirectionSymbol.ASCENDING));
        expectedTokens.add(order);

        // Generate list
        Compiler compiler = Compiler.create(COMPILER_PARSE_VALUE_FUNCTION,
                COMPILER_PARSE_OPERATOR_FUNCTION);
        AbstractSyntaxTree ast = compiler.parse(input);
        List<Symbol> tokens = compiler.tokenize(ast);

        Assert.assertEquals(expectedTokens, tokens);
    }

    @Test
    public void testOrderKeyWithStringTimestamp() {
        String input = ORDER + " age during \"1992-10-02\"";

        // Build expected list
        List<Object> expectedTokens = Lists.newArrayList();

        OrderSymbol order = new OrderSymbol();
        order.add(new OrderComponentSymbol(new KeySymbol("age"),
                new TimestampSymbol(NaturalLanguage.parseMicros("1992-10-02"),
                        TimeUnit.DAYS),
                DirectionSymbol.ASCENDING));
        expectedTokens.add(order);

        // Generate list
        Compiler compiler = Compiler.create(COMPILER_PARSE_VALUE_FUNCTION,
                COMPILER_PARSE_OPERATOR_FUNCTION);
        AbstractSyntaxTree ast = compiler.parse(input);
        List<Symbol> tokens = compiler.tokenize(ast);

        Assert.assertEquals(expectedTokens, tokens);
    }

    @Test
    public void testOrderKeyWithNumberTimestampAscending() {
        String input = ORDER + " < age on " + String.valueOf(122L);

        // Build expected list
        List<Object> expectedTokens = Lists.newArrayList();

        OrderSymbol order = new OrderSymbol();
        order.add(new OrderComponentSymbol(new KeySymbol("age"),
                new TimestampSymbol(122L), DirectionSymbol.ASCENDING));
        expectedTokens.add(order);

        // Generate list
        Compiler compiler = Compiler.create(COMPILER_PARSE_VALUE_FUNCTION,
                COMPILER_PARSE_OPERATOR_FUNCTION);
        AbstractSyntaxTree ast = compiler.parse(input);
        List<Symbol> tokens = compiler.tokenize(ast);

        Assert.assertEquals(expectedTokens, tokens);
    }

    @Test
    public void testOrderKeyWithStringTimestampAscending() {
        String input = ORDER + " < age during \"1992-10-02\"";

        // Build expected list
        List<Object> expectedTokens = Lists.newArrayList();

        OrderSymbol order = new OrderSymbol();
        order.add(new OrderComponentSymbol(new KeySymbol("age"),
                new TimestampSymbol(NaturalLanguage.parseMicros("1992-10-02"),
                        TimeUnit.DAYS),
                DirectionSymbol.ASCENDING));
        expectedTokens.add(order);

        // Generate list
        Compiler compiler = Compiler.create(COMPILER_PARSE_VALUE_FUNCTION,
                COMPILER_PARSE_OPERATOR_FUNCTION);
        AbstractSyntaxTree ast = compiler.parse(input);
        List<Symbol> tokens = compiler.tokenize(ast);

        Assert.assertEquals(expectedTokens, tokens);
    }

    @Test
    public void testOrderKeyWithNumberTimestampDescending() {
        String input = ORDER + " > age during " + String.valueOf(122L);

        // Build expected list
        List<Object> expectedTokens = Lists.newArrayList();

        OrderSymbol order = new OrderSymbol();
        order.add(new OrderComponentSymbol(new KeySymbol("age"),
                new TimestampSymbol(122L), DirectionSymbol.DESCENDING));
        expectedTokens.add(order);

        // Generate list
        Compiler compiler = Compiler.create(COMPILER_PARSE_VALUE_FUNCTION,
                COMPILER_PARSE_OPERATOR_FUNCTION);
        AbstractSyntaxTree ast = compiler.parse(input);
        List<Symbol> tokens = compiler.tokenize(ast);

        Assert.assertEquals(expectedTokens, tokens);
    }

    @Test
    public void testOrderKeyWithStringTimestampDescending() {
        String input = ORDER + " > age at 1992-10-02";

        // Build expected list
        List<Object> expectedTokens = Lists.newArrayList();

        OrderSymbol order = new OrderSymbol();
        order.add(new OrderComponentSymbol(new KeySymbol("age"),
                new TimestampSymbol(NaturalLanguage.parseMicros("1992-10-02"),
                        TimeUnit.DAYS),
                DirectionSymbol.DESCENDING));
        expectedTokens.add(order);

        // Generate list
        Compiler compiler = Compiler.create(COMPILER_PARSE_VALUE_FUNCTION,
                COMPILER_PARSE_OPERATOR_FUNCTION);
        AbstractSyntaxTree ast = compiler.parse(input);
        List<Symbol> tokens = compiler.tokenize(ast);

        Assert.assertEquals(expectedTokens, tokens);
    }

    @Test
    public void testOrderMultipleKeys() {
        String input = ORDER + " age, salary";

        // Build expected list
        List<Object> expectedTokens = Lists.newArrayList();

        OrderSymbol order = new OrderSymbol();
        order.add(new OrderComponentSymbol(new KeySymbol("age"),
                DirectionSymbol.ASCENDING));
        order.add(new OrderComponentSymbol(new KeySymbol("salary"),
                DirectionSymbol.ASCENDING));
        expectedTokens.add(order);

        // Generate list
        Compiler compiler = Compiler.create(COMPILER_PARSE_VALUE_FUNCTION,
                COMPILER_PARSE_OPERATOR_FUNCTION);
        AbstractSyntaxTree ast = compiler.parse(input);
        List<Symbol> tokens = compiler.tokenize(ast);

        Assert.assertEquals(expectedTokens, tokens);
    }

    @Test
    public void testOrderMultipleKeysWithDirectional() {
        String input = ORDER + " age asc, salary desc";

        // Build expected list
        List<Object> expectedTokens = Lists.newArrayList();

        OrderSymbol order = new OrderSymbol();
        order.add(new OrderComponentSymbol(new KeySymbol("age"),
                DirectionSymbol.ASCENDING));
        order.add(new OrderComponentSymbol(new KeySymbol("salary"),
                DirectionSymbol.DESCENDING));
        expectedTokens.add(order);

        // Generate list
        Compiler compiler = Compiler.create(COMPILER_PARSE_VALUE_FUNCTION,
                COMPILER_PARSE_OPERATOR_FUNCTION);
        AbstractSyntaxTree ast = compiler.parse(input);
        List<Symbol> tokens = compiler.tokenize(ast);

        Assert.assertEquals(expectedTokens, tokens);
    }

    @Test
    public void testOrderMultipleKeysWithDirectionalAST() {
        String input = ORDER + " < age, > salary";

        OrderSymbol expectedOrder = new OrderSymbol();
        expectedOrder.add(new OrderComponentSymbol(new KeySymbol("age"),
                DirectionSymbol.ASCENDING));
        expectedOrder.add(new OrderComponentSymbol(new KeySymbol("salary"),
                DirectionSymbol.DESCENDING));

        // Generate tree
        Compiler compiler = Compiler.create(COMPILER_PARSE_VALUE_FUNCTION,
                COMPILER_PARSE_OPERATOR_FUNCTION);
        AbstractSyntaxTree ast = compiler.parse(input);

        // Root node
        Assert.assertTrue(ast instanceof OrderTree);

        // Order Node
        OrderSymbol order = (OrderSymbol) ((OrderTree) ast).root();
        Assert.assertEquals(order, expectedOrder);
    }

    @Test
    public void testOrderSingleExpressionWithOrderTokenize() {
        String ccl = "a = 1 " + ORDER + " a";

        // Build expected queue
        List<Object> expectedTokens = Lists.newArrayList();

        expectedTokens.add(new KeySymbol("a"));
        expectedTokens.add(new OperatorSymbol(
                COMPILER_PARSE_OPERATOR_FUNCTION.apply("=")));
        expectedTokens
                .add(new ValueSymbol(COMPILER_PARSE_VALUE_FUNCTION.apply("1")));

        OrderSymbol order = new OrderSymbol();
        order.add(new OrderComponentSymbol(new KeySymbol("a"),
                DirectionSymbol.ASCENDING));
        expectedTokens.add(order);

        // Generate queue
        Compiler compiler = Compiler.create(COMPILER_PARSE_VALUE_FUNCTION,
                COMPILER_PARSE_OPERATOR_FUNCTION);
        AbstractSyntaxTree ast = compiler.parse(ccl);
        List<Symbol> tokens = compiler.tokenize(ast);

        Assert.assertEquals(expectedTokens, tokens);
    }

    @Test
    public void testOrderSingleExpressionWithOrderAbstractSyntaxTree() {
        String ccl = "a = 1 " + ORDER + " a";

        OrderSymbol expectedOrder = new OrderSymbol();
        expectedOrder.add(new OrderComponentSymbol(new KeySymbol("a"),
                DirectionSymbol.ASCENDING));

        // Generate tree
        Compiler compiler = Compiler.create(COMPILER_PARSE_VALUE_FUNCTION,
                COMPILER_PARSE_OPERATOR_FUNCTION);
        AbstractSyntaxTree tree = compiler.parse(ccl);

        // Root node
        Assert.assertTrue(tree instanceof CommandTree);
        CommandTree rootNode = (CommandTree) tree;

        Assert.assertTrue(rootNode.conditionTree() != null);
        ConditionTree conditionTree = rootNode.conditionTree();

        Assert.assertTrue(conditionTree instanceof ExpressionTree);
        ExpressionSymbol expression = (ExpressionSymbol) conditionTree.root();
        Assert.assertEquals("a", expression.key().toString());
        Assert.assertEquals("=", expression.operator().toString());
        Assert.assertEquals("1", expression.values().get(0).toString());

        // Order Node
        Assert.assertTrue(((CommandTree) tree).orderTree() != null);
        OrderSymbol order = (OrderSymbol) ((CommandTree) tree).orderTree()
                .root();
        Assert.assertEquals(order, expectedOrder);
    }

    @Test
    public void testOrderSingleExpressionWithOrderAndPageAbstractSyntaxTree() {
        String ccl = "a = 1 " + ORDER + " a " + LIMIT + " 1 " + OFFSET + " 3";

        OrderSymbol expectedOrder = new OrderSymbol();
        expectedOrder.add(new OrderComponentSymbol(new KeySymbol("a"),
                DirectionSymbol.ASCENDING));

        // Generate tree
        Compiler compiler = Compiler.create(COMPILER_PARSE_VALUE_FUNCTION,
                COMPILER_PARSE_OPERATOR_FUNCTION);
        AbstractSyntaxTree tree = compiler.parse(ccl);

        // Root node
        Assert.assertTrue(tree instanceof CommandTree);
        CommandTree rootNode = (CommandTree) tree;

        Assert.assertTrue(rootNode.conditionTree() != null);
        ConditionTree conditionTree = rootNode.conditionTree();

        Assert.assertTrue(conditionTree instanceof ExpressionTree);
        ExpressionSymbol expression = (ExpressionSymbol) conditionTree.root();
        Assert.assertEquals("a", expression.key().toString());
        Assert.assertEquals("=", expression.operator().toString());
        Assert.assertEquals("1", expression.values().get(0).toString());

        // Order Node
        Assert.assertTrue(((CommandTree) tree).orderTree() != null);
        OrderSymbol order = (OrderSymbol) ((CommandTree) tree).orderTree()
                .root();
        Assert.assertEquals(order, expectedOrder);

        // Page Node
        Assert.assertTrue(((CommandTree) tree).pageTree() != null);
        PageSymbol page = (PageSymbol) ((CommandTree) tree).pageTree().root();
        Assert.assertEquals(3, page.offset());
        Assert.assertEquals(Integer.valueOf(1), page.limit());
    }

    @Test
    public void testImplicitKeyRecordFunctionTokenize() {
        String ccl = "age | avg";

        // Build expected queue
        List<Object> expectedTokens = Lists.newArrayList();

        expectedTokens.add(new FunctionKeySymbol(
                new ImplicitKeyRecordFunction("avg", "age")));

        // Generate queue
        Compiler compiler = Compiler.create(COMPILER_PARSE_VALUE_FUNCTION,
                COMPILER_PARSE_OPERATOR_FUNCTION);
        AbstractSyntaxTree ast = compiler.parse(ccl);
        List<Symbol> tokens = compiler.tokenize(ast);

        Assert.assertEquals(expectedTokens, tokens);
    }

    @Test
    public void testImplicitKeyRecordFunctionAbstractSyntaxTree() {
        String ccl = "age | avg";

        // Generate tree
        Compiler compiler = Compiler.create(COMPILER_PARSE_VALUE_FUNCTION,
                COMPILER_PARSE_OPERATOR_FUNCTION);
        AbstractSyntaxTree tree = compiler.parse(ccl);

        // Root node
        Assert.assertTrue(tree instanceof FunctionTree);

        FunctionTokenSymbol symbol = (FunctionTokenSymbol) tree.root();
        Assert.assertEquals("age", symbol.function().key());
        Assert.assertEquals("avg", symbol.function().operation());
    }

    @Test
    public void testIndexFunctionAbstractSyntaxTree() {
        String ccl = "avg(age)";

        // Generate tree
        Compiler compiler = Compiler.create(COMPILER_PARSE_VALUE_FUNCTION,
                COMPILER_PARSE_OPERATOR_FUNCTION);
        AbstractSyntaxTree tree = compiler.parse(ccl);

        // Root node
        Assert.assertTrue(tree instanceof FunctionTree);

        FunctionTokenSymbol symbol = (FunctionTokenSymbol) tree.root();
        Assert.assertEquals(IndexFunction.class, symbol.function().getClass());
        Assert.assertEquals("age", symbol.function().key());
        Assert.assertEquals("avg", symbol.function().operation());
    }

    @Test
    public void testKeyCclFunctionAbstractSyntaxTree() {
        String ccl = "avg(age, age > 3)";

        // Generate tree
        Compiler compiler = Compiler.create(COMPILER_PARSE_VALUE_FUNCTION,
                COMPILER_PARSE_OPERATOR_FUNCTION);
        AbstractSyntaxTree tree = compiler.parse(ccl);

        // Root node
        Assert.assertTrue(tree instanceof FunctionTree);
        FunctionTokenSymbol symbol = (FunctionTokenSymbol) tree.root();

        Assert.assertEquals("avg",
                ((KeyConditionFunction) symbol.function()).operation());
        Assert.assertEquals("age",
                ((KeyConditionFunction) symbol.function()).key());

        Assert.assertTrue((((KeyConditionFunction) symbol.function())
                .source()) instanceof ExpressionTree);
        Assert.assertEquals("age",
                ((ExpressionSymbol) ((AbstractSyntaxTree) ((KeyConditionFunction) symbol
                        .function()).source()).root()).key().toString());
        Assert.assertEquals(">",
                ((ExpressionSymbol) ((AbstractSyntaxTree) ((KeyConditionFunction) symbol
                        .function()).source()).root()).operator().toString());
        Assert.assertEquals("3",
                ((ExpressionSymbol) ((AbstractSyntaxTree) ((KeyConditionFunction) symbol
                        .function()).source()).root()).values().get(0)
                                .toString());
    }

    @Test
    public void testKeyCclFunctionWithTimestampAbstractSyntaxTree() {
        String ccl = "avg(age, age > 3, at 1992-10-02)";

        // Generate tree
        Compiler compiler = Compiler.create(COMPILER_PARSE_VALUE_FUNCTION,
                COMPILER_PARSE_OPERATOR_FUNCTION);
        AbstractSyntaxTree tree = compiler.parse(ccl);

        // Root node
        Assert.assertTrue(tree instanceof FunctionTree);
        FunctionTokenSymbol symbol = (FunctionTokenSymbol) tree.root();

        Assert.assertEquals("avg",
                ((KeyConditionFunction) symbol.function()).operation());
        Assert.assertEquals("age",
                ((KeyConditionFunction) symbol.function()).key());

        Assert.assertTrue((((KeyConditionFunction) symbol.function())
                .source()) instanceof ExpressionTree);
        Assert.assertEquals("age",
                ((ExpressionSymbol) ((AbstractSyntaxTree) ((KeyConditionFunction) symbol
                        .function()).source()).root()).key().toString());
        Assert.assertEquals(">",
                ((ExpressionSymbol) ((AbstractSyntaxTree) ((KeyConditionFunction) symbol
                        .function()).source()).root()).operator().toString());
        Assert.assertEquals("3",
                ((ExpressionSymbol) ((AbstractSyntaxTree) ((KeyConditionFunction) symbol
                        .function()).source()).root()).values().get(0)
                                .toString());
        Assert.assertEquals(
                TimeUnit.DAYS.convert(
                        ((KeyConditionFunction) symbol.function()).timestamp(),
                        TimeUnit.MICROSECONDS),
                TimeUnit.DAYS.convert(NaturalLanguage.parseMicros("1992-10-02"),
                        TimeUnit.MICROSECONDS));
    }

    @Test
    public void testKeyRecordsFunctionAbstractSyntaxTree() {
        String ccl = "avg(age, 1)";

        // Generate tree
        Compiler compiler = Compiler.create(COMPILER_PARSE_VALUE_FUNCTION,
                COMPILER_PARSE_OPERATOR_FUNCTION);
        AbstractSyntaxTree tree = compiler.parse(ccl);

        // Root node
        Assert.assertTrue(tree instanceof FunctionTree);
        FunctionTokenSymbol symbol = (FunctionTokenSymbol) tree.root();

        Assert.assertEquals("avg",
                ((KeyRecordsFunction) symbol.function()).operation());
        Assert.assertEquals("age",
                ((KeyRecordsFunction) symbol.function()).key());

        Assert.assertEquals(1,
                ((List<Long>) ((KeyRecordsFunction) symbol.function()).source())
                        .size());
        Assert.assertEquals((long) 1,
                (long) ((List<Long>) ((KeyRecordsFunction) symbol.function())
                        .source()).get(0));
    }

    @Test
    public void testKeyMultiRecordsFunctionAbstractSyntaxTree() {
        String ccl = "avg(age, 1,2,3,5,11)";

        // Generate tree
        Compiler compiler = Compiler.create(COMPILER_PARSE_VALUE_FUNCTION,
                COMPILER_PARSE_OPERATOR_FUNCTION);
        AbstractSyntaxTree tree = compiler.parse(ccl);

        // Root node
        Assert.assertTrue(tree instanceof FunctionTree);
        FunctionTokenSymbol symbol = (FunctionTokenSymbol) tree.root();

        Assert.assertEquals("avg",
                ((KeyRecordsFunction) symbol.function()).operation());
        Assert.assertEquals("age",
                ((KeyRecordsFunction) symbol.function()).key());

        Assert.assertEquals(5,
                ((List<Long>) ((KeyRecordsFunction) symbol.function()).source())
                        .size());
        Assert.assertEquals((long) 1,
                (long) ((List<Long>) ((KeyRecordsFunction) symbol.function())
                        .source()).get(0));
    }

    @Test
    public void testKeyMultiRecordsFunctionWithTimestampAbstractSyntaxTree() {
        String ccl = "avg(age, [1,2,3,5,11], at 1992-10-02)";

        // Generate tree
        Compiler compiler = Compiler.create(COMPILER_PARSE_VALUE_FUNCTION,
                COMPILER_PARSE_OPERATOR_FUNCTION);
        AbstractSyntaxTree tree = compiler.parse(ccl);

        // Root node
        Assert.assertTrue(tree instanceof FunctionTree);
        FunctionTokenSymbol symbol = (FunctionTokenSymbol) tree.root();

        Assert.assertEquals("avg",
                ((KeyRecordsFunction) symbol.function()).operation());
        Assert.assertEquals("age",
                ((KeyRecordsFunction) symbol.function()).key());

        Assert.assertEquals(5,
                ((List<Long>) ((KeyRecordsFunction) symbol.function()).source())
                        .size());
        Assert.assertEquals((long) 1,
                (long) ((List<Long>) ((KeyRecordsFunction) symbol.function())
                        .source()).get(0));
        Assert.assertEquals(
                TimeUnit.DAYS.convert(
                        ((KeyRecordsFunction) symbol.function()).timestamp(),
                        TimeUnit.MICROSECONDS),
                TimeUnit.DAYS.convert(NaturalLanguage.parseMicros("1992-10-02"),
                        TimeUnit.MICROSECONDS));
    }

    @Test
    public void testIndexFunctionWithTimestampAbstractSyntaxTree() {
        String ccl = "avg(age, at 1992-10-02)";

        // Generate tree
        Compiler compiler = Compiler.create(COMPILER_PARSE_VALUE_FUNCTION,
                COMPILER_PARSE_OPERATOR_FUNCTION);
        AbstractSyntaxTree tree = compiler.parse(ccl);

        // Root node
        Assert.assertTrue(tree instanceof FunctionTree);
        FunctionTokenSymbol symbol = (FunctionTokenSymbol) tree.root();

        Assert.assertEquals("avg",
                ((IndexFunction) symbol.function()).operation());
        Assert.assertEquals("age", ((IndexFunction) symbol.function()).key());

        Assert.assertEquals(
                TimeUnit.DAYS.convert(
                        ((IndexFunction) symbol.function()).timestamp(),
                        TimeUnit.MICROSECONDS),
                TimeUnit.DAYS.convert(NaturalLanguage.parseMicros("1992-10-02"),
                        TimeUnit.MICROSECONDS));
    }

    @Test
    public void testLinkCommand() {
        String ccl = "link friends from 1 to 2";

        // Generate tree
        Compiler compiler = Compiler.create(COMPILER_PARSE_VALUE_FUNCTION,
                COMPILER_PARSE_OPERATOR_FUNCTION);
        AbstractSyntaxTree tree = compiler.parse(ccl);

        // Root node
        Assert.assertTrue(tree instanceof CommandTree);
        CommandTree rootNode = (CommandTree) tree;

        LinkSymbol linkSymbol = (LinkSymbol) rootNode.root();
        Assert.assertEquals("friends", linkSymbol.key().toString());
        Assert.assertEquals(1L, linkSymbol.source());
        Assert.assertTrue(linkSymbol.destinations().contains(2L));
    }

    @Test
    public void testVerifyOrSetCommandTokenize() {
        String ccl = "verifyOrSet name as \"John Doe\" in 1";

        // Generate queue
        Compiler compiler = Compiler.create(COMPILER_PARSE_VALUE_FUNCTION,
                COMPILER_PARSE_OPERATOR_FUNCTION);
        AbstractSyntaxTree ast = compiler.parse(ccl);

        // Verify the command symbol
        CommandTree commandTree = (CommandTree) ast;
        VerifyOrSetSymbol verifyOrSetSymbol = (VerifyOrSetSymbol) commandTree.root();

        Assert.assertEquals("name", verifyOrSetSymbol.key().toString());
        Assert.assertEquals("\"John Doe\"", verifyOrSetSymbol.value().toString());
        Assert.assertEquals(1L, verifyOrSetSymbol.record());
        Assert.assertEquals("VERIFY_OR_SET", verifyOrSetSymbol.type());
    }

    @Test
    public void testChronicleCommand() {
        String ccl = "chronicle location in 5 from \"2024-01-01\" to \"2024-02-01\"";

        // Generate tree
        Compiler compiler = Compiler.create(COMPILER_PARSE_VALUE_FUNCTION,
                COMPILER_PARSE_OPERATOR_FUNCTION);
        AbstractSyntaxTree tree = compiler.parse(ccl);

        // Root node
        Assert.assertTrue(tree instanceof CommandTree);
        CommandTree rootNode = (CommandTree) tree;

        ChronicleSymbol chronicleSymbol = (ChronicleSymbol) rootNode.root();
        Assert.assertEquals("location", chronicleSymbol.key().toString());
        Assert.assertEquals(5L, chronicleSymbol.record());

        // Validate timestamps
        Assert.assertEquals(
                TimeUnit.DAYS.convert(
                        chronicleSymbol.start().timestamp(),
                        TimeUnit.MICROSECONDS),
                TimeUnit.DAYS.convert(
                        NaturalLanguage.parseMicros("2024-01-01"),
                        TimeUnit.MICROSECONDS)
        );

        Assert.assertEquals(
                TimeUnit.DAYS.convert(
                        chronicleSymbol.end().timestamp(),
                        TimeUnit.MICROSECONDS),
                TimeUnit.DAYS.convert(
                        NaturalLanguage.parseMicros("2024-02-01"),
                        TimeUnit.MICROSECONDS)
        );
    }

    @Test
    public void testSearchCommandTokenize() {
        String ccl = "search email for \"john@example.com\"";

        // Generate queue
        Compiler compiler = Compiler.create(COMPILER_PARSE_VALUE_FUNCTION,
                COMPILER_PARSE_OPERATOR_FUNCTION);
        AbstractSyntaxTree ast = compiler.parse(ccl);
        compiler.tokenize(ast);

        // Verify the command symbol
        CommandTree commandTree = (CommandTree) ast;
        SearchSymbol searchSymbol = (SearchSymbol) commandTree.root();

        Assert.assertEquals("email", searchSymbol.key().toString());
        Assert.assertEquals("john@example.com", searchSymbol.query());
        Assert.assertEquals("SEARCH", searchSymbol.type());
    }

    @Test
    public void testGetWithOrderTimestampAndPage() {
        String ccl = "get [name, age] where salary > 50000 " + ORDER + " age at \"2024-01-01\" " + OFFSET + " 5 " + LIMIT + " 5";

        Compiler compiler = Compiler.create(COMPILER_PARSE_VALUE_FUNCTION,
                COMPILER_PARSE_OPERATOR_FUNCTION);
        AbstractSyntaxTree tree = compiler.parse(ccl);

        // Validate Condition Tree
        Assert.assertTrue(tree instanceof CommandTree);
        CommandTree commandTree = (CommandTree) tree;

        // Validate Keys
        GetSymbol getSymbol = (GetSymbol) commandTree.root();
        Assert.assertEquals(2, getSymbol.keys().size());

        // Validate Condition
        ExpressionSymbol expression = (ExpressionSymbol) commandTree.conditionTree().root();
        Assert.assertEquals("salary", expression.key().toString());
        Assert.assertEquals(">", expression.operator().toString());
        Assert.assertEquals("50000", expression.values().get(0).toString());

        // Validate Order
        OrderSymbol order = (OrderSymbol) commandTree.orderTree().root();
        Assert.assertEquals(1, order.components().size());
        Assert.assertEquals("age", order.components().get(0).key().toString());

        // Validate Timestamp
        Assert.assertEquals(
                TimeUnit.DAYS.convert(
                        order.components().get(0).timestamp().timestamp(),
                        TimeUnit.MICROSECONDS),
                TimeUnit.DAYS.convert(
                        NaturalLanguage.parseMicros("2024-01-01"),
                        TimeUnit.MICROSECONDS)
        );

        // Validate Page
        PageSymbol page = (PageSymbol) commandTree.pageTree().root();
        Assert.assertEquals(5, page.offset());
        Assert.assertEquals(Integer.valueOf(5), page.limit());
    }

    @Test
    public void testFindWithOrderAndPage() {
        String ccl = "find age > 25 " + ORDER + " name ASC, age DESC " + LIMIT + " 10";

        Compiler compiler = Compiler.create(COMPILER_PARSE_VALUE_FUNCTION,
                COMPILER_PARSE_OPERATOR_FUNCTION);
        AbstractSyntaxTree tree = compiler.parse(ccl);

        // Validate Condition Tree
        Assert.assertTrue(tree instanceof CommandTree);
        CommandTree commandTree = (CommandTree) tree;

        // Validate Condition
        ExpressionSymbol expression = (ExpressionSymbol) commandTree.conditionTree().root();
        Assert.assertEquals("age", expression.key().toString());
        Assert.assertEquals(">", expression.operator().toString());
        Assert.assertEquals("25", expression.values().get(0).toString());

        // Validate Order
        OrderSymbol order = (OrderSymbol) commandTree.orderTree().root();
        Assert.assertEquals(2, order.components().size());
        Assert.assertEquals("name", order.components().get(0).key().toString());
        Assert.assertEquals(DirectionSymbol.ASCENDING, order.components().get(0).direction());
        Assert.assertEquals("age", order.components().get(1).key().toString());
        Assert.assertEquals(DirectionSymbol.DESCENDING, order.components().get(1).direction());

        // Validate Page
        PageSymbol page = (PageSymbol) commandTree.pageTree().root();
        Assert.assertEquals(0, page.offset());
        Assert.assertEquals(Integer.valueOf(10), page.limit());
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
        Compiler compiler = Compiler.create(COMPILER_PARSE_VALUE_FUNCTION,
                COMPILER_PARSE_OPERATOR_FUNCTION);
        AbstractSyntaxTree ast = compiler.parse(criteria.ccl());
        List<Symbol> tokens = compiler.tokenize(ast);
        tokens.forEach(token -> {
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
        Compiler compiler = Compiler.create(COMPILER_PARSE_VALUE_FUNCTION,
                COMPILER_PARSE_OPERATOR_FUNCTION);
        AbstractSyntaxTree ast = compiler.parse(criteria.ccl());
        List<Symbol> tokens = compiler.tokenize(ast);
        tokens.forEach(token -> {
            if(token instanceof ValueSymbol) {
                Assert.assertEquals(Tag.class,
                        ((ValueSymbol) token).value().getClass());
            }
        });
    }

    @Test
    public void testRegressionV3_1_1A() {
        String ccl = "(a = b) or c = d and (email like email.com)";
        Compiler compiler = Compiler.create(COMPILER_PARSE_VALUE_FUNCTION,
                COMPILER_PARSE_OPERATOR_FUNCTION);
        compiler.parse(ccl);
        Assert.assertTrue(true); // lack of Exception means the test passes
    }

    @Test
    public void testNavigateCommandWithTransitiveKey() {
        String ccl = "navigate children*.name from 1";
        Compiler compiler = Compiler.create(COMPILER_PARSE_VALUE_FUNCTION,
                COMPILER_PARSE_OPERATOR_FUNCTION);
        AbstractSyntaxTree tree = compiler.parse(ccl);

        Assert.assertTrue(tree instanceof CommandTree);
        CommandTree commandTree = (CommandTree) tree;
        NavigateSymbol navigate = (NavigateSymbol) commandTree.root();
        Assert.assertEquals(1, navigate.keys().size());
        KeyTokenSymbol<?> key = navigate.keys().iterator().next();
        Assert.assertTrue(key instanceof NavigationKeySymbol);
        Assert.assertEquals("children*.name", key.toString());
        List<NavigationKeyStop> stops = ((NavigationKeySymbol) key).stops();
        Assert.assertEquals(2, stops.size());
        Assert.assertEquals(NavigationKeyStop.parse("children*"),
                stops.get(0));
        Assert.assertEquals(NavigationKeyStop.parse("name"), stops.get(1));
        Assert.assertEquals(Long.valueOf(1L), navigate.record());
    }

    @Test
    public void testNavigateCommandWithMultipleTransitiveKeys() {
        String ccl = "navigate [a.b*.c, d.e*.f*.g] from [1, 2]";
        Compiler compiler = Compiler.create(COMPILER_PARSE_VALUE_FUNCTION,
                COMPILER_PARSE_OPERATOR_FUNCTION);
        AbstractSyntaxTree tree = compiler.parse(ccl);

        Assert.assertTrue(tree instanceof CommandTree);
        CommandTree commandTree = (CommandTree) tree;
        NavigateSymbol navigate = (NavigateSymbol) commandTree.root();
        Assert.assertEquals(2, navigate.keys().size());
        List<KeyTokenSymbol<?>> keys = Lists.newArrayList(navigate.keys());
        Assert.assertTrue(keys.get(0) instanceof NavigationKeySymbol);
        Assert.assertTrue(keys.get(1) instanceof NavigationKeySymbol);

        List<NavigationKeyStop> first = ((NavigationKeySymbol) keys.get(0))
                .stops();
        Assert.assertEquals(3, first.size());
        Assert.assertEquals(NavigationKeyStop.parse("a"), first.get(0));
        Assert.assertEquals(NavigationKeyStop.parse("b*"), first.get(1));
        Assert.assertEquals(NavigationKeyStop.parse("c"), first.get(2));

        List<NavigationKeyStop> second = ((NavigationKeySymbol) keys.get(1))
                .stops();
        Assert.assertEquals(4, second.size());
        Assert.assertEquals(NavigationKeyStop.parse("d"), second.get(0));
        Assert.assertEquals(NavigationKeyStop.parse("e*"), second.get(1));
        Assert.assertEquals(NavigationKeyStop.parse("f*"), second.get(2));
        Assert.assertEquals(NavigationKeyStop.parse("g"), second.get(3));
    }

    @Test
    public void testNavigateCommandWithTransitiveKeyAndCriteria() {
        String ccl = "navigate children*.name where age > 25";
        Compiler compiler = Compiler.create(COMPILER_PARSE_VALUE_FUNCTION,
                COMPILER_PARSE_OPERATOR_FUNCTION);
        AbstractSyntaxTree tree = compiler.parse(ccl);

        Assert.assertTrue(tree instanceof CommandTree);
        CommandTree commandTree = (CommandTree) tree;
        NavigateSymbol navigate = (NavigateSymbol) commandTree.root();
        Assert.assertEquals(1, navigate.keys().size());
        KeyTokenSymbol<?> key = navigate.keys().iterator().next();
        Assert.assertTrue(key instanceof NavigationKeySymbol);
        Assert.assertEquals("children*.name", key.toString());

        // Validate Condition
        ExpressionSymbol expression = (ExpressionSymbol) commandTree
                .conditionTree().root();
        Assert.assertEquals("age", expression.key().toString());
        Assert.assertEquals(">", expression.operator().toString());
        Assert.assertEquals("25", expression.values().get(0).toString());
    }

    /**
     * Constants
     */
    private static final String LIMIT = "limit";
    private static final String OFFSET = "offset";
    private static final String ORDER = "order by";

    /**
     * The canonical function to transform strings to java values in a
     * {@link Parser}.
     */
    public final Function<String, Object> COMPILER_PARSE_VALUE_FUNCTION = value -> Convert
            .stringToJava(value);

    /**
     * The canonical function to transform strings to operators in a
     * {@link Parser}.
     */
    public final Function<String, Operator> COMPILER_PARSE_OPERATOR_FUNCTION = operator -> Convert
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
