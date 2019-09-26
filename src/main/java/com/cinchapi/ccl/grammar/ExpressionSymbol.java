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
import java.util.stream.Collectors;

import com.cinchapi.ccl.generated.ASTExpression;
import com.cinchapi.ccl.type.Operator;

/**
 * An {@link ExpressionSymbol} is a {@link Symbol} that describes a query
 * operation on a key with respect to one or more values (e.g. key = value, key
 * >= value, etc) at a certain timestamp.
 * <p>
 * This class is designed to make it easier to process the results of the
 * Shunting-Yard algorithm.
 * </p>
 * 
 * @author Jeff Nelson
 */
public interface ExpressionSymbol extends PostfixNotationSymbol {

    /**
     * Create a new {@link ExpressionSymbol} to represent the expression made up
     * on the {@code key} symbol, {@code operator} symbol and {@code values}
     * symbols.
     * 
     * @param key
     * @param operator
     * @param values
     * @return a new {@link ExpressionSymbol}
     */
    public static ExpressionSymbol create(AbstractKeySymbol<?> key,
            OperatorSymbol operator, AbstractValueSymbol<?>... values) {
        return create(null, key, operator, values);
    }

    /**
     * Create a new {@link ExpressionSymbol} to represent the expression made up
     * on the {@code key} symbol, {@code operator} symbol, {@code values}
     * symbols and {@code timestamp} symbol.
     * 
     * @param timestamp
     * @param key
     * @param operator
     * @param values
     * @return a new {@link ExpressionSymbol}
     */
    public static ExpressionSymbol create(TimestampSymbol timestamp,
            AbstractKeySymbol<?> key, OperatorSymbol operator, AbstractValueSymbol<?>... values) {
        ASTExpression token = new ASTExpression(0);
        if(timestamp != null && timestamp != TimestampSymbol.PRESENT) {
            token.timestamp(timestamp);
        }
        token.key(key);
        token.operator(operator);
        for(AbstractValueSymbol<?> value : values) {
            token.addValue(value);
        }
        return token;
    }

    /**
     * Return the raw key associated with this {@link ExpressionSymbol}.
     * 
     * @return the key
     */
    public <T extends AbstractKeySymbol<?>> T key();

    /**
     * Return the operator associated with this {@link ExpressionSymbol}.
     * 
     * @return the operator
     */
    public OperatorSymbol operator();

    /**
     * Return the timestamp associated with this {@link ExpressionSymbol}.
     * 
     * @return the timestamp
     */
    public TimestampSymbol timestamp();

    /**
     * Return the values associated with this {@link ExpressionSymbol}.
     * 
     * @return the values
     */
    public List<AbstractValueSymbol<?>> values();

    public default Content raw() {
        return new Content() {

            @Override
            public String key() {
                return ExpressionSymbol.this.key().key().toString();
            }

            @Override
            public Operator operator() {
                return ExpressionSymbol.this.operator().operator();
            }

            @Override
            public long timestamp() {
                return ExpressionSymbol.this.timestamp().timestamp();
            }

            @SuppressWarnings("unchecked")
            @Override
            public <T> List<T> values() {
                return (List<T>) ExpressionSymbol.this.values().stream()
                        .map(AbstractValueSymbol::value).collect(Collectors.toList());
            }

        };
    }

    interface Content {

        public String key();

        public Operator operator();

        public long timestamp();

        public <T> List<T> values();

    }

}
