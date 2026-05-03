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
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;

import com.cinchapi.ccl.grammar.ExpressionSymbol;
import com.cinchapi.ccl.grammar.KeySymbol;
import com.cinchapi.ccl.grammar.KeyTokenSymbol;
import com.cinchapi.ccl.grammar.NavigationKeyStop;
import com.cinchapi.ccl.grammar.NavigationKeySymbol;
import com.cinchapi.ccl.grammar.Symbol;
import com.cinchapi.ccl.grammar.TemporalKeySymbol;
import com.cinchapi.ccl.grammar.TimestampSymbol;
import com.cinchapi.ccl.syntax.AbstractSyntaxTree;
import com.cinchapi.ccl.syntax.AndTree;
import com.cinchapi.ccl.syntax.ConditionTree;
import com.cinchapi.ccl.syntax.ExpressionTree;
import com.cinchapi.ccl.syntax.ScopedConditionTree;
import com.cinchapi.ccl.type.Operator;
import com.cinchapi.concourse.util.Convert;

/**
 * Comprehensive tests for the bracket-timestamp syntax introduced by
 * GH-58. Each test maps to a row in the epic's test matrix (flat,
 * navigation, scoped, keyword-equivalence, round-trip).
 *
 * @author Jeff Nelson
 */
public class BracketTimestampMatrixTest {

    private static final long T = 1700000000L;
    private static final long T1 = 1700000001L;
    private static final long T2 = 1700000002L;
    private static final long T3 = 1700000003L;

    // ---------------------------------------------------------------
    // Flat criteria, simple keys (F1-F6)
    // ---------------------------------------------------------------

    /**
     * F1: {@code foo = X AND bar = Y} - both leaves unstamped.
     */
    @Test
    public void testF1_NoBrackets() {
        AndTree and = parseAnd("foo = \"X\" AND bar = \"Y\"");
        assertLeafKeyUnstamped(and.left());
        assertLeafKeyUnstamped(and.right());
    }

    /**
     * F2: {@code foo[t] = X AND bar[t] = Y} - both leaves bracket-stamped.
     */
    @Test
    public void testF2_BothLeavesBracketed() {
        String ccl = String.format("foo[%d] = \"X\" AND bar[%d] = \"Y\"",
                T, T);
        AndTree and = parseAnd(ccl);
        assertLeafKeyTemporal(and.left(), "foo", T);
        assertLeafKeyTemporal(and.right(), "bar", T);
    }

    /**
     * F3: {@code foo = X at t AND bar = Y at t} (legacy trailing-at).
     */
    @Test
    public void testF3_LegacyTrailingAt() {
        String ccl = String.format(
                "foo = \"X\" at %d AND bar = \"Y\" at %d", T, T);
        AndTree and = parseAnd(ccl);
        assertLeafExpressionTimestamp(and.left(), T);
        assertLeafExpressionTimestamp(and.right(), T);
    }

    /**
     * F4: {@code foo[t1] = X AND bar[t2] = Y} - distinct bracket stamps.
     */
    @Test
    public void testF4_DistinctBrackets() {
        String ccl = String.format("foo[%d] = \"X\" AND bar[%d] = \"Y\"",
                T1, T2);
        AndTree and = parseAnd(ccl);
        assertLeafKeyTemporal(and.left(), "foo", T1);
        assertLeafKeyTemporal(and.right(), "bar", T2);
    }

    /**
     * F5: {@code foo = X at t1 AND bar = Y at t2} (legacy distinct).
     */
    @Test
    public void testF5_LegacyTrailingAtDistinct() {
        String ccl = String.format(
                "foo = \"X\" at %d AND bar = \"Y\" at %d", T1, T2);
        AndTree and = parseAnd(ccl);
        assertLeafExpressionTimestamp(and.left(), T1);
        assertLeafExpressionTimestamp(and.right(), T2);
    }

    /**
     * F6: {@code foo[t1] = X AND bar = Y at t2} - mixed bracket and
     * legacy trailing-at.
     */
    @Test
    public void testF6_MixedBracketAndLegacy() {
        String ccl = String.format(
                "foo[%d] = \"X\" AND bar = \"Y\" at %d", T1, T2);
        AndTree and = parseAnd(ccl);
        assertLeafKeyTemporal(and.left(), "foo", T1);
        assertLeafExpressionTimestamp(and.right(), T2);
    }

