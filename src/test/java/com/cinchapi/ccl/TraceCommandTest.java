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

import java.util.function.Function;

import org.junit.Assert;
import org.junit.Test;

import com.cinchapi.ccl.grammar.KeySymbol;
import com.cinchapi.ccl.grammar.command.TraceSymbol;
import com.cinchapi.ccl.syntax.CommandTree;
import com.cinchapi.ccl.type.Operator;
import com.cinchapi.concourse.util.Convert;
import com.google.common.collect.ImmutableList;

/**
 * Coverage for the key-qualified {@code trace} command, which names the key
 * through which the incoming links are traced.
 *
 * @author Jeff Nelson
 */
public class TraceCommandTest {

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
     * Parse {@code ccl} as a {@code trace} command and return its symbol.
     *
     * @param ccl the statement to parse
     * @return the {@link TraceSymbol}
     */
    private static TraceSymbol trace(String ccl) {
        return (TraceSymbol) ((CommandTree) Compiler.create(VALUE_FN, OP_FN)
                .parse(ccl)).root();
    }

    /**
     * Assert that parsing {@code ccl} fails with a {@link SyntaxException}.
     *
     * @param ccl the statement to parse
     */
    private static void assertRejected(String ccl) {
        try {
            Compiler.create(VALUE_FN, OP_FN).parse(ccl);
            Assert.fail("expected a SyntaxException for " + ccl);
        }
        catch (SyntaxException e) {
            // expected
        }
    }

    /**
     * <strong>Goal:</strong> Verify that {@code trace <key> from <record>}
     * parses to a {@link TraceSymbol} that carries the key, and that each
     * preposition and the collection and timestamp forms are accepted.
     * <p>
     * <strong>Start state:</strong> No prior state needed.
     * <p>
     * <strong>Workflow:</strong>
     * <ul>
     * <li>Parse {@code trace manager from 42}.</li>
     * <li>Parse {@code trace manager in [1, 2]} and
     * {@code trace manager within 1, 2}.</li>
     * <li>Parse {@code trace manager from 42 at 1700000000}.</li>
     * </ul>
     * <p>
     * <strong>Expected:</strong> The first has the key {@code manager}, record
     * 42, no records and no timestamp. The collection forms have the key and
     * the records 1 and 2. The timed form has the timestamp {@code 1700000000}.
     */
    @Test
    public void testTraceAcceptsAKey() {
        TraceSymbol single = trace("trace manager from 42");
        Assert.assertEquals(new KeySymbol("manager"), single.key());
        Assert.assertEquals(Long.valueOf(42), single.record());
        Assert.assertNull(single.records());
        Assert.assertNull(single.timestamp());
        for (String ccl : ImmutableList.of("trace manager in [1, 2]",
                "trace manager within 1, 2")) {
            TraceSymbol many = trace(ccl);
            Assert.assertEquals(ccl, new KeySymbol("manager"), many.key());
            Assert.assertEquals(ccl, ImmutableList.of(1L, 2L),
                    ImmutableList.copyOf(many.records()));
            Assert.assertNull(ccl, many.record());
        }
        Assert.assertEquals(1700000000L,
                trace("trace manager from 42 at 1700000000").timestamp()
                        .timestamp());
    }

    /**
     * <strong>Goal:</strong> Verify that the record-only {@code trace} forms
     * keep their meaning and report no key.
     * <p>
     * <strong>Start state:</strong> No prior state needed.
     * <p>
     * <strong>Workflow:</strong>
     * <ul>
     * <li>Parse {@code trace 42} and {@code trace [1, 2] at 1700000000}.</li>
     * </ul>
     * <p>
     * <strong>Expected:</strong> Each has a {@code null} key. The first has
     * record 42; the second has records 1 and 2 and the timestamp
     * {@code 1700000000}.
     */
    @Test
    public void testTraceWithoutAKeyIsUnchanged() {
        TraceSymbol single = trace("trace 42");
        Assert.assertNull(single.key());
        Assert.assertEquals(Long.valueOf(42), single.record());
        TraceSymbol many = trace("trace [1, 2] at 1700000000");
        Assert.assertNull(many.key());
        Assert.assertEquals(ImmutableList.of(1L, 2L),
                ImmutableList.copyOf(many.records()));
        Assert.assertEquals(1700000000L, many.timestamp().timestamp());
    }

    /**
     * <strong>Goal:</strong> Verify that a key without records, a preposition
     * without a key, and a key with a bracket timestamp are rejected, as they
     * are for the other single-key commands.
     * <p>
     * <strong>Start state:</strong> No prior state needed.
     * <p>
     * <strong>Workflow:</strong>
     * <ul>
     * <li>Parse {@code trace manager}, {@code trace from 42} and
     * {@code trace manager[1700000000] from 42}.</li>
     * </ul>
     * <p>
     * <strong>Expected:</strong> Each statement fails with a
     * {@link SyntaxException}.
     */
    @Test
    public void testIncompleteOrParameterizedKeyedTraceIsRejected() {
        assertRejected("trace manager");
        assertRejected("trace from 42");
        assertRejected("trace manager[1700000000] from 42");
    }

}
