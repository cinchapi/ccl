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

import java.time.Duration;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.cinchapi.ccl.grammar.DirectionSymbol;
import com.cinchapi.ccl.grammar.ExpressionSymbol;
import com.cinchapi.ccl.grammar.KeySymbol;
import com.cinchapi.ccl.grammar.KeyTokenSymbol;
import com.cinchapi.ccl.grammar.NavigationKeySymbol;
import com.cinchapi.ccl.grammar.OperatorSymbol;
import com.cinchapi.ccl.grammar.PageSymbol;
import com.cinchapi.ccl.grammar.TimestampSymbol;
import com.cinchapi.ccl.grammar.ValueSymbol;
import com.cinchapi.ccl.grammar.command.CalculateSymbol;
import com.cinchapi.ccl.grammar.command.CalculationSymbol;
import com.cinchapi.ccl.grammar.command.GroupSymbol;
import com.cinchapi.ccl.grammar.command.HavingSymbol;
import com.cinchapi.ccl.grammar.command.ResultOrderSymbol;
import com.cinchapi.ccl.syntax.CommandTree;
import com.cinchapi.ccl.syntax.ConditionTree;
import com.cinchapi.concourse.thrift.Operator;
import com.cinchapi.concourse.util.Convert;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

/**
 * Coverage for the {@code calculate} command: its functions, the
 * {@code group by} clause with its ranges, {@code having}, result order and
 * page, several calculations in one statement, the text of a
 * {@link CalculateSymbol}, and the statements the grammar rejects.
 *
 * @author Jeff Nelson
 */
public class CalculateCommandTest {

    /**
     * Assert that each of {@code ccl} parses to a {@link CalculateSymbol} whose
     * text parses to an equal {@link CalculateSymbol} with an equal hash code.
     *
     * @param ccl the statements to parse
     */
    private static void assertParsesBackFromItsText(String... ccl) {
        for (String statement : ccl) {
            CalculateSymbol symbol = calculate(statement);
            CalculateSymbol parsed = calculate(symbol.toString());
            Assert.assertEquals(statement, symbol, parsed);
            Assert.assertEquals(statement, symbol.hashCode(),
                    parsed.hashCode());
        }
    }

    /**
     * Assert that parsing {@code ccl} fails with a {@link SyntaxException}.
     *
     * @param ccl the statement to parse
     * @param message text the exception message must contain, or {@code null}
     *            to accept any message
     */
    private static void assertRejected(String ccl, String message) {
        try {
            parse(ccl);
            Assert.fail("expected a SyntaxException for " + ccl);
        }
        catch (SyntaxException e) {
            if(message != null) {
                Assert.assertTrue(
                        "expected message to contain '" + message
                                + "' but was: " + e.getMessage(),
                        e.getMessage().contains(message));
            }
        }
    }

    /**
     * Parse {@code ccl} and return the {@link CalculateSymbol} at the root of
     * its {@link CommandTree}.
     *
     * @param ccl the statement to parse
     * @return the {@link CalculateSymbol}
     */
    private static CalculateSymbol calculate(String ccl) {
        return (CalculateSymbol) parse(ccl).root();
    }

    /**
     * Return a {@link CalculationSymbol} for a function without a fraction.
     *
     * @param function the function name
     * @param key the key the function applies to
     * @return the {@link CalculationSymbol}
     */
    private static CalculationSymbol calculation(String function, String key) {
        return new CalculationSymbol(function, new KeySymbol(key));
    }

    /**
     * Return the condition of the {@code find} command with {@code condition}.
     *
     * @param condition the condition text
     * @return the {@link ConditionTree}
     */
    private static ConditionTree condition(String condition) {
        return parse("find " + condition).conditionTree();
    }

    /**
     * Return a {@link List} of {@link KeySymbol KeySymbols} for {@code keys}.
     *
     * @param keys the key names
     * @return the {@link KeySymbol KeySymbols}
     */
    private static List<KeyTokenSymbol<?>> keys(String... keys) {
        ImmutableList.Builder<KeyTokenSymbol<?>> symbols = ImmutableList
                .builder();
        for (String key : keys) {
            symbols.add(new KeySymbol(key));
        }
        return symbols.build();
    }

    /**
     * Parse {@code ccl} as one {@link CommandTree}.
     *
     * @param ccl the statement to parse
     * @return the {@link CommandTree}
     */
    private static CommandTree parse(String ccl) {
        return (CommandTree) COMPILER.parse(ccl);
    }

    /**
     * A {@link Compiler} with the value and operator transforms the Concourse
     * driver uses.
     */
    private static final Compiler COMPILER = Compiler
            .create(Convert::stringToJava, Convert::stringToOperator);

    /**
     * <strong>Goal:</strong> Verify that analysis reports the keys of every
     * calculation and every group key.
     * <p>
     * <strong>Start state:</strong> No prior state needed.
     * <p>
     * <strong>Workflow:</strong>
     * <ul>
     * <li>Analyze
     * {@code calculate count $id$, sum price where active = true group by category, brand.name}.</li>
     * </ul>
     * <p>
     * <strong>Expected:</strong> The keys are {@code $id$}, {@code price},
     * {@code category}, {@code brand.name} and {@code active}.
     */
    @Test
    public void testAnalysisReportsCalculationAndGroupKeys() {
        CommandAnalysis analysis = COMPILER.analyze(parse("calculate count "
                + "$id$, sum price where active = true group by category, "
                + "brand.name"));
        Assert.assertEquals(ImmutableSet.of("$id$", "price", "category",
                "brand.name", "active"), analysis.keys());
    }

