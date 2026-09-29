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

import java.util.Collection;
import java.util.Objects;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import com.cinchapi.ccl.grammar.KeyTokenSymbol;
import com.google.common.collect.ImmutableList;

/**
 * A {@link CommandSymbol} for an AWAIT GET command, which waits up to its
 * timeout for its read, with its order and page, to have a non-empty result,
 * and then gets keys from the matching records.
 * <p>
 * The condition, order and page are children of the
 * {@link com.cinchapi.ccl.syntax.CommandTree CommandTree} whose root is this
 * symbol, the same as for a {@link GetSymbol}.
 * </p>
 *
 * @author Jeff Nelson
 */
@Immutable
public final class AwaitGetSymbol implements CommandSymbol {

    /**
     * The longest time, in milliseconds, that the command may wait.
     */
    private final long timeout;

    /**
     * The keys to read, or {@code null} to read every key.
     */
    @Nullable
    private final Collection<KeyTokenSymbol<?>> keys;

    /**
     * Construct a new instance.
     *
     * @param timeout the longest time, in milliseconds, that the command may
     *            wait
     * @param keys the keys to read, or {@code null} to read every key
     */
    public AwaitGetSymbol(long timeout,
            @Nullable Collection<KeyTokenSymbol<?>> keys) {
        this.timeout = timeout;
        this.keys = keys != null ? ImmutableList.copyOf(keys) : null;
    }

    @Override
    public String type() {
        return "AWAIT_GET";
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
     * Return the keys to read.
     *
     * @return an unmodifiable {@link Collection} of the keys in the order the
     *         command lists them, or {@code null} to read every key
     */
    @Nullable
    public Collection<KeyTokenSymbol<?>> keys() {
        return keys;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof AwaitGetSymbol) {
            AwaitGetSymbol other = (AwaitGetSymbol) obj;
            return timeout == other.timeout
                    && Objects.equals(keys, other.keys);
        }
        else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(timeout, keys);
    }

}
