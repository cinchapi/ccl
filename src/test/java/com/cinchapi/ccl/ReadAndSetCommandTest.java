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

import java.util.function.Function;

import org.junit.Assert;
import org.junit.Test;

import com.cinchapi.ccl.grammar.KeySymbol;
import com.cinchapi.ccl.grammar.ValueSymbol;
import com.cinchapi.ccl.grammar.command.FindAndSetSymbol;
import com.cinchapi.ccl.grammar.command.GetAndSetSymbol;
import com.cinchapi.ccl.grammar.command.SelectAndSetSymbol;
import com.cinchapi.ccl.syntax.CommandTree;
import com.cinchapi.ccl.type.Operator;
import com.cinchapi.concourse.util.Convert;
import com.google.common.collect.ImmutableList;

/**
 * Coverage for the {@code findAndSet}, {@code selectAndSet} and
 * {@code getAndSet} commands: the tree each form parses to, the aliases that
 * parse to the same tree, and the statements the grammar rejects.
 *
 * @author Jeff Nelson
 */
public class ReadAndSetCommandTest {

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
     * <strong>Goal:</strong> Verify that {@code findAndSet} exposes its set
     * clause and carries its condition, order and page the same way
     * {@code find} does.
     * <p>
     * <strong>Start state:</strong> No prior state needed.
     * <p>
     * <strong>Workflow:</strong>
     * <ul>
     * <li>Parse a {@code findAndSet} statement with a condition, an order, a
     * page and a set clause.</li>
     * <li>Parse the matching {@code find} statement.</li>
     * </ul>
     * <p>
     * <strong>Expected:</strong> The root is a {@link FindAndSetSymbol} whose
     * key is {@code status} and whose value is {@code claimed}, and the
     * children equal those of the {@code find} tree.
     */
    @Test
    public void testFindAndSetKeepsConditionOrderAndPageLikeFind() {
        CommandTree tree = parse("findAndSet status = pending "
                + "order by priority desc limit 1 set status as claimed");
        FindAndSetSymbol symbol = (FindAndSetSymbol) tree.root();
        Assert.assertEquals("FIND_AND_SET", symbol.type());
        Assert.assertEquals(new KeySymbol("status"), symbol.key());
        Assert.assertEquals(new ValueSymbol("claimed"), symbol.value());
        Assert.assertEquals(
                parse("find status = pending order by priority desc limit 1")
                        .children(),
                tree.children());
    }

    /**
     * <strong>Goal:</strong> Verify that order and page are optional in
     * {@code findAndSet}.
     * <p>
     * <strong>Start state:</strong> No prior state needed.
     * <p>
     * <strong>Workflow:</strong>
     * <ul>
     * <li>Parse {@code findAndSet a = 1 set b as 2}.</li>
     * </ul>
     * <p>
     * <strong>Expected:</strong> The tree has a condition, no order and no
     * page.
     */
    @Test
    public void testFindAndSetWithoutOrderOrPageHasOnlyCondition() {
        CommandTree tree = parse("findAndSet a = 1 set b as 2");
        Assert.assertNotNull(tree.conditionTree());
        Assert.assertNull(tree.orderTree());
        Assert.assertNull(tree.pageTree());
    }

    /**
     * <strong>Goal:</strong> Verify that {@code selectAndSet} exposes the keys
     * it reads and its set clause, and carries its condition, order and page
     * the same way {@code select} does.
     * <p>
     * <strong>Start state:</strong> No prior state needed.
     * <p>
     * <strong>Workflow:</strong>
     * <ul>
     * <li>Parse a {@code selectAndSet} statement that reads {@code name} and
     * {@code age}.</li>
     * <li>Parse the matching {@code select} statement.</li>
     * </ul>
     * <p>
     * <strong>Expected:</strong> The root is a {@link SelectAndSetSymbol} with
     * keys {@code [name, age]}, key {@code status} and value {@code claimed},
     * and the children equal those of the {@code select} tree.
     */
    @Test
    public void testSelectAndSetReadsListedKeysAndKeepsChildrenLikeSelect() {
        CommandTree tree = parse("selectAndSet [name, age] where "
                + "status = pending order by priority limit 1 "
                + "set status as claimed");
        SelectAndSetSymbol symbol = (SelectAndSetSymbol) tree.root();
        Assert.assertEquals("SELECT_AND_SET", symbol.type());
        Assert.assertEquals(
                ImmutableList.of(new KeySymbol("name"), new KeySymbol("age")),
                ImmutableList.copyOf(symbol.keys()));
        Assert.assertEquals(new KeySymbol("status"), symbol.key());
        Assert.assertEquals(new ValueSymbol("claimed"), symbol.value());
        Assert.assertEquals(parse("select [name, age] where status = pending "
                + "order by priority limit 1").children(), tree.children());
    }

