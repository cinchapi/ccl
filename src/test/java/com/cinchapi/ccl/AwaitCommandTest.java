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

import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;

import org.junit.Assert;
import org.junit.Test;

import com.cinchapi.ccl.grammar.KeySymbol;
import com.cinchapi.ccl.grammar.ValueSymbol;
import com.cinchapi.ccl.grammar.command.AwaitFindAndSetSymbol;
import com.cinchapi.ccl.grammar.command.AwaitFindSymbol;
import com.cinchapi.ccl.grammar.command.AwaitGetAndSetSymbol;
import com.cinchapi.ccl.grammar.command.AwaitGetSymbol;
import com.cinchapi.ccl.grammar.command.AwaitSelectAndSetSymbol;
import com.cinchapi.ccl.grammar.command.AwaitSelectSymbol;
import com.cinchapi.ccl.syntax.CommandTree;
import com.cinchapi.ccl.type.Operator;
import com.cinchapi.concourse.util.Convert;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

/**
 * Coverage for the {@code await} commands: the duration of the {@code within}
 * clause, the tree each form parses to, the aliases and optional words that
 * parse to the same tree, and the statements the grammar rejects.
 *
 * @author Jeff Nelson
 */
public class AwaitCommandTest {

    /**
     * The value transform the Concourse driver uses.
     */
    private static final Function<String, Object> VALUE_FN =
            Convert::stringToJava;

    /**
     * The operator transform the Concourse driver uses.
     */
    private static final Function<String, Operator> OP_FN =
            Convert::stringToOperator;

