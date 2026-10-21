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
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;

import com.cinchapi.ccl.generated.ASTCommand;
import com.cinchapi.ccl.generated.ASTPage;
import com.cinchapi.ccl.generated.ASTStart;
import com.cinchapi.ccl.generated.Grammar;
import com.cinchapi.ccl.generated.ParseException;
import com.cinchapi.ccl.grammar.DirectionSymbol;
import com.cinchapi.ccl.grammar.ExpressionSymbol;
import com.cinchapi.ccl.grammar.FunctionKeySymbol;
import com.cinchapi.ccl.grammar.FunctionValueSymbol;
import com.cinchapi.ccl.grammar.KeySymbol;
import com.cinchapi.ccl.grammar.KeyTokenSymbol;
import com.cinchapi.ccl.grammar.NavigationKeySymbol;
import com.cinchapi.ccl.grammar.OperatorSymbol;
import com.cinchapi.ccl.grammar.OrderComponentSymbol;
import com.cinchapi.ccl.grammar.OrderSymbol;
import com.cinchapi.ccl.grammar.PageSymbol;
import com.cinchapi.ccl.grammar.Symbol;
import com.cinchapi.ccl.grammar.TemporalKeySymbol;
import com.cinchapi.ccl.grammar.TimestampSymbol;
import com.cinchapi.ccl.grammar.ValueSymbol;
import com.cinchapi.ccl.grammar.ValueTokenSymbol;
import com.cinchapi.ccl.grammar.command.AuditSymbol;
import com.cinchapi.ccl.grammar.command.AwaitSelectSymbol;
import com.cinchapi.ccl.grammar.command.ClearSymbol;
import com.cinchapi.ccl.grammar.command.FindSymbol;
import com.cinchapi.ccl.grammar.command.GetSymbol;
import com.cinchapi.ccl.grammar.command.ImplicitSymbol;
import com.cinchapi.ccl.grammar.command.LinkSymbol;
import com.cinchapi.ccl.grammar.command.NavigateSymbol;
import com.cinchapi.ccl.grammar.command.SearchSymbol;
import com.cinchapi.ccl.grammar.command.SelectSymbol;
import com.cinchapi.ccl.grammar.command.SetSymbol;
import com.cinchapi.ccl.syntax.AbstractSyntaxTree;
import com.cinchapi.ccl.syntax.CommandTree;
import com.cinchapi.ccl.syntax.ConditionTree;
import com.cinchapi.ccl.syntax.ExpressionTree;
import com.cinchapi.ccl.syntax.FunctionTree;
import com.cinchapi.ccl.syntax.OrTree;
import com.cinchapi.ccl.syntax.OrderTree;
import com.cinchapi.ccl.syntax.PageTree;
import com.cinchapi.ccl.syntax.ScopedConditionTree;
import com.cinchapi.ccl.type.function.ImplicitKeyRecordFunction;
import com.cinchapi.ccl.type.function.IndexFunction;
import com.cinchapi.ccl.type.function.KeyConditionFunction;
import com.cinchapi.ccl.type.function.KeyRecordsFunction;
import com.cinchapi.concourse.lang.Criteria;
import com.cinchapi.concourse.thrift.Operator;
import com.cinchapi.concourse.util.Convert;
import com.google.common.collect.ImmutableList;

/**
 * Coverage for keys spelled like the words CCL reserves for its commands,
 * prepositions and pages. Such a key parses wherever a key is expected, and
 * each command keeps its meaning.
 *
 * @author Jeff Nelson
 */
public class ReservedWordKeyTest {

    /**
     * Every command name, alias, preposition and page word that CCL reserves.
     */
    private static final List<String> RESERVED_WORDS = ImmutableList.of(
            "abort", "add", "as", "audit", "awaitFind", "await_find",
            "awaitFindAndSet", "await_find_and_set", "awaitGet", "await_get",
            "awaitGetAndSet", "await_get_and_set", "awaitSelect",
            "await_select", "awaitSelectAndSet", "await_select_and_set",
            "browse", "calculate", "chronicle", "clear", "commit",
            "consolidate", "describe", "diff", "find", "findAndSet",
            "find_and_set", "findOrAdd", "findOrInsert", "for", "from", "get",
            "getAndSet", "get_and_set", "holds", "in", "insert", "into",
            "inventory", "jsonify", "limit", "link", "navigate", "offset",
            "ping", "reconcile", "remove", "revert", "search", "select",
            "selectAndSet", "select_and_set", "set", "skip", "stage", "to",
            "trace", "unlink", "verify", "verifyAndSwap", "verify_and_swap",
            "verifyOrSet", "verify_or_set", "with", "within", "without");