    /**
     * <strong>Goal:</strong> Verify that {@code avg} and {@code average} name
     * the same calculation in {@code having} and {@code order by}, in any
     * letter case.
     * <p>
     * <strong>Start state:</strong> No prior state needed.
     * <p>
     * <strong>Workflow:</strong>
     * <ul>
     * <li>Parse a statement that calculates {@code average price} and orders
     * by {@code AVG}.</li>
     * <li>Parse a statement that calculates {@code avg price} and filters on
     * {@code average}.</li>
     * </ul>
     * <p>
     * <strong>Expected:</strong> Each clause refers to the calculation of the
     * statement as written.
     */
    @Test
    public void testAvgAndAverageNameTheSameCalculation() {
        Assert.assertEquals(calculation("average", "price"),
                calculate("calculate average price group by category "
                        + "order by AVG desc").group().order().calculation());
        Assert.assertEquals(calculation("avg", "price"), calculate(
                "calculate avg price group by category having average > 3")
                .group().having().calculation());
    }

    /**
     * <strong>Goal:</strong> Verify that a {@link CalculateSymbol} built from
     * its parts checks them as the grammar does.
     * <p>
     * <strong>Start state:</strong> No prior state needed.
     * <p>
     * <strong>Workflow:</strong>
     * <ul>
     * <li>Build a symbol with several calculations and no group.</li>
     * <li>Build one whose having clause names a calculation the statement
     * lacks.</li>
     * <li>Build one whose order names a function two calculations share.</li>
     * <li>Build one whose having clause has a calculation that shares its
     * function, but not its key, with the statement's only calculation.</li>
     * <li>Build a valid one with a list of records and parse its text.</li>
     * <li>Build one with a set of records and parse its text.</li>
     * </ul>
     * <p>
     * <strong>Expected:</strong> Each invalid build fails with an
     * {@link IllegalArgumentException}, and each valid one parses back from its
     * text.
     */
    @Test
    public void testBuiltSymbolFollowsGrammarRules() {
        CalculationSymbol count = calculation("count", "$id$");
        CalculationSymbol sum = calculation("sum", "price");
        CalculationSymbol cost = calculation("sum", "cost");
        List<KeyTokenSymbol<?>> category = keys("category");
        for (Runnable invalid : ImmutableList.<Runnable> of(
                () -> new CalculateSymbol(ImmutableList.of(count, sum), null,
                        null, null),
                () -> new CalculateSymbol(ImmutableList.of(count), null, null,
                        GroupSymbol.builder(category)
                                .having(new HavingSymbol(sum,
                                        new OperatorSymbol(Operator.EQUALS),
                                        new ValueSymbol(1)))
                                .build()),
                () -> new CalculateSymbol(ImmutableList.of(sum, cost), null,
                        null,
                        GroupSymbol.builder(category)
                                .order(new ResultOrderSymbol(sum,
                                        DirectionSymbol.ASCENDING))
                                .build()),
                () -> new CalculateSymbol(ImmutableList.of(sum), null, null,
                        GroupSymbol.builder(category)
                                .having(new HavingSymbol(cost,
                                        new OperatorSymbol(Operator.EQUALS),
                                        new ValueSymbol(1)))
                                .build()))) {
            try {
                invalid.run();
                Assert.fail("expected an IllegalArgumentException");
            }
            catch (IllegalArgumentException e) {
                // expected
            }
        }
        CalculateSymbol built = new CalculateSymbol(
                ImmutableList.of(count, sum), ImmutableList.of(1L, 2L),
                new TimestampSymbol(7),
                GroupSymbol.builder(keys("category", "brand")).per(10L)
                        .between(0.0, 100)
                        .order(new ResultOrderSymbol(sum,
                                DirectionSymbol.DESCENDING))
                        .page(PageSymbol.fromLimit(3)).build());
        Assert.assertEquals(built, calculate(built.toString()));
        CalculateSymbol set = new CalculateSymbol("sum", new KeySymbol("age"),
                ImmutableSet.of(1L, 2L), null);
        CalculateSymbol parsed = calculate(set.toString());
        Assert.assertEquals(set, parsed);
        Assert.assertEquals(set.hashCode(), parsed.hashCode());
    }

    /**
     * <strong>Goal:</strong> Verify that {@code countDistinct} parses to its
     * canonical name and keeps its condition in the tree.
     * <p>
     * <strong>Start state:</strong> No prior state needed.
     * <p>
     * <strong>Workflow:</strong>
     * <ul>
     * <li>Parse {@code calculate countDistinct email where _ = User}.</li>
     * </ul>
     * <p>
     * <strong>Expected:</strong> The symbol has one calculation,
     * {@code countDistinct} of {@code email}, no group, records or timestamp,
     * and the tree's condition is {@code _ = User}.
     */
    @Test
    public void testCountDistinctParsesWithItsCondition() {
        CommandTree tree = parse(
                "calculate countDistinct email where _ = User");
        CalculateSymbol symbol = (CalculateSymbol) tree.root();
        Assert.assertEquals("CALCULATE", symbol.type());
        Assert.assertEquals(
                ImmutableList.of(calculation("countDistinct", "email")),
                symbol.calculations());
        Assert.assertEquals("countDistinct", symbol.function());
        Assert.assertEquals(new KeySymbol("email"), symbol.key());
        Assert.assertNull(symbol.group());
        Assert.assertNull(symbol.records());
        Assert.assertNull(symbol.timestamp());
        Assert.assertEquals(condition("_ = User"), tree.conditionTree());
    }

