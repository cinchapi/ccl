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

import javax.annotation.Nullable;

import com.google.common.base.Preconditions;

/**
 * A {@link TemporalRangeKeySymbol} pairs a {@link KeyTokenSymbol} with a
 * half-open temporal interval {@code [start, end)} rather than the single
 * instant a {@link TemporalKeySymbol} pins. The interval is
 * start-inclusive and end-exclusive, matching the {@code BETWEEN}
 * convention used elsewhere for time windows.
 *
 * <p>Either endpoint may be absent to express an open-ended interval:
 * an absent {@code start} is the open-start form ({@code key[...end]}),
 * and an absent {@code end} is the open-end form ({@code key[start...]}).
 * At least one endpoint is always present.
 *
 * <p>This symbol records the endpoints only; it does not decide what a
 * range read returns. That evaluation lives server-side and is out of
 * scope for the grammar layer.
 *
 * <p>As with {@link TemporalKeySymbol}, a {@link NavigationKeySymbol} is
 * never wrapped and an already-parameterized key is rejected, enforcing
 * the "at most one bracket-timestamp parameter per key" invariant the
 * rest of the AST relies on.
 *
 * @author Jeff Nelson
 */
public final class TemporalRangeKeySymbol
        extends KeyTokenSymbol<KeyTokenSymbol<?>> {

    /**
     * The inclusive start of the interval, or {@code null} for the
     * open-start form.
     */
    @Nullable
    private final TimestampSymbol start;

    /**
     * The exclusive end of the interval, or {@code null} for the open-end
     * form.
     */
    @Nullable
    private final TimestampSymbol end;

    /**
     * Construct a new {@link TemporalRangeKeySymbol}.
     *
     * @param key the wrapped {@link KeyTokenSymbol}; must not be a
     *            {@link NavigationKeySymbol} (navigation timestamps live
     *            on the path's stops) and must not already be
     *            parameterized (a key carries at most one
     *            bracket-timestamp parameter)
     * @param start the inclusive start of the interval, or {@code null}
     *            for the open-start form
     * @param end the exclusive end of the interval, or {@code null} for
     *            the open-end form
     * @throws IllegalArgumentException if {@code key} is a
     *             {@link NavigationKeySymbol} or already parameterized,
     *             if both endpoints are absent, or if {@code start} is
     *             strictly greater than {@code end}
     */
    public TemporalRangeKeySymbol(KeyTokenSymbol<?> key,
            @Nullable TimestampSymbol start, @Nullable TimestampSymbol end) {
        super(Preconditions.checkNotNull(key));
        Preconditions.checkArgument(!(key instanceof NavigationKeySymbol),
                "TemporalRangeKeySymbol cannot wrap a NavigationKeySymbol; "
                        + "temporal-range bindings on navigation keys are "
                        + "not yet supported");
        Preconditions.checkArgument(!key.isParameterized(),
                "TemporalRangeKeySymbol cannot wrap an already-parameterized "
                        + "key; a key carries at most one bracket-timestamp "
                        + "parameter");
        Preconditions.checkArgument(start != null || end != null,
                "a temporal range must pin at least one endpoint");
        Preconditions.checkArgument(
                start == null || end == null
                        || start.timestamp() <= end.timestamp(),
                "a temporal range cannot start after it ends");
        this.start = start;
        this.end = end;
    }

    /**
     * Return the inclusive start of the interval, or {@code null} for the
     * open-start form.
     *
     * @return the start {@link TimestampSymbol} or {@code null}
     */
    @Nullable
    public TimestampSymbol start() {
        return start;
    }

    /**
     * Return the exclusive end of the interval, or {@code null} for the
     * open-end form.
     *
     * @return the end {@link TimestampSymbol} or {@code null}
     */
    @Nullable
    public TimestampSymbol end() {
        return end;
    }

    @Override
    public boolean equals(Object obj) {
        if(this == obj) {
            return true;
        }
        else if(!(obj instanceof TemporalRangeKeySymbol)) {
            return false;
        }
        TemporalRangeKeySymbol other = (TemporalRangeKeySymbol) obj;
        return key.equals(other.key) && Objects.equals(start, other.start)
                && Objects.equals(end, other.end);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, start, end);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(key.toString()).append('[');
        if(start != null) {
            sb.append(start.timestamp());
        }
        sb.append("...");
        if(end != null) {
            sb.append(end.timestamp());
        }
        return sb.append(']').toString();
    }

    @Override
    public String baseKey() {
        return key.baseKey();
    }

    @Override
    public boolean isParameterized() {
        return true;
    }

    @Override
    public KeyTokenSymbol<?> stripParameters() {
        return key.stripParameters();
    }

}