    /**
     * Parse {@code ccl} as one {@link CommandTree}.
     *
     * @param ccl the statement to parse
     * @return the {@link CommandTree}
     */
    private static CommandTree parse(String ccl) {
        return (CommandTree) Compiler.create(VALUE_FN, OP_FN).parse(ccl);
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
     * Assert that {@code ccl} parses to equal trees with equal hash codes on
     * two parses, and to a tree unequal to that of each of {@code others}.
     *
     * @param ccl the statement to parse
     * @param others statements that differ from {@code ccl} in one part
     */
    private static void assertEqualOnlyToItself(String ccl,
            String... others) {
        Assert.assertEquals(parse(ccl), parse(ccl));
        Assert.assertEquals(parse(ccl).hashCode(), parse(ccl).hashCode());
        for (String other : others) {
            Assert.assertNotEquals(other, parse(ccl), parse(other));
        }
    }

    /**
     * <strong>Goal:</strong> Verify that the {@code within} clause accepts each
     * unit and converts the duration to milliseconds.
     * <p>
     * <strong>Start state:</strong> No prior state needed.
     * <p>
     * <strong>Workflow:</strong>
     * <ul>
     * <li>Parse {@code awaitFind within "<duration>" a = 1} for each unit
     * name, including an upper case unit.</li>
     * </ul>
     * <p>
     * <strong>Expected:</strong> {@link AwaitFindSymbol#timeout()} is the
     * duration in milliseconds, worked out by hand for each case.
     */
    @Test
    public void testWithinAcceptsEachUnitAsMilliseconds() {
        Map<String, Long> expected = ImmutableMap.<String, Long> builder()
                .put("7 ms", 7L).put("7 millisecond", 7L)
                .put("7 milliseconds", 7L).put("2 s", 2000L)
                .put("2 second", 2000L).put("2 seconds", 2000L)
                .put("3 m", 180000L).put("3 minute", 180000L)
                .put("3 minutes", 180000L).put("2 SECONDS", 2000L)
                .put(" 4   s ", 4000L).build();
        for (Entry<String, Long> entry : expected.entrySet()) {
            AwaitFindSymbol symbol = (AwaitFindSymbol) parse(
                    "awaitFind within \"" + entry.getKey() + "\" a = 1")
                            .root();
            Assert.assertEquals(entry.getKey(), (long) entry.getValue(),
                    symbol.timeout());
        }
    }

    /**
     * <strong>Goal:</strong> Verify that the parser accepts a positive duration
     * of any length, and that a duration longer than {@link Long#MAX_VALUE}
     * milliseconds resolves to {@link Long#MAX_VALUE}.
     * <p>
     * <strong>Start state:</strong> No prior state needed.
     * <p>
     * <strong>Workflow:</strong>
     * <ul>
     * <li>Parse {@code awaitFind} with a 19-digit duration that fits in a
     * {@code long}, with {@link Long#MAX_VALUE} milliseconds, with a minute
     * count whose milliseconds overflow a {@code long}, and with a 20-digit
     * amount.</li>
     * </ul>
     * <p>
     * <strong>Expected:</strong> The timeouts are 10^18, then
     * {@link Long#MAX_VALUE} for each of the other three.
     */
    @Test
    public void testWithinAcceptsVeryLongDurations() {
        Map<String, Long> expected = ImmutableMap.<String, Long> builder()
                .put("1000000000000000000 ms", 1000000000000000000L)
                .put("9223372036854775807 ms", Long.MAX_VALUE)
                .put("200000000000000000 m", Long.MAX_VALUE)
                .put("99999999999999999999 s", Long.MAX_VALUE).build();
        for (Entry<String, Long> entry : expected.entrySet()) {
            AwaitFindSymbol symbol = (AwaitFindSymbol) parse(
                    "awaitFind within \"" + entry.getKey() + "\" a = 1")
                            .root();
            Assert.assertEquals(entry.getKey(), (long) entry.getValue(),
                    symbol.timeout());
        }
    }

    /**
     * <strong>Goal:</strong> Verify that {@code awaitFind} carries its
     * condition, order and page the same way {@code find} does, and that
     * {@code for} adds no meaning.
     * <p>
     * <strong>Start state:</strong> No prior state needed.
     * <p>
     * <strong>Workflow:</strong>
     * <ul>
     * <li>Parse an {@code awaitFind} statement with a condition, an order and
     * a page, with and without {@code for}.</li>
     * <li>Parse the matching {@code find} statement.</li>
     * </ul>
     * <p>
     * <strong>Expected:</strong> The root is an {@link AwaitFindSymbol} with a
     * timeout of 5000, the two {@code awaitFind} trees are equal, and their
     * children equal those of the {@code find} tree.
     */
    @Test
    public void testAwaitFindKeepsConditionOrderAndPageLikeFind() {
        CommandTree tree = parse("awaitFind within \"5 s\" for "
                + "status = pending order by priority desc skip 2 limit 1");
        AwaitFindSymbol symbol = (AwaitFindSymbol) tree.root();
        Assert.assertEquals("AWAIT_FIND", symbol.type());
        Assert.assertEquals(5000, symbol.timeout());
        Assert.assertEquals(tree, parse("awaitFind within \"5 s\" "
                + "status = pending order by priority desc skip 2 limit 1"));
        Assert.assertEquals(parse("find status = pending "
                + "order by priority desc skip 2 limit 1").children(),
                tree.children());
    }

    /**
     * <strong>Goal:</strong> Verify that {@code awaitSelect} exposes the keys
     * it reads and carries its condition, order and page the same way
     * {@code select} does.
     * <p>
     * <strong>Start state:</strong> No prior state needed.
     * <p>
     * <strong>Workflow:</strong>
     * <ul>
     * <li>Parse an {@code awaitSelect} statement that reads {@code name} and
     * {@code age}, with and without {@code for}.</li>
     * <li>Parse the matching {@code select} statement.</li>
     * </ul>
     * <p>
     * <strong>Expected:</strong> The keys are {@code [name, age]}, the timeout
     * is 1000, the two {@code awaitSelect} trees are equal, and their children
     * equal those of the {@code select} tree.
     */
    @Test
    public void testAwaitSelectReadsListedKeysAndKeepsChildrenLikeSelect() {
        CommandTree tree = parse("awaitSelect within \"1 s\" for "
                + "[name, age] where a = 1 order by name limit 5");
        AwaitSelectSymbol symbol = (AwaitSelectSymbol) tree.root();
        Assert.assertEquals("AWAIT_SELECT", symbol.type());
        Assert.assertEquals(1000, symbol.timeout());
        Assert.assertEquals(
                ImmutableList.of(new KeySymbol("name"), new KeySymbol("age")),
                ImmutableList.copyOf(symbol.keys()));
        Assert.assertEquals(tree, parse("awaitSelect within \"1 s\" "
                + "[name, age] where a = 1 order by name limit 5"));
        Assert.assertEquals(parse(
                "select [name, age] where a = 1 order by name limit 5")
                        .children(),
                tree.children());
    }

    /**
     * <strong>Goal:</strong> Verify that {@code awaitSelect} with no keys reads
     * every key.
     * <p>
     * <strong>Start state:</strong> No prior state needed.
     * <p>
     * <strong>Workflow:</strong>
     * <ul>
     * <li>Parse {@code awaitSelect within "1 s" where a = 1}.</li>
     * </ul>
     * <p>
     * <strong>Expected:</strong> {@link AwaitSelectSymbol#keys()} is
     * {@code null}.
     */
    @Test
    public void testAwaitSelectWithoutKeysReadsEveryKey() {
        AwaitSelectSymbol symbol = (AwaitSelectSymbol) parse(
                "awaitSelect within \"1 s\" where a = 1").root();
        Assert.assertNull(symbol.keys());
    }

    /**
     * <strong>Goal:</strong> Verify that {@code awaitGet} exposes the keys it
     * reads and carries its condition, order and page the same way {@code get}
     * does.
     * <p>
     * <strong>Start state:</strong> No prior state needed.
     * <p>
     * <strong>Workflow:</strong>
     * <ul>
     * <li>Parse an {@code awaitGet} statement that reads {@code name}, with
     * and without {@code for}.</li>
     * <li>Parse the matching {@code get} statement.</li>
     * </ul>
     * <p>
     * <strong>Expected:</strong> The keys are {@code [name]}, the timeout is
     * 60000, the two {@code awaitGet} trees are equal, and their children equal
     * those of the {@code get} tree.
     */
    @Test
    public void testAwaitGetReadsListedKeysAndKeepsChildrenLikeGet() {
        CommandTree tree = parse("awaitGet within \"1 minute\" for name "
                + "where a = 1 order by name limit 5");
        AwaitGetSymbol symbol = (AwaitGetSymbol) tree.root();
        Assert.assertEquals("AWAIT_GET", symbol.type());
        Assert.assertEquals(60000, symbol.timeout());
        Assert.assertEquals(ImmutableList.of(new KeySymbol("name")),
                ImmutableList.copyOf(symbol.keys()));
        Assert.assertEquals(tree, parse("awaitGet within \"1 minute\" name "
                + "where a = 1 order by name limit 5"));
        Assert.assertEquals(
                parse("get name where a = 1 order by name limit 5").children(),
                tree.children());
    }

    /**
     * <strong>Goal:</strong> Verify that {@code awaitGet} with no keys reads
     * every key.
     * <p>
     * <strong>Start state:</strong> No prior state needed.
     * <p>
     * <strong>Workflow:</strong>
     * <ul>
     * <li>Parse {@code awaitGet within "1 s" where a = 1}.</li>
     * </ul>
     * <p>
     * <strong>Expected:</strong> {@link AwaitGetSymbol#keys()} is {@code null}.
     */
    @Test
    public void testAwaitGetWithoutKeysReadsEveryKey() {
        AwaitGetSymbol symbol = (AwaitGetSymbol) parse(
                "awaitGet within \"1 s\" where a = 1").root();
        Assert.assertNull(symbol.keys());
    }

    /**
     * <strong>Goal:</strong> Verify that {@code awaitFindAndSet} exposes its
     * timeout and set clause, and carries its condition, order and page the
     * same way {@code find} does.
     * <p>
     * <strong>Start state:</strong> No prior state needed.
     * <p>
     * <strong>Workflow:</strong>
     * <ul>
     * <li>Parse an {@code awaitFindAndSet} statement with and without
     * {@code for}.</li>
     * <li>Parse the matching {@code find} statement.</li>
     * </ul>
     * <p>
     * <strong>Expected:</strong> The timeout is 30000, the key is
     * {@code status}, the value is {@code claimed}, the two
     * {@code awaitFindAndSet} trees are equal, and their children equal those
     * of the {@code find} tree.
     */
    @Test
    public void testAwaitFindAndSetExposesTimeoutAndSetClause() {
        CommandTree tree = parse("awaitFindAndSet within \"30 seconds\" for "
                + "status = pending order by priority limit 1 "
                + "set status as claimed");
        AwaitFindAndSetSymbol symbol = (AwaitFindAndSetSymbol) tree.root();
        Assert.assertEquals("AWAIT_FIND_AND_SET", symbol.type());
        Assert.assertEquals(30000, symbol.timeout());
        Assert.assertEquals(new KeySymbol("status"), symbol.key());
        Assert.assertEquals(new ValueSymbol("claimed"), symbol.value());
        Assert.assertEquals(tree,
                parse("awaitFindAndSet within \"30 seconds\" "
                        + "status = pending order by priority limit 1 "
                        + "set status as claimed"));
        Assert.assertEquals(
                parse("find status = pending order by priority limit 1")
                        .children(),
                tree.children());
    }

    /**
     * <strong>Goal:</strong> Verify that {@code awaitSelectAndSet} exposes its
     * timeout, the keys it reads and its set clause, and carries its condition,
     * order and page the same way {@code select} does.
     * <p>
     * <strong>Start state:</strong> No prior state needed.
     * <p>
     * <strong>Workflow:</strong>
     * <ul>
     * <li>Parse an {@code awaitSelectAndSet} statement that reads
     * {@code payload}, with and without {@code for}.</li>
     * <li>Parse the form with no keys.</li>
     * <li>Parse the matching {@code select} statement.</li>
     * </ul>
     * <p>
     * <strong>Expected:</strong> The keys are {@code [payload]}, the timeout is
     * 500, the set clause is {@code status} as {@code claimed}, the two keyed
     * trees are equal, their children equal those of the {@code select} tree,
     * and the form with no keys has {@code null} keys.
     */
    @Test
    public void testAwaitSelectAndSetExposesTimeoutKeysAndSetClause() {
        CommandTree tree = parse("awaitSelectAndSet within \"500 ms\" for "
                + "payload where status = pending limit 1 "
                + "set status as claimed");
        AwaitSelectAndSetSymbol symbol = (AwaitSelectAndSetSymbol) tree
                .root();
        Assert.assertEquals("AWAIT_SELECT_AND_SET", symbol.type());
        Assert.assertEquals(500, symbol.timeout());
        Assert.assertEquals(ImmutableList.of(new KeySymbol("payload")),
                ImmutableList.copyOf(symbol.keys()));
        Assert.assertEquals(new KeySymbol("status"), symbol.key());
        Assert.assertEquals(new ValueSymbol("claimed"), symbol.value());
        Assert.assertEquals(tree,
                parse("awaitSelectAndSet within \"500 ms\" payload where "
                        + "status = pending limit 1 set status as claimed"));
        Assert.assertEquals(
                parse("select payload where status = pending limit 1")
                        .children(),
                tree.children());
        AwaitSelectAndSetSymbol all = (AwaitSelectAndSetSymbol) parse(
                "awaitSelectAndSet within \"500 ms\" where a = 1 set b as 2")
                        .root();
        Assert.assertNull(all.keys());
    }

    /**
     * <strong>Goal:</strong> Verify that {@code awaitGetAndSet} exposes its
     * timeout, the keys it reads and its set clause, and carries its condition,
     * order and page the same way {@code get} does.
     * <p>
     * <strong>Start state:</strong> No prior state needed.
     * <p>
     * <strong>Workflow:</strong>
     * <ul>
     * <li>Parse an {@code awaitGetAndSet} statement that reads
     * {@code payload} and {@code owner}, with and without {@code for}.</li>
     * <li>Parse the form with no keys.</li>
     * <li>Parse the matching {@code get} statement.</li>
     * </ul>
     * <p>
     * <strong>Expected:</strong> The keys are {@code [payload, owner]}, the
     * timeout is 120000, the set clause is {@code status} as {@code claimed},
     * the two keyed trees are equal, their children equal those of the
     * {@code get} tree, and the form with no keys has {@code null} keys.
     */
    @Test
    public void testAwaitGetAndSetExposesTimeoutKeysAndSetClause() {
        CommandTree tree = parse("awaitGetAndSet within \"2 m\" for "
                + "[payload, owner] where status = pending order by priority "
                + "set status as claimed");
        AwaitGetAndSetSymbol symbol = (AwaitGetAndSetSymbol) tree.root();
        Assert.assertEquals("AWAIT_GET_AND_SET", symbol.type());
        Assert.assertEquals(120000, symbol.timeout());
        Assert.assertEquals(
                ImmutableList.of(new KeySymbol("payload"),
                        new KeySymbol("owner")),
                ImmutableList.copyOf(symbol.keys()));
        Assert.assertEquals(new KeySymbol("status"), symbol.key());
        Assert.assertEquals(new ValueSymbol("claimed"), symbol.value());
        Assert.assertEquals(tree, parse("awaitGetAndSet within \"2 m\" "
                + "[payload, owner] where status = pending order by priority "
                + "set status as claimed"));
        Assert.assertEquals(parse("get [payload, owner] where status = pending "
                + "order by priority").children(), tree.children());
        AwaitGetAndSetSymbol all = (AwaitGetAndSetSymbol) parse(
                "awaitGetAndSet within \"2 m\" where a = 1 set b as 2").root();
        Assert.assertNull(all.keys());
    }

    /**
     * <strong>Goal:</strong> Verify that each snake_case alias parses to the
     * same tree as its camelCase command name.
     * <p>
     * <strong>Start state:</strong> No prior state needed.
     * <p>
     * <strong>Workflow:</strong>
     * <ul>
     * <li>Parse each await command with its camelCase name and with its
     * snake_case alias.</li>
     * </ul>
     * <p>
     * <strong>Expected:</strong> Each pair of trees is equal.
     */
    @Test
    public void testSnakeCaseAliasesParseToEqualTrees() {
        String within = " within \"1 s\" ";
        Assert.assertEquals(parse("awaitFind" + within + "a = 1"),
                parse("await_find" + within + "a = 1"));
        Assert.assertEquals(parse("awaitSelect" + within + "where a = 1"),
                parse("await_select" + within + "where a = 1"));
        Assert.assertEquals(parse("awaitGet" + within + "name where a = 1"),
                parse("await_get" + within + "name where a = 1"));
        Assert.assertEquals(
                parse("awaitFindAndSet" + within + "a = 1 set b as 2"),
                parse("await_find_and_set" + within + "a = 1 set b as 2"));
        Assert.assertEquals(
                parse("awaitSelectAndSet" + within
                        + "name where a = 1 set b as 2"),
                parse("await_select_and_set" + within
                        + "name where a = 1 set b as 2"));
        Assert.assertEquals(
                parse("awaitGetAndSet" + within + "where a = 1 set b as 2"),
                parse("await_get_and_set" + within
                        + "where a = 1 set b as 2"));
    }

    /**
     * <strong>Goal:</strong> Verify that durations of equal length parse to
     * equal trees and durations of different length do not.
     * <p>
     * <strong>Start state:</strong> No prior state needed.
     * <p>
     * <strong>Workflow:</strong>
     * <ul>
     * <li>Parse {@code awaitFind} with {@code "2 s"} and with
     * {@code "2000 ms"}.</li>
     * <li>Parse {@code awaitFind} with {@code "2 s"} and with
     * {@code "3 s"}.</li>
     * </ul>
     * <p>
     * <strong>Expected:</strong> The first pair is equal and the second pair is
     * not.
     */
    @Test
    public void testEqualDurationsParseToEqualTrees() {
        Assert.assertEquals(parse("awaitFind within \"2 s\" a = 1"),
                parse("awaitFind within \"2000 ms\" a = 1"));
        Assert.assertNotEquals(parse("awaitFind within \"2 s\" a = 1"),
                parse("awaitFind within \"3 s\" a = 1"));
    }

    /**
     * <strong>Goal:</strong> Verify that a missing or unquoted duration is a
     * syntax error.
     * <p>
     * <strong>Start state:</strong> No prior state needed.
     * <p>
     * <strong>Workflow:</strong>
     * <ul>
     * <li>Parse {@code awaitFind} with no {@code within} clause, with
     * {@code within} and no duration, with empty quotes, and with an unquoted
     * duration.</li>
     * <li>Parse {@code awaitSelectAndSet} with no {@code within}
     * clause.</li>
     * </ul>
     * <p>
     * <strong>Expected:</strong> Each parse fails with a
     * {@link SyntaxException}.
     */
    @Test
    public void testRejectsMissingDuration() {
        assertRejected("awaitFind a = 1", null);
        assertRejected("awaitFind within a = 1", null);
        assertRejected("awaitFind within \"\" a = 1", null);
        assertRejected("awaitFind within 5 s a = 1", null);
        assertRejected("awaitSelectAndSet name where a = 1 set b as 2", null);
    }

    /**
     * <strong>Goal:</strong> Verify that a duration that is not a positive
     * integer followed by a known unit is a syntax error that names the
     * duration.
     * <p>
     * <strong>Start state:</strong> No prior state needed.
     * <p>
     * <strong>Workflow:</strong>
     * <ul>
     * <li>Parse {@code awaitFind} with a zero, negative, fractional,
     * unit-less, amount-less and unknown-unit duration.</li>
     * </ul>
     * <p>
     * <strong>Expected:</strong> Each parse fails with a
     * {@link SyntaxException} whose message contains the duration.
     */
    @Test
    public void testRejectsMalformedDuration() {
        for (String duration : ImmutableList.of("0 s", "-1 s", "1.5 s", "5",
                "s", "5 hours")) {
            assertRejected("awaitFind within \"" + duration + "\" a = 1",
                    "but got \"" + duration + "\"");
        }
    }

    /**
     * <strong>Goal:</strong> Verify that each await command rejects a
     * command-level timestamp with a message that says so.
     * <p>
     * <strong>Start state:</strong> No prior state needed.
     * <p>
     * <strong>Workflow:</strong>
     * <ul>
     * <li>Parse each await command with {@code as of} or a parenthesized
     * condition followed by {@code at}.</li>
     * </ul>
     * <p>
     * <strong>Expected:</strong> Each parse fails with a
     * {@link SyntaxException} whose message says the command does not accept a
     * timestamp.
     */
    @Test
    public void testRejectsCommandTimestamp() {
        String message = "does not accept a timestamp";
        String within = " within \"1 s\" ";
        assertRejected("awaitFind" + within + "a = 1 as of 123", message);
        assertRejected("awaitSelect" + within + "where (a = 1) at 123",
                message);
        assertRejected("awaitGet" + within + "name where a = 1 as of 123",
                message);
        assertRejected(
                "awaitFindAndSet" + within + "a = 1 as of 123 set b as 2",
                message);
        assertRejected("awaitSelectAndSet" + within
                + "where (a = 1) at 123 set b as 2", message);
        assertRejected("awaitGetAndSet" + within
                + "name where a = 1 as of 123 set b as 2", message);
    }

    /**
     * <strong>Goal:</strong> Verify that {@code for} may not stand directly
     * before {@code where}.
     * <p>
     * <strong>Start state:</strong> No prior state needed.
     * <p>
     * <strong>Workflow:</strong>
     * <ul>
     * <li>Parse {@code awaitSelect} and {@code awaitGetAndSet} with
     * {@code for where}.</li>
     * </ul>
     * <p>
     * <strong>Expected:</strong> Each parse fails with a
     * {@link SyntaxException}.
     */
    @Test
    public void testRejectsForBeforeWhere() {
        assertRejected("awaitSelect within \"1 s\" for where a = 1", null);
        assertRejected(
                "awaitGetAndSet within \"1 s\" for where a = 1 set b as 2",
                null);
    }

    /**
     * <strong>Goal:</strong> Verify that the await commands with a set clause
     * reject a missing or incomplete set clause.
     * <p>
     * <strong>Start state:</strong> No prior state needed.
     * <p>
     * <strong>Workflow:</strong>
     * <ul>
     * <li>Parse each await command with a set clause, leaving out the set
     * clause or part of it.</li>
     * </ul>
     * <p>
     * <strong>Expected:</strong> Each parse fails with a
     * {@link SyntaxException}.
     */
    @Test
    public void testRejectsMissingOrIncompleteSetClause() {
        assertRejected("awaitFindAndSet within \"1 s\" a = 1", null);
        assertRejected("awaitSelectAndSet within \"1 s\" where a = 1 set b",
                null);
        assertRejected("awaitGetAndSet within \"1 s\" where a = 1 set as 2",
                null);
    }

    /**
     * <strong>Goal:</strong> Verify that statements that differ in one part of
     * the command parse to unequal trees, and that two parses of one statement
     * are equal with equal hash codes.
     * <p>
     * <strong>Start state:</strong> No prior state needed.
     * <p>
     * <strong>Workflow:</strong>
     * <ul>
     * <li>For each await command, parse a statement twice, and parse one
     * variant for each of its timeout, keys, set key and set value.</li>
     * </ul>
     * <p>
     * <strong>Expected:</strong> The two parses are equal with equal hash
     * codes, and each variant is unequal to the statement.
     */
    @Test
    public void testStatementsThatDifferInOnePartParseToUnequalTrees() {
        assertEqualOnlyToItself("awaitFind within \"1 s\" a = 1",
                "awaitFind within \"2 s\" a = 1");
        assertEqualOnlyToItself("awaitSelect within \"1 s\" name where a = 1",
                "awaitSelect within \"2 s\" name where a = 1",
                "awaitSelect within \"1 s\" age where a = 1",
                "awaitSelect within \"1 s\" where a = 1");
        assertEqualOnlyToItself("awaitGet within \"1 s\" name where a = 1",
                "awaitGet within \"2 s\" name where a = 1",
                "awaitGet within \"1 s\" age where a = 1",
                "awaitGet within \"1 s\" where a = 1");
        assertEqualOnlyToItself(
                "awaitFindAndSet within \"1 s\" a = 1 set b as 2",
                "awaitFindAndSet within \"2 s\" a = 1 set b as 2",
                "awaitFindAndSet within \"1 s\" a = 1 set c as 2",
                "awaitFindAndSet within \"1 s\" a = 1 set b as 3");
        assertEqualOnlyToItself(
                "awaitSelectAndSet within \"1 s\" name where a = 1 set b as 2",
                "awaitSelectAndSet within \"2 s\" name where a = 1 set b as 2",
                "awaitSelectAndSet within \"1 s\" where a = 1 set b as 2",
                "awaitSelectAndSet within \"1 s\" name where a = 1 set c as 2",
                "awaitSelectAndSet within \"1 s\" name where a = 1 set b as 3");
        assertEqualOnlyToItself(
                "awaitGetAndSet within \"1 s\" name where a = 1 set b as 2",
                "awaitGetAndSet within \"2 s\" name where a = 1 set b as 2",
                "awaitGetAndSet within \"1 s\" age where a = 1 set b as 2",
                "awaitGetAndSet within \"1 s\" name where a = 1 set c as 2",
                "awaitGetAndSet within \"1 s\" name where a = 1 set b as 3");
    }

}
