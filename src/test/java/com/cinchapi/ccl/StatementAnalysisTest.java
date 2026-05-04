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

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import org.junit.Assert;
import org.junit.Test;

import com.cinchapi.ccl.syntax.ConditionTree;
import com.cinchapi.ccl.type.Operator;
import com.cinchapi.concourse.util.Convert;
import com.google.common.collect.ImmutableSet;

/**
 * Coverage for the {@link StatementAnalysis} accessors introduced
 * alongside the bracket-timestamp paradigm.
 *
 * @author Jeff Nelson
 */
public class StatementAnalysisTest {

    private static final long T1 = 1700000001L;
    private static final long T2 = 1700000002L;
    private static final long T3 = 1700000003L;

    @Test
    public void testStorageKeysFlat() {
        Assert.assertEquals(ImmutableSet.of("name"),
                analyze("name = \"jeff\"").storageKeys());
    }

    @Test
    public void testStorageKeysFlatBracketed() {
        Assert.assertEquals(ImmutableSet.of("name"),
                analyze(String.format("name[%d] = \"jeff\"", T1))
                        .storageKeys());
    }

    @Test
    public void testStorageKeysAcrossLeaves() {
        Assert.assertEquals(ImmutableSet.of("name", "age"),
                analyze("name = \"jeff\" AND age > 30").storageKeys());
    }

    @Test
    public void testStorageKeysExplodesNavigation() {
        Assert.assertEquals(ImmutableSet.of("a", "b", "c"),
                analyze("a.b.c = \"X\"").storageKeys());
    }

    @Test
    public void testStorageKeysCombinesFlatAndNavigation() {
        Assert.assertEquals(ImmutableSet.of("name", "a", "b", "c"),
                analyze("name = \"jeff\" AND a.b.c = \"X\"").storageKeys());
    }

    @Test
    public void testStorageKeysIncludesScopePivot() {
        Assert.assertEquals(ImmutableSet.of("A", "foo", "bar"),
                analyze("A.(foo = \"X\" AND bar = \"Y\")").storageKeys());
    }

    @Test
    public void testStorageKeysExplodesScopePivotNavigation() {
        Assert.assertEquals(ImmutableSet.of("a", "b", "foo"),
                analyze("a.b.(foo = \"X\")").storageKeys());
    }

    @Test
    public void testStorageKeysWithOperatorFiltersByLeaf() {
        Assert.assertEquals(ImmutableSet.of("a", "b", "c"),
                analyze("a.b.c = \"X\" AND age > 30")
                        .storageKeys(OP_FN.apply("=")));
    }

    @Test
    public void testTemporalKeysFlatBracket() {
        Map<String, Set<Long>> temporal = analyze(
                String.format("name[%d] = \"jeff\"", T1)).temporalKeys();
        Assert.assertEquals(ImmutableSet.of(T1), temporal.get("name"));
        Assert.assertEquals(1, temporal.size());
    }

    @Test
    public void testTemporalKeysIgnoresLegacyTrailingAt() {
        Map<String, Set<Long>> temporal = analyze(
                String.format("name = \"jeff\" at %d", T1)).temporalKeys();
        Assert.assertTrue(
                "trailing-at is not a per-key bracket annotation; "
                        + "temporalKeys must not surface it",
                temporal.isEmpty());
    }

    @Test
    public void testTemporalKeysSameKeyMultipleTimes() {
        Map<String, Set<Long>> temporal = analyze(String.format(
                "name[%d] = \"jeff\" AND name[%d] = \"bob\"", T1, T2))
                        .temporalKeys();
        Assert.assertEquals(new HashSet<>(Arrays.asList(T1, T2)),
                temporal.get("name"));
    }

    @Test
    public void testTemporalKeysPerStopOnNavigation() {
        Map<String, Set<Long>> temporal = analyze(String
                .format("a[%d].b.c[%d] = \"X\"", T1, T2)).temporalKeys();
        Assert.assertEquals(ImmutableSet.of(T1), temporal.get("a"));
        Assert.assertEquals(ImmutableSet.of(T2), temporal.get("c"));
        Assert.assertFalse("b has no bracket and must not appear",
                temporal.containsKey("b"));
    }

    @Test
    public void testTemporalKeysOnScopePrefix() {
        Map<String, Set<Long>> temporal = analyze(
                String.format("A[%d].(foo = \"X\")", T1)).temporalKeys();
        Assert.assertEquals(ImmutableSet.of(T1), temporal.get("A"));
    }

    @Test
    public void testTemporalKeysWithOperator() {
        Map<String, Set<Long>> temporal = analyze(String.format(
                "name[%d] = \"jeff\" AND age[%d] > 30", T1, T2))
                        .temporalKeys(OP_FN.apply("="));
        Assert.assertEquals(ImmutableSet.of(T1), temporal.get("name"));
        Assert.assertFalse(temporal.containsKey("age"));
    }

    @Test
    public void testTransitiveNavigationKeysSingle() {
        Assert.assertEquals(ImmutableSet.of("friends"),
                analyze("friends*.name = \"jeff\"")
                        .transitiveNavigationKeys());
    }

