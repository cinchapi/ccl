/*
 * Copyright (c) 2013-2017 Cinchapi Inc.
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

import java.util.concurrent.TimeUnit;

import com.cinchapi.common.base.AnyStrings;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.primitives.Longs;

/**
 * A {@link Symbol} representing a timestamp (in microseconds) phrase.
 *
 * @author Jeff Nelson
 */
public class TimestampSymbol implements Symbol {

    /**
     * A {@link TimestampSymbol} that can be included in a
     * {@link ExpressionSymbol} to indicate that the expression is not temporal.
     */
    // default timestamp value of 0 indicates this is a present state query
    public static final TimestampSymbol PRESENT = new TimestampSymbol(0);

    /**
     * The content of the symbol.
     */
    private final long timestamp;

    /**
     * The degree of precision to use for this {@link TimestampSymbol} when
     * determining {@link #equals(Object) equality} and the {@link #hashCode()}.
     */
    private TimeUnit precision = TimeUnit.MILLISECONDS;

    /**
     * Construct a new instance.
     * 
     * @param timestamp
     */
    public TimestampSymbol(long timestamp) {
        this(timestamp, TimeUnit.MICROSECONDS);
    }

    /**
     * DO NOT CALL
     * Construct a new instance.
     * 
     * @param timestamp
     * @param precision
     */
    @VisibleForTesting
    public TimestampSymbol(long timestamp, TimeUnit precision) {
        this.timestamp = timestamp;
        this.precision = precision;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof TimestampSymbol) {
            return TimeUnit.MICROSECONDS.convert(timestamp,
                    precision) == TimeUnit.MICROSECONDS.convert(
                            ((TimestampSymbol) obj).timestamp, precision);
        }
        else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Longs
                .hashCode(TimeUnit.MICROSECONDS.convert(timestamp, precision));
    }

    /**
     * Return the timestamp (in microseconds) associated with this
     * {@link Symbol}.
     * 
     * @return the timestamp
     */
    public long timestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return AnyStrings.format("at {}", timestamp);
    }

}