    /**
     * <strong>Goal:</strong> Verify that equivalent statements parse to equal
     * trees and that a change to any part of a grouped statement makes the
     * trees unequal.
     * <p>
     * <strong>Start state:</strong> No prior state needed.
     * <p>
     * <strong>Workflow:</strong>
     * <ul>
     * <li>Parse a grouped statement twice.</li>
     * <li>Parse it with {@code bw} for {@code between} and {@code 10.0} for the
     * width.</li>
     * <li>Parse statements that differ from it in one part.</li>
     * </ul>
     * <p>
     * <strong>Expected:</strong> The two parses are equal with equal hash
     * codes, as is the parse that spells {@code between} as {@code bw} and the
     * width as {@code 10.0}. Each differing statement parses to an unequal
     * tree.
     */
    @Test
    public void testEquivalentStatementsParseToEqualTrees() {
        String ccl = "calculate count $id$, sum price where active = true "
                + "group by category per 10 between 0 and 100 "
                + "having count >= 2 order by sum desc limit 10";
        Assert.assertEquals(parse(ccl), parse(ccl));
        Assert.assertEquals(parse(ccl).hashCode(), parse(ccl).hashCode());
        String decimal = ccl.replace("between", "bw").replace("10 bw",
                "10.0 bw");
        Assert.assertEquals(parse(ccl), parse(decimal));
        Assert.assertEquals(parse(ccl).hashCode(), parse(decimal).hashCode());
        for (String other : ImmutableList.of(
                ccl.replace("sum price", "sum price, max price"),
                ccl.replace("sum price", "sum cost"),
                ccl.replace("where active = true", "in 1, 2"),
                ccl.replace("category", "brand"),
                ccl.replace("per 10", "per 20"),
                ccl.replace("and 100", "and 200"), ccl.replace(">= 2", ">= 3"),
                ccl.replace("sum desc", "sum asc"),
                ccl.replace("limit 10", "limit 20"),
                ccl.replace("active = true", "active = true as of 7"))) {
            Assert.assertNotEquals(other, parse(ccl), parse(other));
        }
    }

    /**
     * <strong>Goal:</strong> Verify that every new function name parses in any
     * letter case to its canonical name, and that each existing name keeps the
     * lower case name it parses to.
     * <p>
     * <strong>Start state:</strong> No prior state needed.
     * <p>
     * <strong>Workflow:</strong>
     * <ul>
     * <li>Parse {@code calculate <name> age} for each new name in upper
     * case.</li>
     * <li>Parse {@code calculate <name> age} for each existing name in upper
     * case.</li>
     * </ul>
     * <p>
     * <strong>Expected:</strong> Each new name parses to its camelCase name,
     * and each existing name parses to its lower case name.
     */
    @Test
    public void testFunctionNamesParseToCanonicalNames() {
        for (String name : ImmutableList.of("countDistinct", "median", "stddev",
                "stddevPopulation", "variance", "variancePopulation")) {
            Assert.assertEquals(name,
                    calculate("calculate " + name.toUpperCase() + " age")
                            .function());
        }
        for (String name : ImmutableList.of("sum", "avg", "average", "count",
                "min", "max")) {
            Assert.assertEquals(name,
                    calculate("calculate " + name.toUpperCase() + " age")
                            .function());
        }
        Assert.assertEquals("percentile",
                calculate("calculate PERCENTILE 0.5 age").function());
    }

    /**
     * <strong>Goal:</strong> Verify that the {@link GroupSymbol} builder
     * rejects an inconsistent range.
     * <p>
     * <strong>Start state:</strong> No prior state needed.
     * <p>
     * <strong>Workflow:</strong>
     * <ul>
     * <li>Build a group with no keys.</li>
     * <li>Build one with both a width and a period.</li>
     * <li>Build one with number bounds and a period, and one with timestamp
     * bounds and a width.</li>
     * <li>Build one with bounds and no width or period.</li>
     * </ul>
     * <p>
     * <strong>Expected:</strong> Each build fails with an
     * {@link IllegalArgumentException} or an {@link IllegalStateException}.
     */
    @Test
    public void testGroupBuilderRejectsInconsistentRange() {
        List<KeyTokenSymbol<?>> price = keys("price");
        for (Runnable invalid : ImmutableList.<Runnable> of(
                () -> GroupSymbol.builder(ImmutableList.of()).build(),
                () -> GroupSymbol.builder(price).per(10).per(Duration.ofDays(1))
                        .build(),
                () -> GroupSymbol.builder(price).per(Duration.ofDays(1))
                        .between(0, 10).build(),
                () -> GroupSymbol.builder(price).per(10)
                        .between(new TimestampSymbol(1), new TimestampSymbol(2))
                        .build(),
                () -> GroupSymbol.builder(price).between(0, 10).build(),
                () -> GroupSymbol.builder(price).per(Double.NaN).build())) {
            try {
                invalid.run();
                Assert.fail("expected an exception");
            }
            catch (IllegalArgumentException | IllegalStateException e) {
                // expected
            }
        }
    }

    /**
     * <strong>Goal:</strong> Verify that a navigation key groups, and that
     * {@code order by} and {@code limit} order and page the groups by a result.
     * <p>
     * <strong>Start state:</strong> No prior state needed.
     * <p>
     * <strong>Workflow:</strong>
     * <ul>
     * <li>Parse
     * {@code calculate count $id$ where active = true group by education.school.name order by count desc limit 10}.</li>
     * </ul>
     * <p>
     * <strong>Expected:</strong> The group key is the navigation key
     * {@code education.school.name}, the order is the {@code count} calculation
     * descending, and the page is a limit of {@code 10}. The tree has no order
     * or page of its own.
     */
    @Test
    public void testGroupByNavigationKeyOrdersAndPagesGroups() {
        CommandTree tree = parse("calculate count $id$ where active = true "
                + "group by education.school.name order by count desc "
                + "limit 10");
        GroupSymbol group = ((CalculateSymbol) tree.root()).group();
        Assert.assertEquals(
                ImmutableList
                        .of(new NavigationKeySymbol("education.school.name")),
                group.keys());
        Assert.assertEquals(new ResultOrderSymbol(calculation("count", "$id$"),
                DirectionSymbol.DESCENDING), group.order());
        Assert.assertEquals(PageSymbol.fromLimit(10), group.page());
        Assert.assertEquals(condition("active = true"), tree.conditionTree());
        Assert.assertNull(tree.orderTree());
        Assert.assertNull(tree.pageTree());
    }