    /**
     * A {@link Compiler} with the value and operator transforms the Concourse
     * driver uses.
     */
    private static final Compiler COMPILER = Compiler
            .create(Convert::stringToJava, Convert::stringToOperator);

    /**
     * Parse {@code ccl} as one statement.
     *
     * @param ccl the statement to parse
     * @return the {@link AbstractSyntaxTree}
     */
    private static AbstractSyntaxTree parse(String ccl) {
        return COMPILER.parse(ccl);
    }

    /**
     * Parse {@code ccl} as one {@link CommandTree}.
     *
     * @param ccl the statement to parse
     * @return the {@link CommandTree}
     */
    private static CommandTree command(String ccl) {
        return (CommandTree) parse(ccl);
    }

    /**
     * Parse {@code line} as one command line with
     * {@link Grammar#StartCommandLine()}.
     *
     * @param line the command line to parse, without its line break
     * @return the {@link ASTStart} of the command line
     * @throws ParseException if {@code line} is not a valid command line
     */
    private static ASTStart commandLine(String line) throws ParseException {
        Grammar grammar = new Grammar(
                new ByteArrayInputStream(
                        (line + "\n").getBytes(StandardCharsets.UTF_8)),
                Convert::stringToJava, Convert::stringToOperator, null);
        return grammar.StartCommandLine();
    }

    /**
     * Return the {@link ExpressionTree} that compares {@code key} to
     * {@code value} with {@code operator}.
     *
     * @param key the key
     * @param operator the operator
     * @param value the value
     * @return the {@link ExpressionTree}
     */
    private static ExpressionTree expression(KeyTokenSymbol<?> key,
            Operator operator, Object value) {
        return new ExpressionTree(ExpressionSymbol.create(key,
                new OperatorSymbol(operator), new ValueSymbol(value)));
    }

    /**
     * Return the {@link ExpressionTree} for {@code key = 1}.
     *
     * @param key the key
     * @return the {@link ExpressionTree}
     */
    private static ExpressionTree equalsOne(String key) {
        return expression(new KeySymbol(key), Operator.EQUALS, 1);
    }

    /**
     * Return an {@link OrderSymbol} that sorts by each of {@code keys} in
     * ascending order.
     *
     * @param keys the keys to sort by
     * @return the {@link OrderSymbol}
     */
    private static OrderSymbol ascending(String... keys) {
        OrderSymbol order = new OrderSymbol();
        for (String key : keys) {
            order.add(new OrderComponentSymbol(new KeySymbol(key),
                    DirectionSymbol.ASCENDING));
        }
        return order;
    }

    /**
     * Return a {@link KeySymbol} for each of {@code keys}, in order.
     *
     * @param keys the keys
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
     * <strong>Goal:</strong> Verify that a reserved word parses as the key of a
     * comparison.
     * <p>
     * <strong>Start state:</strong> No prior state needed.
     * <p>
     * <strong>Workflow:</strong>
     * <ul>
     * <li>For each reserved word, parse {@code <word> = 1}.</li>
     * </ul>
     * <p>
     * <strong>Expected:</strong> Each statement is a condition whose key is the
     * word, whose operator is {@code =} and whose value is {@code 1}.
     */
    @Test
    public void testReservedWordParsesAsKeyOfCondition() {
        for (String word : RESERVED_WORDS) {
            Assert.assertEquals(word, equalsOne(word), parse(word + " = 1"));
        }
    }

    /**
     * <strong>Goal:</strong> Verify that a reserved word parses as a key in any
     * letter case and keeps that spelling.
     * <p>
     * <strong>Start state:</strong> No prior state needed.
     * <p>
     * <strong>Workflow:</strong>
     * <ul>
     * <li>Parse conditions on {@code From}, {@code SELECT} and
     * {@code Limit}.</li>
     * </ul>
     * <p>
     * <strong>Expected:</strong> Each key is spelled as written.
     */
    @Test
    public void testReservedWordParsesAsKeyInAnyLetterCase() {
        for (String word : ImmutableList.of("From", "SELECT", "Limit")) {
            Assert.assertEquals(word, equalsOne(word), parse(word + " = 1"));
        }
    }

