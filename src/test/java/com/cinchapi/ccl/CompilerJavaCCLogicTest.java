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
import com.cinchapi.ccl.grammar.Symbol;
import com.cinchapi.ccl.grammar.TimestampSymbol;
import com.cinchapi.ccl.syntax.AbstractSyntaxTree;
import com.cinchapi.ccl.syntax.AndTree;
import com.cinchapi.ccl.syntax.ConditionTree;
import com.cinchapi.ccl.syntax.ConjunctionTree;
import com.cinchapi.ccl.syntax.ExpressionTree;
import com.cinchapi.ccl.syntax.FunctionTree;
import com.cinchapi.ccl.syntax.OrTree;
import com.cinchapi.ccl.syntax.OrderTree;
import com.cinchapi.ccl.syntax.PageTree;
import com.cinchapi.ccl.syntax.CommandTree;
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
    public void testPageWithNumber() {
        String input = PAGE + " 3";

        // Build expected list
        List<Object> expectedTokens = Lists.newArrayList();

        expectedTokens.add(new PageSymbol(3, null));

        // Generate list
        Compiler compiler = Compiler.create(COMPILER_PARSE_VALUE_FUNCTION,
                COMPILER_PARSE_OPERATOR_FUNCTION);
        AbstractSyntaxTree ast = compiler.parse(input);
        List<Symbol> tokens = compiler.tokenize(ast);

        Assert.assertEquals(expectedTokens, tokens);
    }

    @Test
    public void testPageWithNumberAndSize() {
        String input = SIZE + " 1 " + PAGE + " 3";

        // Build expected list
        List<Object> expectedTokens = Lists.newArrayList();

        expectedTokens.add(new PageSymbol(3, 1));

        // Generate list
        Compiler compiler = Compiler.create(COMPILER_PARSE_VALUE_FUNCTION,
                COMPILER_PARSE_OPERATOR_FUNCTION);
        AbstractSyntaxTree ast = compiler.parse(input);
        List<Symbol> tokens = compiler.tokenize(ast);

        Assert.assertEquals(expectedTokens, tokens);
    }

    @Test
    public void testPageWithSize() {
        String input = SIZE + " 3";

        // Build expected list
        List<Object> expectedTokens = Lists.newArrayList();

        expectedTokens.add(new PageSymbol(null, 3));

        // Generate list
        Compiler compiler = Compiler.create(COMPILER_PARSE_VALUE_FUNCTION,
                COMPILER_PARSE_OPERATOR_FUNCTION);
        AbstractSyntaxTree ast = compiler.parse(input);
        List<Symbol> tokens = compiler.tokenize(ast);

        Assert.assertEquals(expectedTokens, tokens);
    }

    @Test
    public void testPageWithSizeAndNumber() {
        String input = SIZE + " 1 " + PAGE + " 3";

        // Build expected list
        List<Object> expectedTokens = Lists.newArrayList();

        expectedTokens.add(new PageSymbol(3, 1));

        // Generate list
        Compiler compiler = Compiler.create(COMPILER_PARSE_VALUE_FUNCTION,
                COMPILER_PARSE_OPERATOR_FUNCTION);
        AbstractSyntaxTree ast = compiler.parse(input);
        List<Symbol> tokens = compiler.tokenize(ast);

        Assert.assertEquals(expectedTokens, tokens);
    }

    @Test
    public void testPageWithSizeAndNumberAST() {
        String input = SIZE + " 1 " + PAGE + " 3";

        // Generate queue
        Compiler compiler = Compiler.create(COMPILER_PARSE_VALUE_FUNCTION,
                COMPILER_PARSE_OPERATOR_FUNCTION);
        AbstractSyntaxTree tree = compiler.parse(input);

        // Root node
        Assert.assertTrue(tree instanceof PageTree);

        PageSymbol page = (PageSymbol) tree.root();
        Assert.assertEquals(2, page.offset());
        Assert.assertEquals(1, page.limit());
    }

    @Test
    public void testSingleExpressionTokenizeWithPage() {
        String ccl = "a = 1 " + SIZE + " 3 " + PAGE + " 1 ";

        // Build expected queue
        List<Object> expectedTokens = Lists.newArrayList();

        expectedTokens.add(new KeySymbol("a"));
        expectedTokens.add(new OperatorSymbol(
                COMPILER_PARSE_OPERATOR_FUNCTION.apply("=")));
        expectedTokens
                .add(new ValueSymbol(COMPILER_PARSE_VALUE_FUNCTION.apply("1")));
        expectedTokens.add(new PageSymbol(1, 3));

        // Generate queue
        Compiler compiler = Compiler.create(COMPILER_PARSE_VALUE_FUNCTION,
                COMPILER_PARSE_OPERATOR_FUNCTION);
        AbstractSyntaxTree ast = compiler.parse(ccl);
        List<Symbol> tokens = compiler.tokenize(ast);

        Assert.assertEquals(expectedTokens, tokens);
    }

    @Test
    public void testSingleExpressionASTWithPage() {
        String ccl = "a = 1 " + SIZE + " 1 " + PAGE + " 3 ";

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
        Assert.assertEquals(2, page.offset());
        Assert.assertEquals(1, page.limit());
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
        String input = ORDER + " > age in 1992-10-02";

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
        String ccl = "a = 1 " + ORDER + " a " + SIZE + " 1 " + PAGE + " 3";

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
        Assert.assertEquals(2, page.offset());
        Assert.assertEquals(1, page.limit());
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

    /**
     * Constants
     */
    private static final String PAGE = "page";
    private static final String SIZE = "size";
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
