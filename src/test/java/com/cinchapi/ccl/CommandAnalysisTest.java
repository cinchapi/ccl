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
import java.util.Set;
import java.util.function.Function;

import org.junit.Assert;
import org.junit.Test;

import com.cinchapi.ccl.syntax.CommandTree;
import com.cinchapi.ccl.type.Operator;
import com.cinchapi.concourse.util.Convert;
import com.google.common.collect.ImmutableSet;

/**
 * Coverage for {@link CommandAnalysis} — the
 * {@link Compiler#analyze(CommandTree)} accessors that aggregate
 * selection-side and condition-side keys, surface the command-level
 * timestamp (or range), and expose the referenced record set.
 *
 * @author Jeff Nelson
 */
public class CommandAnalysisTest {

    private static final long T1 = 1700000001L;
    private static final long T2 = 1700000002L;
    private static final long T3 = 1700000003L;

    @Test
    public void testCommandTypeIsExposed() {
        Assert.assertEquals("SELECT",
                analyze("select name from 1").commandType());
        Assert.assertEquals("FIND",
                analyze("find name = \"jeff\"").commandType());
        Assert.assertEquals("ADD",
                analyze("add name as \"jeff\" in 1").commandType());
    }

    @Test
    public void testStorageKeysFromSelectionOnly() {
        Assert.assertEquals(ImmutableSet.of("name", "age"),
                analyze("select [name, age] from 1").storageKeys());
    }

    @Test
    public void testStorageKeysFromConditionOnly() {
        Assert.assertEquals(ImmutableSet.of("name", "age"),
                analyze("find name = \"jeff\" AND age > 30").storageKeys());
    }

    @Test
    public void testStorageKeysUnionsSelectionAndCondition() {
        Assert.assertEquals(ImmutableSet.of("name", "age", "active"),
                analyze("select [name, age] where active = true")
                        .storageKeys());
    }

    @Test
    public void testStorageKeysExplodesNavigationOnSelection() {
        Assert.assertEquals(ImmutableSet.of("friends", "name"),
                analyze("select friends.name from 1").storageKeys());
    }

    @Test
    public void testTemporalKeysFromSelection() {
        Map<String, Set<Long>> temporal = analyze(String
                .format("select name[%d] from 1", T1)).temporalKeys();
        Assert.assertEquals(ImmutableSet.of(T1), temporal.get("name"));
    }

    @Test
    public void testTemporalKeysFromCondition() {
        Map<String, Set<Long>> temporal = analyze(String
                .format("find name[%d] = \"jeff\"", T1)).temporalKeys();
        Assert.assertEquals(ImmutableSet.of(T1), temporal.get("name"));
    }

    @Test
    public void testTemporalKeysUnionsSelectionAndCondition() {
        Map<String, Set<Long>> temporal = analyze(String.format(
                "select name[%d] where age[%d] > 30", T1, T2))
                        .temporalKeys();
        Assert.assertEquals(ImmutableSet.of(T1), temporal.get("name"));
        Assert.assertEquals(ImmutableSet.of(T2), temporal.get("age"));
    }

    @Test
    public void testTemporalKeysSameStorageKeyFromBothSides() {
        Map<String, Set<Long>> temporal = analyze(String.format(
                "select name[%d] where name[%d] = \"jeff\"", T1, T2))
                        .temporalKeys();
        Assert.assertEquals(ImmutableSet.of(T1, T2),
                temporal.get("name"));
    }

    @Test
    public void testTransitiveNavigationKeysFromSelection() {
        Assert.assertEquals(ImmutableSet.of("friends"),
                analyze("select friends*.name from 1")
                        .transitiveNavigationKeys());
    }

    @Test
    public void testTransitiveNavigationKeysFromCondition() {
        Assert.assertEquals(ImmutableSet.of("friends"),
                analyze("find friends*.name = \"jeff\"")
                        .transitiveNavigationKeys());
    }

    @Test
    public void testNavigationKeysFromSelection() {
        Assert.assertEquals(ImmutableSet.of("friends.name"),
                analyze("select friends.name from 1").navigationKeys());
    }