    /**
     * <strong>Goal:</strong> Verify that a reserved word parses as the key of a
     * {@code LINKS_TO} comparison, alone and in a disjunction.
     * <p>
     * <strong>Start state:</strong> No prior state needed.
     * <p>
     * <strong>Workflow:</strong>
     * <ul>
     * <li>For each reserved word, parse
     * {@code <word> LINKS_TO 1746828956920001}.</li>
     * <li>Parse
     * {@code from LINKS_TO 1746828956920001 OR to LINKS_TO 1746828956920001}.
     * </li>
     * </ul>
     * <p>
     * <strong>Expected:</strong> Each statement compares the word to the record
     * with {@code LINKS_TO}, and the disjunction is an {@link OrTree} of the
     * comparisons on {@code from} and {@code to}.
     */
    @Test
    public void testReservedWordParsesAsKeyOfLinksTo() {
        long record = 1746828956920001L;
        for (String word : RESERVED_WORDS) {
            Assert.assertEquals(word,
                    expression(new KeySymbol(word), Operator.LINKS_TO, record),
                    parse(word + " LINKS_TO " + record));
        }
        Assert.assertEquals(
                new OrTree(
                        expression(new KeySymbol("from"), Operator.LINKS_TO,
                                record),
                        expression(new KeySymbol("to"), Operator.LINKS_TO,
                                record)),
                parse("from LINKS_TO " + record + " OR to LINKS_TO "
                        + record));
    }

    /**
     * <strong>Goal:</strong> Verify that a reserved word parses as a key with a
     * bracket timestamp.
     * <p>
     * <strong>Start state:</strong> No prior state needed.
     * <p>
     * <strong>Workflow:</strong>
     * <ul>
     * <li>For each reserved word, parse {@code <word>[1700000000] = 1}.</li>
     * </ul>
     * <p>
     * <strong>Expected:</strong> Each key is a {@link TemporalKeySymbol} for
     * the word at {@code 1700000000}.
     */
    @Test
    public void testReservedWordParsesAsKeyWithBracketTimestamp() {
        for (String word : RESERVED_WORDS) {
            Assert.assertEquals(word,
                    expression(
                            new TemporalKeySymbol(new KeySymbol(word),
                                    new TimestampSymbol(1700000000L)),
                            Operator.EQUALS, 1),
                    parse(word + "[1700000000] = 1"));
        }
    }

    /**
     * <strong>Goal:</strong> Verify that a reserved word parses as a key in the
     * condition of a command.
     * <p>
     * <strong>Start state:</strong> No prior state needed.
     * <p>
     * <strong>Workflow:</strong>
     * <ul>
     * <li>For each reserved word, parse {@code select name where <word> = 1}
     * and {@code find <word> = 1}.</li>
     * </ul>
     * <p>
     * <strong>Expected:</strong> The condition of each command is
     * {@code <word> = 1}.
     */
    @Test
    public void testReservedWordParsesAsKeyInConditionOfCommand() {
        for (String word : RESERVED_WORDS) {
            Assert.assertEquals(word, equalsOne(word),
                    command("select name where " + word + " = 1")
                            .conditionTree());
            Assert.assertEquals(word, equalsOne(word),
                    command("find " + word + " = 1").conditionTree());
        }
    }

    /**
     * <strong>Goal:</strong> Verify that a reserved word parses as a key to
     * order by.
     * <p>
     * <strong>Start state:</strong> No prior state needed.
     * <p>
     * <strong>Workflow:</strong>
     * <ul>
     * <li>For each reserved word, parse {@code order by <word>},
     * {@code name = 1 order by <word>} and
     * {@code find name = 1 order by age, <word>}.</li>
     * </ul>
     * <p>
     * <strong>Expected:</strong> Each order sorts by the word, after
     * {@code age} in the last statement.
     */
    @Test
    public void testReservedWordParsesAsOrderKey() {
        for (String word : RESERVED_WORDS) {
            Assert.assertEquals(word, new OrderTree(ascending(word)),
                    parse("order by " + word));
            Assert.assertEquals(word, ascending(word),
                    command("name = 1 order by " + word).orderTree().root());
            Assert.assertEquals(word, ascending("age", word),
                    command("find name = 1 order by age, " + word)
                            .orderTree().root());
        }
    }

