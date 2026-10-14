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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;

import com.cinchapi.ccl.grammar.ExpressionSymbol;
import com.cinchapi.ccl.grammar.NullValueSymbol;
import com.cinchapi.ccl.grammar.QuantifierSymbol;
import com.cinchapi.ccl.grammar.Symbol;
import com.cinchapi.ccl.grammar.ValueSymbol;
import com.cinchapi.ccl.syntax.AbstractSyntaxTree;
import com.cinchapi.ccl.syntax.ConditionTree;
import com.cinchapi.ccl.type.Operator;
import com.cinchapi.concourse.util.Convert;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultimap;

/**
 * Coverage for the {@code null} literal: the unquoted token that stands for the
 * absence of a value, the operators that accept it, the quoted string that
 * keeps its literal meaning, and the round trips of each.
 *
 * @author Jeff Nelson
 */
public class NullLiteralTest {

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
     * Parse {@code ccl}, which must be a single comparison, and return its
     * {@link ExpressionSymbol}.
     *
     * @param ccl the comparison to parse
     * @return the {@link ExpressionSymbol}
     */
    private static ExpressionSymbol expression(String ccl) {
        return (ExpressionSymbol) ((ConditionTree) compiler().parse(ccl))
                .root();
    }

    /**
     * Assert that parsing {@code ccl} fails with a {@link SyntaxException}
     * whose message mentions {@code null}.
     *
     * @param ccl the statement to parse
     */
    private static void assertRejected(String ccl) {
        try {
            compiler().parse(ccl);
            Assert.fail("expected a SyntaxException for " + ccl);
        }
        catch (SyntaxException e) {
            Assert.assertTrue(e.getMessage(),
                    e.getMessage().contains("null"));
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
     * <strong>Goal:</strong> Verify that an unquoted {@code null} with
     * {@code =} or {@code !=} parses to the {@link NullValueSymbol}, whose
     * value is Java {@code null}, in any letter case.
     * <p>
     * <strong>Start state:</strong> No prior state needed.
     * <p>
     * <strong>Workflow:</strong>
     * <ul>
     * <li>Parse {@code locked = null}, {@code email != null} and
     * {@code locked eq NULL}.</li>
     * </ul>
     * <p>
     * <strong>Expected:</strong> Each expression's single value is
     * {@link NullValueSymbol#INSTANCE}, its raw values are a list holding one
     * {@code null}, and the symbol renders as {@code null}.
     */
    @Test
    public void testUnquotedNullParsesToNullValueSymbol() {
        for (String ccl : ImmutableList.of("locked = null", "email != null",
                "locked eq NULL")) {
            ExpressionSymbol expression = expression(ccl);
            Assert.assertEquals(ccl, ImmutableList.of(NullValueSymbol.INSTANCE),
                    expression.values());
            Assert.assertEquals(ccl, Arrays.asList((Object) null),
                    expression.raw().values());
            Assert.assertNull(ccl, expression.values().get(0).value());
        }
        Assert.assertEquals("null", NullValueSymbol.INSTANCE.toString());
    }

    /**
     * <strong>Goal:</strong> Verify that the quoted string {@code "null"} keeps
     * its literal meaning and renders with its quotes, so that it is never read
     * back as the {@code null} literal.
     * <p>
     * <strong>Start state:</strong> No prior state needed.
     * <p>
     * <strong>Workflow:</strong>
     * <ul>
     * <li>Parse {@code name = "null"} and {@code name = 'NULL'}.</li>
     * <li>Parse {@code note = null pointer}.</li>
     * </ul>
     * <p>
     * <strong>Expected:</strong> The quoted forms have a {@link ValueSymbol}
     * whose value is the string, and the symbol renders as the quoted string.
     * The multi-word value is the string {@code null pointer}.
     */
    @Test
    public void testQuotedNullStaysAString() {
        ExpressionSymbol quoted = expression("name = \"null\"");
        Assert.assertEquals(ImmutableList.of(new ValueSymbol("null")),
                quoted.values());
        Assert.assertEquals("\"null\"", quoted.values().get(0).toString());
        Assert.assertEquals("NULL",
                expression("name = 'NULL'").values().get(0).value());
        Assert.assertEquals("null pointer",
                expression("note = null pointer").values().get(0).value());
    }

    /**
     * <strong>Goal:</strong> Verify that {@code null} may be a member of a
     * quantified value set with {@code =} or {@code !=}.
     * <p>
     * <strong>Start state:</strong> No prior state needed.
     * <p>
     * <strong>Workflow:</strong>
     * <ul>
     * <li>Parse {@code name = any(null, "Jeff")} and
     * {@code name not_in [null]}.</li>
     * </ul>
     * <p>
     * <strong>Expected:</strong> The first has quantifier
     * {@link QuantifierSymbol#ANY} and the values {@code null} then
     * {@code Jeff}. The second has quantifier {@link QuantifierSymbol#EVERY}
     * and the single value {@code null}.
     */
    @Test
    public void testNullMayBeAMemberOfAQuantifiedSet() {
        ExpressionSymbol any = expression("name = any(null, \"Jeff\")");
        Assert.assertEquals(QuantifierSymbol.ANY, any.quantifier());
        Assert.assertEquals(ImmutableList.of(NullValueSymbol.INSTANCE,
                new ValueSymbol("Jeff")), any.values());
        ExpressionSymbol notIn = expression("name not_in [null]");
        Assert.assertEquals(QuantifierSymbol.EVERY, notIn.quantifier());
        Assert.assertEquals(ImmutableList.of(NullValueSymbol.INSTANCE),
                notIn.values());
    }

    /**
     * <strong>Goal:</strong> Verify that {@code null} is rejected with every
     * operator other than {@code =} and {@code !=}, alone and inside a
     * quantified set.
     * <p>
     * <strong>Start state:</strong> No prior state needed.
     * <p>
     * <strong>Workflow:</strong>
     * <ul>
     * <li>Parse {@code age > null}, {@code age >= null}, {@code age < null},
     * {@code age lte null}, {@code age bw null 5}, {@code age bw 1 null},
     * {@code name like null}, {@code name regex null},
     * {@code name contains null} and {@code age > any(null, 5)}.</li>
     * </ul>
     * <p>
     * <strong>Expected:</strong> Each statement fails with a
     * {@link SyntaxException} whose message mentions {@code null}.
     */
    @Test
    public void testNullIsRejectedWithNonEqualityOperators() {
        for (String ccl : ImmutableList.of("age > null", "age >= null",
                "age < null", "age lte null", "age bw null 5", "age bw 1 null",
                "name like null", "name regex null", "name contains null",
                "age > any(null, 5)")) {
            assertRejected(ccl);
        }
    }

    /**
     * <strong>Goal:</strong> Verify that local evaluation passes the
     * {@code null} literal to the evaluator as a {@code null} operand, and a
     * key with no stored value as a {@code null} stored value, so that the
     * evaluator decides what absence means.
     * <p>
     * <strong>Start state:</strong> No prior state needed.
     * <p>
     * <strong>Workflow:</strong>
     * <ul>
     * <li>Evaluate {@code locked = null} against {@code other = 1} with an
     * evaluator that records its arguments and returns {@code true}.</li>
     * </ul>
     * <p>
     * <strong>Expected:</strong> The condition holds, and the evaluator was
     * called once with a {@code null} stored value, the {@code EQUALS} operator
     * and a single {@code null} operand.
     */
    @Test
    public void testLocalEvaluationPassesNullToTheEvaluator() {
        List<Object> seen = new ArrayList<>();
        boolean held = compiler().evaluate(
                (ConditionTree) compiler().parse("locked = null"),
                ImmutableMultimap.of("other", 1),
                (value, operator, values) -> {
                    seen.add(value);
                    seen.add(operator);
                    seen.add(values);
                    return true;
                });
        Assert.assertTrue(held);
        Assert.assertEquals(Arrays.asList(null,
                com.cinchapi.concourse.thrift.Operator.EQUALS,
                Arrays.asList((Object) null)), seen);
    }

    /**
     * <strong>Goal:</strong> Verify that the {@code null} literal and the
     * quoted string {@code "null"} each re-parse to an equal tree from their
     * token stream and from their {@code toString()}.
     * <p>
     * <strong>Start state:</strong> No prior state needed.
     * <p>
     * <strong>Workflow:</strong>
     * <ul>
     * <li>Round-trip {@code locked = null},
     * {@code email != null at 1700000000}, {@code name = any(null, "Jeff")}
     * and {@code name = "null"}.</li>
     * <li>Parse {@code locked = null} and {@code locked = "null"}.</li>
     * </ul>
     * <p>
     * <strong>Expected:</strong> Each round trip yields an equal tree, and the
     * literal and the string parse to different trees.
     */
    @Test
    public void testNullFormsRoundTrip() {
        assertRoundTripStable("locked = null");
        assertRoundTripStable("email != null at 1700000000");
        assertRoundTripStable("name = any(null, \"Jeff\")");
        assertRoundTripStable("name = \"null\"");
        Assert.assertNotEquals(compiler().parse("locked = null"),
                compiler().parse("locked = \"null\""));
    }

}
