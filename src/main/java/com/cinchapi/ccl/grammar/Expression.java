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

import java.util.List;

import com.cinchapi.ccl.type.Operator;
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
     * The symbol {@link Content} returned from the {@link #raw()} method.
     */
    private final Content content = new Content();

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
     * Return the raw symbol {@link Content}.
     * 
     * @return the raw symbol content
     */
    public Content raw() {
        return content;
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
            string += timestamp;
        }
        return string;
    }

    /**
     * Return the values associated with this {@link Expression}.
     * 
     * @return the values
     */
    public List<ValueSymbol> values() {
        return Lists.newArrayList(values);
    }

    /**
     * A wrapper class that contains the content of the symbols in this
     * {@link Expression}.
     *
     * @author Jeff Nelson
     */
    public class Content {

        /**
         * Return the content of the {@link KeySymbol}.
         * 
         * @return key content
         */
        public String key() {
            return Expression.this.key().key();
        }

        /**
         * Return the content of the {@link OperatorSymbol}.
         * 
         * @return operator content
         */
        public Operator operator() {
            return Expression.this.operator().operator();
        }

        /**
         * Return the content of the {@link TimestampSymbol}.
         * 
         * @return timestamp content
         */
        public long timestamp() {
            return Expression.this.timestamp().timestamp();
        }

        /**
         * Return the content of each {@link ValueSymbol}.
         * 
         * @return value content
         */
        public List<Object> values() {
            List<Object> values = Lists
                    .newArrayListWithCapacity(Expression.this.values.length);
            for (ValueSymbol symbol : Expression.this.values) {
                values.add(symbol.value());
            }
            return values;
        }

    }

}