    /**
     * <strong>Goal:</strong> Verify that a reserved word parses as a key that a
     * command selects.
     * <p>
     * <strong>Start state:</strong> No prior state needed.
     * <p>
     * <strong>Workflow:</strong>
     * <ul>
     * <li>For each reserved word, parse {@code select <word> from 1} and
     * {@code select name, <word> where age > 30}.</li>
     * </ul>
     * <p>
     * <strong>Expected:</strong> The first selects the word from record
     * {@code 1}, and the second selects {@code name} and the word.
     */
    @Test
    public void testReservedWordParsesAsSelectedKey() {
        for (String word : RESERVED_WORDS) {
            SelectSymbol single = (SelectSymbol) command(
                    "select " + word + " from 1").root();
            Assert.assertEquals(word, keys(word),
                    ImmutableList.copyOf(single.keys()));
            Assert.assertEquals(word, Long.valueOf(1), single.record());
            SelectSymbol many = (SelectSymbol) command(
                    "select name, " + word + " where age > 30").root();
            Assert.assertEquals(word, keys("name", word),
                    ImmutableList.copyOf(many.keys()));
        }
    }

    /**
     * <strong>Goal:</strong> Verify that a condition built from symbols with a
     * reserved word as its key parses back from its text.
     * <p>
     * <strong>Start state:</strong> No prior state needed.
     * <p>
     * <strong>Workflow:</strong>
     * <ul>
     * <li>For each reserved word, build the condition
     * {@code <word> LINKS_TO 1746828956920001} from symbols.</li>
     * <li>Render its symbols as text and parse the text.</li>
     * </ul>
     * <p>
     * <strong>Expected:</strong> The text parses to the built condition.
     */
    @Test
    public void testConditionOnReservedWordKeyParsesBackFromItsText() {
        for (String word : RESERVED_WORDS) {
            ConditionTree built = COMPILER.parse(ImmutableList.of(
                    new KeySymbol(word), new OperatorSymbol(Operator.LINKS_TO),
                    new ValueSymbol(1746828956920001L)));
            String text = COMPILER.tokenize(built).stream()
                    .map(Symbol::toString).collect(Collectors.joining(" "));
            Assert.assertEquals(text, built, parse(text));
        }
    }

    /**
     * <strong>Goal:</strong> Verify that the CCL of a {@link Criteria} whose
     * keys are reserved words parses to the condition of its symbols.
     * <p>
     * <strong>Start state:</strong> No prior state needed.
     * <p>
     * <strong>Workflow:</strong>
     * <ul>
     * <li>Build {@code from LINKS_TO 1746828956920001 OR to LINKS_TO
     * 1746828956920001} as a {@link Criteria}.</li>
     * <li>Parse {@link Criteria#ccl()} and {@link Criteria#symbols()}.</li>
     * </ul>
     * <p>
     * <strong>Expected:</strong> The two conditions are equal.
     */
    @Test
    public void testCriteriaOnReservedWordKeysParsesFromItsText() {
        Criteria criteria = Criteria.where().key("from")
                .operator(Operator.LINKS_TO).value(1746828956920001L).or()
                .key("to").operator(Operator.LINKS_TO)
                .value(1746828956920001L).build();
        Assert.assertEquals(COMPILER.parse(criteria.symbols()),
                parse(criteria.ccl()));
    }