    @Test
    public void testTransitiveNavigationKeysMultiple() {
        Assert.assertEquals(ImmutableSet.of("a", "b"),
                analyze("a*.b*.c = \"X\"").transitiveNavigationKeys());
    }

    @Test
    public void testTransitiveNavigationKeysOmitsNonTransitive() {
        Assert.assertEquals(ImmutableSet.of("friends"),
                analyze("friends*.name = \"jeff\" AND age > 30")
                        .transitiveNavigationKeys());
    }

    @Test
    public void testTransitiveNavigationKeysWithBracket() {
        Assert.assertEquals(ImmutableSet.of("friends"),
                analyze(String.format("friends[%d]*.name = \"jeff\"", T1))
                        .transitiveNavigationKeys());
    }

    @Test
    public void testNavigationKeysSingle() {
        Assert.assertEquals(ImmutableSet.of("a.b.c"),
                analyze("a.b.c = \"X\"").navigationKeys());
    }

    @Test
    public void testNavigationKeysCanonicalizesBrackets() {
        Assert.assertEquals(ImmutableSet.of("a.b"),
                analyze(String.format("a[%d].b[%d] = \"X\"", T1, T2))
                        .navigationKeys());
    }

    @Test
    public void testNavigationKeysPreservesTransitiveMarker() {
        Assert.assertEquals(ImmutableSet.of("friends*.name"),
                analyze("friends*.name = \"jeff\"").navigationKeys());
    }

    @Test
    public void testNavigationKeysExcludesFlat() {
        Assert.assertEquals(ImmutableSet.of("a.b"),
                analyze("name = \"jeff\" AND a.b = \"X\"")
                        .navigationKeys());
    }

    @Test
    public void testNavigationKeysIncludesScopePrefix() {
        Assert.assertEquals(ImmutableSet.of("a.b"),
                analyze("a.b.(foo = \"X\")").navigationKeys());
    }

    @Test
    public void testNavigationKeyStopsExcludesFlat() {
        Assert.assertEquals(ImmutableSet.of("a", "b"),
                analyze("name = \"jeff\" AND a.b = \"X\"")
                        .navigationKeyStops());
    }

    @Test
    public void testNavigationKeyStopsAcrossPaths() {
        Assert.assertEquals(ImmutableSet.of("a", "b", "x", "y"),
                analyze("a.b = \"X\" AND x.y = \"Y\"")
                        .navigationKeyStops());
    }

    @Test
    public void testNavigationKeyStopsEmptyForFlatOnly() {
        Assert.assertTrue(analyze("name = \"jeff\" AND age > 30")
                .navigationKeyStops().isEmpty());
    }

    @Test
    public void testScopedKeysSingleScope() {
        Map<String, List<String>> scoped = analyze(
                "A.(foo = \"X\" AND bar = \"Y\")").scopedKeys();
        Assert.assertEquals(Arrays.asList("foo", "bar"), scoped.get("A"));
    }

    @Test
    public void testScopedKeysMultipleScopes() {
        Map<String, List<String>> scoped = analyze(
                "A.(foo = \"X\") AND B.(baz = \"Y\")").scopedKeys();
        Assert.assertEquals(Arrays.asList("foo"), scoped.get("A"));
        Assert.assertEquals(Arrays.asList("baz"), scoped.get("B"));
    }

    @Test
    public void testScopedKeysNestedListsInnerPivotInOuter() {
        Map<String, List<String>> scoped = analyze(
                "A.(foo = \"X\" AND B.(bar = \"Y\"))").scopedKeys();
        Assert.assertEquals(Arrays.asList("foo", "B"), scoped.get("A"));
        Assert.assertEquals(Arrays.asList("bar"), scoped.get("B"));
    }

    @Test
    public void testScopedKeysExplodesInnerNavigationKey() {
        Map<String, List<String>> scoped = analyze("A.(a.b = \"X\")")
                .scopedKeys();
        Assert.assertEquals(Arrays.asList("a", "b"), scoped.get("A"));
    }

    @Test
    public void testScopedKeysWithOperatorFiltersScopes() {
        Map<String, List<String>> scoped = analyze(
                "A.(foo = \"X\") AND B.(baz > 0)")
                        .scopedKeys(OP_FN.apply("="));
        Assert.assertTrue(scoped.containsKey("A"));
        Assert.assertFalse(
                "B has no EQUALS expression directly inside it",
                scoped.containsKey("B"));
    }

    @Test
    public void testKeysUnchangedReturnsRawReferences() {
        Set<String> keys = analyze("name = \"jeff\" AND a.b.c = \"X\"")
                .keys();
        Assert.assertEquals(ImmutableSet.of("name", "a.b.c"), keys);
    }

    private StatementAnalysis analyze(String ccl) {
        Compiler compiler = Compiler.create(VALUE_FN, OP_FN);
        ConditionTree tree = (ConditionTree) compiler.parse(ccl);
        return compiler.analyze(tree);
    }

    private static final Function<String, Object> VALUE_FN = Convert::stringToJava;
    private static final Function<String, Operator> OP_FN = Convert::stringToOperator;

}
