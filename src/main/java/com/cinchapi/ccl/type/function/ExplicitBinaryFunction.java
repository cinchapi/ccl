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
package com.cinchapi.ccl.type.function;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import com.cinchapi.ccl.type.Function;
import com.cinchapi.common.base.AnyStrings;

/**
 * A {@link Function} that requires two explicit arguments: a key and a source
 * that can be resolved to a list of records whose values stored for key should
 * be used handled to the operation.
 *
 * @author Jeff Nelson
 */
public abstract class ExplicitBinaryFunction<S> extends Function {

    /**
     * Indicates that this function has no timestamp.
     */
    private static long NO_TIMESTAMP = Long.MAX_VALUE; // matches
                                                       // com.cinchapi.concourse.time.Time#NONE

    /**
     * The selection timestamp associated with this function.
     */
    private final long timestamp;

    /**
     * The degree of precision to use for this {@link Function} when
     * determining {@link #equals(Object) equality} and the {@link #hashCode()}.
     */
    private TimeUnit timestampPrecision;

    /**
     * Construct a new instance.
     * 
     * @param name
     * @param key
     * @param source
     */
    protected ExplicitBinaryFunction(String name, String key, S source) {
        this(name, key, source, NO_TIMESTAMP);
    }

    /**
     * Construct a new instance.
     *
     * @param name
     * @param key
     * @param source
     * @param timestamp
     */
    protected ExplicitBinaryFunction(String name, String key, S source,
            long timestamp) {
        super(name, key, source);
        this.timestamp = timestamp;
        this.timestampPrecision = TimeUnit.MICROSECONDS;
    }

    @Override
    public boolean equals(Object obj) {
        boolean equals = super.equals(obj);
        if(timestamp != NO_TIMESTAMP && equals) {
            equals = timestampPrecision.convert(timestamp,
                    TimeUnit.MICROSECONDS) == timestampPrecision.convert(
                            ((ExplicitBinaryFunction<?>) obj).timestamp,
                            TimeUnit.MICROSECONDS);
        }
        return equals;
    }

    @Override
    public int hashCode() {
        int hashCode = super.hashCode();
        if(timestamp != NO_TIMESTAMP) {
            return Objects.hash(hashCode, TimeUnit.MICROSECONDS
                    .convert(timestamp, timestampPrecision));
        }
        return hashCode;
    }

    /**
     * Set the {@link #timestampPrecision} for this {@link Function} and return
     * it for chaining.
     * 
     * @param timestampPrecision
     * @return this
     */
    public ExplicitBinaryFunction<S> setTimestampPrecision(
            TimeUnit timestampPrecision) {
        this.timestampPrecision = timestampPrecision;
        return this;
    }

    /**
     * Return the "source" where the function is applied.
     * 
     * @return the source
     */
    @SuppressWarnings("unchecked")
    public final S source() {
        return (S) args[1];
    }

    /**
     * Return the timestamp describing when the function is applied.
     *
     * @return the timestamp
     */
    public long timestamp() {
        return timestamp;
    }

    @Override
    public final String toString() {
        return timestamp != NO_TIMESTAMP
                ? AnyStrings.format("{}({},{},{})", operation(), key(),
                        _sourceToString(), timestamp)
                : AnyStrings.format("{}({},{})", operation(), key(),
                        _sourceToString());
    }

    /**
     * Return the preferred {@link Object#toString()} of the {@link #source()}.
     * 
     * @return the {@link #source()}'s {@link #toString()}
     */
    protected abstract String _sourceToString();

}
