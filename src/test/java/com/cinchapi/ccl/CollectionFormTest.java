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

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.cinchapi.ccl.generated.Grammar;
import com.cinchapi.ccl.generated.ParseException;
import com.cinchapi.ccl.grammar.KeySymbol;
import com.cinchapi.ccl.grammar.KeyTokenSymbol;
import com.cinchapi.ccl.grammar.NavigationKeySymbol;
import com.cinchapi.ccl.grammar.Symbol;
import com.cinchapi.ccl.grammar.ValueSymbol;
import com.cinchapi.ccl.grammar.command.AwaitGetAndSetSymbol;
import com.cinchapi.ccl.grammar.command.AwaitGetSymbol;
import com.cinchapi.ccl.grammar.command.AwaitSelectAndSetSymbol;
import com.cinchapi.ccl.grammar.command.AwaitSelectSymbol;
import com.cinchapi.ccl.grammar.command.BrowseSymbol;
import com.cinchapi.ccl.grammar.command.CalculateSymbol;
import com.cinchapi.ccl.grammar.command.GetAndSetSymbol;
import com.cinchapi.ccl.grammar.command.GetSymbol;
import com.cinchapi.ccl.grammar.command.LinkSymbol;
import com.cinchapi.ccl.grammar.command.NavigateSymbol;
import com.cinchapi.ccl.grammar.command.SelectAndSetSymbol;
import com.cinchapi.ccl.grammar.command.SelectSymbol;
import com.cinchapi.ccl.syntax.CommandTree;
import com.cinchapi.concourse.util.Convert;
import com.google.common.collect.ImmutableList;

/**
 * Coverage for whether a parsed command names its keys, records or destinations
 * as a collection. A bracketed list, even of one item, and an unbracketed list
 * of more than one item are collections; one bare item is not. The accessors
 * that return the items stay the same for each form.
 *
 * @author Jeff Nelson
 */
public class CollectionFormTest {

    /**
     * Assert that {@code actual} holds exactly {@code expected}, in order, or
     * is {@code null} when {@code expected} is {@code null}.
     *
     * @param ccl the statement that parsed to {@code actual}, for the failure
     *            message
     * @param expected the keys the statement names, or {@code null} when it
     *            names none
     * @param actual the keys of the parsed symbol
     */
    private static void assertKeys(String ccl,
            List<? extends KeyTokenSymbol<?>> expected,
            Collection<KeyTokenSymbol<?>> actual) {
        if(expected == null) {
            Assert.assertNull(ccl, actual);
        }
        else {
            Assert.assertNotNull(ccl, actual);
            Assert.assertEquals(ccl, expected, ImmutableList.copyOf(actual));
        }
    }

    /**
     * Return a {@link Grammar} that reads {@code ccl}.
     *
     * @param ccl the text for the {@link Grammar} to read
     * @return the {@link Grammar}
     */
    private static Grammar grammar(String ccl) {
        return new Grammar(
                new ByteArrayInputStream(ccl.getBytes(StandardCharsets.UTF_8)),
                Convert::stringToJava, Convert::stringToOperator, null);
    }

    /**
     * Return a mutable {@link Collection} that holds {@code keys}, in the shape
     * a caller passes to a symbol's constructor.
     *
     * @param keys the keys
     * @return a new {@link Collection} of {@code keys}
     */
    private static Collection<KeyTokenSymbol<?>> keyCollection(
            List<KeySymbol> keys) {
        return new ArrayList<KeyTokenSymbol<?>>(keys);
    }