    // ---------------------------------------------------------------
    // Flat criteria, navigation keys (N1-N8)
    // ---------------------------------------------------------------

    /**
     * N1: {@code a.foo = X} - no brackets, no per-stop timestamps.
     */
    @Test
    public void testN1_NavigationNoBrackets() {
        NavigationKeySymbol nav = parseNavigationKeyOf("a.foo = \"X\"");
        List<NavigationKeyStop> stops = nav.stops();
        Assert.assertEquals(2, stops.size());
        Assert.assertNull(stops.get(0).timestamp());
        Assert.assertNull(stops.get(1).timestamp());
    }

    /**
     * N2: {@code a[t].foo = X} - first stop bracketed.
     */
    @Test
    public void testN2_FirstStopBracketed() {
        NavigationKeySymbol nav = parseNavigationKeyOf(
                String.format("a[%d].foo = \"X\"", T));
        List<NavigationKeyStop> stops = nav.stops();
        Assert.assertEquals(2, stops.size());
        Assert.assertEquals(T, stops.get(0).timestamp().timestamp());
        Assert.assertNull(stops.get(1).timestamp());
    }

    /**
     * N3: {@code a.foo[t] = X} - last stop bracketed.
     */
    @Test
    public void testN3_LastStopBracketed() {
        NavigationKeySymbol nav = parseNavigationKeyOf(
                String.format("a.foo[%d] = \"X\"", T));
        List<NavigationKeyStop> stops = nav.stops();
        Assert.assertEquals(2, stops.size());
        Assert.assertNull(stops.get(0).timestamp());
        Assert.assertEquals(T, stops.get(1).timestamp().timestamp());
    }

    /**
     * N4: {@code a[t1].foo[t2] = X} - both stops bracketed, distinct.
     */
    @Test
    public void testN4_BothStopsBracketed() {
        NavigationKeySymbol nav = parseNavigationKeyOf(
                String.format("a[%d].foo[%d] = \"X\"", T1, T2));
        List<NavigationKeyStop> stops = nav.stops();
        Assert.assertEquals(2, stops.size());
        Assert.assertEquals(T1, stops.get(0).timestamp().timestamp());
        Assert.assertEquals(T2, stops.get(1).timestamp().timestamp());
    }

    /**
     * N5: {@code a[t1].b[t2].foo[t3] = X} - three stops bracketed.
     */
    @Test
    public void testN5_ThreeStopsBracketed() {
        NavigationKeySymbol nav = parseNavigationKeyOf(String
                .format("a[%d].b[%d].foo[%d] = \"X\"", T1, T2, T3));
        List<NavigationKeyStop> stops = nav.stops();
        Assert.assertEquals(3, stops.size());
        Assert.assertEquals(T1, stops.get(0).timestamp().timestamp());
        Assert.assertEquals(T2, stops.get(1).timestamp().timestamp());
        Assert.assertEquals(T3, stops.get(2).timestamp().timestamp());
    }

    /**
     * N6: {@code a.b.foo at t} (legacy) - whole-chain stamping via
     * expression-level trailing-at.
     */
    @Test
    public void testN6_LegacyWholeChain() {
        String ccl = String.format("a.b.foo = \"X\" at %d", T);
        ConditionTree tree = (ConditionTree) compiler().parse(ccl);
        ExpressionTree leaf = (ExpressionTree) tree;
        ExpressionSymbol expr = (ExpressionSymbol) leaf.root();
        Assert.assertTrue(expr.key() instanceof NavigationKeySymbol);
        for (NavigationKeyStop stop : ((NavigationKeySymbol) expr.key())
                .stops()) {
            Assert.assertNull(stop.timestamp());
        }
        Assert.assertEquals(T, expr.timestamp().timestamp());
    }

