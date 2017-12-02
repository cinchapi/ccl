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

import java.util.Collection;

import com.cinchapi.common.base.AnyStrings;
import com.google.common.collect.Lists;

/**
 * An {@link Expression} is a {@link Symbol} that describes a query operation
 * on a key with respect to one or more values (e.g. key = value, key >=
 * value, etc) at a certain timestamp.
 * <p>
 * This class is designed to make it easier to process the results of the
 * Shunting-Yard algorithm.
 * </p>
 * 
 * @author Jeff Nelson
 */
public class Expression extends BaseSymbol implements PostfixNotationSymbol {

    private final KeySymbol key;
    private final OperatorSymbol operator;
    private final ValueSymbol[] values;
    private final TimestampSymbol timestamp;

    /**
     * Construct a new instance.
     * 
     * @param key
     * @param operator
     * @param values
     */
    public Expression(KeySymbol key, OperatorSymbol operator,
            ValueSymbol... values) {
        this(TimestampSymbol.PRESENT, key, operator, values);
    }

    /**
     * Construct a new instance.
     * 
     * @param timestamp
     * @param key
     * @param operator
     * @param values
     */
    public Expression(TimestampSymbol timestamp, KeySymbol key,
            OperatorSymbol operator, ValueSymbol... values) {
        this.key = key;
        this.operator = operator;
        this.values = values;
        this.timestamp = timestamp;
    }

    /**
     * Return the raw key associated with this {@link Expression}.
     * 
     * @return the key
     */
    public KeySymbol key() {
        return key;
    }

    /**
     * Return the operator associated with this {@link Expression}.
     * 
     * @return the operator
     */
    public OperatorSymbol operator() {
        return operator;
    }

    /**
     * Return the values associated with this {@link Expression}.
     * 
     * @return the values
     */
    public Collection<ValueSymbol> values() {
        return Lists.newArrayList(values);
    }

    /**
     * Return the timestamp associated with this {@link Expression}.
     * 
     * @return the timestamp
     */
    public TimestampSymbol timestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        String string = AnyStrings.format("{} {}", key, operator);
        for (ValueSymbol value : values) {
            string += " " + value;
        }
        if(timestamp.timestamp() > 0) {
            string += " at " + timestamp;
        }
        return string;
    }

}