    /**
     * <strong>Goal:</strong> Verify that {@code group by} parses its key and
     * keeps the condition in the tree.
     * <p>
     * <strong>Start state:</strong> No prior state needed.
     * <p>
     * <strong>Workflow:</strong>
     * <ul>
     * <li>Parse
     * {@code calculate count $id$ where _ = Application group by opportunity}.</li>
     * </ul>
     * <p>
     * <strong>Expected:</strong> The group has the key {@code opportunity} and
     * no range, having, order or page. The tree's condition is
     * {@code _ = Application}, and the tree has no order or page.
     */
    @Test
    public void testGroupByParsesKeyAndKeepsCondition() {
        CommandTree tree = parse("calculate count $id$ where _ = Application "
                + "group by opportunity");
        CalculateSymbol symbol = (CalculateSymbol) tree.root();
        Assert.assertEquals(ImmutableList.of(calculation("count", "$id$")),
                symbol.calculations());
        GroupSymbol group = symbol.group();
        Assert.assertEquals(keys("opportunity"), group.keys());
        Assert.assertNull(group.width());
        Assert.assertNull(group.period());
        Assert.assertNull(group.lowerBound());
        Assert.assertNull(group.upperBound());
        Assert.assertNull(group.start());
        Assert.assertNull(group.end());
        Assert.assertNull(group.having());
        Assert.assertNull(group.order());
        Assert.assertNull(group.page());
        Assert.assertEquals(condition("_ = Application"), tree.conditionTree());
        Assert.assertNull(tree.orderTree());
        Assert.assertNull(tree.pageTree());
    }

    /**
     * <strong>Goal:</strong> Verify that the text {@code group by} ends an
     * unquoted value, even at the start of a longer word, and that a quoted
     * value keeps it.
     * <p>
     * <strong>Start state:</strong> No prior state needed.
     * <p>
     * <strong>Workflow:</strong>
     * <ul>
     * <li>Parse {@code find name = rock group by sea}.</li>
     * <li>Parse {@code find name = group bypass}.</li>
     * <li>Parse {@code find name = "rock group by sea"}.</li>
     * <li>Parse {@code find group = 1}.</li>
     * </ul>
     * <p>
     * <strong>Expected:</strong> The first two parses fail with a
     * {@link SyntaxException}. The third has the value
     * {@code rock group by sea}. The fourth has the key {@code group}.
     */
    @Test
    public void testGroupByPhraseIsReserved() {
        assertRejected("find name = rock group by sea", null);
        assertRejected("find name = group bypass", null);
        ExpressionSymbol quoted = (ExpressionSymbol) condition(
                "name = \"rock group by sea\"").root();
        Assert.assertEquals(
                ImmutableList.of(new ValueSymbol("rock group by sea")),
                quoted.values());
        ExpressionSymbol key = (ExpressionSymbol) condition("group = 1").root();
        Assert.assertEquals(new KeySymbol("group"), key.key());
    }

    /**
     * <strong>Goal:</strong> Verify that a grouped statement lists several
     * calculations separated by commas.
     * <p>
     * <strong>Start state:</strong> No prior state needed.
     * <p>
     * <strong>Workflow:</strong>
     * <ul>
     * <li>Parse
     * {@code calculate count $id$, sum price, avg price where active = true group by category}.</li>
     * </ul>
     * <p>
     * <strong>Expected:</strong> The calculations are {@code count $id$},
     * {@code sum price} and {@code avg price}, in that order. The function and
     * key of the symbol are those of the first calculation.
     */
    @Test
    public void testGroupedStatementListsSeveralCalculations() {
        CommandTree tree = parse("calculate count $id$, sum price, avg price "
                + "where active = true group by category");
        CalculateSymbol symbol = (CalculateSymbol) tree.root();
        Assert.assertEquals(ImmutableList.of(calculation("count", "$id$"),
                calculation("sum", "price"), calculation("avg", "price")),
                symbol.calculations());
        Assert.assertEquals("count", symbol.function());
        Assert.assertEquals(new KeySymbol("$id$"), symbol.key());
        Assert.assertEquals(keys("category"), symbol.group().keys());
        Assert.assertEquals(condition("active = true"), tree.conditionTree());
    }

    /**
     * <strong>Goal:</strong> Verify that a grouped statement takes a list of
     * records.
     * <p>
     * <strong>Start state:</strong> No prior state needed.
     * <p>
     * <strong>Workflow:</strong>
     * <ul>
     * <li>Parse {@code calculate sum price in [1, 2, 3] group by category}.</li>
     * <li>Parse {@code calculate sum price from 1 group by category}.</li>
     * </ul>
     * <p>
     * <strong>Expected:</strong> The records are {@code 1}, {@code 2} and
     * {@code 3}, and then {@code 1}, with the group key {@code category}.
     */
    @Test
    public void testGroupedStatementTakesRecords() {
        CalculateSymbol many = calculate(
                "calculate sum price in [1, 2, 3] group by category");
        Assert.assertEquals(ImmutableList.of(1L, 2L, 3L),
                ImmutableList.copyOf(many.records()));
        Assert.assertEquals(keys("category"), many.group().keys());
        CalculateSymbol one = calculate(
                "calculate sum price from 1 group by category");
        Assert.assertEquals(ImmutableList.of(1L),
                ImmutableList.copyOf(one.records()));
    }

    /**
     * <strong>Goal:</strong> Verify that a grouped statement takes the
     * timestamp of the calculation before {@code group by}, in each scope.
     * <p>
     * <strong>Start state:</strong> No prior state needed.
     * <p>
     * <strong>Workflow:</strong>
     * <ul>
     * <li>Parse a grouped statement with {@code at} and no scope.</li>
     * <li>Parse one with records and {@code at}.</li>
     * <li>Parse one with a condition and {@code as of}.</li>
     * <li>Parse one with a parenthesized condition and {@code at}.</li>
     * </ul>
     * <p>
     * <strong>Expected:</strong> Each symbol has the timestamp
     * {@code 1700000000} and the group key {@code category}.
     */
    @Test
    public void testGroupedStatementTakesTimestamp() {
        for (String ccl : ImmutableList.of(
                "calculate count $id$ at 1700000000 group by category",
                "calculate sum price in 1, 2 at 1700000000 group by category",
                "calculate sum price where active = true as of 1700000000 "
                        + "group by category",
                "calculate sum price where (active = true) at 1700000000 "
                        + "group by category")) {
            CalculateSymbol symbol = calculate(ccl);
            Assert.assertEquals(ccl, new TimestampSymbol(1700000000),
                    symbol.timestamp());
            Assert.assertEquals(ccl, keys("category"), symbol.group().keys());
        }
    }

