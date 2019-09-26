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
package com.cinchapi.ccl.grammar.v3;

import com.cinchapi.common.base.AnyStrings;
import com.google.common.primitives.Longs;

/**
 * A {@link Token} representing a timestamp (in microseconds) phrase.
 *
 * @author Jeff Nelson
 */
public class TimestampToken implements Token {

    /**
     * A {@link TimestampToken} that can be included in a
     * {@link ExpressionToken} to indicate that the expression is not temporal.
     */
    // default timestamp value of 0 indicates this is a present state query
    public static final TimestampToken PRESENT = new TimestampToken(0);

    /**
     * The content of the symbol.
     */
    private final long timestamp;

    /**
     * Construct a new instance.
     * 
     * @param timestamp
     */
    public TimestampToken(long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof TimestampToken) {
            return timestamp == ((TimestampToken) obj).timestamp;
        }
        else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Longs.hashCode(timestamp);
    }
    
    /**
     * Return the timestamp (in microseconds) associated with this
     * {@link Token}.
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