    /**
     * <strong>Goal:</strong> Verify that {@code selectAndSet} accepts one key
     * and a list of keys without brackets, as {@code select} does.
     * <p>
     * <strong>Start state:</strong> No prior state needed.
     * <p>
     * <strong>Workflow:</strong>
     * <ul>
     * <li>Parse {@code selectAndSet name where a = 1 set b as 2}.</li>
     * <li>Parse {@code selectAndSet name, age where a = 1 set b as 2}.</li>
     * </ul>
     * <p>
     * <strong>Expected:</strong> The keys are {@code [name]} and
     * {@code [name, age]}.
     */
    @Test
    public void testSelectAndSetAcceptsSingleKeyAndBracketlessKeys() {
        SelectAndSetSymbol single = (SelectAndSetSymbol) parse(
                "selectAndSet name where a = 1 set b as 2").root();
        Assert.assertEquals(ImmutableList.of(new KeySymbol("name")),
                ImmutableList.copyOf(single.keys()));
        SelectAndSetSymbol many = (SelectAndSetSymbol) parse(
                "selectAndSet name, age where a = 1 set b as 2").root();
        Assert.assertEquals(
                ImmutableList.of(new KeySymbol("name"), new KeySymbol("age")),
                ImmutableList.copyOf(many.keys()));
    }

    /**
     * <strong>Goal:</strong> Verify that {@code selectAndSet} with no keys
     * reads every key.
     * <p>
     * <strong>Start state:</strong> No prior state needed.
     * <p>
     * <strong>Workflow:</strong>
     * <ul>
     * <li>Parse {@code selectAndSet where a = 1 set b as 2}.</li>
     * </ul>
     * <p>
     * <strong>Expected:</strong> {@link SelectAndSetSymbol#keys()} is
     * {@code null}.
     */
    @Test
    public void testSelectAndSetWithoutKeysReadsEveryKey() {
        SelectAndSetSymbol symbol = (SelectAndSetSymbol) parse(
                "selectAndSet where a = 1 set b as 2").root();
        Assert.assertNull(symbol.keys());
    }

    /**
     * <strong>Goal:</strong> Verify that {@code getAndSet} exposes the keys it
     * reads and its set clause, and carries its condition, order and page the
     * same way {@code get} does.
     * <p>
     * <strong>Start state:</strong> No prior state needed.
     * <p>
     * <strong>Workflow:</strong>
     * <ul>
     * <li>Parse a {@code getAndSet} statement that reads {@code name}.</li>
     * <li>Parse the matching {@code get} statement.</li>
     * </ul>
     * <p>
     * <strong>Expected:</strong> The root is a {@link GetAndSetSymbol} with
     * keys {@code [name]}, key {@code status} and value {@code claimed}, and
     * the children equal those of the {@code get} tree.
     */
    @Test
    public void testGetAndSetReadsListedKeysAndKeepsChildrenLikeGet() {
        CommandTree tree = parse("getAndSet name where status = pending "
                + "order by priority limit 1 set status as claimed");
        GetAndSetSymbol symbol = (GetAndSetSymbol) tree.root();
        Assert.assertEquals("GET_AND_SET", symbol.type());
        Assert.assertEquals(ImmutableList.of(new KeySymbol("name")),
                ImmutableList.copyOf(symbol.keys()));
        Assert.assertEquals(new KeySymbol("status"), symbol.key());
        Assert.assertEquals(new ValueSymbol("claimed"), symbol.value());
        Assert.assertEquals(parse("get name where status = pending "
                + "order by priority limit 1").children(), tree.children());
    }

