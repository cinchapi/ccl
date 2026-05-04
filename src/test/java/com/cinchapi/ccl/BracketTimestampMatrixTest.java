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
import java.util.concurrent.TimeUnit;
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
import com.cinchapi.ccl.util.NaturalLanguage;
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

    /**
     * <strong>Goal:</strong> Verify that {@code foo = X AND bar = Y}
     * parses with both leaves unstamped (no bracket annotation and no
     * expression-level timestamp).
     */
    @Test
    public void testF1_NoBrackets() {
        AndTree and = parseAnd("foo = \"X\" AND bar = \"Y\"");
        assertLeafKeyUnstamped(and.left());
        assertLeafKeyUnstamped(and.right());
    }

    /**
     * <strong>Goal:</strong> Verify that
     * {@code foo[t] = X AND bar[t] = Y} parses with both leaves
     * carrying a {@link TemporalKeySymbol} stamped at {@code t}.
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
     * <strong>Goal:</strong> Verify that the legacy trailing-{@code at}
     * form ({@code foo = X at t AND bar = Y at t}) parses with both
     * leaves carrying an expression-level {@link TimestampSymbol}.
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
     * <strong>Goal:</strong> Verify that distinct per-leaf bracket
     * timestamps ({@code foo[t1] = X AND bar[t2] = Y}) parse to
     * independent {@link TemporalKeySymbol TemporalKeySymbols}.
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
     * <strong>Goal:</strong> Verify that the legacy trailing-{@code at}
     * form with distinct per-leaf timestamps
     * ({@code foo = X at t1 AND bar = Y at t2}) parses to independent
     * expression-level {@link TimestampSymbol TimestampSymbols}.
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
     * <strong>Goal:</strong> Verify that a bracket annotation on one
     * leaf and a legacy trailing-{@code at} on the other
     * ({@code foo[t1] = X AND bar = Y at t2}) coexist, producing the
     * expected mix of {@link TemporalKeySymbol} key and
     * expression-level {@link TimestampSymbol}.
     */
    @Test
    public void testF6_MixedBracketAndLegacy() {
        String ccl = String.format(
                "foo[%d] = \"X\" AND bar = \"Y\" at %d", T1, T2);
        AndTree and = parseAnd(ccl);
        assertLeafKeyTemporal(and.left(), "foo", T1);
        assertLeafExpressionTimestamp(and.right(), T2);
    }

    /**
     * <strong>Goal:</strong> Verify that an unbracketed navigation key
     * ({@code a.foo = X}) parses to a {@link NavigationKeySymbol}
     * whose stops carry no timestamps.
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
     * <strong>Goal:</strong> Verify that a bracket on the first stop
     * ({@code a[t].foo = X}) pins only that stop, leaving the leaf
     * stop unstamped.
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
     * <strong>Goal:</strong> Verify that a bracket on the last stop
     * ({@code a.foo[t] = X}) pins only the leaf, leaving the first
     * stop unstamped.
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
     * <strong>Goal:</strong> Verify that distinct brackets on both
     * stops ({@code a[t1].foo[t2] = X}) parse to a
     * {@link NavigationKeySymbol} whose two stops carry independent
     * timestamps.
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
     * <strong>Goal:</strong> Verify that a three-stop navigation key
     * with distinct brackets ({@code a[t1].b[t2].foo[t3] = X}) parses
     * with a per-stop timestamp on every stop.
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
     * <strong>Goal:</strong> Verify that the legacy trailing-{@code at}
     * form on a navigation key ({@code a.b.foo = X at t}) leaves the
     * navigation stops unstamped and pins the timestamp on the
     * expression instead.
     */
    @Test
    public void testN6_LegacyWholeChain() {
        String ccl = String.format("a.b.foo = \"X\" at %d", T);
        ExpressionSymbol expr = parseExpression(ccl);
        Assert.assertTrue(expr.key() instanceof NavigationKeySymbol);
        for (NavigationKeyStop stop : ((NavigationKeySymbol) expr.key())
                .stops()) {
            Assert.assertNull(stop.timestamp());
        }
        Assert.assertEquals(T, expr.timestamp().timestamp());
    }

    /**
     * <strong>Goal:</strong> Verify that a per-stop bracket coexists
     * with a legacy trailing-{@code at}
     * ({@code a[t1].foo = X at t2}): the bracket pins the first stop,
     * the trailing-{@code at} pins the expression.
     */
    @Test
    public void testN7_MixedBracketAndLegacy() {
        String ccl = String.format("a[%d].foo = \"X\" at %d", T1, T2);
        ExpressionSymbol expr = parseExpression(ccl);
        NavigationKeySymbol nav = (NavigationKeySymbol) expr.key();
        Assert.assertEquals(T1,
                nav.stops().get(0).timestamp().timestamp());
        Assert.assertNull(nav.stops().get(1).timestamp());
        Assert.assertEquals(T2, expr.timestamp().timestamp());
    }

    /**
     * <strong>Goal:</strong> Verify that a transitive marker and a
     * bracket annotation ({@code a[t]*.foo = X}) coexist on the same
     * stop, with both flags reflected on the resulting
     * {@link NavigationKeyStop}.
     */
    @Test
    public void testN8_TransitiveAndBracket() {
        NavigationKeySymbol nav = parseNavigationKeyOf(
                String.format("a[%d]*.foo = \"X\"", T));
        List<NavigationKeyStop> stops = nav.stops();
        Assert.assertTrue(stops.get(0).isTransitive());
        Assert.assertEquals(T, stops.get(0).timestamp().timestamp());
        Assert.assertFalse(stops.get(1).isTransitive());
        Assert.assertNull(stops.get(1).timestamp());
    }

    /**
     * <strong>Goal:</strong> Verify that a standalone transitive stop
     * with a bracket annotation ({@code children[t]* = "X"}) folds the
     * bracket onto the sole {@link NavigationKeyStop} rather than
     * wrapping the {@link NavigationKeySymbol} in an outer
     * {@link TemporalKeySymbol}. Per-stop consumers reading
     * {@link NavigationKeySymbol#stops() stops()} must see the
     * timestamp.
     */
    @Test
    public void testN9_StandaloneTransitiveBracketFolds() {
        ExpressionSymbol expr = parseExpression(
                String.format("children[%d]* = \"X\"", T));
        Assert.assertFalse(
                "standalone transitive bracket must fold onto the stop "
                        + "rather than wrap the navigation symbol in a "
                        + "TemporalKeySymbol",
                expr.key() instanceof TemporalKeySymbol);
        Assert.assertTrue(expr.key() instanceof NavigationKeySymbol);
        NavigationKeySymbol nav = (NavigationKeySymbol) expr.key();
        List<NavigationKeyStop> stops = nav.stops();
        Assert.assertEquals(1, stops.size());
        Assert.assertEquals("children", stops.get(0).key());
        Assert.assertTrue(stops.get(0).isTransitive());
        Assert.assertEquals(T, stops.get(0).timestamp().timestamp());
    }

    /**
     * <strong>Goal:</strong> Verify that two adjacent bracket
     * annotations on a navigation key ({@code a.b[t1][t2] = X}) are
     * rejected at parse time. A single key carries at most one
     * bracket-timestamp annotation.
     */
    @Test
    public void testN10_DoubleBracketOnNavigationRejected() {
        assertDoubleBracketRejected(
                String.format("a.b[%d][%d] = \"X\"", T1, T2));
    }

    /**
     * <strong>Goal:</strong> Verify that two adjacent bracket
     * annotations on a single transitive key
     * ({@code children[t1]*[t2] = X}) are rejected at parse time.
     */
    @Test
    public void testN11_DoubleBracketOnTransitiveRejected() {
        assertDoubleBracketRejected(
                String.format("children[%d]*[%d] = \"X\"", T1, T2));
    }

    /**
     * <strong>Goal:</strong> Verify that the non-canonical
     * asterisk-then-bracket order on a standalone transitive key
     * ({@code children*[t] = X}) is rejected at parse time. The
     * canonical form binds the bracket to the key and lets the
     * transitive marker terminate the stop ({@code children[t]*}).
     */
    @Test
    public void testN12_NonCanonicalAsteriskBracketOnSingleStopRejected() {
        assertNonCanonicalAsteriskBracketRejected(
                String.format("children*[%d] = \"X\"", T));
    }

    /**
     * <strong>Goal:</strong> Verify that the non-canonical
     * asterisk-then-bracket order on a multi-stop navigation key
     * ({@code a.b*[t] = X}) is rejected at parse time.
     */
    @Test
    public void testN13_NonCanonicalAsteriskBracketOnNavigationRejected() {
        assertNonCanonicalAsteriskBracketRejected(
                String.format("a.b*[%d] = \"X\"", T));
    }

    /**
     * <strong>Goal:</strong> Verify that an unbracketed scope
     * ({@code A.(foo = X AND bar = Y)}) parses to a
     * {@link ScopedConditionTree} with a plain {@link KeySymbol}
     * prefix and unstamped inner leaves.
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
     * <strong>Goal:</strong> Verify that bracket annotations on inner
     * leaves ({@code A.(foo[t] = X AND bar[t] = Y)}) pin the leaves
     * while leaving the scope prefix unstamped.
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
     * <strong>Goal:</strong> Verify that a bracket on the scope
     * prefix ({@code A[t].(foo = X AND bar = Y)}) pins the prefix's
     * traversal time while leaving inner leaves unstamped.
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
     * <strong>Goal:</strong> Verify that a bracket on both the scope
     * prefix and every inner leaf
     * ({@code A[t].(foo[t] = X AND bar[t] = Y)}) pins all three
     * positions independently at the same time.
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
     * <strong>Goal:</strong> Verify that the legacy trailing-{@code at}
     * form survives inside a scope
     * ({@code A.(foo = X at t AND bar = Y at t)}): the inner leaves
     * carry expression-level timestamps and the prefix is unstamped.
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
     * <strong>Goal:</strong> Verify that distinct per-leaf brackets
     * inside an unbracketed scope
     * ({@code A.(foo[t1] = X AND bar[t2] = Y)}) parse to independent
     * leaf-level timestamps with no prefix stamp.
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
     * <strong>Goal:</strong> Verify that distinct brackets at the
     * scope prefix and at each inner leaf
     * ({@code A[t3].(foo[t1] = X AND bar[t2] = Y)}) all parse to
     * independent timestamps with no leakage between positions.
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
     * <strong>Goal:</strong> Verify that a multi-stop scope prefix
     * with per-stop brackets ({@code a[t1].b[t2].(foo = X)}) parses to
     * a {@link NavigationKeySymbol} prefix carrying per-stop
     * timestamps.
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
     * <strong>Goal:</strong> Verify that a nested scope whose inner
     * prefix is bracketed ({@code A.(B[t].(foo = X) AND bar = Y)})
     * pins the inner scope's traversal time without affecting the
     * outer scope.
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
     * <strong>Goal:</strong> Verify that nested scopes with brackets
     * at both levels ({@code A[t1].(B[t2].(foo = X) AND bar = Y)})
     * carry independent prefix timestamps on each scope.
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

    /**
     * <strong>Goal:</strong> Verify that the legacy keyword forms
     * inside a leaf bracket ({@code foo[at t]}, {@code foo[on t]},
     * {@code foo[during t]}) produce ASTs equal to the canonical
     * keyword-less form ({@code foo[t]}).
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
     * <strong>Goal:</strong> Verify that the legacy keyword form on a
     * navigation key's per-stop brackets
     * ({@code a[at t1].foo[at t2]}) produces an AST equal to the
     * canonical keyword-less form ({@code a[t1].foo[t2]}).
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
     * <strong>Goal:</strong> Verify that the legacy keyword form on a
     * scope-prefix bracket ({@code A[at t].(foo = X)}) produces an
     * AST equal to the canonical keyword-less form
     * ({@code A[t].(foo = X)}).
     */
    @Test
    public void testK3_ScopeKeywordEquivalence() {
        AbstractSyntaxTree canonical = compiler()
                .parse(String.format("A[%d].(foo = \"X\")", T));
        AbstractSyntaxTree withKeyword = compiler()
                .parse(String.format("A[at %d].(foo = \"X\")", T));
        Assert.assertEquals(canonical, withKeyword);
    }

    /**
     * <strong>Goal:</strong> Verify that a natural-language timestamp on
     * a leaf bracket ({@code foo[last week] = "X"}) routes through the
     * grammar's {@code BracketedTimestamp} production to
     * {@link NaturalLanguage#parseMicros}. Compared at day precision
     * because natural-language phrases are anchored to the current
     * instant.
     */
    @Test
    public void testL1_LeafNaturalLanguageBracket() {
        TimestampSymbol expected = new TimestampSymbol(
                NaturalLanguage.parseMicros("last week"), TimeUnit.DAYS);
        ExpressionSymbol expr = parseExpression("foo[last week] = \"X\"");
        TemporalKeySymbol temporal = (TemporalKeySymbol) expr.key();
        Assert.assertEquals(expected, temporal.timestamp());
    }

    /**
     * <strong>Goal:</strong> Verify that a natural-language timestamp on
     * a navigation stop ({@code a[last week].foo = "X"}) — where the
     * lexer captures the bracket inside a {@code PERIOD_SEPARATED_STRING}
     * and {@link NavigationKeyStop} parses the content — routes to
     * {@link NaturalLanguage#parseMicros}. Compared at day precision.
     */
    @Test
    public void testL2_NavigationStopNaturalLanguageBracket() {
        TimestampSymbol expected = new TimestampSymbol(
                NaturalLanguage.parseMicros("last week"), TimeUnit.DAYS);
        NavigationKeySymbol nav = parseNavigationKeyOf(
                "a[last week].foo = \"X\"");
        Assert.assertEquals(expected, nav.stops().get(0).timestamp());
        Assert.assertNull(nav.stops().get(1).timestamp());
    }

    /**
     * <strong>Goal:</strong> Verify lossless round-trip of the F1
     * shape (no brackets, no trailing-{@code at}).
     */
    @Test
    public void testRoundTripF1() {
        assertRoundTrip("foo = \"X\" AND bar = \"Y\"");
    }

    /**
     * <strong>Goal:</strong> Verify lossless round-trip of the F2
     * shape (per-leaf brackets at the same timestamp).
     */
    @Test
    public void testRoundTripF2() {
        assertRoundTrip(String
                .format("foo[%d] = \"X\" AND bar[%d] = \"Y\"", T, T));
    }

    /**
     * <strong>Goal:</strong> Verify lossless round-trip of the F3
     * shape (legacy trailing-{@code at} on both leaves).
     */
    @Test
    public void testRoundTripF3() {
        assertRoundTrip(String.format(
                "foo = \"X\" at %d AND bar = \"Y\" at %d", T, T));
    }

    /**
     * <strong>Goal:</strong> Verify lossless round-trip of the F4
     * shape (per-leaf brackets with distinct timestamps).
     */
    @Test
    public void testRoundTripF4() {
        assertRoundTrip(String
                .format("foo[%d] = \"X\" AND bar[%d] = \"Y\"", T1, T2));
    }

    /**
     * <strong>Goal:</strong> Verify lossless round-trip of the F5
     * shape (legacy trailing-{@code at} with distinct timestamps).
     */
    @Test
    public void testRoundTripF5() {
        assertRoundTrip(String.format(
                "foo = \"X\" at %d AND bar = \"Y\" at %d", T1, T2));
    }

    /**
     * <strong>Goal:</strong> Verify lossless round-trip of the F6
     * shape (one leaf bracketed, the other carrying a legacy
     * trailing-{@code at}).
     */
    @Test
    public void testRoundTripF6() {
        assertRoundTrip(String.format(
                "foo[%d] = \"X\" AND bar = \"Y\" at %d", T1, T2));
    }

    /**
     * <strong>Goal:</strong> Verify lossless round-trip of the N1
     * shape (unbracketed two-stop navigation key).
     */
    @Test
    public void testRoundTripN1() {
        assertRoundTrip("a.foo = \"X\"");
    }

    /**
     * <strong>Goal:</strong> Verify lossless round-trip of the N2
     * shape (bracket on the first navigation stop).
     */
    @Test
    public void testRoundTripN2() {
        assertRoundTrip(String.format("a[%d].foo = \"X\"", T));
    }

    /**
     * <strong>Goal:</strong> Verify lossless round-trip of the N3
     * shape (bracket on the leaf navigation stop).
     */
    @Test
    public void testRoundTripN3() {
        assertRoundTrip(String.format("a.foo[%d] = \"X\"", T));
    }

    /**
     * <strong>Goal:</strong> Verify lossless round-trip of the N4
     * shape (distinct brackets on both stops of a two-stop
     * navigation key).
     */
    @Test
    public void testRoundTripN4() {
        assertRoundTrip(
                String.format("a[%d].foo[%d] = \"X\"", T1, T2));
    }

    /**
     * <strong>Goal:</strong> Verify lossless round-trip of the N5
     * shape (distinct brackets on three navigation stops).
     */
    @Test
    public void testRoundTripN5() {
        assertRoundTrip(String
                .format("a[%d].b[%d].foo[%d] = \"X\"", T1, T2, T3));
    }

    /**
     * <strong>Goal:</strong> Verify lossless round-trip of the N6
     * shape (legacy trailing-{@code at} on a navigation key).
     */
    @Test
    public void testRoundTripN6() {
        assertRoundTrip(String.format("a.b.foo = \"X\" at %d", T));
    }

    /**
     * <strong>Goal:</strong> Verify lossless round-trip of the N7
     * shape (per-stop bracket combined with legacy trailing-{@code at}).
     */
    @Test
    public void testRoundTripN7() {
        assertRoundTrip(
                String.format("a[%d].foo = \"X\" at %d", T1, T2));
    }

    /**
     * <strong>Goal:</strong> Verify lossless round-trip of the N8
     * shape (transitive marker plus bracket on the same stop).
     */
    @Test
    public void testRoundTripN8() {
        assertRoundTrip(String.format("a[%d]*.foo = \"X\"", T));
    }

    /**
     * <strong>Goal:</strong> Verify lossless round-trip of the N9
     * shape (standalone single transitive stop with a bracket).
     */
    @Test
    public void testRoundTripN9() {
        assertRoundTrip(String.format("children[%d]* = \"X\"", T));
    }

    /**
     * <strong>Goal:</strong> Verify lossless round-trip of the S1
     * shape (scope with no brackets).
     */
    @Test
    public void testRoundTripS1() {
        assertRoundTrip("A.(foo = \"X\" AND bar = \"Y\")");
    }

    /**
     * <strong>Goal:</strong> Verify lossless round-trip of the S2
     * shape (per-leaf brackets inside an unbracketed scope).
     */
    @Test
    public void testRoundTripS2() {
        assertRoundTrip(String.format(
                "A.(foo[%d] = \"X\" AND bar[%d] = \"Y\")", T, T));
    }

    /**
     * <strong>Goal:</strong> Verify lossless round-trip of the S3
     * shape (bracket on a single-key scope prefix).
     */
    @Test
    public void testRoundTripS3() {
        assertRoundTrip(String
                .format("A[%d].(foo = \"X\" AND bar = \"Y\")", T));
    }

    /**
     * <strong>Goal:</strong> Verify lossless round-trip of the S4
     * shape (brackets on the scope prefix and on every inner leaf).
     */
    @Test
    public void testRoundTripS4() {
        assertRoundTrip(String.format(
                "A[%d].(foo[%d] = \"X\" AND bar[%d] = \"Y\")", T, T, T));
    }

    /**
     * <strong>Goal:</strong> Verify lossless round-trip of the S5
     * shape (legacy trailing-{@code at} per leaf inside a scope).
     */
    @Test
    public void testRoundTripS5() {
        assertRoundTrip(String.format(
                "A.(foo = \"X\" at %d AND bar = \"Y\" at %d)", T, T));
    }

    /**
     * <strong>Goal:</strong> Verify lossless round-trip of the S6
     * shape (distinct per-leaf brackets inside an unbracketed scope).
     */
    @Test
    public void testRoundTripS6() {
        assertRoundTrip(String.format(
                "A.(foo[%d] = \"X\" AND bar[%d] = \"Y\")", T1, T2));
    }

    /**
     * <strong>Goal:</strong> Verify lossless round-trip of the S7
     * shape (distinct brackets on the scope prefix and on each inner
     * leaf).
     */
    @Test
    public void testRoundTripS7() {
        assertRoundTrip(String.format(
                "A[%d].(foo[%d] = \"X\" AND bar[%d] = \"Y\")", T3, T1,
                T2));
    }

    /**
     * <strong>Goal:</strong> Verify lossless round-trip of the S8
     * shape (multi-stop scope prefix with per-stop brackets).
     */
    @Test
    public void testRoundTripS8() {
        assertRoundTrip(
                String.format("a[%d].b[%d].(foo = \"X\")", T1, T2));
    }

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
        return (NavigationKeySymbol) parseExpression(ccl).key();
    }

    private ExpressionSymbol parseExpression(String ccl) {
        ExpressionTree leaf = (ExpressionTree) compiler().parse(ccl);
        return (ExpressionSymbol) leaf.root();
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

    private void assertDoubleBracketRejected(String ccl) {
        try {
            compiler().parse(ccl);
            Assert.fail("expected SyntaxException for double "
                    + "bracket-timestamp annotation in: " + ccl);
        }
        catch (SyntaxException e) {
            Assert.assertTrue(
                    "expected rejection message to mention 'two "
                            + "bracket-timestamp annotations' but was: "
                            + e.getMessage(),
                    e.getMessage().contains(
                            "two bracket-timestamp annotations"));
        }
    }

    private void assertNonCanonicalAsteriskBracketRejected(String ccl) {
        try {
            compiler().parse(ccl);
            Assert.fail("expected SyntaxException for non-canonical "
                    + "'key*[t]' order in: " + ccl);
        }
        catch (SyntaxException e) {
            Assert.assertTrue(
                    "expected rejection message to mention 'key*[t]' "
                            + "but was: " + e.getMessage(),
                    e.getMessage().contains("key*[t]"));
        }
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
