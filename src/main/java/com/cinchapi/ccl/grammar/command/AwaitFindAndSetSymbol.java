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
package com.cinchapi.ccl.grammar.command;

import java.util.Objects;

import javax.annotation.concurrent.Immutable;

import com.cinchapi.ccl.grammar.KeyTokenSymbol;
import com.cinchapi.ccl.grammar.ValueTokenSymbol;

/**
 * A {@link CommandSymbol} for an AWAIT FIND AND SET command, which waits up to
 * its timeout for its read, with its order and page, to have a non-empty
 * result, and then does what a {@link FindAndSetSymbol FIND AND SET} command
 * does.
 * <p>
 * The condition, order and page are children of the
 * {@link com.cinchapi.ccl.syntax.CommandTree CommandTree} whose root is this
 * symbol, the same as for a {@link FindSymbol}.
 * </p>
 *
 * @author Jeff Nelson
 */
@Immutable
public final class AwaitFindAndSetSymbol implements CommandSymbol {

    /**
     * The longest time, in milliseconds, that the command may wait.
     */
    private final long timeout;

    /**
     * The key that the set clause writes.
     */
    private final KeyTokenSymbol<?> key;

    /**
     * The value that the set clause writes.
     */
    private final ValueTokenSymbol<?> value;

    /**
     * Construct a new instance.
     *
     * @param timeout the longest time, in milliseconds, that the command may
     *            wait
     * @param key the key that the set clause writes
     * @param value the value that the set clause writes
     */
    public AwaitFindAndSetSymbol(long timeout, KeyTokenSymbol<?> key,
            ValueTokenSymbol<?> value) {
        this.timeout = timeout;
        this.key = key;
        this.value = value;
    }

    @Override
    public String type() {
        return "AWAIT_FIND_AND_SET";
    }

    /**
     * Return the longest time that the command may wait for its read to have a
     * non-empty result.
     *
     * @return the timeout in milliseconds
     */
    public long timeout() {
        return timeout;
    }

    /**
     * Return the key that the set clause writes.
     *
     * @return the key
     */
    public KeyTokenSymbol<?> key() {
        return key;
    }

    /**
     * Return the value that the set clause writes.
     *
     * @return the value
     */
    public ValueTokenSymbol<?> value() {
        return value;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof AwaitFindAndSetSymbol) {
            AwaitFindAndSetSymbol other = (AwaitFindAndSetSymbol) obj;
            return timeout == other.timeout && key.equals(other.key)
                    && value.equals(other.value);
        }
        else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(timeout, key, value);
    }

}