    /**
     * <strong>Goal:</strong> Verify that {@code getAndSet} with no keys reads
     * every key.
     * <p>
     * <strong>Start state:</strong> No prior state needed.
     * <p>
     * <strong>Workflow:</strong>
     * <ul>
     * <li>Parse {@code getAndSet where a = 1 set b as 2}.</li>
     * </ul>
     * <p>
     * <strong>Expected:</strong> {@link GetAndSetSymbol#keys()} is
     * {@code null}.
     */
    @Test
    public void testGetAndSetWithoutKeysReadsEveryKey() {
        GetAndSetSymbol symbol = (GetAndSetSymbol) parse(
                "getAndSet where a = 1 set b as 2").root();
        Assert.assertNull(symbol.keys());
    }

    /**
     * <strong>Goal:</strong> Verify that each snake_case alias parses to the
     * same tree as its camelCase command name.
     * <p>
     * <strong>Start state:</strong> No prior state needed.
     * <p>
     * <strong>Workflow:</strong>
     * <ul>
     * <li>Parse each command with its camelCase name and with its snake_case
     * alias.</li>
     * </ul>
     * <p>
     * <strong>Expected:</strong> Each pair of trees is equal.
     */
    @Test
    public void testSnakeCaseAliasesParseToEqualTrees() {
        Assert.assertEquals(parse("findAndSet a = 1 limit 1 set b as 2"),
                parse("find_and_set a = 1 limit 1 set b as 2"));
        Assert.assertEquals(
                parse("selectAndSet name where a = 1 set b as 2"),
                parse("select_and_set name where a = 1 set b as 2"));
        Assert.assertEquals(parse("getAndSet where a = 1 set b as 2"),
                parse("get_and_set where a = 1 set b as 2"));
    }

    /**
     * <strong>Goal:</strong> Verify that the set clause accepts a quoted value
     * that contains spaces.
     * <p>
     * <strong>Start state:</strong> No prior state needed.
     * <p>
     * <strong>Workflow:</strong>
     * <ul>
     * <li>Parse {@code findAndSet a = 1 set note as "in progress"}.</li>
     * </ul>
     * <p>
     * <strong>Expected:</strong> The value is {@code in progress}.
     */
    @Test
    public void testSetClauseAcceptsQuotedValue() {
        FindAndSetSymbol symbol = (FindAndSetSymbol) parse(
                "findAndSet a = 1 set note as \"in progress\"").root();
        Assert.assertEquals(new ValueSymbol("in progress"), symbol.value());
    }

    /**
     * <strong>Goal:</strong> Verify that an {@code at} directly after a
     * comparison stays part of the condition, as it does for {@code find}.
     * <p>
     * <strong>Start state:</strong> No prior state needed.
     * <p>
     * <strong>Workflow:</strong>
     * <ul>
     * <li>Parse {@code findAndSet a = 1 at 123 set b as 2}.</li>
     * <li>Parse {@code find a = 1 at 123}.</li>
     * </ul>
     * <p>
     * <strong>Expected:</strong> The children of both trees are equal.
     */
    @Test
    public void testTrailingAtAfterComparisonStaysInCondition() {
        Assert.assertEquals(parse("find a = 1 at 123").children(),
                parse("findAndSet a = 1 at 123 set b as 2").children());
    }

    /**
     * <strong>Goal:</strong> Verify that each read-and-set command rejects a
     * command-level timestamp with a message that says so.
     * <p>
     * <strong>Start state:</strong> No prior state needed.
     * <p>
     * <strong>Workflow:</strong>
     * <ul>
     * <li>Parse each command with {@code as of} after its condition.</li>
     * <li>Parse each command with {@code at} after a parenthesized
     * condition.</li>
     * </ul>
     * <p>
     * <strong>Expected:</strong> Each parse fails with a
     * {@link SyntaxException} whose message says the command does not accept a
     * timestamp.
     */
    @Test
    public void testRejectsCommandTimestamp() {
        String message = "does not accept a timestamp";
        assertRejected("findAndSet a = 1 as of 123 set b as 2", message);
        assertRejected("findAndSet (a = 1) at 123 set b as 2", message);
        assertRejected("selectAndSet name where a = 1 as of 123 set b as 2",
                message);
        assertRejected("selectAndSet where (a = 1) at 123 set b as 2",
                message);
        assertRejected("getAndSet name where a = 1 as of 123 set b as 2",
                message);
        assertRejected("getAndSet where (a = 1) at 123 set b as 2", message);
    }