    /**
     * <strong>Goal:</strong> Verify that {@code having} filters groups by the
     * result of a calculation, with several group keys.
     * <p>
     * <strong>Start state:</strong> No prior state needed.
     * <p>
     * <strong>Workflow:</strong>
     * <ul>
     * <li>Parse
     * {@code calculate count $id$ where _ = Application group by candidate, opportunity having count >= 2}.</li>
     * </ul>
     * <p>
     * <strong>Expected:</strong> The group keys are {@code candidate} and
     * {@code opportunity}, and the having clause tests whether the
     * {@code count} calculation is at least {@code 2}.
     */
    @Test
    public void testHavingFiltersGroupsByResult() {
        GroupSymbol group = calculate("calculate count $id$ where "
                + "_ = Application group by candidate, opportunity "
                + "having count >= 2").group();
        Assert.assertEquals(keys("candidate", "opportunity"), group.keys());
        Assert.assertEquals(new HavingSymbol(calculation("count", "$id$"),
                new OperatorSymbol(Operator.GREATER_THAN_OR_EQUALS),
                new ValueSymbol(2)), group.having());
    }

    /**
     * <strong>Goal:</strong> Verify that {@code median} parses without a scope.
     * <p>
     * <strong>Start state:</strong> No prior state needed.
     * <p>
     * <strong>Workflow:</strong>
     * <ul>
     * <li>Parse {@code calculate median age}.</li>
     * </ul>
     * <p>
     * <strong>Expected:</strong> The symbol has one calculation, {@code median}
     * of {@code age}, and the tree has no condition.
     */
    @Test
    public void testMedianParsesWithoutScope() {
        CommandTree tree = parse("calculate median age");
        CalculateSymbol symbol = (CalculateSymbol) tree.root();
        Assert.assertEquals(ImmutableList.of(calculation("median", "age")),
                symbol.calculations());
        Assert.assertNull(symbol.group());
        Assert.assertNull(tree.conditionTree());
    }

    /**
     * <strong>Goal:</strong> Verify that {@code per} and {@code having} parse
     * as keys where a key is expected, because CCL does not reserve them.
     * <p>
     * <strong>Start state:</strong> No prior state needed.
     * <p>
     * <strong>Workflow:</strong>
     * <ul>
     * <li>Parse
     * {@code calculate count per group by per, having per 10 having count >= 2}.</li>
     * </ul>
     * <p>
     * <strong>Expected:</strong> The calculation is {@code count} of the key
     * {@code per}, the group keys are {@code per} and {@code having}, the width
     * is {@code 10}, and the having clause tests whether the {@code count}
     * calculation is at least {@code 2}.
     */
    @Test
    public void testPerAndHavingParseAsKeys() {
        CalculateSymbol symbol = calculate("calculate count per group by per, "
                + "having per 10 having count >= 2");
        Assert.assertEquals(ImmutableList.of(calculation("count", "per")),
                symbol.calculations());
        GroupSymbol group = symbol.group();
        Assert.assertEquals(keys("per", "having"), group.keys());
        Assert.assertEquals(10, group.width());
        Assert.assertEquals(new HavingSymbol(calculation("count", "per"),
                new OperatorSymbol(Operator.GREATER_THAN_OR_EQUALS),
                new ValueSymbol(2)), group.having());
    }

    /**
     * <strong>Goal:</strong> Verify that {@code percentile} needs a fraction
     * from 0 to 1, and that no other function takes one.
     * <p>
     * <strong>Start state:</strong> No prior state needed.
     * <p>
     * <strong>Workflow:</strong>
     * <ul>
     * <li>Parse {@code percentile} without a fraction, with {@code 1.5} and
     * with {@code -0.5}.</li>
     * <li>Construct a {@link CalculationSymbol} for {@code median} with a
     * fraction and one for {@code percentile} without one.</li>
     * <li>Parse the fractions {@code 0}, {@code 1} and {@code -0}.</li>
     * </ul>
     * <p>
     * <strong>Expected:</strong> Each parse of a missing or out of range
     * fraction fails with a {@link SyntaxException}, each construction fails
     * with an {@link IllegalArgumentException}, and the bounds {@code 0} and
     * {@code 1} parse. The fraction {@code -0} is {@code 0}.
     */
    @Test
    public void testPercentileRequiresFractionFromZeroToOne() {
        assertRejected("calculate percentile latency", null);
        assertRejected("calculate percentile 1.5 latency", "fraction");
        assertRejected("calculate percentile -0.5 latency", "fraction");
        try {
            new CalculationSymbol("median", 0.5, new KeySymbol("age"));
            Assert.fail("expected an IllegalArgumentException");
        }
        catch (IllegalArgumentException e) {
            Assert.assertTrue(e.getMessage(),
                    e.getMessage().contains("fraction"));
        }
        try {
            new CalculationSymbol("percentile", new KeySymbol("age"));
            Assert.fail("expected an IllegalArgumentException");
        }
        catch (IllegalArgumentException e) {
            Assert.assertTrue(e.getMessage(),
                    e.getMessage().contains("fraction"));
        }
        Assert.assertEquals(Double.valueOf(0),
                calculate("calculate percentile 0 latency").calculations()
                        .get(0).fraction());
        Assert.assertEquals(Double.valueOf(1),
                calculate("calculate percentile 1 latency").calculations()
                        .get(0).fraction());
        Assert.assertEquals(Double.valueOf(0),
                calculate("calculate percentile -0 latency").calculations()
                        .get(0).fraction());
    }