    /**
     * N7: {@code a[t1].foo = X at t2} - bracket on stop, legacy
     * trailing-at on expression.
     */
    @Test
    public void testN7_MixedBracketAndLegacy() {
        String ccl = String.format("a[%d].foo = \"X\" at %d", T1, T2);
        ConditionTree tree = (ConditionTree) compiler().parse(ccl);
        ExpressionTree leaf = (ExpressionTree) tree;
        ExpressionSymbol expr = (ExpressionSymbol) leaf.root();
        NavigationKeySymbol nav = (NavigationKeySymbol) expr.key();
        Assert.assertEquals(T1,
                nav.stops().get(0).timestamp().timestamp());
        Assert.assertNull(nav.stops().get(1).timestamp());
        Assert.assertEquals(T2, expr.timestamp().timestamp());
    }

    /**
     * N8: {@code a*[t].foo = X} - transitive marker plus bracket on the
     * same stop.
     */
    @Test
    public void testN8_TransitiveAndBracket() {
        NavigationKeySymbol nav = parseNavigationKeyOf(
                String.format("a*[%d].foo = \"X\"", T));
        List<NavigationKeyStop> stops = nav.stops();
        Assert.assertTrue(stops.get(0).isTransitive());
        Assert.assertEquals(T, stops.get(0).timestamp().timestamp());
        Assert.assertFalse(stops.get(1).isTransitive());
        Assert.assertNull(stops.get(1).timestamp());
    }

    // ---------------------------------------------------------------
    // Scoped queries (S1-S10)
    // ---------------------------------------------------------------

    /**
     * S1: {@code A.(foo = X AND bar = Y)} - no brackets anywhere.
     */
    @Test
    public void testS1_ScopedNoBrackets() {
        ScopedConditionTree scoped = parseScoped(
                "A.(foo = \"X\" AND bar = \"Y\")");
        Assert.assertTrue(scoped.prefix() instanceof KeySymbol);
        AndTree inner = (AndTree) scoped.condition();
        assertLeafKeyUnstamped(inner.left());
        assertLeafKeyUnstamped(inner.right());
    }

    /**
     * S2: {@code A.(foo[t] = X AND bar[t] = Y)} - leaves bracketed,
     * prefix unstamped.
     */
    @Test
    public void testS2_ScopedLeafBrackets() {
        ScopedConditionTree scoped = parseScoped(String.format(
                "A.(foo[%d] = \"X\" AND bar[%d] = \"Y\")", T, T));
        Assert.assertTrue(scoped.prefix() instanceof KeySymbol);
        AndTree inner = (AndTree) scoped.condition();
        assertLeafKeyTemporal(inner.left(), "foo", T);
        assertLeafKeyTemporal(inner.right(), "bar", T);
    }

    /**
     * S3: {@code A[t].(foo = X AND bar = Y)} - prefix bracketed.
     */
    @Test
    public void testS3_ScopedPrefixBracketed() {
        ScopedConditionTree scoped = parseScoped(String
                .format("A[%d].(foo = \"X\" AND bar = \"Y\")", T));
        assertScopePrefixTemporal(scoped, "A", T);
        AndTree inner = (AndTree) scoped.condition();
        assertLeafKeyUnstamped(inner.left());
        assertLeafKeyUnstamped(inner.right());
    }

    /**
     * S4: {@code A[t].(foo[t] = X AND bar[t] = Y)} - scope and leaves
     * all stamped at the same time.
     */
    @Test
    public void testS4_ScopedPrefixAndLeavesBracketed() {
        ScopedConditionTree scoped = parseScoped(String.format(
                "A[%d].(foo[%d] = \"X\" AND bar[%d] = \"Y\")", T, T, T));
        assertScopePrefixTemporal(scoped, "A", T);
        AndTree inner = (AndTree) scoped.condition();
        assertLeafKeyTemporal(inner.left(), "foo", T);
        assertLeafKeyTemporal(inner.right(), "bar", T);
    }

    /**
     * S5: {@code A.(foo = X at t AND bar = Y at t)} - legacy
     * trailing-at per leaf.
     */
    @Test
    public void testS5_ScopedLegacyTrailingAt() {
        ScopedConditionTree scoped = parseScoped(String.format(
                "A.(foo = \"X\" at %d AND bar = \"Y\" at %d)", T, T));
        Assert.assertTrue(scoped.prefix() instanceof KeySymbol);
        AndTree inner = (AndTree) scoped.condition();
        assertLeafExpressionTimestamp(inner.left(), T);
        assertLeafExpressionTimestamp(inner.right(), T);
    }

