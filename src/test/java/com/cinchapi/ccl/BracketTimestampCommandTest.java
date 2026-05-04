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

import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

import org.junit.Assert;
import org.junit.Test;

import com.cinchapi.ccl.grammar.KeySymbol;
import com.cinchapi.ccl.grammar.KeyTokenSymbol;
import com.cinchapi.ccl.grammar.NavigationKeySymbol;
import com.cinchapi.ccl.grammar.NavigationKeyStop;
import com.cinchapi.ccl.grammar.OrderComponentSymbol;
import com.cinchapi.ccl.grammar.OrderSymbol;
import com.cinchapi.ccl.grammar.TemporalKeySymbol;
import com.cinchapi.ccl.grammar.command.BrowseSymbol;
import com.cinchapi.ccl.grammar.command.CalculateSymbol;
import com.cinchapi.ccl.grammar.command.GetSymbol;
import com.cinchapi.ccl.grammar.command.NavigateSymbol;
import com.cinchapi.ccl.grammar.command.SearchSymbol;
import com.cinchapi.ccl.grammar.command.SelectSymbol;
import com.cinchapi.ccl.grammar.command.VerifySymbol;
import com.cinchapi.ccl.syntax.AbstractSyntaxTree;
import com.cinchapi.ccl.syntax.CommandTree;
import com.cinchapi.ccl.syntax.OrderTree;
import com.cinchapi.ccl.type.Operator;
import com.cinchapi.concourse.util.Convert;

/**
 * Coverage for bracket-timestamp behavior across non-condition contexts:
 * read commands accept brackets (including mixed-time multi-key reads),
 * write commands reject brackets at parse time, and per-key brackets
 * coexist with a trailing-{@code at} on the same operation (the AST
 * preserves both so downstream consumers can apply the precedence rule
 * &mdash; bracket wins, trailing-{@code at} fills in unspecified keys).
 *
 * @author Jeff Nelson
 */
public class BracketTimestampCommandTest {

    private static final long T = 1700000000L;
    private static final long T1 = 1700000001L;
    private static final long T2 = 1700000002L;
    private static final long T3 = 1700000003L;

    @Test
    public void testSelectSingleKeyBracket() {
        SelectSymbol cmd = parseCommand(
                String.format("select name[%d] from 1", T), SelectSymbol.class);
        Assert.assertNull(cmd.timestamp());
        KeyTokenSymbol<?> key = cmd.keys().iterator().next();
        assertTemporalKey(key, "name", T);
    }

    @Test
    public void testSelectMultiKeyMixedBrackets() {
        SelectSymbol cmd = parseCommand(String.format(
                "select [name[%d], age[%d]] from 1", T1, T2),
                SelectSymbol.class);
        Iterator<KeyTokenSymbol<?>> it = cmd.keys().iterator();
        assertTemporalKey(it.next(), "name", T1);
        assertTemporalKey(it.next(), "age", T2);
    }

    @Test
    public void testSelectNavigationKeyPerStopBrackets() {
        SelectSymbol cmd = parseCommand(String.format(
                "select friends[%d].name[%d] from 1", T1, T2),
                SelectSymbol.class);
        NavigationKeySymbol nav = (NavigationKeySymbol) cmd.keys().iterator()
                .next();
        Assert.assertEquals(T1, nav.stops().get(0).timestamp().timestamp());
        Assert.assertEquals(T2, nav.stops().get(1).timestamp().timestamp());
    }

    @Test
    public void testGetSingleKeyBracket() {
        GetSymbol cmd = parseCommand(
                String.format("get name[%d] from 1", T), GetSymbol.class);
        assertTemporalKey(cmd.keys().iterator().next(), "name", T);
    }

    @Test
    public void testGetMultiKeyMixedBrackets() {
        GetSymbol cmd = parseCommand(String.format(
                "get [name[%d], age[%d]] from 1", T1, T2), GetSymbol.class);
        Iterator<KeyTokenSymbol<?>> it = cmd.keys().iterator();
        assertTemporalKey(it.next(), "name", T1);
        assertTemporalKey(it.next(), "age", T2);
    }

    @Test
    public void testBrowseBracket() {
        BrowseSymbol cmd = parseCommand(
                String.format("browse name[%d]", T), BrowseSymbol.class);
        assertTemporalKey(cmd.keys().iterator().next(), "name", T);
    }

