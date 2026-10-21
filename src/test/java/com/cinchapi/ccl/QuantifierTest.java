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
import com.cinchapi.ccl.grammar.NavigationKeySymbol;
import com.cinchapi.ccl.grammar.OperatorSymbol;
import com.cinchapi.ccl.grammar.QuantifierSymbol;
import com.cinchapi.ccl.grammar.Symbol;
import com.cinchapi.ccl.grammar.ValueSetSymbol;
import com.cinchapi.ccl.grammar.ValueSymbol;
import com.cinchapi.ccl.grammar.ValueTokenSymbol;
import com.cinchapi.ccl.syntax.AbstractSyntaxTree;
import com.cinchapi.ccl.syntax.CommandTree;
import com.cinchapi.ccl.syntax.ConditionTree;
import com.cinchapi.ccl.syntax.ExpressionTree;
import com.cinchapi.ccl.type.Operator;
import com.cinchapi.common.base.Array;
import com.cinchapi.common.function.TriFunction;
import com.cinchapi.concourse.thrift.TObject;
import com.cinchapi.concourse.util.Convert;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;

/**
 * Coverage for quantified value sets: the {@code any} and {@code every}
 * quantifiers and their aliases, the {@code in} and {@code not_in} sugar, the
 * tuple form for {@code between}, the symbols a quantified expression exposes,
 * the token stream it flattens to, local evaluation, and the statements the
 * grammar rejects.
 *
 * @author Jeff Nelson
 */