    @Test
    public void testNavigationKeyStopsExcludesFlat() {
        Assert.assertEquals(ImmutableSet.of("friends", "name"),
                analyze("select [age, friends.name] from 1")
                        .navigationKeyStops());
    }

    @Test
    public void testCommandTimestampOnSelect() {
        Assert.assertEquals(Long.valueOf(T1), analyze(
                String.format("select name from 1 at %d", T1))
                        .commandTimestamp());
    }

    @Test
    public void testCommandTimestampOnFind() {
        // `as of` binds at the command level; `at` would be consumed by
        // RelationalExpression's optional trailing-Timestamp.
        Assert.assertEquals(Long.valueOf(T1),
                analyze(String.format("find name = \"jeff\" as of %d", T1))
                        .commandTimestamp());
    }

    @Test
    public void testCommandTimestampNullWhenAbsent() {
        Assert.assertNull(analyze("select name from 1").commandTimestamp());
    }

    @Test
    public void testRangeStartAndEndOnAudit() {
        CommandAnalysis analysis = analyze(
                String.format("audit name in 1 from %d to %d", T1, T2));
        Assert.assertNull(analysis.commandTimestamp());
        Assert.assertEquals(Long.valueOf(T1), analysis.rangeStart());
        Assert.assertEquals(Long.valueOf(T2), analysis.rangeEnd());
    }

    @Test
    public void testRangeStartAndEndOnChronicle() {
        CommandAnalysis analysis = analyze(String
                .format("chronicle name in 1 from %d to %d", T1, T2));
        Assert.assertEquals(Long.valueOf(T1), analysis.rangeStart());
        Assert.assertEquals(Long.valueOf(T2), analysis.rangeEnd());
    }

    @Test
    public void testRangeOpenEndedReturnsNullEnd() {
        CommandAnalysis analysis = analyze(
                String.format("audit name in 1 from %d", T1));
        Assert.assertEquals(Long.valueOf(T1), analysis.rangeStart());
        Assert.assertNull(analysis.rangeEnd());
    }

    @Test
    public void testRangeStartAndEndOnDiff() {
        CommandAnalysis analysis = analyze(
                String.format("diff name in 1 from %d to %d", T1, T2));
        Assert.assertNull(analysis.commandTimestamp());
        Assert.assertEquals(Long.valueOf(T1), analysis.rangeStart());
        Assert.assertEquals(Long.valueOf(T2), analysis.rangeEnd());
    }

    @Test
    public void testRangeOpenEndedOnDiff() {
        CommandAnalysis analysis = analyze(
                String.format("diff name in 1 from %d", T1));
        Assert.assertEquals(Long.valueOf(T1), analysis.rangeStart());
        Assert.assertNull(analysis.rangeEnd());
    }

    @Test
    public void testReferencedRecordsSingle() {
        Assert.assertEquals(ImmutableSet.of(1L),
                analyze("select name from 1").referencedRecords());
    }

    @Test
    public void testReferencedRecordsCollection() {
        Assert.assertEquals(ImmutableSet.of(1L, 2L, 3L), analyze(
                "select name from [1, 2, 3]").referencedRecords());
    }

    @Test
    public void testReferencedRecordsEmptyForWhereOnly() {
        Assert.assertTrue(
                analyze("find name = \"jeff\"").referencedRecords()
                        .isEmpty());
    }

    @Test
    public void testReferencedRecordsForWriteCommand() {
        Assert.assertEquals(ImmutableSet.of(1L),
                analyze("add name as \"jeff\" in 1").referencedRecords());
    }

    @Test
    public void testReferencedRecordsForVerifyCommand() {
        Assert.assertEquals(ImmutableSet.of(1L), analyze(
                "verify name as \"jeff\" in 1").referencedRecords());
    }

    @Test
    public void testReferencedRecordsForLinkCommand() {
        Assert.assertEquals(ImmutableSet.of(1L, 2L, 3L),
                analyze("link friends from 1 to [2, 3]")
                        .referencedRecords());
    }

    @Test
    public void testReferencedRecordsForUnlinkCommand() {
        Assert.assertEquals(ImmutableSet.of(1L, 2L),
                analyze("unlink friends from 1 to 2")
                        .referencedRecords());
    }