    /**
     * <strong>Goal:</strong> Verify that a reserved word parses as a key that a
     * command names, as a key in a function, and as the prefix of a scoped
     * condition.
     * <p>
     * <strong>Start state:</strong> No prior state needed.
     * <p>
     * <strong>Workflow:</strong>
     * <ul>
     * <li>Parse commands that name a reserved word where they take a key.</li>
     * <li>Parse a function statement, a key function and value functions on
     * reserved words.</li>
     * <li>Parse {@code find from contains "x" at 1700000000}.</li>
     * <li>Parse a scoped condition whose prefix and key are reserved
     * words.</li>
     * </ul>
     * <p>
     * <strong>Expected:</strong> Each reserved word is the key in the place
     * where it appears. The {@code find} command reads at {@code 1700000000},
     * and its condition is {@code from contains "x"}.
     */
    @Test
    public void testReservedWordParsesAsKeyWhereverAKeyIsExpected() {
        GetSymbol get = (GetSymbol) command("get from in 1").root();
        Assert.assertEquals(keys("from"), ImmutableList.copyOf(get.keys()));
        Assert.assertEquals(Long.valueOf(1), get.record());
        SetSymbol set = (SetSymbol) command("set set as 1 in 1").root();
        Assert.assertEquals(new KeySymbol("set"), set.key());
        Assert.assertEquals(new ValueSymbol(1), set.value());
        LinkSymbol link = (LinkSymbol) command("link to from 1 to 2").root();
        Assert.assertEquals(new KeySymbol("to"), link.key());
        Assert.assertEquals(1, link.source());
        Assert.assertEquals(ImmutableList.of(2L),
                ImmutableList.copyOf(link.destinations()));
        Assert.assertEquals(new KeySymbol("for"),
                ((SearchSymbol) command("search for for \"jeff\"").root())
                        .key());
        Assert.assertEquals(new KeySymbol("from"),
                ((AuditSymbol) command("audit from in 1").root()).key());
        Assert.assertEquals(new KeySymbol("in"),
                ((ClearSymbol) command("clear in from 1").root()).key());
        Assert.assertEquals(keys("limit"), ImmutableList.copyOf(
                ((NavigateSymbol) command("navigate limit from 1").root())
                        .keys()));
        Assert.assertEquals(
                new FunctionTree(new FunctionKeySymbol(
                        new ImplicitKeyRecordFunction("count", "select"))),
                parse("select | count"));
        Assert.assertEquals(
                expression(
                        new FunctionKeySymbol(new ImplicitKeyRecordFunction(
                                "count", "from")),
                        Operator.GREATER_THAN, 3),
                parse("from | count > 3"));
        Assert.assertEquals(
                new FunctionValueSymbol(new IndexFunction("sum", "limit")),
                ((ExpressionSymbol) parse("total = sum(limit)").root())
                        .values().get(0));
        Assert.assertEquals(
                new FunctionValueSymbol(new KeyRecordsFunction("sum", "limit",
                        ImmutableList.of("1", "2"))),
                ((ExpressionSymbol) parse("total = sum(limit, 1, 2)").root())
                        .values().get(0));
        CommandTree search = command("find from contains \"x\" at 1700000000");
        Assert.assertEquals(new TimestampSymbol(1700000000L),
                ((FindSymbol) search.root()).timestamp());
        Assert.assertEquals(
                expression(new KeySymbol("from"), Operator.CONTAINS, "x"),
                search.conditionTree());
        ValueTokenSymbol<?> conditional = ((ExpressionSymbol) parse(
                "total = count(to, from > 1)").root()).values().get(0);
        Assert.assertEquals(new FunctionValueSymbol(new KeyConditionFunction(
                "count", "to",
                expression(new KeySymbol("from"), Operator.GREATER_THAN, 1))),
                conditional);
        Assert.assertEquals(
                new ScopedConditionTree(new KeySymbol("from"),
                        equalsOne("to")),
                parse("from.(to = 1)"));
    }

