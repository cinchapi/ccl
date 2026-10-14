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

import javax.annotation.Nullable;

import com.cinchapi.ccl.generated.ASTExpression;
import com.cinchapi.ccl.type.Operator;
import com.google.common.base.Preconditions;

/**
 * An {@link ExpressionSymbol} is a {@link Symbol} that describes a query
 * operation on a key with respect to one or more values (e.g. key = value, key
 * >= value, etc) at a certain timestamp.
 * <p>
 * A quantified expression, such as {@code name = any("Jeff", "John")}, holds
 * every member of its value set in {@link #values()} and the way the operator
 * combines with them in {@link #quantifier()}. For an operator with more than
 * one operand, each consecutive group of {@link Operator#operands()} values is
 * one member of the set.
 * </p>
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
     * <p>
     * A single {@link ValueSetSymbol} may stand in for {@code values}; the
     * expression then takes the set's quantifier and holds the set's members as
     * its values.
     * </p>
     * 
     * @param key
     * @param operator
     * @param values
     * @return a new {@link ExpressionSymbol}
     * @throws IllegalArgumentException if a {@link ValueSetSymbol} is supplied
     *             alongside other values
     */
    public static ExpressionSymbol create(KeyTokenSymbol<?> key,
                                          OperatorSymbol operator, ValueTokenSymbol<?>... values) {
        return create(null, key, operator, values);
    }

    /**
     * Create a new {@link ExpressionSymbol} to represent the quantified
     * expression made up on the {@code key} symbol, {@code operator} symbol,
     * {@code quantifier} and the member {@code values}.
     * 
     * @param key
     * @param operator
     * @param quantifier how the operator combines with the members
     * @param values every member in order; must not be empty, and its size must
     *            be a multiple of the operator's operand count
     * @return a new {@link ExpressionSymbol}
     * @throws IllegalArgumentException if {@code values} is empty or its size
     *             is not a multiple of the operator's operand count
     */
    public static ExpressionSymbol create(KeyTokenSymbol<?> key,
            OperatorSymbol operator, QuantifierSymbol quantifier,
            ValueTokenSymbol<?>... values) {
        return create(null, key, operator, quantifier, values);
    }

    /**
     * Create a new {@link ExpressionSymbol} to represent the expression made up
     * on the {@code key} symbol, {@code operator} symbol, {@code values}
     * symbols and {@code timestamp} symbol.
     * <p>
     * A single {@link ValueSetSymbol} may stand in for {@code values}; the
     * expression then takes the set's quantifier and holds the set's members as
     * its values.
     * </p>
     * 
     * @param timestamp
     * @param key
     * @param operator
     * @param values
     * @return a new {@link ExpressionSymbol}
     * @throws IllegalArgumentException if a {@link ValueSetSymbol} is supplied
     *             alongside other values
     */
    public static ExpressionSymbol create(TimestampSymbol timestamp,
            KeyTokenSymbol<?> key, OperatorSymbol operator,
            ValueTokenSymbol<?>... values) {
        if(values.length == 1 && values[0] instanceof ValueSetSymbol) {
            ValueSetSymbol set = (ValueSetSymbol) values[0];
            return create(timestamp, key, operator, set.quantifier(),
                    set.values().toArray(new ValueTokenSymbol<?>[0]));
        }
        else {
            for (ValueTokenSymbol<?> value : values) {
                Preconditions.checkArgument(
                        !(value instanceof ValueSetSymbol),
                        "A value set must be the only value of an expression");
            }
            return create(timestamp, key, operator, null, values);
        }
    }

    /**
     * Create a new {@link ExpressionSymbol} to represent the expression made up
     * on the {@code key} symbol, {@code operator} symbol, {@code quantifier},
     * member {@code values} and {@code timestamp} symbol.
     * 
     * @param timestamp
     * @param key
     * @param operator
     * @param quantifier how the operator combines with the members, or
     *            {@code null} for a plain comparison
     * @param values the values, or every member of the set in order when
     *            {@code quantifier} is not {@code null}, in which case there
     *            must be at least one and their count must be a multiple of the
     *            operator's operand count
     * @return a new {@link ExpressionSymbol}
     * @throws IllegalArgumentException if {@code quantifier} is not
     *             {@code null} and {@code values} is empty or its size is not a
     *             multiple of the operator's operand count
     */
    public static ExpressionSymbol create(TimestampSymbol timestamp,
            KeyTokenSymbol<?> key, OperatorSymbol operator,
            @Nullable QuantifierSymbol quantifier,
            ValueTokenSymbol<?>... values) {
        if(quantifier != null) {
            int arity = operator.operator().operands();
            Preconditions.checkArgument(values.length > 0,
                    "A quantified value set must hold at least one value");
            Preconditions.checkArgument(values.length % arity == 0,
                    "A value set with arity %s cannot hold %s values", arity,
                    values.length);
        }
        ASTExpression token = new ASTExpression(0);
        if(timestamp != null && timestamp != TimestampSymbol.PRESENT) {
            token.timestamp(timestamp);
        }
        token.key(key);
        token.operator(operator);
        token.quantifier(quantifier);
        for (ValueTokenSymbol<?> value : values) {
            token.addValue(value);
        }
        return token;
    }

    /**
     * Return the raw key associated with this {@link ExpressionSymbol}.
     * 
     * @return the key
     */
    public <T extends KeyTokenSymbol<?>> T key();

    /**
     * Return the operator associated with this {@link ExpressionSymbol}.
     * 
     * @return the operator
     */
    public OperatorSymbol operator();

    /**
     * Return how the operator combines with the members of the value set, or
     * {@code null} when this is a plain comparison against {@link #values()}.
     * 
     * @return the {@link QuantifierSymbol} or {@code null}
     */
    @Nullable
    public QuantifierSymbol quantifier();

    /**
     * Return the raw symbol {@link Content}.
     * 
     * @return the raw symbol content
     */
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
            public QuantifierSymbol quantifier() {
                return ExpressionSymbol.this.quantifier();
            }

            @Override
            public long timestamp() {
                return ExpressionSymbol.this.timestamp().timestamp();
            }

            @SuppressWarnings("unchecked")
            @Override
            public <T> List<T> values() {
                return (List<T>) ExpressionSymbol.this.values().stream()
                        .map(ValueTokenSymbol::value)
                        .collect(Collectors.toList());
            }

        };
    }

    /**
     * Set the {@link #timestamp()} symbol of this {@link ExpressionSymbol}.
     * 
     * @param symbol
     */
    public void timestamp(TimestampSymbol symbol);

    /**
     * Return the timestamp associated with this {@link ExpressionSymbol}.
     * 
     * @return the timestamp
     */
    public TimestampSymbol timestamp();

    /**
     * Return the values associated with this {@link ExpressionSymbol}. For a
     * quantified expression these are every member of the value set in order.
     * 
     * @return the values
     */
    public List<ValueTokenSymbol<?>> values();

    /**
     * A wrapper class that contains the content of the symbols in this
     * {@link Expression}.
     *
     * @author Jeff Nelson
     */
    interface Content {

        /**
         * Return the content of the {@link KeySymbol}.
         * 
         * @return key content
         */
        public String key();

        /**
         * Return the content of the {@link OperatorSymbol}.
         * 
         * @return operator content
         */
        public Operator operator();

        /**
         * Return how the operator combines with the members of the value set,
         * or {@code null} for a plain comparison.
         * 
         * @return the {@link QuantifierSymbol} or {@code null}
         */
        @Nullable
        public QuantifierSymbol quantifier();

        /**
         * Return the content of the {@link TimestampSymbol}.
         * 
         * @return timestamp content
         */
        public long timestamp();

        /**
         * Return the content of each {@link ValueSymbol}. For a quantified
         * expression these are every member of the value set in order.
         * 
         * @return value content
         */
        public <T> List<T> values();

    }

}