    @Test
    public void testReferencedRecordsForConsolidateCommand() {
        Assert.assertEquals(ImmutableSet.of(1L, 2L, 3L),
                analyze("consolidate 1 [2, 3]").referencedRecords());
    }

    @Test
    public void testKeysWithOperatorFiltersCondition() {
        CommandAnalysis analysis = analyze(
                "select name where age > 30 AND active = true");
        Assert.assertEquals(ImmutableSet.of("active"),
                analysis.keys(OP_FN.apply("=")));
    }

    @Test
    public void testStorageKeysWithOperatorIgnoresSelection() {
        CommandAnalysis analysis = analyze(
                "select name where age > 30");
        Assert.assertEquals(
                "operator filter must scope to condition leaves only",
                ImmutableSet.of("age"),
                analysis.storageKeys(OP_FN.apply(">")));
    }

    @Test
    public void testScopedKeysFromConditionOnly() {
        Map<String, java.util.List<String>> scoped = analyze(
                "find A.(foo = \"X\")").scopedKeys();
        Assert.assertEquals(java.util.Arrays.asList("foo"),
                scoped.get("A"));
    }

    @Test
    public void testScopedKeysEmptyForSelectionOnlyCommand() {
        Assert.assertTrue(
                analyze("select name from 1").scopedKeys().isEmpty());
    }

    @Test
    public void testWriteCommandSelectionKey() {
        CommandAnalysis analysis = analyze("add name as \"jeff\" in 1");
        Assert.assertEquals(ImmutableSet.of("name"),
                analysis.storageKeys());
    }

    @Test
    public void testCalculateExposesKeyAndRecord() {
        CommandAnalysis analysis = analyze(
                String.format("calculate sum age[%d] from 1 at %d", T1, T2));
        Assert.assertEquals(ImmutableSet.of("age"), analysis.storageKeys());
        Assert.assertEquals(ImmutableSet.of(T1),
                analysis.temporalKeys().get("age"));
        Assert.assertEquals(Long.valueOf(T2), analysis.commandTimestamp());
        Assert.assertEquals(ImmutableSet.of(1L),
                analysis.referencedRecords());
    }

    @Test
    public void testNavigateExposesPathAndStops() {
        CommandAnalysis analysis = analyze(
                "navigate friends.name from 1");
        Assert.assertEquals(ImmutableSet.of("friends.name"),
                analysis.navigationKeys());
        Assert.assertEquals(ImmutableSet.of("friends", "name"),
                analysis.navigationKeyStops());
    }

    @Test
    public void testFindWithBracketsAndCommandTimestampCoexist() {
        CommandAnalysis analysis = analyze(String.format(
                "find name[%d] = \"jeff\" AND age[%d] > 30 as of %d", T1,
                T2, T3));
        Assert.assertEquals(Long.valueOf(T3), analysis.commandTimestamp());
        Map<String, Set<Long>> temporal = analysis.temporalKeys();
        Assert.assertEquals(ImmutableSet.of(T1), temporal.get("name"));
        Assert.assertEquals(ImmutableSet.of(T2), temporal.get("age"));
    }

    /**
     * <strong>Goal:</strong> Verify that the analysis of a read-and-set command
     * reports the keys it reads, the keys in its condition and the key it sets.
     * <p>
     * <strong>Start state:</strong> No prior state needed.
     * <p>
     * <strong>Workflow:</strong>
     * <ul>
     * <li>Analyze a {@code selectAndSet} statement that reads {@code name}
     * and {@code age}, filters on {@code status}, and sets {@code owner}.</li>
     * </ul>
     * <p>
     * <strong>Expected:</strong> The command type is {@code SELECT_AND_SET},
     * the storage keys are {@code name}, {@code age}, {@code status} and
     * {@code owner}, and there is no command timestamp and no referenced
     * record.
     */
    @Test
    public void testSelectAndSetKeysUnionReadConditionAndSetKeys() {
        CommandAnalysis analysis = analyze("selectAndSet [name, age] where "
                + "status = pending set owner as jeff");
        Assert.assertEquals("SELECT_AND_SET", analysis.commandType());
        Assert.assertEquals(ImmutableSet.of("name", "age", "status", "owner"),
                analysis.storageKeys());
        Assert.assertNull(analysis.commandTimestamp());
        Assert.assertEquals(ImmutableSet.of(), analysis.referencedRecords());
    }

