/*
 * Copyright (c) 2013-2026 Cinchapi Inc.
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

import java.util.List;
import java.util.function.Function;

import org.junit.Assert;
import org.junit.Test;

import com.cinchapi.ccl.grammar.ConjunctionSymbol;
import com.cinchapi.ccl.grammar.ExpressionSymbol;
import com.cinchapi.ccl.grammar.FunctionKeySymbol;
import com.cinchapi.ccl.grammar.KeySymbol;
import com.cinchapi.ccl.grammar.OperatorSymbol;
import com.cinchapi.ccl.grammar.OrderComponentSymbol;
import com.cinchapi.ccl.grammar.OrderSymbol;
import com.cinchapi.ccl.grammar.Symbol;
import com.cinchapi.ccl.grammar.ValueSymbol;
import com.cinchapi.ccl.grammar.command.FindOrInsertSymbol;
import com.cinchapi.ccl.syntax.AbstractSyntaxTree;
import com.cinchapi.ccl.syntax.AndTree;
import com.cinchapi.ccl.syntax.CommandTree;
import com.cinchapi.ccl.syntax.ConditionTree;
import com.cinchapi.ccl.syntax.ExpressionTree;
import com.cinchapi.ccl.syntax.OrTree;
import com.cinchapi.ccl.type.Operator;
import com.cinchapi.concourse.util.Convert;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

/**
 * Tests for conditions that use an {@link Operator} CCL does not define, such
 * as one that a Concourse plugin provides.
 *
 * @author Jeff Nelson
 */
public class CustomOperatorTest {

    /**
     * An {@link Operator} that takes 1 to 2 operands, the first of which must
     * be a string.
     */
    private static final Operator ABOUT = new TestOperator("about", 1, 2);

    /**
     * An {@link Operator} that takes 1 to 3 operands, the first of which must
     * be a string.
     */
    private static final Operator NEAR = new TestOperator("near", 1, 3);

    /**
     * Resolves {@code about}, {@code vec.about} and {@code near}, and every
     * other symbol through the Concourse driver.
     */
    private static final Function<String, Operator> OPERATORS = symbol -> {
        if(symbol.equals("about") || symbol.equals("vec.about")) {
            return ABOUT;
        }
        else if(symbol.equals("near")) {
            return NEAR;
        }
        else {
            return Convert.stringToOperator(symbol);
        }
    };

    /**
     * The {@link Compiler} under test.
     */
    private final Compiler compiler = Compiler.create(Convert::stringToJava,
            OPERATORS);

    @Test
    public void testBuiltInOperatorsParseAsBefore() {
        Assert.assertEquals(
                ExpressionSymbol.create(new KeySymbol("a"),
                        new OperatorSymbol(Convert.stringToOperator("like")),
                        new ValueSymbol("x")),
                expression(compiler.parse("a like \"x\"")));
        Assert.assertEquals(
                ExpressionSymbol.create(new KeySymbol("a"),
                        new OperatorSymbol(Convert.stringToOperator("bw")),
                        new ValueSymbol(1), new ValueSymbol(3)),
                expression(compiler.parse("a bw 1 3")));
        Assert.assertEquals(
                ExpressionSymbol.create(new KeySymbol("a"),
                        new OperatorSymbol(Convert.stringToOperator("=")),
                        new ValueSymbol("1 2 3")),
                expression(compiler.parse("a = 1 2 3")));
        Assert.assertEquals(
                ExpressionSymbol.create(new KeySymbol("a"),
                        new OperatorSymbol(
                                Convert.stringToOperator("contains")),
                        new ValueSymbol("x")),
                expression(compiler.parse("a contains x")));
    }