    /**
     * S6: {@code A.(foo[t1] = X AND bar[t2] = Y)} - leaves with
     * distinct brackets.
     */
    @Test
    public void testS6_ScopedLeavesDistinctBrackets() {
        ScopedConditionTree scoped = parseScoped(String.format(
                "A.(foo[%d] = \"X\" AND bar[%d] = \"Y\")", T1, T2));
        Assert.assertTrue(scoped.prefix() instanceof KeySymbol);
        AndTree inner = (AndTree) scoped.condition();
        assertLeafKeyTemporal(inner.left(), "foo", T1);
        assertLeafKeyTemporal(inner.right(), "bar", T2);
    }

    /**
     * S7: {@code A[t3].(foo[t1] = X AND bar[t2] = Y)} - scope and
     * per-leaf brackets all distinct.
     */
    @Test
    public void testS7_ScopedAllDistinct() {
        ScopedConditionTree scoped = parseScoped(String.format(
                "A[%d].(foo[%d] = \"X\" AND bar[%d] = \"Y\")", T3, T1,
                T2));
        assertScopePrefixTemporal(scoped, "A", T3);
        AndTree inner = (AndTree) scoped.condition();
        assertLeafKeyTemporal(inner.left(), "foo", T1);
        assertLeafKeyTemporal(inner.right(), "bar", T2);
    }

    /**
     * S8: {@code a[t1].b[t2].(foo = X)} - multi-stop scope prefix with
     * per-stop brackets.
     */
    @Test
    public void testS8_ScopedMultiStopPrefix() {
        ScopedConditionTree scoped = parseScoped(
                String.format("a[%d].b[%d].(foo = \"X\")", T1, T2));
        Assert.assertTrue(scoped.prefix() instanceof NavigationKeySymbol);
        NavigationKeySymbol nav = (NavigationKeySymbol) scoped.prefix();
        Assert.assertEquals(T1,
                nav.stops().get(0).timestamp().timestamp());
        Assert.assertEquals(T2,
                nav.stops().get(1).timestamp().timestamp());
    }

    /**
     * S9: {@code A.(B[t].(foo = X) AND bar = Y)} - nested scope with
     * inner-scope bracket.
     */
    @Test
    public void testS9_NestedScopeInnerBracket() {
        ScopedConditionTree outer = parseScoped(String.format(
                "A.(B[%d].(foo = \"X\") AND bar = \"Y\")", T));
        Assert.assertTrue(outer.prefix() instanceof KeySymbol);
        AndTree and = (AndTree) outer.condition();
        ScopedConditionTree inner = (ScopedConditionTree) and.left();
        assertScopePrefixTemporal(inner, "B", T);
    }

    /**
     * S10: {@code A[t1].(B[t2].(foo = X) AND bar = Y)} - nested scopes
     * with brackets at both levels.
     */
    @Test
    public void testS10_NestedScopeBothBracketed() {
        ScopedConditionTree outer = parseScoped(String.format(
                "A[%d].(B[%d].(foo = \"X\") AND bar = \"Y\")", T1, T2));
        assertScopePrefixTemporal(outer, "A", T1);
        AndTree and = (AndTree) outer.condition();
        ScopedConditionTree inner = (ScopedConditionTree) and.left();
        assertScopePrefixTemporal(inner, "B", T2);
    }

    // ---------------------------------------------------------------
    // Bracket keyword equivalence (K1-K3)
    // ---------------------------------------------------------------

    /**
     * K1: {@code foo[t]}, {@code foo[at t]}, {@code foo[on t]},
     * {@code foo[during t]} all produce equal ASTs.
     */
    @Test
    public void testK1_FlatKeywordEquivalence() {
        AbstractSyntaxTree canonical = compiler()
                .parse(String.format("foo[%d] = \"X\"", T));
        for (String kw : new String[] { "at", "on", "during" }) {
            AbstractSyntaxTree variant = compiler().parse(
                    String.format("foo[%s %d] = \"X\"", kw, T));
            Assert.assertEquals(canonical, variant);
        }
    }