    /**
     * Return a {@link KeySymbol} for each of {@code names}, in order.
     *
     * @param names the key names
     * @return the {@link KeySymbol KeySymbols}
     */
    private static List<KeySymbol> keys(String... names) {
        List<KeySymbol> keys = new ArrayList<>();
        for (String name : names) {
            keys.add(new KeySymbol(name));
        }
        return keys;
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
     * Parse {@code ccl} and return the {@link Symbol} for its command.
     *
     * @param ccl the statement to parse
     * @return the root of the {@link CommandTree}
     */
    private static Symbol root(String ccl) {
        return parse(ccl).root();
    }

    /**
     * A {@link Compiler} with the value and operator transforms the Concourse
     * driver uses.
     */
    private static final Compiler COMPILER = Compiler
            .create(Convert::stringToJava, Convert::stringToOperator);

    /**
     * <strong>Goal:</strong> Verify that an await command reports whether it
     * names the keys it reads as a collection, through each way it can name
     * them, and keeps its keys, set key and set value.
     * <p>
     * <strong>Start state:</strong> No prior state needed.
     * <p>
     * <strong>Workflow:</strong>
     * <ul>
     * <li>For each of {@code awaitSelect}, {@code awaitGet},
     * {@code awaitSelectAndSet} and {@code awaitGetAndSet}, parse the command
     * with no keys, {@code name}, {@code [name]}, {@code name, age},
     * {@code for name}, {@code for [name]}, {@code for name, age} and
     * {@code for, name}.</li>
     * </ul>
     * <p>
     * <strong>Expected:</strong> {@code hasKeysInCollection()} is {@code false}
     * for no keys, {@code name} and {@code for name}, and {@code true} for
     * every other form. {@code keys()} is {@code null} for no keys,
     * {@code [name]} for {@code name}, {@code [name]} and {@code for name},
     * {@code [name, age]} for the lists of two names, and {@code [for, name]}
     * for {@code for, name}. Each command with a set clause has key {@code b}
     * and value {@code 2}.
     */
    @Test
    public void testAwaitCommandsReportWhetherKeysAreInCollection() {
        String within = " within \"1 s\" ";
        String[] forms = { "", "name ", "[name] ", "name, age ", "for name ",
                "for [name] ", "for name, age ", "for, name " };
        boolean[] inCollection = { false, false, true, true, false, true, true,
                true };
        List<List<KeySymbol>> expected = new ArrayList<>();
        expected.add(null);
        expected.add(keys("name"));
        expected.add(keys("name"));
        expected.add(keys("name", "age"));
        expected.add(keys("name"));
        expected.add(keys("name"));
        expected.add(keys("name", "age"));
        expected.add(keys("for", "name"));
        for (int i = 0; i < forms.length; ++i) {
            String ccl = "awaitSelect" + within + forms[i] + "where a = 1";
            AwaitSelectSymbol select = (AwaitSelectSymbol) root(ccl);
            Assert.assertEquals(ccl, inCollection[i],
                    select.hasKeysInCollection());
            assertKeys(ccl, expected.get(i), select.keys());
            ccl = "awaitGet" + within + forms[i] + "where a = 1";
            AwaitGetSymbol get = (AwaitGetSymbol) root(ccl);
            Assert.assertEquals(ccl, inCollection[i],
                    get.hasKeysInCollection());
            assertKeys(ccl, expected.get(i), get.keys());
            ccl = "awaitSelectAndSet" + within + forms[i]
                    + "where a = 1 set b as 2";
            AwaitSelectAndSetSymbol selectAndSet = (AwaitSelectAndSetSymbol) root(
                    ccl);
            Assert.assertEquals(ccl, inCollection[i],
                    selectAndSet.hasKeysInCollection());
            assertKeys(ccl, expected.get(i), selectAndSet.keys());
            Assert.assertEquals(ccl, new KeySymbol("b"), selectAndSet.key());
            Assert.assertEquals(ccl, new ValueSymbol(2), selectAndSet.value());
            ccl = "awaitGetAndSet" + within + forms[i]
                    + "where a = 1 set b as 2";
            AwaitGetAndSetSymbol getAndSet = (AwaitGetAndSetSymbol) root(ccl);
            Assert.assertEquals(ccl, inCollection[i],
                    getAndSet.hasKeysInCollection());
            assertKeys(ccl, expected.get(i), getAndSet.keys());
            Assert.assertEquals(ccl, new KeySymbol("b"), getAndSet.key());
            Assert.assertEquals(ccl, new ValueSymbol(2), getAndSet.value());
        }
    }

    /**
     * <strong>Goal:</strong> Verify that {@code browse} reports whether it
     * names its keys as a collection and keeps its keys.
     * <p>
     * <strong>Start state:</strong> No prior state needed.
     * <p>
     * <strong>Workflow:</strong>
     * <ul>
     * <li>Parse {@code browse name}, {@code browse [name]},
     * {@code browse name, age} and {@code browse [name] at 1704067200000000}.
     * </li>
     * </ul>
     * <p>
     * <strong>Expected:</strong> {@code hasKeysInCollection()} is {@code false}
     * for {@code browse name} and {@code true} for the others. {@code keys()}
     * is {@code [name]} for each form with one key and {@code [name, age]} for
     * the list of two.
     */
    @Test
    public void testBrowseReportsWhetherKeysAreInCollection() {
        BrowseSymbol symbol = (BrowseSymbol) root("browse name");
        Assert.assertFalse(symbol.hasKeysInCollection());
        assertKeys("browse name", keys("name"), symbol.keys());
        symbol = (BrowseSymbol) root("browse [name]");
        Assert.assertTrue(symbol.hasKeysInCollection());
        assertKeys("browse [name]", keys("name"), symbol.keys());
        symbol = (BrowseSymbol) root("browse name, age");
        Assert.assertTrue(symbol.hasKeysInCollection());
        assertKeys("browse name, age", keys("name", "age"), symbol.keys());
        symbol = (BrowseSymbol) root("browse [name] at 1704067200000000");
        Assert.assertTrue(symbol.hasKeysInCollection());
        assertKeys("browse [name] at", keys("name"), symbol.keys());
        Assert.assertNotNull(symbol.timestamp());
    }

    /**
     * <strong>Goal:</strong> Verify that {@code calculate} reports whether it
     * names its records as a collection and keeps its records.
     * <p>
     * <strong>Start state:</strong> No prior state needed.
     * <p>
     * <strong>Workflow:</strong>
     * <ul>
     * <li>Parse {@code calculate sum age} with {@code in 1},
     * {@code from [1]}, {@code within 1, 2}, a condition and no records.</li>
     * </ul>
     * <p>
     * <strong>Expected:</strong> {@code hasRecordsInCollection()} is
     * {@code true} for {@code from [1]} and {@code within 1, 2}, and
     * {@code false} otherwise. {@code records()} is {@code [1]} for
     * {@code in 1} and {@code from [1]}, {@code [1, 2]} for
     * {@code within 1, 2}, and {@code null} for a condition and for no records.
     */
    @Test
    public void testCalculateReportsWhetherRecordsAreInCollection() {
        CalculateSymbol symbol = (CalculateSymbol) root(
                "calculate sum age in 1");
        Assert.assertFalse(symbol.hasRecordsInCollection());
        Assert.assertEquals(ImmutableList.of(1L),
                ImmutableList.copyOf(symbol.records()));
        symbol = (CalculateSymbol) root("calculate sum age from [1]");
        Assert.assertTrue(symbol.hasRecordsInCollection());
        Assert.assertEquals(ImmutableList.of(1L),
                ImmutableList.copyOf(symbol.records()));
        symbol = (CalculateSymbol) root("calculate sum age within 1, 2");
        Assert.assertTrue(symbol.hasRecordsInCollection());
        Assert.assertEquals(ImmutableList.of(1L, 2L),
                ImmutableList.copyOf(symbol.records()));
        symbol = (CalculateSymbol) root("calculate sum age where a = 1");
        Assert.assertFalse(symbol.hasRecordsInCollection());
        Assert.assertNull(symbol.records());
        symbol = (CalculateSymbol) root("calculate sum age");
        Assert.assertFalse(symbol.hasRecordsInCollection());
        Assert.assertNull(symbol.records());
    }

    /**
     * <strong>Goal:</strong> Verify that each constructor that does not take a
     * flag for whether the command names its items as a collection treats more
     * than one item as a collection, and keeps the items it receives.
     * <p>
     * <strong>Start state:</strong> No prior state needed.
     * <p>
     * <strong>Workflow:</strong>
     * <ul>
     * <li>Construct each symbol whose command names keys with one key, with
     * two keys and, where it accepts them, with no keys, through each
     * constructor that does not take the flag.</li>
     * <li>Construct a {@link CalculateSymbol} with one record, two records
     * and no records, and a {@link LinkSymbol} with one destination, two
     * destinations and {@code null} destinations.</li>
     * </ul>
     * <p>
     * <strong>Expected:</strong> {@code hasKeysInCollection()},
     * {@code hasRecordsInCollection()} and
     * {@code hasDestinationsInCollection()} are {@code true} for two items and
     * {@code false} for one item, no items and {@code null}. Each symbol that
     * keeps the collection it receives returns that same collection, and each
     * {@link NavigateSymbol} keeps the criteria or CCL it receives.
     */
    @Test
    public void testConstructorsWithoutFlagTreatMoreThanOneItemAsCollection() {
        Duration timeout = Duration.ofSeconds(1);
        KeySymbol key = new KeySymbol("b");
        ValueSymbol value = new ValueSymbol(2);
        Collection<KeyTokenSymbol<?>> one = keyCollection(keys("name"));
        Collection<KeyTokenSymbol<?>> two = keyCollection(keys("name", "age"));
        Collection<Long> records = ImmutableList.of(1L, 2L);

        Assert.assertFalse(
                new SelectSymbol(one, 1, null).hasKeysInCollection());
        Assert.assertTrue(new SelectSymbol(two, 1, null).hasKeysInCollection());
        Assert.assertFalse(
                new SelectSymbol(null, 1, null).hasKeysInCollection());
        Assert.assertFalse(
                new SelectSymbol(one, records, null).hasKeysInCollection());
        Assert.assertTrue(
                new SelectSymbol(two, records, null).hasKeysInCollection());
        Assert.assertFalse(new SelectSymbol(one, null).hasKeysInCollection());
        Assert.assertTrue(new SelectSymbol(two, null).hasKeysInCollection());
        Assert.assertSame(two, new SelectSymbol(two, 1, null).keys());

        Assert.assertFalse(new GetSymbol(one, 1, null).hasKeysInCollection());
        Assert.assertTrue(new GetSymbol(two, 1, null).hasKeysInCollection());
        Assert.assertFalse(new GetSymbol(null, 1, null).hasKeysInCollection());
        Assert.assertFalse(
                new GetSymbol(one, records, null).hasKeysInCollection());
        Assert.assertTrue(
                new GetSymbol(two, records, null).hasKeysInCollection());
        Assert.assertFalse(new GetSymbol(one, null).hasKeysInCollection());
        Assert.assertTrue(new GetSymbol(two, null).hasKeysInCollection());
        Assert.assertSame(two, new GetSymbol(two, 1, null).keys());

        Assert.assertFalse(
                new NavigateSymbol(one, 1, null).hasKeysInCollection());
        Assert.assertTrue(
                new NavigateSymbol(two, 1, null).hasKeysInCollection());
        Assert.assertFalse(
                new NavigateSymbol(one, records, null).hasKeysInCollection());
        Assert.assertTrue(
                new NavigateSymbol(two, records, null).hasKeysInCollection());
        NavigateSymbol navigate = new NavigateSymbol(one, (Object) "criteria",
                null);
        Assert.assertFalse(navigate.hasKeysInCollection());
        Assert.assertEquals("criteria", navigate.criteria());
        Assert.assertNull(navigate.ccl());
        Assert.assertTrue(new NavigateSymbol(two, (Object) "criteria", null)
                .hasKeysInCollection());
        navigate = new NavigateSymbol(one, "a = 1", null);
        Assert.assertFalse(navigate.hasKeysInCollection());
        Assert.assertEquals("a = 1", navigate.ccl());
        Assert.assertNull(navigate.criteria());
        Assert.assertTrue(
                new NavigateSymbol(two, "a = 1", null).hasKeysInCollection());
        Assert.assertSame(two, new NavigateSymbol(two, 1, null).keys());

        Assert.assertFalse(new BrowseSymbol(one).hasKeysInCollection());
        Assert.assertTrue(new BrowseSymbol(two).hasKeysInCollection());
        Assert.assertFalse(new BrowseSymbol(one, null).hasKeysInCollection());
        Assert.assertTrue(new BrowseSymbol(two, null).hasKeysInCollection());
        Assert.assertSame(two, new BrowseSymbol(two, null).keys());

        Assert.assertFalse(
                new AwaitSelectSymbol(timeout, one).hasKeysInCollection());
        Assert.assertTrue(
                new AwaitSelectSymbol(timeout, two).hasKeysInCollection());
        Assert.assertFalse(
                new AwaitSelectSymbol(timeout, null).hasKeysInCollection());
        Assert.assertFalse(
                new AwaitGetSymbol(timeout, one).hasKeysInCollection());
        Assert.assertTrue(
                new AwaitGetSymbol(timeout, two).hasKeysInCollection());
        Assert.assertFalse(
                new AwaitGetSymbol(timeout, null).hasKeysInCollection());
        Assert.assertFalse(
                new SelectAndSetSymbol(one, key, value).hasKeysInCollection());
        Assert.assertTrue(
                new SelectAndSetSymbol(two, key, value).hasKeysInCollection());
        Assert.assertFalse(
                new SelectAndSetSymbol(null, key, value).hasKeysInCollection());
        Assert.assertFalse(
                new GetAndSetSymbol(one, key, value).hasKeysInCollection());
        Assert.assertTrue(
                new GetAndSetSymbol(two, key, value).hasKeysInCollection());
        Assert.assertFalse(
                new GetAndSetSymbol(null, key, value).hasKeysInCollection());
        Assert.assertFalse(new AwaitSelectAndSetSymbol(timeout, one, key, value)
                .hasKeysInCollection());
        Assert.assertTrue(new AwaitSelectAndSetSymbol(timeout, two, key, value)
                .hasKeysInCollection());
        Assert.assertFalse(
                new AwaitSelectAndSetSymbol(timeout, null, key, value)
                        .hasKeysInCollection());
        Assert.assertFalse(new AwaitGetAndSetSymbol(timeout, one, key, value)
                .hasKeysInCollection());
        Assert.assertTrue(new AwaitGetAndSetSymbol(timeout, two, key, value)
                .hasKeysInCollection());
        Assert.assertFalse(new AwaitGetAndSetSymbol(timeout, null, key, value)
                .hasKeysInCollection());

        Assert.assertFalse(
                new CalculateSymbol("sum", key, ImmutableList.of(1L), null)
                        .hasRecordsInCollection());
        Assert.assertTrue(new CalculateSymbol("sum", key, records, null)
                .hasRecordsInCollection());
        Assert.assertFalse(
                new CalculateSymbol("sum", key, null).hasRecordsInCollection());
        Assert.assertSame(records,
                new CalculateSymbol("sum", key, records, null).records());

        Assert.assertFalse(new LinkSymbol(key, 1, ImmutableList.of(2L))
                .hasDestinationsInCollection());
        Assert.assertTrue(
                new LinkSymbol(key, 1, records).hasDestinationsInCollection());
        LinkSymbol link = new LinkSymbol(key, 1, null);
        Assert.assertFalse(link.hasDestinationsInCollection());
        Assert.assertNull(link.destinations());
        Assert.assertSame(records,
                new LinkSymbol(key, 1, records).destinations());
    }

    /**
     * <strong>Goal:</strong> Verify that {@code get} reports whether it names
     * its keys as a collection for each form of the command, and keeps its
     * keys, single key and records.
     * <p>
     * <strong>Start state:</strong> No prior state needed.
     * <p>
     * <strong>Workflow:</strong>
     * <ul>
     * <li>Parse {@code get name}, {@code get [name]} and
     * {@code get name, age} with {@code from 1}, {@code from [1, 2]} and a
     * condition.</li>
     * <li>Parse {@code get from 1} and {@code get where a = 1}.</li>
     * </ul>
     * <p>
     * <strong>Expected:</strong> {@code hasKeysInCollection()} is {@code false}
     * for {@code name} and for no keys, and {@code true} for {@code [name]} and
     * {@code name, age}. {@code keys()} is {@code [name]} for {@code name} and
     * {@code [name]}, {@code [name, age]} for the list of two, and {@code null}
     * for no keys. {@code key()} is {@code name} for both forms with one key
     * and {@code null} for the list of two. The records are {@code 1} or
     * {@code [1, 2]} as the statement names them.
     */
    @Test
    public void testGetReportsWhetherKeysAreInCollection() {
        String[] forms = { "name", "[name]", "name, age" };
        boolean[] inCollection = { false, true, true };
        List<List<KeySymbol>> expected = ImmutableList.of(keys("name"),
                keys("name"), keys("name", "age"));
        for (int i = 0; i < forms.length; ++i) {
            KeySymbol key = i < 2 ? new KeySymbol("name") : null;
            String ccl = "get " + forms[i] + " from 1";
            GetSymbol symbol = (GetSymbol) root(ccl);
            Assert.assertEquals(ccl, inCollection[i],
                    symbol.hasKeysInCollection());
            assertKeys(ccl, expected.get(i), symbol.keys());
            Assert.assertEquals(ccl, key, symbol.key());
            Assert.assertEquals(ccl, Long.valueOf(1), symbol.record());
            Assert.assertNull(ccl, symbol.records());
            ccl = "get " + forms[i] + " from [1, 2]";
            symbol = (GetSymbol) root(ccl);
            Assert.assertEquals(ccl, inCollection[i],
                    symbol.hasKeysInCollection());
            assertKeys(ccl, expected.get(i), symbol.keys());
            Assert.assertEquals(ccl, key, symbol.key());
            Assert.assertNull(ccl, symbol.record());
            Assert.assertEquals(ccl, ImmutableList.of(1L, 2L),
                    ImmutableList.copyOf(symbol.records()));
            ccl = "get " + forms[i] + " where a = 1";
            symbol = (GetSymbol) root(ccl);
            Assert.assertEquals(ccl, inCollection[i],
                    symbol.hasKeysInCollection());
            assertKeys(ccl, expected.get(i), symbol.keys());
            Assert.assertEquals(ccl, key, symbol.key());
        }
        GetSymbol symbol = (GetSymbol) root("get from 1");
        Assert.assertFalse(symbol.hasKeysInCollection());
        Assert.assertNull(symbol.keys());
        symbol = (GetSymbol) root("get where a = 1");
        Assert.assertFalse(symbol.hasKeysInCollection());
        Assert.assertNull(symbol.keys());
    }

    /**
     * <strong>Goal:</strong> Verify that {@code link} reports whether it names
     * its destinations as a collection and keeps its key, source and
     * destinations.
     * <p>
     * <strong>Start state:</strong> No prior state needed.
     * <p>
     * <strong>Workflow:</strong>
     * <ul>
     * <li>Parse {@code link friends from 1} with {@code to 2},
     * {@code to [2]} and {@code to 2, 3}.</li>
     * </ul>
     * <p>
     * <strong>Expected:</strong> {@code hasDestinationsInCollection()} is
     * {@code false} for {@code to 2} and {@code true} for the others. Each
     * symbol has key {@code friends} and source {@code 1}, and
     * {@code destinations()} is {@code [2]} for {@code to 2} and {@code to [2]}
     * and {@code [2, 3]} for {@code to 2, 3}.
     */
    @Test
    public void testLinkReportsWhetherDestinationsAreInCollection() {
        LinkSymbol symbol = (LinkSymbol) root("link friends from 1 to 2");
        Assert.assertFalse(symbol.hasDestinationsInCollection());
        Assert.assertEquals(new KeySymbol("friends"), symbol.key());
        Assert.assertEquals(1, symbol.source());
        Assert.assertEquals(ImmutableList.of(2L),
                ImmutableList.copyOf(symbol.destinations()));
        symbol = (LinkSymbol) root("link friends from 1 to [2]");
        Assert.assertTrue(symbol.hasDestinationsInCollection());
        Assert.assertEquals(new KeySymbol("friends"), symbol.key());
        Assert.assertEquals(1, symbol.source());
        Assert.assertEquals(ImmutableList.of(2L),
                ImmutableList.copyOf(symbol.destinations()));
        symbol = (LinkSymbol) root("link friends from 1 to 2, 3");
        Assert.assertTrue(symbol.hasDestinationsInCollection());
        Assert.assertEquals(ImmutableList.of(2L, 3L),
                ImmutableList.copyOf(symbol.destinations()));
    }

    /**
     * <strong>Goal:</strong> Verify that the {@link NavigateSymbol}
     * constructors that take {@code hasKeysInCollection} keep it along with the
     * criteria or CCL they receive, which no parsed statement passes to them.
     * <p>
     * <strong>Start state:</strong> No prior state needed.
     * <p>
     * <strong>Workflow:</strong>
     * <ul>
     * <li>Construct a {@link NavigateSymbol} with one key,
     * {@code hasKeysInCollection} set to {@code true} and criteria.</li>
     * <li>Construct a {@link NavigateSymbol} with one key,
     * {@code hasKeysInCollection} set to {@code true} and CCL.</li>
     * </ul>
     * <p>
     * <strong>Expected:</strong> Both symbols have keys in a collection. The
     * first has the criteria and no CCL, and the second has the CCL and no
     * criteria.
     */
    @Test
    public void testNavigateConstructorsWithFlagKeepCriteriaAndCcl() {
        Collection<KeyTokenSymbol<?>> one = keyCollection(keys("name"));
        NavigateSymbol navigate = new NavigateSymbol(one, true,
                (Object) "criteria", null);
        Assert.assertTrue(navigate.hasKeysInCollection());
        Assert.assertEquals("criteria", navigate.criteria());
        Assert.assertNull(navigate.ccl());
        navigate = new NavigateSymbol(one, true, "a = 1", null);
        Assert.assertTrue(navigate.hasKeysInCollection());
        Assert.assertEquals("a = 1", navigate.ccl());
        Assert.assertNull(navigate.criteria());
    }

    /**
     * <strong>Goal:</strong> Verify that {@code navigate} reports whether it
     * names its keys as a collection for each form of the command, and keeps
     * its keys and records.
     * <p>
     * <strong>Start state:</strong> No prior state needed.
     * <p>
     * <strong>Workflow:</strong>
     * <ul>
     * <li>Parse {@code navigate friends.name},
     * {@code navigate [friends.name]} and
     * {@code navigate friends.name, friends.age} with {@code from 1},
     * {@code from [1, 2]} and a condition.</li>
     * </ul>
     * <p>
     * <strong>Expected:</strong> {@code hasKeysInCollection()} is {@code false}
     * for {@code friends.name} and {@code true} for the other forms.
     * {@code keys()} is {@code [friends.name]} for both forms with one key and
     * {@code [friends.name, friends.age]} for the list of two. The records are
     * {@code 1} or {@code [1, 2]} as the statement names them.
     */
    @Test
    public void testNavigateReportsWhetherKeysAreInCollection() {
        String[] forms = { "friends.name", "[friends.name]",
                "friends.name, friends.age" };
        boolean[] inCollection = { false, true, true };
        List<List<NavigationKeySymbol>> expected = ImmutableList.of(
                ImmutableList.of(new NavigationKeySymbol("friends.name")),
                ImmutableList.of(new NavigationKeySymbol("friends.name")),
                ImmutableList.of(new NavigationKeySymbol("friends.name"),
                        new NavigationKeySymbol("friends.age")));
        for (int i = 0; i < forms.length; ++i) {
            String ccl = "navigate " + forms[i] + " from 1";
            NavigateSymbol symbol = (NavigateSymbol) root(ccl);
            Assert.assertEquals(ccl, inCollection[i],
                    symbol.hasKeysInCollection());
            assertKeys(ccl, expected.get(i), symbol.keys());
            Assert.assertEquals(ccl, Long.valueOf(1), symbol.record());
            Assert.assertNull(ccl, symbol.records());
            ccl = "navigate " + forms[i] + " from [1, 2]";
            symbol = (NavigateSymbol) root(ccl);
            Assert.assertEquals(ccl, inCollection[i],
                    symbol.hasKeysInCollection());
            assertKeys(ccl, expected.get(i), symbol.keys());
            Assert.assertNull(ccl, symbol.record());
            Assert.assertEquals(ccl, ImmutableList.of(1L, 2L),
                    ImmutableList.copyOf(symbol.records()));
            ccl = "navigate " + forms[i] + " where a = 1";
            symbol = (NavigateSymbol) root(ccl);
            Assert.assertEquals(ccl, inCollection[i],
                    symbol.hasKeysInCollection());
            assertKeys(ccl, expected.get(i), symbol.keys());
            Assert.assertNull(ccl, symbol.record());
            Assert.assertNull(ccl, symbol.records());
        }
    }

    /**
     * <strong>Goal:</strong> Verify that the commands whose symbols implement
     * {@code equals} and {@code hashCode} parse to equal trees whether they
     * name one key in brackets or without them.
     * <p>
     * <strong>Start state:</strong> No prior state needed.
     * <p>
     * <strong>Workflow:</strong>
     * <ul>
     * <li>For {@code awaitSelect}, {@code awaitGet},
     * {@code awaitSelectAndSet}, {@code awaitGetAndSet}, {@code selectAndSet}
     * and {@code getAndSet}, parse the command with {@code [name]} and with
     * {@code name}.</li>
     * </ul>
     * <p>
     * <strong>Expected:</strong> Each pair of trees is equal, with equal hash
     * codes, while only the tree for {@code [name]} has keys in a collection.
     */
    @Test
    public void testOneKeyInBracketsParsesToTreeEqualToOneBareKey() {
        String within = " within \"1 s\" ";
        List<String> commands = ImmutableList.of(
                "awaitSelect" + within + "%s where a = 1",
                "awaitGet" + within + "%s where a = 1",
                "awaitSelectAndSet" + within + "%s where a = 1 set b as 2",
                "awaitGetAndSet" + within + "%s where a = 1 set b as 2",
                "selectAndSet %s where a = 1 set b as 2",
                "getAndSet %s where a = 1 set b as 2");
        for (String command : commands) {
            CommandTree bracketed = parse(String.format(command, "[name]"));
            CommandTree bare = parse(String.format(command, "name"));
            Assert.assertEquals(command, bare, bracketed);
            Assert.assertEquals(command, bare.hashCode(), bracketed.hashCode());
        }
    }

    /**
     * <strong>Goal:</strong> Verify that the public
     * {@link Grammar#KeyOrKeyCollection()} and {@link Grammar#AwaitKeys()}
     * productions return only the keys, as they do for callers that use the
     * parser directly.
     * <p>
     * <strong>Start state:</strong> No prior state needed.
     * <p>
     * <strong>Workflow:</strong>
     * <ul>
     * <li>Call {@link Grammar#KeyOrKeyCollection()} on {@code name},
     * {@code [name]} and {@code name, age}.</li>
     * <li>Call {@link Grammar#AwaitKeys()} on {@code where},
     * {@code name where}, {@code [name] where}, {@code for name, age where} and
     * {@code for, name where}.</li>
     * </ul>
     * <p>
     * <strong>Expected:</strong> {@link Grammar#KeyOrKeyCollection()} returns
     * {@code [name]}, {@code [name]} and {@code [name, age]}.
     * {@link Grammar#AwaitKeys()} returns {@code null} for {@code where}, and
     * {@code [name]}, {@code [name]}, {@code [name, age]} and
     * {@code [for, name]} for the others.
     */
    @Test
    public void testPublicKeyProductionsReturnOnlyTheKeys()
            throws ParseException {
        assertKeys("name", keys("name"), grammar("name").KeyOrKeyCollection());
        assertKeys("[name]", keys("name"),
                grammar("[name]").KeyOrKeyCollection());
        assertKeys("name, age", keys("name", "age"),
                grammar("name, age").KeyOrKeyCollection());
        assertKeys("where", null, grammar("where").AwaitKeys());
        assertKeys("name where", keys("name"),
                grammar("name where").AwaitKeys());
        assertKeys("[name] where", keys("name"),
                grammar("[name] where").AwaitKeys());
        assertKeys("for name, age where", keys("name", "age"),
                grammar("for name, age where").AwaitKeys());
        assertKeys("for, name where", keys("for", "name"),
                grammar("for, name where").AwaitKeys());
    }

    /**
     * <strong>Goal:</strong> Verify that {@code selectAndSet} and
     * {@code getAndSet} report whether they name the keys they read as a
     * collection, and keep their keys, set key and set value.
     * <p>
     * <strong>Start state:</strong> No prior state needed.
     * <p>
     * <strong>Workflow:</strong>
     * <ul>
     * <li>Parse each command with no keys, {@code name}, {@code [name]} and
     * {@code name, age}, a condition and {@code set b as 2}.</li>
     * </ul>
     * <p>
     * <strong>Expected:</strong> {@code hasKeysInCollection()} is {@code false}
     * for no keys and {@code name}, and {@code true} for {@code [name]} and
     * {@code name, age}. {@code keys()} is {@code null} for no keys,
     * {@code [name]} for both forms with one key and {@code [name, age]} for
     * the list of two. Each symbol has key {@code b} and value {@code 2}.
     */
    @Test
    public void testReadAndSetCommandsReportWhetherKeysAreInCollection() {
        String[] forms = { "", "name ", "[name] ", "name, age " };
        boolean[] inCollection = { false, false, true, true };
        List<List<KeySymbol>> expected = new ArrayList<>();
        expected.add(null);
        expected.add(keys("name"));
        expected.add(keys("name"));
        expected.add(keys("name", "age"));
        for (int i = 0; i < forms.length; ++i) {
            String ccl = "selectAndSet " + forms[i] + "where a = 1 set b as 2";
            SelectAndSetSymbol select = (SelectAndSetSymbol) root(ccl);
            Assert.assertEquals(ccl, inCollection[i],
                    select.hasKeysInCollection());
            assertKeys(ccl, expected.get(i), select.keys());
            Assert.assertEquals(ccl, new KeySymbol("b"), select.key());
            Assert.assertEquals(ccl, new ValueSymbol(2), select.value());
            ccl = "getAndSet " + forms[i] + "where a = 1 set b as 2";
            GetAndSetSymbol get = (GetAndSetSymbol) root(ccl);
            Assert.assertEquals(ccl, inCollection[i],
                    get.hasKeysInCollection());
            assertKeys(ccl, expected.get(i), get.keys());
            Assert.assertEquals(ccl, new KeySymbol("b"), get.key());
            Assert.assertEquals(ccl, new ValueSymbol(2), get.value());
        }
    }

    /**
     * <strong>Goal:</strong> Verify that {@code select} reports whether it
     * names its keys as a collection for each form of the command, and keeps
     * its keys and records.
     * <p>
     * <strong>Start state:</strong> No prior state needed.
     * <p>
     * <strong>Workflow:</strong>
     * <ul>
     * <li>Parse {@code select name}, {@code select [name]} and
     * {@code select name, age} with {@code from 1}, {@code from [1, 2]} and a
     * condition.</li>
     * <li>Parse {@code select 1}, {@code select [1, 2]} and
     * {@code select where a = 1}.</li>
     * </ul>
     * <p>
     * <strong>Expected:</strong> {@code hasKeysInCollection()} is {@code false}
     * for {@code name} and for no keys, and {@code true} for {@code [name]} and
     * {@code name, age}. {@code keys()} is {@code [name]} for {@code name} and
     * {@code [name]}, {@code [name, age]} for the list of two, and {@code null}
     * for no keys. The records are {@code 1} or {@code [1, 2]} as the statement
     * names them.
     */
    @Test
    public void testSelectReportsWhetherKeysAreInCollection() {
        String[] forms = { "name", "[name]", "name, age" };
        boolean[] inCollection = { false, true, true };
        List<List<KeySymbol>> expected = ImmutableList.of(keys("name"),
                keys("name"), keys("name", "age"));
        for (int i = 0; i < forms.length; ++i) {
            String ccl = "select " + forms[i] + " from 1";
            SelectSymbol symbol = (SelectSymbol) root(ccl);
            Assert.assertEquals(ccl, inCollection[i],
                    symbol.hasKeysInCollection());
            assertKeys(ccl, expected.get(i), symbol.keys());
            Assert.assertEquals(ccl, Long.valueOf(1), symbol.record());
            Assert.assertNull(ccl, symbol.records());
            ccl = "select " + forms[i] + " from [1, 2]";
            symbol = (SelectSymbol) root(ccl);
            Assert.assertEquals(ccl, inCollection[i],
                    symbol.hasKeysInCollection());
            assertKeys(ccl, expected.get(i), symbol.keys());
            Assert.assertNull(ccl, symbol.record());
            Assert.assertEquals(ccl, ImmutableList.of(1L, 2L),
                    ImmutableList.copyOf(symbol.records()));
            ccl = "select " + forms[i] + " where a = 1";
            symbol = (SelectSymbol) root(ccl);
            Assert.assertEquals(ccl, inCollection[i],
                    symbol.hasKeysInCollection());
            assertKeys(ccl, expected.get(i), symbol.keys());
            Assert.assertNull(ccl, symbol.record());
            Assert.assertNull(ccl, symbol.records());
        }
        for (String ccl : ImmutableList.of("select 1", "select [1, 2]",
                "select where a = 1")) {
            SelectSymbol symbol = (SelectSymbol) root(ccl);
            Assert.assertFalse(ccl, symbol.hasKeysInCollection());
            Assert.assertNull(ccl, symbol.keys());
        }
    }

}