    @Test
    public void testFindOrInsertKeepsTrailingJsonOutOfOperands() {
        CommandTree tree = (CommandTree) compiler
                .parse("findOrInsert bio about x '{\"a\": 1}'");
        Assert.assertEquals("{\"a\": 1}",
                ((FindOrInsertSymbol) tree.root()).json());
        Assert.assertEquals(ImmutableList.of(new ValueSymbol("x")),
                expression(tree.conditionTree()).values());
        tree = (CommandTree) compiler
                .parse("findOrInsert bio about x 'y' '{\"a\": 1}'");
        Assert.assertEquals(
                ImmutableList.of(new ValueSymbol("x"), new ValueSymbol("y")),
                expression(tree.conditionTree()).values());
    }

    @Test
    public void testIsOperatorNameAcceptsOnlyPlainWords() {
        Assert.assertTrue(Parsing.isOperatorName("about"));
        Assert.assertTrue(Parsing.isOperatorName("similar_to"));
        Assert.assertFalse(Parsing.isOperatorName("like"));
        Assert.assertFalse(Parsing.isOperatorName("LIKE"));
        Assert.assertFalse(Parsing.isOperatorName("eq"));
        Assert.assertFalse(Parsing.isOperatorName("and"));
        Assert.assertFalse(Parsing.isOperatorName("at"));
        Assert.assertFalse(Parsing.isOperatorName("limit"));
        Assert.assertFalse(Parsing.isOperatorName("contains"));
        Assert.assertFalse(Parsing.isOperatorName("123"));
        Assert.assertFalse(Parsing.isOperatorName("vec.about"));
        Assert.assertFalse(Parsing.isOperatorName("two words"));
        Assert.assertFalse(Parsing.isOperatorName(" about"));
        Assert.assertFalse(Parsing.isOperatorName("a-b"));
        Assert.assertFalse(Parsing.isOperatorName(""));
    }

    @Test
    public void testOrderCanNameAQualifiedFunction() {
        CommandTree tree = (CommandTree) compiler
                .parse("find bio about \"x\" order by bio | vec.about desc");
        OrderSymbol order = (OrderSymbol) tree.orderTree().root();
        OrderComponentSymbol component = order.components().get(0);
        FunctionKeySymbol key = (FunctionKeySymbol) component.key();
        Assert.assertEquals("vec.about", key.function().operation());
        Assert.assertEquals("bio", key.function().key());
        Assert.assertEquals("DESCENDING", component.direction().name());
    }

    @Test
    public void testParseAcceptsCustomOperatorInsideParentheses() {
        AbstractSyntaxTree tree = compiler.parse("(bio about x) or b = 2");
        Assert.assertTrue(tree instanceof OrTree);
        Assert.assertSame(ABOUT, expression(
                ((OrTree) tree).children().iterator().next()).raw()
                        .operator());
    }

    @Test
    public void testParseAcceptsQualifiedOperatorName() {
        ExpressionSymbol expression = expression(
                compiler.parse("bio vec.about \"x\""));
        Assert.assertSame(ABOUT, expression.operator().operator());
        Assert.assertEquals(ImmutableList.of(new ValueSymbol("x")),
                expression.values());
    }

    @Test
    public void testParseAttachesTimestampToCustomOperator() {
        ExpressionSymbol expression = expression(
                compiler.parse("bio about \"x\" at 123"));
        ExpressionSymbol builtIn = expression(compiler.parse("a = 1 at 123"));
        Assert.assertNotNull(expression.timestamp());
        Assert.assertEquals(builtIn.timestamp(), expression.timestamp());
        Assert.assertEquals(ImmutableList.of(new ValueSymbol("x")),
                expression.values());
    }

    @Test
    public void testParseGroupsEveryOperandOfCustomOperator() {
        AbstractSyntaxTree tree = compiler
                .parse("bio about \"red shoes\" 10 and status = active");
        Assert.assertTrue(tree instanceof AndTree);
        ExpressionSymbol first = expression(
                ((AndTree) tree).children().iterator().next());
        Assert.assertEquals(new KeySymbol("bio"), first.key());
        Assert.assertSame(ABOUT, first.operator().operator());
        Assert.assertEquals(ImmutableList.of(new ValueSymbol("red shoes"),
                new ValueSymbol(10)), first.values());
        Assert.assertEquals(
                ImmutableList.of(new ValueSymbol("x"), new ValueSymbol(1),
                        new ValueSymbol(2)),
                expression(compiler.parse("loc near x 1 2")).values());
    }