    /**
     * <strong>Goal:</strong> Verify that {@code percentile} takes its fraction
     * before its key, and that an {@code at} right after a comparison stays
     * part of the condition, as it does for {@code find}.
     * <p>
     * <strong>Start state:</strong> No prior state needed.
     * <p>
     * <strong>Workflow:</strong>
     * <ul>
     * <li>Parse
     * {@code calculate percentile 0.99 latency where service = api at "yesterday"}.</li>
     * </ul>
     * <p>
     * <strong>Expected:</strong> The calculation is {@code percentile} with
     * fraction {@code 0.99} of {@code latency}. The symbol has no timestamp,
     * and the condition {@code service = api} carries a past timestamp.
     */
    @Test
    public void testPercentileTakesFractionBeforeKey() {
        CommandTree tree = parse("calculate percentile 0.99 latency "
                + "where service = api at \"yesterday\"");
        CalculateSymbol symbol = (CalculateSymbol) tree.root();
        CalculationSymbol percentile = symbol.calculations().get(0);
        Assert.assertEquals("percentile", percentile.function());
        Assert.assertEquals(Double.valueOf(0.99), percentile.fraction());
        Assert.assertEquals(new KeySymbol("latency"), percentile.key());
        Assert.assertEquals(new CalculationSymbol("percentile", 0.99,
                new KeySymbol("latency")), percentile);
        Assert.assertNull(symbol.timestamp());
        ExpressionSymbol expression = (ExpressionSymbol) tree.conditionTree()
                .root();
        Assert.assertEquals(new KeySymbol("service"), expression.key());
        Assert.assertEquals(ImmutableList.of(new ValueSymbol("api")),
                expression.values());
        Assert.assertTrue(expression.timestamp()
                .timestamp() > TimestampSymbol.PRESENT.timestamp());
    }

    /**
     * <strong>Goal:</strong> Verify that {@code per} with a quoted duration
     * groups the last key into time ranges.
     * <p>
     * <strong>Start state:</strong> No prior state needed.
     * <p>
     * <strong>Workflow:</strong>
     * <ul>
     * <li>Parse
     * {@code calculate count $id$ where _ = Credential group by lastUsed per "1 day"}.</li>
     * </ul>
     * <p>
     * <strong>Expected:</strong> The group key is {@code lastUsed}, the period
     * is one day, and the group has no width or bounds.
     */
    @Test
    public void testPerDurationGroupsIntoTimeRanges() {
        GroupSymbol group = calculate("calculate count $id$ where "
                + "_ = Credential group by lastUsed per \"1 day\"").group();
        Assert.assertEquals(keys("lastUsed"), group.keys());
        Assert.assertEquals(Duration.ofDays(1), group.period());
        Assert.assertNull(group.width());
        Assert.assertNull(group.start());
        Assert.assertNull(group.end());
    }

    /**
     * <strong>Goal:</strong> Verify that {@code per} with a number and
     * {@code between} group the last key into bounded number ranges.
     * <p>
     * <strong>Start state:</strong> No prior state needed.
     * <p>
     * <strong>Workflow:</strong>
     * <ul>
     * <li>Parse
     * {@code calculate count $id$ group by price per 10 between 0 and 100}.</li>
     * <li>Parse a decimal width and negative bounds.</li>
     * </ul>
     * <p>
     * <strong>Expected:</strong> The width is {@code 10} and the bounds are
     * {@code 0} and {@code 100}. A decimal width is a {@link Double}, and a
     * negative bound keeps its sign.
     */
    @Test
    public void testPerNumberBetweenGroupsIntoNumberRanges() {
        GroupSymbol group = calculate(
                "calculate count $id$ group by price per 10 between 0 and 100")
                .group();
        Assert.assertEquals(keys("price"), group.keys());
        Assert.assertEquals(10, group.width());
        Assert.assertEquals(0, group.lowerBound());
        Assert.assertEquals(100, group.upperBound());
        Assert.assertNull(group.period());

        GroupSymbol decimal = calculate("calculate count $id$ group by price "
                + "per 0.5 between -1.5 and 2").group();
        Assert.assertEquals(0.5, decimal.width());
        Assert.assertEquals(-1.5, decimal.lowerBound());
        Assert.assertEquals(2, decimal.upperBound());
    }

    /**
     * <strong>Goal:</strong> Verify that a result order, a having clause and a
     * page need a {@code group by}.
     * <p>
     * <strong>Start state:</strong> No prior state needed.
     * <p>
     * <strong>Workflow:</strong>
     * <ul>
     * <li>Parse an ungrouped statement with an order, with a having clause and
     * with a limit.</li>
     * </ul>
     * <p>
     * <strong>Expected:</strong> Each parse fails with a
     * {@link SyntaxException}.
     */
    @Test
    public void testRejectsGroupClausesWithoutGroup() {
        assertRejected("calculate count $id$ order by count desc", null);
        assertRejected("calculate count $id$ having count > 1", null);
        assertRejected("calculate sum age where active = true limit 10", null);
    }

    /**
     * <strong>Goal:</strong> Verify that the grammar rejects an invalid range.
     * <p>
     * <strong>Start state:</strong> No prior state needed.
     * <p>
     * <strong>Workflow:</strong>
     * <ul>
     * <li>Parse a width of {@code 0} and of {@code -5}.</li>
     * <li>Parse bounds in the wrong order, for numbers and for
     * timestamps.</li>
     * <li>Parse {@code between} without {@code per}.</li>
     * <li>Parse quoted bounds after a numeric width.</li>
     * <li>Parse a duration that is not a duration.</li>
     * </ul>
     * <p>
     * <strong>Expected:</strong> Each parse fails with a
     * {@link SyntaxException}.
     */
    @Test
    public void testRejectsInvalidRange() {
        assertRejected("calculate count $id$ group by price per 0", "width");
        assertRejected("calculate count $id$ group by price per -5", "width");
        assertRejected("calculate count $id$ group by price per 10 "
                + "between 100 and 0", "bound");
        assertRejected("calculate count $id$ group by lastUsed per \"1 day\" "
                + "between 2 and 1", "start");
        assertRejected("calculate count $id$ group by price between 0 and 100",
                null);
        assertRejected("calculate count $id$ group by price per 10 "
                + "between \"a\" and \"b\"", null);
        assertRejected("calculate count $id$ group by lastUsed per \"day\"",
                "duration");
    }