    /**
     * <strong>Goal:</strong> Verify that a missing or incomplete set clause is
     * a syntax error.
     * <p>
     * <strong>Start state:</strong> No prior state needed.
     * <p>
     * <strong>Workflow:</strong>
     * <ul>
     * <li>Parse a statement with no set clause.</li>
     * <li>Parse statements whose set clause lacks the {@code set} word, the
     * key, the value, or both the {@code as} and the value.</li>
     * </ul>
     * <p>
     * <strong>Expected:</strong> Each parse fails with a
     * {@link SyntaxException}.
     */
    @Test
    public void testRejectsMissingOrIncompleteSetClause() {
        assertRejected("findAndSet a = 1", null);
        assertRejected("findAndSet a = 1 set b", null);
        assertRejected("findAndSet a = 1 set b as", null);
        assertRejected("selectAndSet name where a = 1 set as 2", null);
        assertRejected("getAndSet name where a = 1 b as 2", null);
    }

    /**
     * <strong>Goal:</strong> Verify that {@code selectAndSet} and
     * {@code getAndSet} need {@code where}, and that {@code findAndSet} takes
     * none, as {@code find} does.
     * <p>
     * <strong>Start state:</strong> No prior state needed.
     * <p>
     * <strong>Workflow:</strong>
     * <ul>
     * <li>Parse {@code selectAndSet} and {@code getAndSet} with records in
     * place of a condition.</li>
     * <li>Parse {@code findAndSet} with {@code where}.</li>
     * </ul>
     * <p>
     * <strong>Expected:</strong> Each parse fails with a
     * {@link SyntaxException}.
     */
    @Test
    public void testReadPartRequiresWhereExceptForFindAndSet() {
        assertRejected("selectAndSet name from 1 set b as 2", null);
        assertRejected("getAndSet name from 1 set b as 2", null);
        assertRejected("findAndSet where a = 1 set b as 2", null);
    }

    /**
     * <strong>Goal:</strong> Verify that a set value that is a function with a
     * condition leaves the command's condition in place.
     * <p>
     * <strong>Start state:</strong> No prior state needed.
     * <p>
     * <strong>Workflow:</strong>
     * <ul>
     * <li>Parse {@code findAndSet a = 1 set b as avg(age, c > 1)}.</li>
     * <li>Parse {@code find a = 1}.</li>
     * </ul>
     * <p>
     * <strong>Expected:</strong> The children of both trees are equal.
     */
    @Test
    public void testSetClauseFunctionValueKeepsCommandCondition() {
        Assert.assertEquals(parse("find a = 1").children(),
                parse("findAndSet a = 1 set b as avg(age, c > 1)")
                        .children());
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
     * <li>For each read-and-set command, parse a statement twice, and parse
     * one variant for each of its keys, set key and set value.</li>
     * </ul>
     * <p>
     * <strong>Expected:</strong> The two parses are equal with equal hash
     * codes, and each variant is unequal to the statement.
     */
    @Test
    public void testStatementsThatDifferInOnePartParseToUnequalTrees() {
        assertEqualOnlyToItself("findAndSet a = 1 set b as 2",
                "findAndSet a = 1 set c as 2", "findAndSet a = 1 set b as 3");
        assertEqualOnlyToItself("selectAndSet name where a = 1 set b as 2",
                "selectAndSet where a = 1 set b as 2",
                "selectAndSet age where a = 1 set b as 2",
                "selectAndSet name where a = 1 set c as 2",
                "selectAndSet name where a = 1 set b as 3");
        assertEqualOnlyToItself("getAndSet name where a = 1 set b as 2",
                "getAndSet where a = 1 set b as 2",
                "getAndSet age where a = 1 set b as 2",
                "getAndSet name where a = 1 set c as 2",
                "getAndSet name where a = 1 set b as 3");
    }

}
