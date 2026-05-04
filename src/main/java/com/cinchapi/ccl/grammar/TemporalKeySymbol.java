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
package com.cinchapi.ccl.grammar;

import java.util.Objects;

import com.google.common.base.Preconditions;

/**
 * A {@link TemporalKeySymbol} pairs a {@link KeyTokenSymbol} with a
 * {@link TimestampSymbol} that pins the read which the wrapped key
 * represents. The pinned read is leaf evaluation when the wrapped key is
 * a leaf, traversal when the wrapped key is a navigation stop or scope
 * prefix.
 *
 * @author Jeff Nelson
 */
public final class TemporalKeySymbol
        extends KeyTokenSymbol<KeyTokenSymbol<?>> {

    /**
     * The {@link TimestampSymbol} pinned to the wrapped {@link #key()}.
     */
    private final TimestampSymbol timestamp;

    /**
     * Construct a new {@link TemporalKeySymbol}.
     *
     * @param key the wrapped {@link KeyTokenSymbol}
     * @param timestamp the {@link TimestampSymbol} pinned to {@code key}
     */
    public TemporalKeySymbol(KeyTokenSymbol<?> key,
            TimestampSymbol timestamp) {
        super(Preconditions.checkNotNull(key));
        this.timestamp = Preconditions.checkNotNull(timestamp);
    }

    /**
     * Return the {@link TimestampSymbol} pinned to the wrapped
     * {@link #key()}.
     *
     * @return the {@link TimestampSymbol}
     */
    public TimestampSymbol timestamp() {
        return timestamp;
    }

    @Override
    public boolean equals(Object obj) {
        if(this == obj) {
            return true;
        }
        else if(!(obj instanceof TemporalKeySymbol)) {
            return false;
        }
        TemporalKeySymbol other = (TemporalKeySymbol) obj;
        return key.equals(other.key) && timestamp.equals(other.timestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, timestamp);
    }

    @Override
    public String toString() {
        return key.toString() + "[" + timestamp.timestamp() + "]";
    }

    @Override
    public String bareKey() {
        return key.bareKey();
    }

    @Override
    public boolean isTemporal() {
        return true;
    }

    @Override
    public KeyTokenSymbol<?> untemporal() {
        return key.untemporal();
    }

}