    /**
     * <strong>Goal:</strong> Verify that {@code having} and {@code order by}
     * must name exactly one calculation of the statement, so a grouped
     * statement cannot order its groups by a key.
     * <p>
     * <strong>Start state:</strong> No prior state needed.
     * <p>
     * <strong>Workflow:</strong>
     * <ul>
     * <li>Parse a having clause that names a function the statement does not
     * calculate.</li>
     * <li>Parse an order by a group key.</li>
     * <li>Parse an order by a function two calculations share.</li>
     * </ul>
     * <p>
     * <strong>Expected:</strong> Each parse fails with a
     * {@link SyntaxException} that says the name matches no calculation, or
     * more than one.
     */
    @Test
    public void testRejectsResultReferenceThatNamesNoSingleCalculation() {
        assertRejected("calculate count $id$ group by category having sum > 2",
                "names none of");
        assertRejected(
                "calculate count $id$ group by category order by category",
                "names none of");
        assertRejected("calculate sum price, sum cost group by category "
                + "order by sum desc", "names more than one of");
    }

    /**
     * <strong>Goal:</strong> Verify that several calculations need a
     * {@code group by}.
     * <p>
     * <strong>Start state:</strong> No prior state needed.
     * <p>
     * <strong>Workflow:</strong>
     * <ul>
     * <li>Parse {@code calculate count $id$, sum price}.</li>
     * <li>Parse it with a condition.</li>
     * </ul>
     * <p>
     * <strong>Expected:</strong> Each parse fails with a
     * {@link SyntaxException} that says the calculations need a group.
     */
    @Test
    public void testRejectsSeveralCalculationsWithoutGroup() {
        assertRejected("calculate count $id$, sum price", "need a group");
        assertRejected("calculate count $id$, sum price where active = true",
                "need a group");
    }

    /**
     * <strong>Goal:</strong> Verify that a function name outside the supported
     * set is rejected.
     * <p>
     * <strong>Start state:</strong> No prior state needed.
     * <p>
     * <strong>Workflow:</strong>
     * <ul>
     * <li>Parse {@code calculate xyzgarbage age}.</li>
     * <li>Parse it as the second calculation of a grouped statement.</li>
     * </ul>
     * <p>
     * <strong>Expected:</strong> Each parse fails with a
     * {@link SyntaxException} that lists the supported functions.
     */
    @Test
    public void testRejectsUnknownFunction() {
        assertRejected("calculate xyzgarbage age", "Must be one of");
        assertRejected("calculate count $id$, xyzgarbage age group by a",
                "Must be one of");
    }

    /**
     * <strong>Goal:</strong> Verify that a result order without a direction is
     * ascending, and that an offset pages the groups.
     * <p>
     * <strong>Start state:</strong> No prior state needed.
     * <p>
     * <strong>Workflow:</strong>
     * <ul>
     * <li>Parse
     * {@code calculate sum price group by category order by sum skip 5 limit 10}.</li>
     * </ul>
     * <p>
     * <strong>Expected:</strong> The order is the {@code sum} calculation
     * ascending, and the page skips {@code 5} and limits to {@code 10}.
     */
    @Test
    public void testResultOrderDefaultsToAscending() {
        GroupSymbol group = calculate("calculate sum price group by category "
                + "order by sum skip 5 limit 10").group();
        Assert.assertEquals(new ResultOrderSymbol(calculation("sum", "price"),
                DirectionSymbol.ASCENDING), group.order());
        Assert.assertEquals(PageSymbol.fromSkipLimit(5, 10), group.page());
    }

    /**
     * <strong>Goal:</strong> Verify that {@code stddev} parses over a list of
     * records.
     * <p>
     * <strong>Start state:</strong> No prior state needed.
     * <p>
     * <strong>Workflow:</strong>
     * <ul>
     * <li>Parse {@code calculate stddev salary in 1, 2, 3}.</li>
     * </ul>
     * <p>
     * <strong>Expected:</strong> The calculation is {@code stddev} of
     * {@code salary}, over records {@code 1}, {@code 2} and {@code 3}.
     */
    @Test
    public void testStddevParsesOverRecords() {
        CalculateSymbol symbol = calculate(
                "calculate stddev salary in 1, 2, 3");
        Assert.assertEquals(ImmutableList.of(calculation("stddev", "salary")),
                symbol.calculations());
        Assert.assertEquals(ImmutableList.of(1L, 2L, 3L),
                ImmutableList.copyOf(symbol.records()));
        Assert.assertNull(symbol.group());
    }

    /**
     * <strong>Goal:</strong> Verify the text of a symbol names every part of
     * the statement except its condition.
     * <p>
     * <strong>Start state:</strong> No prior state needed.
     * <p>
     * <strong>Workflow:</strong>
     * <ul>
     * <li>Parse grouped statements with a range, a having clause, a result
     * order and a page.</li>
     * <li>Read the text of each symbol.</li>
     * </ul>
     * <p>
     * <strong>Expected:</strong> The text is the statement without its
     * condition, with each duration in its largest exact unit.
     */
    @Test
    public void testTextNamesEveryPartButCondition() {
        Assert.assertEquals(
                "calculate count $id$ group by price per 10 between 0 and 100",
                calculate("calculate count $id$ group by price per 10 "
                        + "between 0 and 100").toString());
        Assert.assertEquals(
                "calculate count $id$, percentile 0.5 age in [1, 2] at 7 "
                        + "group by a, b.c having count >= 2 "
                        + "order by count desc skip 5 limit 10",
                calculate("calculate count $id$, percentile 0.5 age in 1, 2 "
                        + "at 7 group by a, b.c having count >= 2 "
                        + "order by count desc skip 5 limit 10").toString());
        Assert.assertEquals(
                "calculate count $id$ group by lastUsed per \"1 day\"",
                calculate("calculate count $id$ where _ = Credential "
                        + "group by lastUsed per \"24 hours\"").toString());
        Assert.assertEquals(
                "calculate count $id$ group by lastUsed per \"90 minutes\" "
                        + "between 1 and 2",
                calculate("calculate count $id$ group by lastUsed "
                        + "per \"1.5 h\" between 1 and 2").toString());
    }

