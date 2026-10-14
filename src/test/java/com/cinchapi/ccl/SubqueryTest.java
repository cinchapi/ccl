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
import com.cinchapi.ccl.grammar.QuantifierSymbol;
import com.cinchapi.ccl.grammar.SubquerySymbol;
import com.cinchapi.ccl.grammar.Symbol;
import com.cinchapi.ccl.grammar.command.FindSymbol;
import com.cinchapi.ccl.grammar.command.GetSymbol;
import com.cinchapi.ccl.grammar.command.NavigateSymbol;
import com.cinchapi.ccl.grammar.command.SelectSymbol;
import com.cinchapi.ccl.grammar.command.TraceSymbol;
import com.cinchapi.ccl.syntax.AbstractSyntaxTree;
import com.cinchapi.ccl.syntax.CommandTree;
import com.cinchapi.ccl.syntax.ConditionTree;
import com.cinchapi.ccl.type.Operator;
import com.cinchapi.concourse.util.Convert;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;

/**
 * Coverage for subqueries as the source of a quantified value set: the commands
 * that may supply the set, the {@link SubquerySymbol} that carries the parsed
 * command, the isolation of the inner condition from the outer one, round
 * trips, local evaluation, and the statements the grammar rejects.
 *
 * @author Jeff Nelson
 */