    @Test
    public void testNavigateBracket() {
        NavigateSymbol cmd = parseCommand(
                String.format("navigate friends[%d].name[%d] from 1", T1, T2),
                NavigateSymbol.class);
        NavigationKeySymbol nav = (NavigationKeySymbol) cmd.keys().iterator()
                .next();
        Assert.assertEquals(T1, nav.stops().get(0).timestamp().timestamp());
        Assert.assertEquals(T2, nav.stops().get(1).timestamp().timestamp());
    }

    @Test
    public void testCalculateBracket() {
        CalculateSymbol cmd = parseCommand(
                String.format("calculate sum age[%d] from 1", T),
                CalculateSymbol.class);
        assertTemporalKey(cmd.key(), "age", T);
    }

    @Test
    public void testSearchBracket() {
        SearchSymbol cmd = parseCommand(
                String.format("search name[%d] for \"jeff\"", T),
                SearchSymbol.class);
        assertTemporalKey(cmd.key(), "name", T);
    }

    @Test
    public void testVerifyBracket() {
        VerifySymbol cmd = parseCommand(
                String.format("verify name[%d] as \"jeff\" in 1", T),
                VerifySymbol.class);
        assertTemporalKey(cmd.key(), "name", T);
    }

    @Test
    public void testOrderClauseBracket() {
        OrderTree order = parseOrder(
                String.format("order by name[%d] asc", T));
        OrderComponentSymbol comp = ((OrderSymbol) order.root()).components()
                .get(0);
        assertTemporalKey(comp.key(), "name", T);
    }

    @Test
    public void testFindWithBracketCondition() {
        AbstractSyntaxTree tree = compiler().parse(
                String.format("find name[%d] = \"jeff\"", T));
        Assert.assertTrue(tree instanceof CommandTree);
    }

    @Test
    public void testAddRejectsBracketKey() {
        assertCommandRejected(
                String.format("add name[%d] as \"jeff\" in 1", T),
                "add command");
    }

    @Test
    public void testSetRejectsBracketKey() {
        assertCommandRejected(
                String.format("set name[%d] as \"jeff\" in 1", T),
                "set command");
    }

    @Test
    public void testRemoveRejectsBracketKey() {
        assertCommandRejected(
                String.format("remove name[%d] as \"jeff\" in 1", T),
                "remove command");
    }

    @Test
    public void testClearRejectsBracketKey() {
        assertCommandRejected(String.format("clear name[%d] from 1", T),
                "clear command");
    }

    @Test
    public void testClearRejectsBracketKeyCollection() {
        assertCommandRejected(
                String.format("clear [name[%d], age] from 1", T),
                "clear command");
    }

    @Test
    public void testLinkRejectsBracketKey() {
        assertCommandRejected(
                String.format("link friends[%d] from 1 to 2", T),
                "link command");
    }

    @Test
    public void testUnlinkRejectsBracketKey() {
        assertCommandRejected(
                String.format("unlink friends[%d] from 1 to 2", T),
                "unlink command");
    }

    @Test
    public void testReconcileRejectsBracketKey() {
        assertCommandRejected(
                String.format("reconcile name[%d] in 1 with [\"jeff\"]", T),
                "reconcile command");
    }

    @Test
    public void testVerifyAndSwapRejectsBracketKey() {
        assertCommandRejected(
                String.format(
                        "verify_and_swap name[%d] as \"jeff\" in 1 with \"bob\"",
                        T),
                "verify_and_swap command");
    }

    @Test
    public void testVerifyOrSetRejectsBracketKey() {
        assertCommandRejected(
                String.format("verify_or_set name[%d] as \"jeff\" in 1", T),
                "verify_or_set command");
    }

    @Test
    public void testFindOrAddRejectsBracketKey() {
        assertCommandRejected(
                String.format("findOrAdd name[%d] as \"jeff\"", T),
                "find_or_add command");
    }

    @Test
    public void testRevertRejectsBracketKey() {
        assertCommandRejected(
                String.format("revert name[%d] in 1 at %d", T, T2),
                "revert command");
    }

    @Test
    public void testRevertRejectsBracketKeyCollection() {
        assertCommandRejected(
                String.format("revert [name[%d], age] in 1 at %d", T, T2),
                "revert command");
    }

    @Test
    public void testAuditRejectsBracketKey() {
        assertCommandRejected(String.format("audit name[%d] in 1", T),
                "audit command");
    }

    @Test
    public void testChronicleRejectsBracketKey() {
        assertCommandRejected(String.format("chronicle name[%d] in 1", T),
                "chronicle command");
    }