public class QuantifierTest {

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
     * The evaluator the tests pass to {@link Compiler#evaluate}, which applies
     * a Concourse {@link Operator} to a stored value and the operand values of
     * an expression.
     */
    private static final TriFunction<Object, Operator, List<Object>, Boolean> EVALUATOR =
            QuantifierTest::evaluate;

    /**
     * Return whether {@code value} satisfies the Concourse {@code operator} in
     * relation to {@code values}.
     *
     * @param value the stored value
     * @param operator the operator
     * @param values the operand values
     * @return {@code true} if the comparison holds
     */
    private static Boolean evaluate(Object value, Operator operator,
            List<Object> values) {
        TObject tvalue = Convert.javaToThrift(value);
        TObject[] tvalues = values.stream().map(Convert::javaToThrift)
                .collect(Collectors.toList()).toArray(Array.containing());
        com.cinchapi.concourse.thrift.Operator toperator = Convert
                .stringToOperator(operator.symbol());
        return tvalue.is(toperator, tvalues);
    }

    /**
     * Return a {@link Compiler} that uses the Concourse driver transforms.
     *
     * @return the {@link Compiler}
     */
    private static Compiler compiler() {
        return Compiler.create(VALUE_FN, OP_FN);
    }

    /**
     * Parse {@code ccl} as one {@link ConditionTree}.
     *
     * @param ccl the condition to parse
     * @return the {@link ConditionTree}
     */
    private static ConditionTree parse(String ccl) {
        return (ConditionTree) compiler().parse(ccl);
    }

    /**
     * Parse {@code ccl}, which must be a single comparison, and return its
     * {@link ExpressionSymbol}.
     *
     * @param ccl the comparison to parse
     * @return the {@link ExpressionSymbol}
     */
    private static ExpressionSymbol expression(String ccl) {
        return (ExpressionSymbol) parse(ccl).root();
    }

    /**
     * Return an {@link OperatorSymbol} for the Concourse {@code operator}.
     *
     * @param operator the Concourse operator
     * @return the {@link OperatorSymbol}
     */
    private static OperatorSymbol op(
            com.cinchapi.concourse.thrift.Operator operator) {
        return new OperatorSymbol(operator);
    }

    /**
     * Return {@link ValueSymbol ValueSymbols} for each of {@code values}.
     *
     * @param values the raw values
     * @return the symbols, in order
     */
    private static ValueTokenSymbol<?>[] values(Object... values) {
        ValueTokenSymbol<?>[] symbols = new ValueTokenSymbol<?>[values.length];
        for (int i = 0; i < values.length; ++i) {
            symbols[i] = new ValueSymbol(values[i]);
        }
        return symbols;
    }

    /**
     * Assert that parsing {@code ccl} fails with a {@link SyntaxException}.
     *
     * @param ccl the statement to parse
     */
    private static void assertRejected(String ccl) {
        try {
            compiler().parse(ccl);
            Assert.fail("expected a SyntaxException for " + ccl);
        }
        catch (SyntaxException e) {
            // expected
        }
    }

    /**
     * Assert that {@code ccl} parses to the same tree as the text produced by
     * joining its token stream with spaces, and as its own
     * {@link Object#toString()}.
     *
     * @param ccl the condition to parse
     */
    private static void assertRoundTripStable(String ccl) {
        Compiler compiler = compiler();
        AbstractSyntaxTree first = compiler.parse(ccl);
        String reemitted = compiler.tokenize(first).stream()
                .map(Symbol::toString).collect(Collectors.joining(" "));
        Assert.assertEquals("token round-trip failed for: " + ccl
                + " (re-emitted as: " + reemitted + ")", first,
                compiler.parse(reemitted));
        Assert.assertEquals("toString round-trip failed for: " + ccl, first,
                compiler.parse(first.root().toString()));
    }

    /**
     * <strong>Goal:</strong> Verify that {@code any[...]} parses to an
     * expression that exposes the quantifier and each member value, and that it
     * equals the expression built through the {@code ExpressionSymbol.create}
     * overload that takes a quantifier.
     * <p>
     * <strong>Start state:</strong> No prior state needed.
     * <p>
     * <strong>Workflow:</strong>
     * <ul>
     * <li>Parse {@code name = any["Jeff", "John", "Jane"]}.</li>
     * <li>Read the key, operator, quantifier, values and raw content.</li>
     * <li>Build the same expression with {@code ExpressionSymbol.create}.</li>
     * </ul>
     * <p>
     * <strong>Expected:</strong> The key is {@code name}, the operator is
     * {@code EQUALS}, the quantifier is {@link QuantifierSymbol#ANY}, the
     * values are the three names in order, the raw content reports the same
     * quantifier and values, and the parsed and built expressions are equal
     * with equal hash codes.
     */
    @Test
    public void testAnyParsesToQuantifiedExpression() {
        ExpressionSymbol expression = expression(
                "name = any[\"Jeff\", \"John\", \"Jane\"]");
        Assert.assertEquals(new KeySymbol("name"), expression.key());
        Assert.assertEquals(op(com.cinchapi.concourse.thrift.Operator.EQUALS),
                expression.operator());
        Assert.assertEquals(QuantifierSymbol.ANY, expression.quantifier());
        Assert.assertEquals(
                ImmutableList.copyOf(values("Jeff", "John", "Jane")),
                expression.values());
        Assert.assertEquals(QuantifierSymbol.ANY,
                expression.raw().quantifier());
        Assert.assertEquals(ImmutableList.of("Jeff", "John", "Jane"),
                expression.raw().values());
        ExpressionSymbol built = ExpressionSymbol.create(new KeySymbol("name"),
                op(com.cinchapi.concourse.thrift.Operator.EQUALS),
                QuantifierSymbol.ANY, values("Jeff", "John", "Jane"));
        Assert.assertEquals(built, expression);
        Assert.assertEquals(built.hashCode(), expression.hashCode());
    }

    /**
     * <strong>Goal:</strong> Verify that a comparison without a quantifier
     * reports no quantifier, and that a one-member quantified set is a
     * different expression from the plain comparison with that value.
     * <p>
     * <strong>Start state:</strong> No prior state needed.
     * <p>
     * <strong>Workflow:</strong>
     * <ul>
     * <li>Parse {@code name = "Jeff"} and {@code name = any["Jeff"]}.</li>
     * </ul>
     * <p>
     * <strong>Expected:</strong> The plain comparison has a {@code null}
     * quantifier and raw quantifier. The quantified one has
     * {@link QuantifierSymbol#ANY} and a single value. The two are not equal.
     */
    @Test
    public void testPlainComparisonHasNoQuantifier() {
        ExpressionSymbol plain = expression("name = \"Jeff\"");
        ExpressionSymbol quantified = expression("name = any[\"Jeff\"]");
        Assert.assertNull(plain.quantifier());
        Assert.assertNull(plain.raw().quantifier());
        Assert.assertEquals(QuantifierSymbol.ANY, quantified.quantifier());
        Assert.assertEquals(ImmutableList.copyOf(values("Jeff")),
                quantified.values());
        Assert.assertNotEquals(plain, quantified);
    }

    /**
     * <strong>Goal:</strong> Verify that {@code either} and {@code some} are
     * aliases of {@code any}, that {@code all} is an alias of {@code every},
     * and that quantifier words are case-insensitive.
     * <p>
     * <strong>Start state:</strong> No prior state needed.
     * <p>
     * <strong>Workflow:</strong>
     * <ul>
     * <li>Parse the same comparison with {@code any}, {@code either},
     * {@code some} and {@code ANY}, and with {@code every}, {@code all} and
     * {@code EVERY}.</li>
     * </ul>
     * <p>
     * <strong>Expected:</strong> Each spelling in a group parses to a tree
     * equal to the others in that group, and the {@code any} group is not equal
     * to the {@code every} group.
     */
    @Test
    public void testQuantifierAliasesAndCaseParseToSameTree() {
        ConditionTree any = parse("tags = any[\"a\", \"b\"]");
        Assert.assertEquals(any, parse("tags = either[\"a\", \"b\"]"));
        Assert.assertEquals(any, parse("tags = some[\"a\", \"b\"]"));
        Assert.assertEquals(any, parse("tags = ANY[\"a\", \"b\"]"));
        ConditionTree every = parse("tags = every[\"a\", \"b\"]");
        Assert.assertEquals(every, parse("tags = all[\"a\", \"b\"]"));
        Assert.assertEquals(every, parse("tags = EVERY[\"a\", \"b\"]"));
        Assert.assertEquals(QuantifierSymbol.EVERY,
                ((ExpressionSymbol) every.root()).quantifier());
        Assert.assertNotEquals(any, every);
    }

    /**
     * <strong>Goal:</strong> Verify that a quantifier composes with every
     * single-operand operator, including the link, pattern and search operators
     * that take their own value forms.
     * <p>
     * <strong>Start state:</strong> No prior state needed.
     * <p>
     * <strong>Workflow:</strong>
     * <ul>
     * <li>For each of {@code =}, {@code !=}, {@code <}, {@code <=}, {@code >},
     * {@code >=}, {@code like}, {@code nlike}, {@code regex}, {@code nregex},
     * {@code contains} and {@code not_contains}, parse
     * {@code name <op> every["a", "b"]}.</li>
     * <li>Parse {@code friends lnks2 any[2, 7, 9]}.</li>
     * </ul>
     * <p>
     * <strong>Expected:</strong> Each expression has the operator the Concourse
     * driver maps the token to, the {@link QuantifierSymbol#EVERY} quantifier
     * and the values {@code a} and {@code b}. The link expression has
     * {@code LINKS_TO}, {@link QuantifierSymbol#ANY} and the three record ids
     * as numbers.
     */
    @Test
    public void testQuantifierComposesWithEachSingleOperandOperator() {
        for (String token : ImmutableList.of("=", "!=", "<", "<=", ">", ">=",
                "like", "nlike", "regex", "nregex", "contains",
                "not_contains")) {
            ExpressionSymbol expression = expression(
                    "name " + token + " every[\"a\", \"b\"]");
            Assert.assertEquals(token, Convert.stringToOperator(token),
                    expression.operator().operator());
            Assert.assertEquals(token, QuantifierSymbol.EVERY,
                    expression.quantifier());
            Assert.assertEquals(token, ImmutableList.copyOf(values("a", "b")),
                    expression.values());
        }
        ExpressionSymbol links = expression("friends lnks2 any[2, 7, 9]");
        Assert.assertEquals(com.cinchapi.concourse.thrift.Operator.LINKS_TO,
                links.operator().operator());
        Assert.assertEquals(QuantifierSymbol.ANY, links.quantifier());
        Assert.assertEquals(ImmutableList.of(2, 7, 9), links.raw().values());
    }

    /**
     * <strong>Goal:</strong> Verify that the quantifier composes with
     * {@code between} through a set of two-value tuples, and that the
     * expression exposes the tuples as one flat value list in order.
     * <p>
     * <strong>Start state:</strong> No prior state needed.
     * <p>
     * <strong>Workflow:</strong>
     * <ul>
     * <li>Parse {@code age bw any[(1, 5), (10, 20)]}.</li>
     * <li>Parse the same set with {@code every} and with {@code ><}.</li>
     * </ul>
     * <p>
     * <strong>Expected:</strong> The operator is {@code BETWEEN}, the
     * quantifier is {@link QuantifierSymbol#ANY} and the raw values are
     * {@code 1, 5, 10, 20}. The {@code every} form has quantifier
     * {@link QuantifierSymbol#EVERY}; the {@code ><} spelling parses to the
     * same tree as {@code bw}.
     */
    @Test
    public void testBetweenAcceptsQuantifiedTuples() {
        ExpressionSymbol expression = expression(
                "age bw any[(1, 5), (10, 20)]");
        Assert.assertEquals(com.cinchapi.concourse.thrift.Operator.BETWEEN,
                expression.operator().operator());
        Assert.assertEquals(QuantifierSymbol.ANY, expression.quantifier());
        Assert.assertEquals(ImmutableList.of(1, 5, 10, 20),
                expression.raw().values());
        Assert.assertEquals(QuantifierSymbol.EVERY,
                expression("age bw every[(1, 5), (10, 20)]").quantifier());
        Assert.assertEquals(parse("age bw any[(1, 5), (10, 20)]"),
                parse("age >< any[(1, 5), (10, 20)]"));
    }

    /**
     * <strong>Goal:</strong> Verify that {@code in} is sugar for
     * {@code = any[...]} and {@code not_in} is sugar for {@code != every[...]},
     * in any letter case and with each alias.
     * <p>
     * <strong>Start state:</strong> No prior state needed.
     * <p>
     * <strong>Workflow:</strong>
     * <ul>
     * <li>Parse {@code name in ["a", "b"]} and {@code name IN ["a", "b"]}.</li>
     * <li>Parse {@code name not_in ["a", "b"]}, {@code name nin ["a", "b"]}
     * and {@code name notin ["a", "b"]}.</li>
     * </ul>
     * <p>
     * <strong>Expected:</strong> The {@code in} forms equal
     * {@code name = any["a", "b"]} and the {@code not_in} forms equal
     * {@code name != every["a", "b"]}.
     */
    @Test
    public void testInAndNotInAreSugarForQuantifiedEquality() {
        ConditionTree any = parse("name = any[\"a\", \"b\"]");
        Assert.assertEquals(any, parse("name in [\"a\", \"b\"]"));
        Assert.assertEquals(any, parse("name IN [\"a\", \"b\"]"));
        ConditionTree every = parse("name != every[\"a\", \"b\"]");
        Assert.assertEquals(every, parse("name not_in [\"a\", \"b\"]"));
        Assert.assertEquals(every, parse("name nin [\"a\", \"b\"]"));
        Assert.assertEquals(every, parse("name notin [\"a\", \"b\"]"));
    }

    /**
     * <strong>Goal:</strong> Verify that {@code not_in}, {@code nin} and
     * {@code notin} are reserved words in any letter case: each is a syntax
     * error as an unquoted value or as a key, parses as a string when quoted,
     * and still parses as a stop in a navigation key.
     * <p>
     * <strong>Start state:</strong> No prior state needed.
     * <p>
     * <strong>Workflow:</strong>
     * <ul>
     * <li>For each of {@code not_in}, {@code nin}, {@code notin} and
     * {@code NIN}, parse {@code status = <word>} and {@code <word> = 1}.</li>
     * <li>For each word, parse {@code status = "<word>"} and
     * {@code <word>.name = 1}.</li>
     * </ul>
     * <p>
     * <strong>Expected:</strong> The unquoted value and the key fail with a
     * {@link SyntaxException}. The quoted form has the string value
     * {@code <word>}, and the navigation form has the key {@code <word>.name}.
     */
    @Test
    public void testNotInWordsAreReserved() {
        for (String word : ImmutableList.of("not_in", "nin", "notin", "NIN")) {
            assertRejected("status = " + word);
            assertRejected(word + " = 1");
            Assert.assertEquals(word, ImmutableList.of(word),
                    expression("status = \"" + word + "\"").raw().values());
            Assert.assertEquals(word, new NavigationKeySymbol(word + ".name"),
                    expression(word + ".name = 1").key());
        }
    }

    /**
     * <strong>Goal:</strong> Verify that a quantified value set must hold at
     * least one member and, for {@code between}, whole pairs, both when parsed
     * and when built through {@code ExpressionSymbol.create}, and that
     * parentheses do not delimit a set.
     * <p>
     * <strong>Start state:</strong> No prior state needed.
     * <p>
     * <strong>Workflow:</strong>
     * <ul>
     * <li>Parse {@code name = any[]}, {@code name = every[]},
     * {@code name in []}, {@code age bw any[]} and
     * {@code age bw any[(1, 5), 7]}.</li>
     * <li>Parse {@code name = any("a", "b")} and
     * {@code name in ("a", "b")}.</li>
     * <li>Build {@code age bw any[1]} and {@code name = any[]} with
     * {@code ExpressionSymbol.create}.</li>
     * </ul>
     * <p>
     * <strong>Expected:</strong> Each statement fails with a
     * {@link SyntaxException}, and each built expression fails with an
     * {@link IllegalArgumentException}.
     */
    @Test
    public void testEmptyAndMalformedValueSetsAreRejected() {
        assertRejected("name = any[]");
        assertRejected("name = every[]");
        assertRejected("name in []");
        assertRejected("age bw any[]");
        assertRejected("age bw any[(1, 5), 7]");
        assertRejected("name = any(\"a\", \"b\")");
        assertRejected("name in (\"a\", \"b\")");
        try {
            ExpressionSymbol.create(new KeySymbol("age"),
                    op(com.cinchapi.concourse.thrift.Operator.BETWEEN),
                    QuantifierSymbol.ANY, values(1));
            Assert.fail(
                    "expected an IllegalArgumentException for a lone value");
        }
        catch (IllegalArgumentException e) {
            // expected
        }
        try {
            ExpressionSymbol.create(new KeySymbol("name"),
                    op(com.cinchapi.concourse.thrift.Operator.EQUALS),
                    QuantifierSymbol.ANY);
            Assert.fail("expected an IllegalArgumentException for no values");
        }
        catch (IllegalArgumentException e) {
            // expected
        }
    }

    /**
     * <strong>Goal:</strong> Verify that a {@code $name} placeholder inside a
     * {@link ValueSetSymbol} is substituted when symbols are parsed with
     * locally-bound data.
     * <p>
     * <strong>Start state:</strong> No prior state needed.
     * <p>
     * <strong>Workflow:</strong>
     * <ul>
     * <li>Build {@code name = any[$x, "b"]} with
     * {@code ExpressionSymbol.create} and tokenize it.</li>
     * <li>Parse the tokens with {@code x} bound to {@code Jeff}.</li>
     * </ul>
     * <p>
     * <strong>Expected:</strong> The result equals the parse of
     * {@code name = any["Jeff", "b"]}.
     */
    @Test
    public void testPlaceholderInsideValueSetIsResolvedFromSymbols() {
        Compiler compiler = compiler();
        ExpressionSymbol built = ExpressionSymbol.create(new KeySymbol("name"),
                op(com.cinchapi.concourse.thrift.Operator.EQUALS),
                QuantifierSymbol.ANY, values("$x", "b"));
        List<Symbol> tokens = compiler.tokenize(new ExpressionTree(built));
        Assert.assertEquals(parse("name = any[\"Jeff\", \"b\"]"),
                compiler.parse(tokens, ImmutableMultimap.of("x", "Jeff")));
    }

    /**
     * <strong>Goal:</strong> Verify that the quantifier words stay usable as
     * ordinary keys and values, so existing statements that use them keep their
     * meaning.
     * <p>
     * <strong>Start state:</strong> No prior state needed.
     * <p>
     * <strong>Workflow:</strong>
     * <ul>
     * <li>Parse {@code status = all}, {@code any = 1}, {@code count = some}
     * and {@code note = all hands}.</li>
     * </ul>
     * <p>
     * <strong>Expected:</strong> {@code status = all} has no quantifier and the
     * value {@code all}; {@code any = 1} has the key {@code any};
     * {@code count = some} has the value {@code some}; the multi-word value is
     * {@code all hands}.
     */
    @Test
    public void testQuantifierWordsRemainOrdinaryKeysAndValues() {
        ExpressionSymbol status = expression("status = all");
        Assert.assertNull(status.quantifier());
        Assert.assertEquals(ImmutableList.of("all"), status.raw().values());
        Assert.assertEquals(new KeySymbol("any"), expression("any = 1").key());
        Assert.assertEquals(ImmutableList.of("some"),
                expression("count = some").raw().values());
        Assert.assertEquals(ImmutableList.of("all hands"),
                expression("note = all hands").raw().values());
    }

    /**
     * <strong>Goal:</strong> Verify that a quantified comparison takes an
     * expression-level timestamp, nests inside parentheses and conjunctions,
     * and parses in the {@code findOrInsert} condition, which uses the
     * no-timestamp expression productions.
     * <p>
     * <strong>Start state:</strong> No prior state needed.
     * <p>
     * <strong>Workflow:</strong>
     * <ul>
     * <li>Parse {@code name = any["a", "b"] at 1700000000}.</li>
     * <li>Parse {@code (name = any["a", "b"] and age > 5) or x in [1, 2]}.</li>
     * <li>Parse {@code findOrInsert name = any["a", "b"]} followed by a JSON
     * payload.</li>
     * </ul>
     * <p>
     * <strong>Expected:</strong> The first expression has the timestamp
     * {@code 1700000000} and quantifier {@link QuantifierSymbol#ANY}. The
     * second is an {@code or} tree whose right leaf is quantified. The third is
     * a {@link CommandTree} whose condition is the quantified expression.
     */
    @Test
    public void testQuantifierComposesWithTimestampsNestingAndCommands() {
        ExpressionSymbol timed = expression(
                "name = any[\"a\", \"b\"] at 1700000000");
        Assert.assertEquals(1700000000L, timed.timestamp().timestamp());
        Assert.assertEquals(QuantifierSymbol.ANY, timed.quantifier());
        ConditionTree nested = parse(
                "(name = any[\"a\", \"b\"] and age > 5) or x in [1, 2]");
        ExpressionSymbol right = (ExpressionSymbol) nested.children().stream()
                .skip(1).findFirst().get().root();
        Assert.assertEquals(QuantifierSymbol.ANY, right.quantifier());
        Assert.assertEquals(ImmutableList.of(1, 2), right.raw().values());
        CommandTree command = (CommandTree) compiler()
                .parse("findOrInsert name = any[\"a\", \"b\"] "
                        + "\"{\\\"name\\\": \\\"a\\\"}\"");
        Assert.assertEquals(expression("name = any[\"a\", \"b\"]"),
                command.conditionTree().root());
    }

    /**
     * <strong>Goal:</strong> Verify that a quantified expression flattens to a
     * token stream in which the value set is one {@link ValueSetSymbol} that
     * renders as valid CCL, that {@link Parsing#groupExpressions(List)}
     * rebuilds the quantified expression from that stream, and that
     * {@link Parsing#ungroupExpressions(List)} reverses it.
     * <p>
     * <strong>Start state:</strong> No prior state needed.
     * <p>
     * <strong>Workflow:</strong>
     * <ul>
     * <li>Parse {@code age bw any[(1, 5), (10, 20)] at 1700000000} and
     * {@link Compiler#tokenize(AbstractSyntaxTree) tokenize} it.</li>
     * <li>Group the tokens, then ungroup the result.</li>
     * </ul>
     * <p>
     * <strong>Expected:</strong> The tokens are the key, the operator, one
     * {@link ValueSetSymbol} with quantifier {@link QuantifierSymbol#ANY},
     * arity 2 and the four values, and the timestamp. The set renders as
     * {@code any[(1, 5), (10, 20)]}. Grouping yields one expression equal to
     * the parsed root, and ungrouping yields the original tokens.
     */
    @Test
    public void testTokenStreamCarriesValueSetAsOneSymbol() {
        Compiler compiler = compiler();
        AbstractSyntaxTree tree = compiler
                .parse("age bw any[(1, 5), (10, 20)] at 1700000000");
        List<Symbol> tokens = compiler.tokenize(tree);
        Assert.assertEquals(4, tokens.size());
        Assert.assertEquals(new KeySymbol("age"), tokens.get(0));
        Assert.assertEquals(op(com.cinchapi.concourse.thrift.Operator.BETWEEN),
                tokens.get(1));
        ValueSetSymbol set = (ValueSetSymbol) tokens.get(2);
        Assert.assertEquals(QuantifierSymbol.ANY, set.quantifier());
        Assert.assertEquals(2, set.arity());
        Assert.assertEquals(ImmutableList.copyOf(values(1, 5, 10, 20)),
                set.values());
        Assert.assertEquals("any[(1, 5), (10, 20)]", set.toString());
        List<Symbol> grouped = Parsing.groupExpressions(tokens);
        Assert.assertEquals(ImmutableList.of(tree.root()), grouped);
        Assert.assertEquals(tokens, Parsing.ungroupExpressions(grouped));
    }

    /**
     * <strong>Goal:</strong> Verify that a {@link ValueSetSymbol} supplied as a
     * value when an expression is built is unpacked into the expression's
     * quantifier and values, so that an expression has one canonical form.
     * <p>
     * <strong>Start state:</strong> No prior state needed.
     * <p>
     * <strong>Workflow:</strong>
     * <ul>
     * <li>Build an expression with {@code ExpressionSymbol.create}, passing a
     * {@code ValueSetSymbol} with {@code EVERY} and two values as the only
     * value.</li>
     * </ul>
     * <p>
     * <strong>Expected:</strong> The expression equals the parse of
     * {@code tags = every["a", "b"]}, with quantifier
     * {@link QuantifierSymbol#EVERY} and the two member values.
     */
    @Test
    public void testValueSetSymbolSuppliedAsValueIsUnpacked() {
        ExpressionSymbol built = ExpressionSymbol.create(new KeySymbol("tags"),
                op(com.cinchapi.concourse.thrift.Operator.EQUALS),
                new ValueSetSymbol(QuantifierSymbol.EVERY,
                        ImmutableList.copyOf(values("a", "b"))));
        Assert.assertEquals(QuantifierSymbol.EVERY, built.quantifier());
        Assert.assertEquals(ImmutableList.copyOf(values("a", "b")),
                built.values());
        Assert.assertEquals(expression("tags = every[\"a\", \"b\"]"), built);
    }

    /**
     * <strong>Goal:</strong> Verify that each quantified form re-parses to an
     * equal tree from its token stream and from its {@code toString()}.
     * <p>
     * <strong>Start state:</strong> No prior state needed.
     * <p>
     * <strong>Workflow:</strong>
     * <ul>
     * <li>Round-trip a string set, a numeric link set, a {@code between}
     * tuple set, an {@code in} form, a {@code not_in} form, a quoted value with
     * a space, and a quantified comparison with a timestamp.</li>
     * </ul>
     * <p>
     * <strong>Expected:</strong> Each round trip yields an equal tree.
     */
    @Test
    public void testQuantifiedFormsRoundTrip() {
        assertRoundTripStable("name = any[\"Jeff\", \"John\"]");
        assertRoundTripStable("friends lnks2 every[2, 7]");
        assertRoundTripStable("age bw any[(1, 5), (10, 20)]");
        assertRoundTripStable("name in [\"a\", \"b\"]");
        assertRoundTripStable("name not_in [\"a\", \"b\"]");
        assertRoundTripStable("name = any[\"Jeff Nelson\", \"a=b\"]");
        assertRoundTripStable("name = all[\"a\", \"b\"] at 1700000000");
    }

    /**
     * <strong>Goal:</strong> Verify that local evaluation treats {@code any} as
     * a disjunction and {@code every} as a conjunction over the per-value
     * predicate, where each per-value predicate holds when any stored value
     * satisfies it.
     * <p>
     * <strong>Start state:</strong> No prior state needed.
     * <p>
     * <strong>Workflow:</strong>
     * <ul>
     * <li>Evaluate {@code tags = every["urgent", "backend"]} against tags
     * {@code urgent, backend, misc} and against {@code urgent, misc}.</li>
     * <li>Evaluate {@code age > every[20, 30, 40]} against 45 and 35.</li>
     * <li>Evaluate {@code age > any[20, 30, 40]} against 25 and 15.</li>
     * <li>Evaluate {@code name in ["Jeff", "John"]} against {@code John} and
     * {@code Jane}, and {@code name not_in ["Jeff", "John"]} against the
     * same.</li>
     * <li>Evaluate {@code age bw any[(1, 5), (10, 20)]} against 12 and 7.</li>
     * </ul>
     * <p>
     * <strong>Expected:</strong> The first value of each pair satisfies the
     * condition and the second does not, with {@code not_in} inverted.
     */
    @Test
    public void testLocalEvaluationOfQuantifiers() {
        Compiler compiler = compiler();
        ConditionTree every = parse("tags = every[\"urgent\", \"backend\"]");
        Assert.assertTrue(compiler.evaluate(every, ImmutableMultimap.of("tags",
                "urgent", "tags", "backend", "tags", "misc"), EVALUATOR));
        Assert.assertFalse(compiler.evaluate(every,
                ImmutableMultimap.of("tags", "urgent", "tags", "misc"),
                EVALUATOR));
        ConditionTree greaterEvery = parse("age > every[20, 30, 40]");
        Assert.assertTrue(compiler.evaluate(greaterEvery,
                ImmutableMultimap.of("age", 45), EVALUATOR));
        Assert.assertFalse(compiler.evaluate(greaterEvery,
                ImmutableMultimap.of("age", 35), EVALUATOR));
        ConditionTree greaterAny = parse("age > any[20, 30, 40]");
        Assert.assertTrue(compiler.evaluate(greaterAny,
                ImmutableMultimap.of("age", 25), EVALUATOR));
        Assert.assertFalse(compiler.evaluate(greaterAny,
                ImmutableMultimap.of("age", 15), EVALUATOR));
        ConditionTree in = parse("name in [\"Jeff\", \"John\"]");
        ConditionTree notIn = parse("name not_in [\"Jeff\", \"John\"]");
        Multimap<String, Object> john = ImmutableMultimap.of("name", "John");
        Multimap<String, Object> jane = ImmutableMultimap.of("name", "Jane");
        Assert.assertTrue(compiler.evaluate(in, john, EVALUATOR));
        Assert.assertFalse(compiler.evaluate(in, jane, EVALUATOR));
        Assert.assertFalse(compiler.evaluate(notIn, john, EVALUATOR));
        Assert.assertTrue(compiler.evaluate(notIn, jane, EVALUATOR));
        ConditionTree between = parse("age bw any[(1, 5), (10, 20)]");
        Assert.assertTrue(compiler.evaluate(between,
                ImmutableMultimap.of("age", 12), EVALUATOR));
        Assert.assertFalse(compiler.evaluate(between,
                ImmutableMultimap.of("age", 7), EVALUATOR));
    }

}