public class SubqueryTest {

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
     * Parse {@code ccl}, which must be a single quantified comparison whose
     * value set is a subquery, and return that subquery.
     *
     * @param ccl the comparison to parse
     * @return the {@link SubquerySymbol}
     */
    private static SubquerySymbol subquery(String ccl) {
        ExpressionSymbol expression = (ExpressionSymbol) parse(ccl).root();
        Assert.assertEquals(1, expression.values().size());
        return (SubquerySymbol) expression.values().get(0);
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
     * <strong>Goal:</strong> Verify that a {@code select} subquery inside
     * {@code any[...]} parses to a {@link SubquerySymbol} that carries the
     * parsed {@link CommandTree} and renders back to the subquery's CCL.
     * <p>
     * <strong>Start state:</strong> No prior state needed.
     * <p>
     * <strong>Workflow:</strong>
     * <ul>
     * <li>Parse {@code name = any[select name where group = "vip"]}.</li>
     * <li>Read the outer quantifier and the subquery's command tree.</li>
     * </ul>
     * <p>
     * <strong>Expected:</strong> The outer quantifier is
     * {@link QuantifierSymbol#ANY}. The subquery's root is a
     * {@link SelectSymbol} for the key {@code name}, its condition is the
     * expression {@code group = "vip"}, and {@code toString()} is
     * {@code select name where group = "vip"}.
     */
    @Test
    public void testSelectSubqueryCarriesCommandTree() {
        ConditionTree tree = parse(
                "name = any[select name where group = \"vip\"]");
        ExpressionSymbol expression = (ExpressionSymbol) tree.root();
        Assert.assertEquals(QuantifierSymbol.ANY, expression.quantifier());
        SubquerySymbol subquery = (SubquerySymbol) expression.values().get(0);
        CommandTree command = subquery.value();
        SelectSymbol select = (SelectSymbol) command.root();
        Assert.assertEquals(ImmutableList.of(new KeySymbol("name")),
                ImmutableList.copyOf(select.keys()));
        Assert.assertEquals(parse("group = \"vip\"").root(),
                command.conditionTree().root());
        Assert.assertEquals("select name where group = \"vip\"",
                subquery.toString());
    }

    /**
     * <strong>Goal:</strong> Verify that {@code get}, {@code find},
     * {@code trace} and {@code navigate} may each supply the value set, with
     * their own order, page and timestamp clauses.
     * <p>
     * <strong>Start state:</strong> No prior state needed.
     * <p>
     * <strong>Workflow:</strong>
     * <ul>
     * <li>Parse {@code name in [get name where age > 30 as of 1700000000 order
     * by name limit 5]}.</li>
     * <li>Parse {@code manager lnks2 every[find group = "vip"]}.</li>
     * <li>Parse {@code reports_to lnks2 any[trace manager from 42]}.</li>
     * <li>Parse {@code friend lnks2 any[navigate team.members from 7]}.</li>
     * </ul>
     * <p>
     * <strong>Expected:</strong> The {@code get} subquery has a
     * {@link GetSymbol} root with the timestamp {@code 1700000000}, an order
     * tree and a page tree. The {@code find} subquery has a {@link FindSymbol}
     * root and the condition {@code group = "vip"}. The {@code trace} subquery
     * has a {@link TraceSymbol} for the key {@code manager} and record 42. The
     * {@code navigate} subquery has a {@link NavigateSymbol} for the navigation
     * key {@code team.members} from record 7.
     */
    @Test
    public void testEachReadCommandMaySupplyTheValueSet() {
        CommandTree get = subquery("name in [get name where age > 30 "
                + "as of 1700000000 order by name limit 5]").value();
        Assert.assertEquals(1700000000L,
                ((GetSymbol) get.root()).timestamp().timestamp());
        Assert.assertNotNull(get.orderTree());
        Assert.assertNotNull(get.pageTree());
        Assert.assertEquals(parse("age > 30").root(),
                get.conditionTree().root());
        CommandTree find = subquery("manager lnks2 every[find group = \"vip\"]")
                .value();
        Assert.assertTrue(find.root() instanceof FindSymbol);
        Assert.assertEquals(parse("group = \"vip\"").root(),
                find.conditionTree().root());
        TraceSymbol trace = (TraceSymbol) subquery(
                "reports_to lnks2 any[trace manager from 42]").value().root();
        Assert.assertEquals(new KeySymbol("manager"), trace.key());
        Assert.assertEquals(Long.valueOf(42), trace.record());
        NavigateSymbol navigate = (NavigateSymbol) subquery(
                "friend lnks2 any[navigate team.members from 7]").value()
                        .root();
        Assert.assertEquals("team.members",
                navigate.keys().iterator().next().toString());
        Assert.assertEquals(Long.valueOf(7), navigate.record());
    }

    /**
     * <strong>Goal:</strong> Verify that the subquery's condition belongs to
     * the subquery alone and does not appear as a child of the condition that
     * encloses the quantified comparison, even when that inner condition is
     * itself quantified.
     * <p>
     * <strong>Start state:</strong> No prior state needed.
     * <p>
     * <strong>Workflow:</strong>
     * <ul>
     * <li>Parse {@code name = any[select name where tags = every["a", "b"]]
     * and age > 5}.</li>
     * </ul>
     * <p>
     * <strong>Expected:</strong> The outer tree is a conjunction with exactly
     * two leaves. The left leaf is the quantified comparison and its subquery's
     * condition is the expression {@code tags = every["a", "b"]}. The right
     * leaf is {@code age > 5}.
     */
    @Test
    public void testSubqueryConditionIsNotAChildOfTheOuterCondition() {
        ConditionTree tree = parse("name = any[select name where "
                + "tags = every[\"a\", \"b\"]] and age > 5");
        List<AbstractSyntaxTree> children = ImmutableList
                .copyOf(tree.children());
        Assert.assertEquals(2, children.size());
        ExpressionSymbol left = (ExpressionSymbol) children.get(0).root();
        SubquerySymbol subquery = (SubquerySymbol) left.values().get(0);
        Assert.assertEquals(parse("tags = every[\"a\", \"b\"]").root(),
                subquery.value().conditionTree().root());
        Assert.assertEquals(parse("age > 5").root(), children.get(1).root());
    }

    /**
     * <strong>Goal:</strong> Verify that a subquery is accepted only as the
     * whole value set of a quantified comparison.
     * <p>
     * <strong>Start state:</strong> No prior state needed.
     * <p>
     * <strong>Workflow:</strong>
     * <ul>
     * <li>Parse {@code name = select name where x = 1} (no quantifier).</li>
     * <li>Parse {@code name = any[select name where x = 1, "a"]} (mixed with
     * a literal).</li>
     * <li>Parse {@code age bw any[select age where x = 1]} (a tuple
     * operator).</li>
     * <li>Parse {@code name = any[add name as 1 in 2]} (a write
     * command).</li>
     * </ul>
     * <p>
     * <strong>Expected:</strong> Each statement fails with a
     * {@link SyntaxException}.
     */
    @Test
    public void testSubqueryOutsideAQuantifiedSetIsRejected() {
        assertRejected("name = select name where x = 1");
        assertRejected("name = any[select name where x = 1, \"a\"]");
        assertRejected("age bw any[select age where x = 1]");
        assertRejected("name = any[add name as 1 in 2]");
    }

    /**
     * <strong>Goal:</strong> Verify that a quantified comparison with a
     * subquery re-parses to an equal tree from its token stream and from its
     * {@code toString()}, with the timestamp after the closing bracket kept on
     * the outer comparison.
     * <p>
     * <strong>Start state:</strong> No prior state needed.
     * <p>
     * <strong>Workflow:</strong>
     * <ul>
     * <li>Round-trip a {@code select} subquery, a {@code trace} subquery, an
     * {@code in} form with a {@code find} subquery, and a subquery followed by
     * {@code at 1700000000}.</li>
     * <li>Parse two spellings of one subquery that differ only in
     * whitespace.</li>
     * </ul>
     * <p>
     * <strong>Expected:</strong> Each round trip yields an equal tree. The
     * timed comparison has the timestamp {@code 1700000000} and its subquery
     * has none. The two spellings parse to equal trees.
     */
    @Test
    public void testSubqueryFormsRoundTrip() {
        assertRoundTripStable(
                "name = any[select name where group = \"vip\"]");
        assertRoundTripStable("reports_to lnks2 every[trace manager from 42]");
        assertRoundTripStable("id in [find group = \"vip\" order by name]");
        assertRoundTripStable(
                "name = any[select name where g = 1] at 1700000000");
        ExpressionSymbol timed = (ExpressionSymbol) parse(
                "name = any[select name where g = 1] at 1700000000").root();
        Assert.assertEquals(1700000000L, timed.timestamp().timestamp());
        Assert.assertNull(((SelectSymbol) ((SubquerySymbol) timed.values()
                .get(0)).value().root()).timestamp());
        Assert.assertEquals(parse("name = any[select name where g = 1]"),
                parse("name = any[ select   name where g=1 ]"));
    }

    /**
     * <strong>Goal:</strong> Verify that a subquery whose condition holds a
     * {@code $name} placeholder renders the bound value, so its CCL re-parses
     * without the bindings and subqueries with different bindings differ.
     * <p>
     * <strong>Start state:</strong> No prior state needed.
     * <p>
     * <strong>Workflow:</strong>
     * <ul>
     * <li>Parse {@code name = any[select name where group = $g]} with
     * {@code g} bound to {@code very important}, and again with {@code g} bound
     * to {@code staff}.</li>
     * <li>Parse the first tree's {@code toString()} with no bindings.</li>
     * </ul>
     * <p>
     * <strong>Expected:</strong> The first tree equals the tree for
     * {@code name = any[select name where group = "very important"]}, and its
     * {@code toString()} re-parses to an equal tree. The two bound trees are
     * not equal.
     */
    @Test
    public void testToStringRendersTheBoundValueOfASubqueryPlaceholder() {
        String ccl = "name = any[select name where group = $g]";
        ConditionTree important = (ConditionTree) compiler().parse(ccl,
                ImmutableMultimap.of("g", "very important"));
        ConditionTree staff = (ConditionTree) compiler().parse(ccl,
                ImmutableMultimap.of("g", "staff"));
        Assert.assertEquals(parse(
                "name = any[select name where group = \"very important\"]"),
                important);
        Assert.assertEquals(important, parse(important.root().toString()));
        Assert.assertNotEquals(important, staff);
    }

    /**
     * <strong>Goal:</strong> Verify that local evaluation refuses a condition
     * whose value set is a subquery, because only the engine can resolve it.
     * <p>
     * <strong>Start state:</strong> No prior state needed.
     * <p>
     * <strong>Workflow:</strong>
     * <ul>
     * <li>Evaluate {@code name = any[select name where g = 1]} against
     * {@code name = Jeff}.</li>
     * </ul>
     * <p>
     * <strong>Expected:</strong> An {@link UnsupportedOperationException} is
     * thrown.
     */
    @Test(expected = UnsupportedOperationException.class)
    public void testLocalEvaluationRejectsSubquery() {
        compiler().evaluate(parse("name = any[select name where g = 1]"),
                ImmutableMultimap.of("name", "Jeff"),
                (value, operator, values) -> true);
    }

    /**
     * <strong>Goal:</strong> Verify that the analysis of a condition stops at a
     * subquery, whose keys belong to the analysis of the subquery's own
     * {@link CommandTree}.
     * <p>
     * <strong>Start state:</strong> No prior state needed.
     * <p>
     * <strong>Workflow:</strong>
     * <ul>
     * <li>Analyze
     * {@code name = any[select email where group[1700000000] > 1]}.</li>
     * <li>Analyze the subquery's {@link CommandTree}.</li>
     * </ul>
     * <p>
     * <strong>Expected:</strong> The condition reports only the key
     * {@code name}, only the operator {@code =} and no temporal keys. The
     * subquery reports the keys {@code email} and {@code group}, the operator
     * {@code >} and the temporal key {@code group}.
     */
    @Test
    public void testAnalyzeExcludesTheKeysOfASubquery() {
        String ccl = "name = any[select email where group[1700000000] > 1]";
        StatementAnalysis outer = compiler().analyze(parse(ccl));
        Assert.assertEquals(ImmutableSet.of("name"), outer.keys());
        Assert.assertEquals(ImmutableSet.of(OP_FN.apply("=")),
                outer.operators());
        Assert.assertEquals(ImmutableSet.of(), outer.temporalKeys().keySet());
        CommandAnalysis inner = compiler().analyze(subquery(ccl).value());
        Assert.assertEquals(ImmutableSet.of("email", "group"), inner.keys());
        Assert.assertEquals(ImmutableSet.of(OP_FN.apply(">")),
                inner.operators());
        Assert.assertEquals(ImmutableSet.of("group"),
                inner.temporalKeys().keySet());
    }

}