    /**
     * K2: navigation per-stop keyword equivalence.
     */
    @Test
    public void testK2_NavigationKeywordEquivalence() {
        AbstractSyntaxTree canonical = compiler().parse(
                String.format("a[%d].foo[%d] = \"X\"", T1, T2));
        AbstractSyntaxTree withKeyword = compiler().parse(String.format(
                "a[at %d].foo[at %d] = \"X\"", T1, T2));
        Assert.assertEquals(canonical, withKeyword);
    }

    /**
     * K3: scope-prefix keyword equivalence.
     */
    @Test
    public void testK3_ScopeKeywordEquivalence() {
        AbstractSyntaxTree canonical = compiler()
                .parse(String.format("A[%d].(foo = \"X\")", T));
        AbstractSyntaxTree withKeyword = compiler()
                .parse(String.format("A[at %d].(foo = \"X\")", T));
        Assert.assertEquals(canonical, withKeyword);
    }

    // ---------------------------------------------------------------
    // Round-trip stability for every matrix row
    // ---------------------------------------------------------------

    @Test
    public void testRoundTripF1() {
        assertRoundTrip("foo = \"X\" AND bar = \"Y\"");
    }

    @Test
    public void testRoundTripF2() {
        assertRoundTrip(String
                .format("foo[%d] = \"X\" AND bar[%d] = \"Y\"", T, T));
    }

    @Test
    public void testRoundTripF3() {
        assertRoundTrip(String.format(
                "foo = \"X\" at %d AND bar = \"Y\" at %d", T, T));
    }

    @Test
    public void testRoundTripF4() {
        assertRoundTrip(String
                .format("foo[%d] = \"X\" AND bar[%d] = \"Y\"", T1, T2));
    }

    @Test
    public void testRoundTripF5() {
        assertRoundTrip(String.format(
                "foo = \"X\" at %d AND bar = \"Y\" at %d", T1, T2));
    }

    @Test
    public void testRoundTripF6() {
        assertRoundTrip(String.format(
                "foo[%d] = \"X\" AND bar = \"Y\" at %d", T1, T2));
    }

    @Test
    public void testRoundTripN1() {
        assertRoundTrip("a.foo = \"X\"");
    }

    @Test
    public void testRoundTripN2() {
        assertRoundTrip(String.format("a[%d].foo = \"X\"", T));
    }

    @Test
    public void testRoundTripN3() {
        assertRoundTrip(String.format("a.foo[%d] = \"X\"", T));
    }

    @Test
    public void testRoundTripN4() {
        assertRoundTrip(
                String.format("a[%d].foo[%d] = \"X\"", T1, T2));
    }

    @Test
    public void testRoundTripN5() {
        assertRoundTrip(String
                .format("a[%d].b[%d].foo[%d] = \"X\"", T1, T2, T3));
    }

    @Test
    public void testRoundTripN6() {
        assertRoundTrip(String.format("a.b.foo = \"X\" at %d", T));
    }

    @Test
    public void testRoundTripN7() {
        assertRoundTrip(
                String.format("a[%d].foo = \"X\" at %d", T1, T2));
    }

    @Test
    public void testRoundTripN8() {
        assertRoundTrip(String.format("a*[%d].foo = \"X\"", T));
    }

    @Test
    public void testRoundTripS1() {
        assertRoundTrip("A.(foo = \"X\" AND bar = \"Y\")");
    }

    @Test
    public void testRoundTripS2() {
        assertRoundTrip(String.format(
                "A.(foo[%d] = \"X\" AND bar[%d] = \"Y\")", T, T));
    }

    @Test
    public void testRoundTripS3() {
        assertRoundTrip(String
                .format("A[%d].(foo = \"X\" AND bar = \"Y\")", T));
    }

    @Test
    public void testRoundTripS4() {
        assertRoundTrip(String.format(
                "A[%d].(foo[%d] = \"X\" AND bar[%d] = \"Y\")", T, T, T));
    }

    @Test
    public void testRoundTripS5() {
        assertRoundTrip(String.format(
                "A.(foo = \"X\" at %d AND bar = \"Y\" at %d)", T, T));
    }

    @Test
    public void testRoundTripS6() {
        assertRoundTrip(String.format(
                "A.(foo[%d] = \"X\" AND bar[%d] = \"Y\")", T1, T2));
    }

