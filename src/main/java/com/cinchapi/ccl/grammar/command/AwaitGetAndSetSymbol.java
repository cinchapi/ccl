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

import java.time.Duration;
import java.util.Collection;
import java.util.Objects;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import com.cinchapi.ccl.grammar.KeyTokenSymbol;
import com.cinchapi.ccl.grammar.ValueTokenSymbol;
import com.google.common.collect.ImmutableList;

/**
 * A {@link CommandSymbol} for an AWAIT GET AND SET command. The command's read
 * gets keys from the records that match its condition and applies its optional
 * order and page. The command waits up to its timeout for the read to have a
 * non-empty result, and then does what a {@link GetAndSetSymbol GET AND SET}
 * command does.
 * <p>
 * The condition, order and page are children of the
 * {@link com.cinchapi.ccl.syntax.CommandTree CommandTree} whose root is this
 * symbol, the same as for a {@link GetSymbol}.
 * </p>
 *
 * @author Jeff Nelson
 */
@Immutable
public final class AwaitGetAndSetSymbol implements CommandSymbol {

    /**
     * The longest time that the command may wait.
     */
    private final Duration timeout;

    /**
     * The keys to read, or {@code null} to read every key.
     */
    @Nullable
    private final Collection<KeyTokenSymbol<?>> keys;

    /**
     * Whether the command names {@link #keys} as a collection.
     */
    private final boolean hasKeysInCollection;

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
     * @param timeout the longest time that the command may wait
     * @param keys the keys to read, or {@code null} to read every key
     * @param hasKeysInCollection whether the command names {@code keys} as a
     *        collection, as {@link #hasKeysInCollection()} defines it
     * @param key the key that the set clause writes
     * @param value the value that the set clause writes
     */
    public AwaitGetAndSetSymbol(Duration timeout,
            @Nullable Collection<KeyTokenSymbol<?>> keys,
            boolean hasKeysInCollection, KeyTokenSymbol<?> key,
            ValueTokenSymbol<?> value) {
        this.timeout = timeout;
        this.keys = keys != null ? ImmutableList.copyOf(keys) : null;
        this.hasKeysInCollection = hasKeysInCollection;
        this.key = key;
        this.value = value;
    }

    /**
     * Construct a new instance. {@link #hasKeysInCollection()} is {@code true}
     * when {@code keys} holds more than one key.
     *
     * @param timeout the longest time that the command may wait
     * @param keys the keys to read, or {@code null} to read every key
     * @param key the key that the set clause writes
     * @param value the value that the set clause writes
     */
    public AwaitGetAndSetSymbol(Duration timeout,
            @Nullable Collection<KeyTokenSymbol<?>> keys, KeyTokenSymbol<?> key,
            ValueTokenSymbol<?> value) {
        this(timeout, keys, CommandSymbols.holdsMoreThanOne(keys), key, value);
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof AwaitGetAndSetSymbol) {
            AwaitGetAndSetSymbol other = (AwaitGetAndSetSymbol) obj;
            return timeout.equals(other.timeout)
                    && Objects.equals(keys, other.keys) && key.equals(other.key)
                    && value.equals(other.value);
        }
        else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(timeout, keys, key, value);
    }

    /**
     * Return whether the command names the {@link #keys() keys to read} as a
     * collection. A command names a collection when it lists the keys in
     * brackets, even one key as in {@code [name]}, or lists more than one key,
     * as in {@code name, age}. A command that names one key without brackets,
     * as in {@code name}, or names no keys, does not. {@link #equals(Object)}
     * and {@link #hashCode()} do not depend on this form, so a command that
     * names {@code [name]} and one that names {@code name} parse to equal
     * symbols.
     *
     * @return {@code true} if the command names the keys to read as a
     *         collection
     */
    public boolean hasKeysInCollection() {
        return hasKeysInCollection;
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
     * Return the keys to read.
     *
     * @return an unmodifiable {@link Collection} of the keys in the order the
     *         command lists them, or {@code null} to read every key
     */
    @Nullable
    public Collection<KeyTokenSymbol<?>> keys() {
        return keys;
    }

    /**
     * Return the longest time that the command may wait for its read to have a
     * non-empty result.
     *
     * @return the timeout
     */
    public Duration timeout() {
        return timeout;
    }

    @Override
    public String type() {
        return "AWAIT_GET_AND_SET";
    }

    /**
     * Return the value that the set clause writes.
     *
     * @return the value
     */
    public ValueTokenSymbol<?> value() {
        return value;
    }

}