    /**
     * <strong>Goal:</strong> Verify that a statement that starts with a
     * reserved word key is a condition, and that a page or a command that
     * starts with the same word keeps its meaning.
     * <p>
     * <strong>Start state:</strong> No prior state needed.
     * <p>
     * <strong>Workflow:</strong>
     * <ul>
     * <li>Parse {@code set = 1 and select > 2 order by limit limit 5}.</li>
     * <li>Parse {@code limit = 5} and {@code limit 5}.</li>
     * <li>Compile {@code set = 1; set name as jeff in 1}.</li>
     * </ul>
     * <p>
     * <strong>Expected:</strong> The first statement has a condition on
     * {@code set} and {@code select}, an order by {@code limit} and a limit of
     * 5. {@code limit = 5} is a condition and {@code limit 5} is a page. The
     *    compiled statements are a condition and a {@code set} command.
     */
    @Test
    public void testStatementThatStartsWithReservedWordKeyIsCondition() {
        CommandTree statement = command(
                "set = 1 and select > 2 order by limit limit 5");
        Assert.assertEquals(ImplicitSymbol.INSTANCE, statement.root());
        Assert.assertEquals(parse("set = 1 and select > 2"),
                statement.conditionTree());
        Assert.assertEquals(ascending("limit"),
                statement.orderTree().root());
        Assert.assertEquals(PageSymbol.fromLimit(5),
                statement.pageTree().root());
        Assert.assertEquals(expression(new KeySymbol("limit"),
                Operator.EQUALS, 5), parse("limit = 5"));
        Assert.assertEquals(new PageTree(PageSymbol.fromLimit(5)),
                parse("limit 5"));
        List<AbstractSyntaxTree> statements = COMPILER
                .compile("set = 1; set name as jeff in 1");
        Assert.assertEquals(equalsOne("set"), statements.get(0));
        Assert.assertTrue(
                ((CommandTree) statements.get(1)).root() instanceof SetSymbol);
    }

    /**
     * <strong>Goal:</strong> Verify that a command line that starts with a
     * reserved word key is a condition, and that a command line with a command
     * or a page keeps its meaning.
     * <p>
     * <strong>Start state:</strong> No prior state needed.
     * <p>
     * <strong>Workflow:</strong>
     * <ul>
     * <li>Parse {@code set = 1}, {@code set name as jeff in 1} and
     * {@code limit 5} with {@link Grammar#StartCommandLine()}.</li>
     * </ul>
     * <p>
     * <strong>Expected:</strong> The first line holds the comparison
     * {@code set = 1}, the second holds a {@link SetSymbol} for the key
     * {@code name}, and the third holds a page with a limit of 5.
     */
    @Test
    public void testCommandLineThatStartsWithReservedWordKeyIsCondition()
            throws ParseException {
        Assert.assertEquals(
                ExpressionSymbol.create(new KeySymbol("set"),
                        new OperatorSymbol(Operator.EQUALS),
                        new ValueSymbol(1)),
                commandLine("set = 1").jjtGetChild(0));
        SetSymbol set = (SetSymbol) ((ASTCommand) commandLine(
                "set name as jeff in 1").jjtGetChild(0)).command();
        Assert.assertEquals(new KeySymbol("name"), set.key());
        Assert.assertEquals(PageSymbol.fromLimit(5),
                ((ASTPage) commandLine("limit 5").jjtGetChild(0)).page());
    }

    /**
     * <strong>Goal:</strong> Verify that the optional {@code for} of an await
     * command is the key {@code for} when it cannot be the optional word.
     * <p>
     * <strong>Start state:</strong> No prior state needed.
     * <p>
     * <strong>Workflow:</strong>
     * <ul>
     * <li>Parse {@code awaitFind} with {@code for = 1},
     * {@code for for = 1} and {@code for name = 1}.</li>
     * <li>Parse {@code awaitSelect} with {@code for, name where},
     * {@code for[1700000000], name where}, {@code for for where} and
     * {@code for name where}.</li>
     * </ul>
     * <p>
     * <strong>Expected:</strong> The {@code awaitFind} conditions are
     * {@code for = 1}, {@code for = 1} and {@code name = 1}. The
     * {@code awaitSelect} commands select {@code for} and {@code name},
     * {@code for} at {@code 1700000000} and {@code name}, {@code for}, and
     * {@code name}.
     */
    @Test
    public void testOptionalForOfAwaitCommandYieldsToKeyNamedFor() {
        String await = "awaitFind within \"1 s\" ";
        Assert.assertEquals(equalsOne("for"),
                command(await + "for = 1").conditionTree());
        Assert.assertEquals(equalsOne("for"),
                command(await + "for for = 1").conditionTree());
        Assert.assertEquals(equalsOne("name"),
                command(await + "for name = 1").conditionTree());
        await = "awaitSelect within \"1 s\" ";
        Assert.assertEquals(keys("for", "name"),
                ImmutableList.copyOf(((AwaitSelectSymbol) command(
                        await + "for, name where a = 1").root()).keys()));
        Assert.assertEquals(
                ImmutableList.of(
                        new TemporalKeySymbol(new KeySymbol("for"),
                                new TimestampSymbol(1700000000L)),
                        new KeySymbol("name")),
                ImmutableList.copyOf(((AwaitSelectSymbol) command(
                        await + "for[1700000000], name where a = 1").root())
                                .keys()));
        Assert.assertEquals(keys("for"),
                ImmutableList.copyOf(((AwaitSelectSymbol) command(
                        await + "for for where a = 1").root()).keys()));
        Assert.assertEquals(keys("name"),
                ImmutableList.copyOf(((AwaitSelectSymbol) command(
                        await + "for name where a = 1").root()).keys()));
    }