    @Test
    public void testDiffRejectsBracketKey() {
        assertCommandRejected(
                String.format("diff name[%d] in 1 from %d", T, T2),
                "diff command");
    }

    @Test
    public void testSelectMixedSingleKeyCoexists() {
        SelectSymbol cmd = parseCommand(String
                .format("select name[%d] from 1 at %d", T1, T2),
                SelectSymbol.class);
        Assert.assertEquals(T2, cmd.timestamp().timestamp());
        KeyTokenSymbol<?> key = cmd.keys().iterator().next();
        assertTemporalKey(key, "name", T1);
    }

    @Test
    public void testSelectMixedMultiKeyCoexists() {
        SelectSymbol cmd = parseCommand(String.format(
                "select [name[%d], age[%d]] from 1 at %d", T1, T2, T3),
                SelectSymbol.class);
        Assert.assertEquals(T3, cmd.timestamp().timestamp());
        Iterator<KeyTokenSymbol<?>> it = cmd.keys().iterator();
        assertTemporalKey(it.next(), "name", T1);
        assertTemporalKey(it.next(), "age", T2);
    }

    @Test
    public void testSelectMixedNavigationKeyCoexists() {
        SelectSymbol cmd = parseCommand(String.format(
                "select friends[%d].name[%d] from 1 at %d", T1, T2, T3),
                SelectSymbol.class);
        Assert.assertEquals(T3, cmd.timestamp().timestamp());
        NavigationKeySymbol nav = (NavigationKeySymbol) cmd.keys().iterator()
                .next();
        Assert.assertEquals(T1, nav.stops().get(0).timestamp().timestamp());
        Assert.assertEquals(T2, nav.stops().get(1).timestamp().timestamp());
    }

    @Test
    public void testSelectDefaultFillsUnbracketedKey() {
        SelectSymbol cmd = parseCommand(String.format(
                "select [name[%d], age] from 1 at %d", T1, T2),
                SelectSymbol.class);
        Assert.assertEquals(T2, cmd.timestamp().timestamp());
        Iterator<KeyTokenSymbol<?>> it = cmd.keys().iterator();
        assertTemporalKey(it.next(), "name", T1);
        KeyTokenSymbol<?> ageKey = it.next();
        Assert.assertFalse(
                "the unbracketed key must stay bare; the command-level "
                        + "timestamp is the engine's default fill",
                ageKey.isTemporal());
        Assert.assertEquals("age", ((KeySymbol) ageKey).key());
    }

    @Test
    public void testGetMixedMultiKeyCoexists() {
        GetSymbol cmd = parseCommand(String.format(
                "get [name[%d], age[%d]] from 1 at %d", T1, T2, T3),
                GetSymbol.class);
        Assert.assertEquals(T3, cmd.timestamp().timestamp());
        Iterator<KeyTokenSymbol<?>> it = cmd.keys().iterator();
        assertTemporalKey(it.next(), "name", T1);
        assertTemporalKey(it.next(), "age", T2);
    }

    @Test
    public void testBrowseMixedCoexists() {
        BrowseSymbol cmd = parseCommand(
                String.format("browse name[%d] at %d", T1, T2),
                BrowseSymbol.class);
        Assert.assertEquals(T2, cmd.timestamp().timestamp());
        assertTemporalKey(cmd.keys().iterator().next(), "name", T1);
    }

    @Test
    public void testCalculateMixedCoexists() {
        CalculateSymbol cmd = parseCommand(
                String.format("calculate sum age[%d] from 1 at %d", T1, T2),
                CalculateSymbol.class);
        Assert.assertEquals(T2, cmd.timestamp().timestamp());
        assertTemporalKey(cmd.key(), "age", T1);
    }

    @Test
    public void testVerifyMixedCoexists() {
        VerifySymbol cmd = parseCommand(String.format(
                "verify name[%d] as \"jeff\" in 1 at %d", T1, T2),
                VerifySymbol.class);
        Assert.assertEquals(T2, cmd.timestamp().timestamp());
        assertTemporalKey(cmd.key(), "name", T1);
    }

    @Test
    public void testOrderClauseMixedCoexists() {
        OrderTree order = parseOrder(
                String.format("order by name[%d] asc at %d", T1, T2));
        OrderComponentSymbol comp = ((OrderSymbol) order.root()).components()
                .get(0);
        Assert.assertEquals(T2, comp.timestamp().timestamp());
        assertTemporalKey(comp.key(), "name", T1);
    }