    /**
     * <strong>Goal:</strong> Verify that the text of each new form parses back
     * to an equal symbol.
     * <p>
     * <strong>Start state:</strong> No prior state needed.
     * <p>
     * <strong>Workflow:</strong>
     * <ul>
     * <li>Parse each statement of the issue and each extra form.</li>
     * <li>Parse the text of each symbol.</li>
     * </ul>
     * <p>
     * <strong>Expected:</strong> Each text parses to a symbol equal to the
     * first, with an equal hash code.
     */
    @Test
    public void testTextParsesBackToEqualSymbol() {
        assertParsesBackFromItsText(
                "calculate countDistinct email where _ = User",
                "calculate median age",
                "calculate percentile 0.99 latency where service = api "
                        + "at \"yesterday\"",
                "calculate percentile 0.00001 latency",
                "calculate stddev salary in 1, 2, 3",
                "calculate count $id$ where _ = Application group by "
                        + "opportunity",
                "calculate count $id$ where active = true group by "
                        + "education.school.name order by count desc "
                        + "limit 10",
                "calculate count $id$ where _ = Application group by "
                        + "candidate, opportunity having count >= 2",
                "calculate count $id$ where _ = Credential group by lastUsed "
                        + "per \"1 day\"",
                "calculate count $id$ group by price per 10 between 0 and "
                        + "100",
                "calculate count $id$, sum price, avg price where active = "
                        + "true group by category",
                "calculate sum price in [1, 2, 3] at 1700000000 group by "
                        + "category per 0.25 between -1.5 and 2.5 "
                        + "having sum != 0 order by sum asc skip 5 limit 10",
                "calculate max name group by lastUsed per \"90 minutes\" "
                        + "between 1700000000000000 and 1700086400000000 "
                        + "having max = \"a b\" skip 3",
                "calculate variancePopulation age[1700000000] from 1",
                "calculate sum age at 1700000000", "calculate avg score from 1",
                "calculate count per group by per, having per 10 having count "
                        + ">= 2");
    }

    /**
     * <strong>Goal:</strong> Verify that {@code between} after a duration takes
     * timestamps.
     * <p>
     * <strong>Start state:</strong> No prior state needed.
     * <p>
     * <strong>Workflow:</strong>
     * <ul>
     * <li>Parse
     * {@code calculate count $id$ group by lastUsed per "1 h" between 1700000000000000 and 1700086400000000}.</li>
     * <li>Parse quoted dates as the bounds, and the same dates as the
     * {@code at} timestamp of a statement.</li>
     * </ul>
     * <p>
     * <strong>Expected:</strong> The period is one hour, and the start and end
     * are those timestamps. A quoted bound is the timestamp that an {@code at}
     * clause reads from the same text.
     */
    @Test
    public void testTimeRangeBetweenTakesTimestamps() {
        GroupSymbol group = calculate("calculate count $id$ group by lastUsed "
                + "per \"1 h\" between 1700000000000000 and 1700086400000000")
                .group();
        Assert.assertEquals(Duration.ofHours(1), group.period());
        Assert.assertEquals(new TimestampSymbol(1700000000000000L),
                group.start());
        Assert.assertEquals(new TimestampSymbol(1700086400000000L),
                group.end());
        Assert.assertNull(group.lowerBound());
        Assert.assertNull(group.upperBound());

        GroupSymbol dates = calculate("calculate count $id$ group by lastUsed "
                + "per \"1 day\" between \"2026-01-01 00:00:00\" and "
                + "\"2026-02-01 00:00:00\"").group();
        Assert.assertEquals(
                calculate("calculate count $id$ at \"2026-01-01 00:00:00\"")
                        .timestamp(),
                dates.start());
        Assert.assertEquals(
                calculate("calculate count $id$ at \"2026-02-01 00:00:00\"")
                        .timestamp(),
                dates.end());
    }

    /**
     * <strong>Goal:</strong> Verify that every calculate statement that parsed
     * before grouping parses to the same function, key, records and timestamp.
     * <p>
     * <strong>Start state:</strong> No prior state needed.
     * <p>
     * <strong>Workflow:</strong>
     * <ul>
     * <li>Parse a calculate statement without a scope, with one record, with
     * a list of records, with a condition, and with a timestamp.</li>
     * </ul>
     * <p>
     * <strong>Expected:</strong> Each symbol has one calculation, no group, and
     * the function, key, records and timestamp of its text.
     */
    @Test
    public void testUngroupedStatementsParseAsBefore() {
        CalculateSymbol sum = calculate("calculate sum age");
        Assert.assertEquals("sum", sum.function());
        Assert.assertEquals(new KeySymbol("age"), sum.key());
        Assert.assertEquals(1, sum.calculations().size());
        Assert.assertNull(sum.records());
        Assert.assertNull(sum.timestamp());
        Assert.assertNull(sum.group());

        CalculateSymbol avg = calculate("calculate AVG score from 1");
        Assert.assertEquals("avg", avg.function());
        Assert.assertEquals(ImmutableList.of(1L),
                ImmutableList.copyOf(avg.records()));

        CalculateSymbol average = calculate(
                "calculate average age in [1, 2, 3] at 1700000000");
        Assert.assertEquals("average", average.function());
        Assert.assertEquals(ImmutableList.of(1L, 2L, 3L),
                ImmutableList.copyOf(average.records()));
        Assert.assertEquals(new TimestampSymbol(1700000000),
                average.timestamp());

        CommandTree count = parse(
                "calculate count name where name = \"Jeff\" as of 1700000000");
        Assert.assertEquals("count",
                ((CalculateSymbol) count.root()).function());
        Assert.assertEquals(new TimestampSymbol(1700000000),
                ((CalculateSymbol) count.root()).timestamp());
        Assert.assertEquals(condition("name = \"Jeff\""),
                count.conditionTree());

        Assert.assertEquals(
                new CalculateSymbol("max", new KeySymbol("score"),
                        ImmutableList.of(1L, 2L), new TimestampSymbol(5)),
                calculate("calculate max score in [1, 2] at 5"));
    }

}