    /**
     * <strong>Goal:</strong> Verify that the analysis of each await command
     * reports its type and the keys it reads, sets and filters on.
     * <p>
     * <strong>Start state:</strong> No prior state needed.
     * <p>
     * <strong>Workflow:</strong>
     * <ul>
     * <li>Analyze one statement for each await command.</li>
     * </ul>
     * <p>
     * <strong>Expected:</strong> Each analysis reports the matching command
     * type and exactly the storage keys worked out by hand for its statement.
     */
    @Test
    public void testAwaitCommandsReportTypeAndKeys() {
        String within = " within \"1 s\" ";
        CommandAnalysis find = analyze("awaitFind" + within + "a = 1");
        Assert.assertEquals("AWAIT_FIND", find.commandType());
        Assert.assertEquals(ImmutableSet.of("a"), find.storageKeys());
        CommandAnalysis select = analyze(
                "awaitSelect" + within + "name where a = 1");
        Assert.assertEquals("AWAIT_SELECT", select.commandType());
        Assert.assertEquals(ImmutableSet.of("name", "a"),
                select.storageKeys());
        CommandAnalysis get = analyze("awaitGet" + within + "name where a = 1");
        Assert.assertEquals("AWAIT_GET", get.commandType());
        Assert.assertEquals(ImmutableSet.of("name", "a"), get.storageKeys());
        CommandAnalysis findAndSet = analyze(
                "awaitFindAndSet" + within + "a = 1 set b as 2");
        Assert.assertEquals("AWAIT_FIND_AND_SET", findAndSet.commandType());
        Assert.assertEquals(ImmutableSet.of("b", "a"),
                findAndSet.storageKeys());
        CommandAnalysis selectAndSet = analyze(
                "awaitSelectAndSet" + within + "name where a = 1 set b as 2");
        Assert.assertEquals("AWAIT_SELECT_AND_SET",
                selectAndSet.commandType());
        Assert.assertEquals(ImmutableSet.of("name", "b", "a"),
                selectAndSet.storageKeys());
        CommandAnalysis getAndSet = analyze(
                "awaitGetAndSet" + within + "name where a = 1 set b as 2");
        Assert.assertEquals("AWAIT_GET_AND_SET", getAndSet.commandType());
        Assert.assertEquals(ImmutableSet.of("name", "b", "a"),
                getAndSet.storageKeys());
    }

    /**
     * <strong>Goal:</strong> Verify that the analysis of {@code findAndSet} and
     * {@code getAndSet} reports the key each sets and the keys in its
     * condition.
     * <p>
     * <strong>Start state:</strong> No prior state needed.
     * <p>
     * <strong>Workflow:</strong>
     * <ul>
     * <li>Analyze {@code findAndSet a = 1 set b as 2}.</li>
     * <li>Analyze {@code getAndSet name where a = 1 set b as 2}.</li>
     * </ul>
     * <p>
     * <strong>Expected:</strong> The command types are {@code FIND_AND_SET} and
     * {@code GET_AND_SET}, and the storage keys are {@code b} and {@code a},
     * plus {@code name} for {@code getAndSet}.
     */
    @Test
    public void testFindAndSetAndGetAndSetReportSetKey() {
        CommandAnalysis find = analyze("findAndSet a = 1 set b as 2");
        Assert.assertEquals("FIND_AND_SET", find.commandType());
        Assert.assertEquals(ImmutableSet.of("b", "a"), find.storageKeys());
        CommandAnalysis get = analyze("getAndSet name where a = 1 set b as 2");
        Assert.assertEquals("GET_AND_SET", get.commandType());
        Assert.assertEquals(ImmutableSet.of("name", "b", "a"),
                get.storageKeys());
    }

    private CommandAnalysis analyze(String ccl) {
        Compiler compiler = Compiler.create(VALUE_FN, OP_FN);
        CommandTree tree = (CommandTree) compiler.parse(ccl);
        return compiler.analyze(tree);
    }

    private static final Function<String, Object> VALUE_FN = Convert::stringToJava;
    private static final Function<String, Operator> OP_FN = Convert::stringToOperator;

}