    @Test
    public void testRoundTripS7() {
        assertRoundTrip(String.format(
                "A[%d].(foo[%d] = \"X\" AND bar[%d] = \"Y\")", T3, T1,
                T2));
    }

    @Test
    public void testRoundTripS8() {
        assertRoundTrip(
                String.format("a[%d].b[%d].(foo = \"X\")", T1, T2));
    }

    // ---------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------

    private Compiler compiler() {
        return Compiler.create(VALUE_FN, OP_FN);
    }

    private AndTree parseAnd(String ccl) {
        ConditionTree tree = (ConditionTree) compiler().parse(ccl);
        return (AndTree) tree;
    }

    private ScopedConditionTree parseScoped(String ccl) {
        ConditionTree tree = (ConditionTree) compiler().parse(ccl);
        return (ScopedConditionTree) tree;
    }

    private NavigationKeySymbol parseNavigationKeyOf(String ccl) {
        ConditionTree tree = (ConditionTree) compiler().parse(ccl);
        ExpressionTree leaf = (ExpressionTree) tree;
        ExpressionSymbol expr = (ExpressionSymbol) leaf.root();
        return (NavigationKeySymbol) expr.key();
    }

    private void assertLeafKeyTemporal(AbstractSyntaxTree leaf,
            String expectedKey, long expectedTs) {
        ExpressionTree exprTree = (ExpressionTree) leaf;
        ExpressionSymbol expr = (ExpressionSymbol) exprTree.root();
        KeyTokenSymbol<?> key = expr.key();
        Assert.assertTrue(
                "expected TemporalKeySymbol but was "
                        + key.getClass().getSimpleName(),
                key instanceof TemporalKeySymbol);
        TemporalKeySymbol temporal = (TemporalKeySymbol) key;
        Assert.assertTrue(temporal.key() instanceof KeySymbol);
        Assert.assertEquals(expectedKey,
                ((KeySymbol) temporal.key()).key().toString());
        Assert.assertEquals(expectedTs,
                temporal.timestamp().timestamp());
    }

    private void assertLeafKeyUnstamped(AbstractSyntaxTree leaf) {
        ExpressionTree exprTree = (ExpressionTree) leaf;
        ExpressionSymbol expr = (ExpressionSymbol) exprTree.root();
        Assert.assertFalse(
                "expected unstamped key but found TemporalKeySymbol",
                expr.key() instanceof TemporalKeySymbol);
        TimestampSymbol ts = expr.timestamp();
        if(ts != null) {
            Assert.assertEquals(0L, ts.timestamp());
        }
    }

    private void assertLeafExpressionTimestamp(AbstractSyntaxTree leaf,
            long expectedTs) {
        ExpressionTree exprTree = (ExpressionTree) leaf;
        ExpressionSymbol expr = (ExpressionSymbol) exprTree.root();
        Assert.assertEquals(expectedTs, expr.timestamp().timestamp());
    }

    private void assertScopePrefixTemporal(ScopedConditionTree scoped,
            String expectedKey, long expectedTs) {
        Assert.assertTrue(scoped.prefix() instanceof TemporalKeySymbol);
        TemporalKeySymbol temporal = (TemporalKeySymbol) scoped.prefix();
        Assert.assertTrue(temporal.key() instanceof KeySymbol);
        Assert.assertEquals(expectedKey,
                ((KeySymbol) temporal.key()).key().toString());
        Assert.assertEquals(expectedTs,
                temporal.timestamp().timestamp());
    }

    private void assertRoundTrip(String ccl) {
        Compiler compiler = compiler();
        AbstractSyntaxTree first = compiler.parse(ccl);
        String reemitted = compiler.tokenize(first).stream()
                .map(Symbol::toString)
                .collect(Collectors.joining(" "));
        AbstractSyntaxTree second = compiler.parse(reemitted);
        Assert.assertEquals(
                "round-trip mismatch for: " + ccl
                        + " (re-emitted: " + reemitted + ")",
                first, second);
    }

    private static final Function<String, Object> VALUE_FN = Convert::stringToJava;
    private static final Function<String, Operator> OP_FN = Convert::stringToOperator;

}