    @Test(expected = SyntaxException.class)
    public void testParseRejectsInvalidOperand() {
        compiler.parse("bio about 5");
    }

    @Test(expected = SyntaxException.class)
    public void testParseRejectsMissingOperand() {
        compiler.parse("bio about");
    }

    @Test(expected = SyntaxException.class)
    public void testParseRejectsTooManyOperands() {
        compiler.parse("bio about \"x\" 1 2");
    }

    @Test(expected = SyntaxException.class)
    public void testParseRejectsUnknownOperator() {
        compiler.parse("bio frobnicate x");
    }

    @Test
    public void testSymbolsGroupEveryOperandOfCustomOperator() {
        ExpressionSymbol expression = expression(compiler.parse(
                Lists.newArrayList(new KeySymbol("bio"),
                        new OperatorSymbol(ABOUT), new ValueSymbol("x"),
                        new ValueSymbol(10))));
        Assert.assertEquals(
                ImmutableList.of(new ValueSymbol("x"), new ValueSymbol(10)),
                expression.values());
        List<Symbol> symbols = Lists.newArrayList(new KeySymbol("loc"),
                new OperatorSymbol(NEAR), new ValueSymbol("x"),
                new ValueSymbol(1), new ValueSymbol(2), ConjunctionSymbol.AND,
                new KeySymbol("a"),
                new OperatorSymbol(Convert.stringToOperator("=")),
                new ValueSymbol(1));
        ConditionTree tree = compiler.parse(symbols);
        Assert.assertTrue(tree instanceof AndTree);
        Assert.assertEquals(3,
                expression(tree.children().iterator().next()).values().size());
    }

    @Test(expected = SyntaxException.class)
    public void testSymbolsRejectInvalidOperand() {
        compiler.parse(Lists.newArrayList(new KeySymbol("bio"),
                new OperatorSymbol(ABOUT), new ValueSymbol(5)));
    }

    @Test(expected = SyntaxException.class)
    public void testSymbolsRejectMissingOperand() {
        compiler.parse(Lists.newArrayList(new KeySymbol("bio"),
                new OperatorSymbol(ABOUT)));
    }

    /**
     * Return the {@link ExpressionSymbol} at the root of {@code tree}, which
     * must hold a single expression.
     *
     * @param tree the parsed condition
     * @return the expression
     */
    private ExpressionSymbol expression(AbstractSyntaxTree tree) {
        return (ExpressionSymbol) ((ExpressionTree) tree).root();
    }

    /**
     * An {@link Operator} with a range of operands, the first of which must be
     * a string.
     */
    private static final class TestOperator implements Operator {

        /**
         * The most operands.
         */
        private final int max;

        /**
         * The fewest operands.
         */
        private final int min;

        /**
         * The symbol.
         */
        private final String symbol;

        /**
         * Construct a new instance.
         *
         * @param symbol the symbol
         * @param min the fewest operands
         * @param max the most operands
         */
        TestOperator(String symbol, int min, int max) {
            this.symbol = symbol;
            this.min = min;
            this.max = max;
        }

        @Override
        public int maxOperands() {
            return max;
        }

        @Override
        public int minOperands() {
            return min;
        }

        @Override
        public int operands() {
            return min;
        }

        @Override
        public String symbol() {
            return symbol;
        }

        @Override
        public String toString() {
            return symbol;
        }

        @Override
        public void validate(List<Object> operands) {
            if(!(operands.get(0) instanceof String)) {
                throw new IllegalArgumentException(
                        "the first operand must be a string");
            }
        }
    }

}