    /**
     * <strong>Goal:</strong> Verify that commands that start with a reserved
     * word, or that use one between their parts, keep their meaning.
     * <p>
     * <strong>Start state:</strong> No prior state needed.
     * <p>
     * <strong>Workflow:</strong>
     * <ul>
     * <li>Parse {@code select}, {@code link}, {@code audit}, {@code set},
     * {@code search}, {@code navigate}, {@code get} and {@code clear} commands
     * whose keys are not reserved words.</li>
     * </ul>
     * <p>
     * <strong>Expected:</strong> Each command has the type, keys, records,
     * values and condition that its text names.
     */
    @Test
    public void testCommandsKeepTheirMeaning() {
        SelectSymbol select = (SelectSymbol) command("select name from 1")
                .root();
        Assert.assertEquals(keys("name"), ImmutableList.copyOf(select.keys()));
        Assert.assertEquals(Long.valueOf(1), select.record());
        CommandTree where = command("select name where age > 30");
        Assert.assertEquals(keys("name"), ImmutableList
                .copyOf(((SelectSymbol) where.root()).keys()));
        Assert.assertEquals(
                expression(new KeySymbol("age"), Operator.GREATER_THAN, 30),
                where.conditionTree());
        Assert.assertEquals(keys("name"), ImmutableList.copyOf(
                ((SelectSymbol) command("select [name] from 1").root())
                        .keys()));
        LinkSymbol link = (LinkSymbol) command("link friends from 1 to 2")
                .root();
        Assert.assertEquals(new KeySymbol("friends"), link.key());
        Assert.assertEquals(1, link.source());
        Assert.assertEquals(ImmutableList.of(2L),
                ImmutableList.copyOf(link.destinations()));
        AuditSymbol audit = (AuditSymbol) command(
                "audit name in 1 from \"2024-01-01\" to \"2024-02-01\"")
                        .root();
        Assert.assertEquals(new KeySymbol("name"), audit.key());
        Assert.assertEquals(1, audit.record());
        audit = (AuditSymbol) command(
                "audit name in 1 from 1700000000 to 1800000000").root();
        Assert.assertEquals(new TimestampSymbol(1700000000L), audit.start());
        Assert.assertEquals(new TimestampSymbol(1800000000L), audit.end());
        SetSymbol set = (SetSymbol) command("set name as jeff in 1").root();
        Assert.assertEquals(new KeySymbol("name"), set.key());
        Assert.assertEquals(new ValueSymbol("jeff"), set.value());
        Assert.assertEquals(Long.valueOf(1), set.record());
        SearchSymbol search = (SearchSymbol) command("search name for \"jeff\"")
                .root();
        Assert.assertEquals(new KeySymbol("name"), search.key());
        Assert.assertEquals("jeff", search.query());
        CommandTree navigate = command("navigate friends.name where age > 21");
        Assert.assertEquals(
                ImmutableList.of(new NavigationKeySymbol("friends.name")),
                ImmutableList
                        .copyOf(((NavigateSymbol) navigate.root()).keys()));
        Assert.assertEquals(
                expression(new KeySymbol("age"), Operator.GREATER_THAN, 21),
                navigate.conditionTree());
        GetSymbol get = (GetSymbol) command("get from 1").root();
        Assert.assertNull(get.keys());
        Assert.assertEquals(Long.valueOf(1), get.record());
        Assert.assertEquals(ImmutableList.of(1L, 2L), ImmutableList.copyOf(
                ((GetSymbol) command("get from [1, 2]").root()).records()));
        Assert.assertEquals(ImmutableList.of(1L, 2L), ImmutableList.copyOf(
                ((ClearSymbol) command("clear [1, 2]").root()).records()));
    }

}