    @Test
    public void testFlatSearchKeyWithBracket() {
        AbstractSyntaxTree tree = compiler().parse(
                String.format("find name[%d] contains \"jeff\"", T));
        Assert.assertTrue(tree instanceof CommandTree);
        com.cinchapi.ccl.syntax.ConditionTree cond =
                ((CommandTree) tree).conditionTree();
        com.cinchapi.ccl.grammar.ExpressionSymbol expr =
                (com.cinchapi.ccl.grammar.ExpressionSymbol)
                        ((com.cinchapi.ccl.syntax.ExpressionTree) cond).root();
        assertTemporalKey(expr.key(), "name", T);
    }

    @Test
    public void testNavigationSearchKeyWithBracket() {
        AbstractSyntaxTree tree = compiler().parse(
                String.format("find friends.name[%d] contains \"jeff\"", T));
        Assert.assertTrue(tree instanceof CommandTree);
        com.cinchapi.ccl.syntax.ConditionTree cond =
                ((CommandTree) tree).conditionTree();
        com.cinchapi.ccl.grammar.ExpressionSymbol expr =
                (com.cinchapi.ccl.grammar.ExpressionSymbol)
                        ((com.cinchapi.ccl.syntax.ExpressionTree) cond).root();
        NavigationKeySymbol nav = (NavigationKeySymbol) expr.key();
        Assert.assertEquals(T,
                nav.stops().get(1).timestamp().timestamp());
    }

    @Test
    public void testFlatSearchKeyWithoutBracketStillParses() {
        AbstractSyntaxTree tree = compiler()
                .parse("find name contains \"jeff\"");
        Assert.assertTrue(tree instanceof CommandTree);
    }

    @Test
    public void testRelationalLeafMixedCoexists() {
        AbstractSyntaxTree tree = compiler().parse(
                String.format("name[%d] = \"jeff\" at %d", T1, T2));
        com.cinchapi.ccl.syntax.ExpressionTree expr = (com.cinchapi.ccl.syntax.ExpressionTree) tree;
        com.cinchapi.ccl.grammar.ExpressionSymbol sym = (com.cinchapi.ccl.grammar.ExpressionSymbol) expr
                .root();
        Assert.assertEquals(T2, sym.timestamp().timestamp());
        assertTemporalKey(sym.key(), "name", T1);
    }

    private Compiler compiler() {
        return Compiler.create(VALUE_FN, OP_FN);
    }

    private <T extends com.cinchapi.ccl.grammar.command.CommandSymbol> T parseCommand(
            String ccl, Class<T> type) {
        AbstractSyntaxTree tree = compiler().parse(ccl);
        Assert.assertTrue("expected a CommandTree but got " + tree.getClass(),
                tree instanceof CommandTree);
        com.cinchapi.ccl.grammar.command.CommandSymbol root = (com.cinchapi.ccl.grammar.command.CommandSymbol) ((CommandTree) tree)
                .root();
        Assert.assertTrue("expected " + type.getSimpleName() + " but got "
                + root.getClass(), type.isInstance(root));
        return type.cast(root);
    }

    private OrderTree parseOrder(String ccl) {
        AbstractSyntaxTree tree = compiler().parse(ccl);
        Assert.assertTrue("expected an OrderTree but got " + tree.getClass(),
                tree instanceof OrderTree);
        return (OrderTree) tree;
    }

    private void assertCommandRejected(String ccl, String contextLabel) {
        try {
            compiler().parse(ccl);
            Assert.fail(
                    "expected SyntaxException rejecting bracket on " + ccl);
        }
        catch (SyntaxException e) {
            Assert.assertTrue(
                    "expected message to mention '" + contextLabel
                            + "' but was: " + e.getMessage(),
                    e.getMessage().contains(contextLabel));
        }
    }

    private void assertTemporalKey(KeyTokenSymbol<?> key, String expectedKey,
            long expectedTs) {
        Assert.assertTrue(
                "expected TemporalKeySymbol but got "
                        + key.getClass().getSimpleName(),
                key instanceof TemporalKeySymbol);
        TemporalKeySymbol temporal = (TemporalKeySymbol) key;
        Assert.assertTrue(temporal.key() instanceof KeySymbol);
        Assert.assertEquals(expectedKey,
                ((KeySymbol) temporal.key()).key());
        Assert.assertEquals(expectedTs, temporal.timestamp().timestamp());
    }

    private static final Function<String, Object> VALUE_FN = Convert::stringToJava;
    private static final Function<String, Operator> OP_FN = Convert::stringToOperator;

}
