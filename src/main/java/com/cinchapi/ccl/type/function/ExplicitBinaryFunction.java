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

    private final long timestamp;

    /**
     * Construct a new instance.
     * 
     * @param name
     * @param key
     * @param source
     */
    protected ExplicitBinaryFunction(String name, String key, S source) {
        super(name, key, source);
        this.timestamp = 0;
    }

    /**
     * Construct a new instance.
     *
     * @param name
     * @param key
     * @param source
     * @param timestamp
     */
    protected ExplicitBinaryFunction(String name, String key, long timestamp,
            S source) {
        super(name, key, source);
        this.timestamp = timestamp;
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
        return AnyStrings.format("{}({},{},{})", operation(), key(),
                _sourceToString(), timestamp);
    }

    /**
     * Return the preferred {@link Object#toString()} of the {@link #source()}.
     * 
     * @return the {@link #source()}'s {@link #toString()}
     */
    protected abstract String _sourceToString();

}
