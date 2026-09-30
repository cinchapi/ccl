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
 * A {@link CommandSymbol} for a FIND AND SET command. The command's read finds
 * the records that match its condition and applies its optional order and page.
 * The command sets its key as its value in each record that the read returns.
 * The read and the writes are one atomic operation.
 * <p>
 * The condition, order and page are children of the
 * {@link com.cinchapi.ccl.syntax.CommandTree CommandTree} whose root is this
 * symbol, the same as for a {@link FindSymbol}.
 * </p>
 *
 * @author Jeff Nelson
 */
@Immutable
public final class FindAndSetSymbol implements CommandSymbol {

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
     * @param key the key that the set clause writes
     * @param value the value that the set clause writes
     */
    public FindAndSetSymbol(KeyTokenSymbol<?> key, ValueTokenSymbol<?> value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public String type() {
        return "FIND_AND_SET";
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
        if(obj instanceof FindAndSetSymbol) {
            FindAndSetSymbol other = (FindAndSetSymbol) obj;
            return key.equals(other.key) && value.equals(other.value);
        }
        else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, value);
    }

}
