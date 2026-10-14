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

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.annotation.concurrent.Immutable;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

/**
 * A {@link ValueTokenSymbol} that stands in the value position of a token
 * stream for a quantified value set, such as {@code any("Jeff", "John")} or
 * {@code every((1, 5), (10, 20))}.
 * <p>
 * An {@link ExpressionSymbol} exposes a quantified set through
 * {@link ExpressionSymbol#quantifier()} and {@link ExpressionSymbol#values()}.
 * When the expression is flattened into tokens, the whole set travels as one
 * {@link ValueSetSymbol} so that the stream renders back to valid CCL and
 * regroups into the same expression.
 * </p>
 * <p>
 * The {@link #arity()} is the number of consecutive values that form one member
 * of the set, which is the number of operands the expression's operator takes.
 * A {@code between} set has arity 2 and renders each member as a parenthesized
 * tuple. The {@link #values()} hold every member in order, so their count is a
 * multiple of the arity.
 * </p>
 *
 * @author Jeff Nelson
 */
@Immutable
public final class ValueSetSymbol
        extends ValueTokenSymbol<List<ValueTokenSymbol<?>>> {

    /**
     * How the operator combines with the members.
     */
    private final QuantifierSymbol quantifier;

    /**
     * The number of consecutive values that form one member.
     */
    private final int arity;

    /**
     * Construct a set whose members are single values.
     *
     * @param quantifier how the operator combines with the members
     * @param values the members in order; must not be empty
     * @throws IllegalArgumentException if {@code values} is empty
     */
    public ValueSetSymbol(QuantifierSymbol quantifier,
            List<? extends ValueTokenSymbol<?>> values) {
        this(quantifier, 1, values);
    }

    /**
     * Construct a set whose members are tuples of {@code arity} values.
     *
     * @param quantifier how the operator combines with the members
     * @param arity the number of consecutive values that form one member; must
     *            be at least 1
     * @param values every member in order; must not be empty and its size must
     *            be a multiple of {@code arity}
     * @throws IllegalArgumentException if {@code arity} is less than 1, if
     *             {@code values} is empty, or if its size is not a multiple of
     *             {@code arity}
     */
    public ValueSetSymbol(QuantifierSymbol quantifier, int arity,
            List<? extends ValueTokenSymbol<?>> values) {
        super(ImmutableList.copyOf(values));
        Preconditions.checkArgument(arity >= 1,
                "The arity of a value set must be at least 1");
        Preconditions.checkArgument(!values.isEmpty(),
                "A quantified value set must hold at least one value");
        Preconditions.checkArgument(values.size() % arity == 0,
                "A value set with arity %s cannot hold %s values", arity,
                values.size());
        this.quantifier = Objects.requireNonNull(quantifier);
        this.arity = arity;
    }

    /**
     * Return the number of consecutive values that form one member of the set.
     *
     * @return the arity
     */
    public int arity() {
        return arity;
    }

    /**
     * Return how the operator combines with the members of the set.
     *
     * @return the {@link QuantifierSymbol}
     */
    public QuantifierSymbol quantifier() {
        return quantifier;
    }

    /**
     * Return every member value in order. A tuple member occupies
     * {@link #arity()} consecutive positions.
     *
     * @return the unmodifiable values
     */
    public List<ValueTokenSymbol<?>> values() {
        return value;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof ValueSetSymbol) {
            ValueSetSymbol other = (ValueSetSymbol) obj;
            return quantifier == other.quantifier && arity == other.arity
                    && value.equals(other.value);
        }
        else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(quantifier, arity, value);
    }

    @Override
    public String toString() {
        String members;
        if(arity == 1) {
            members = value.stream().map(Object::toString)
                    .collect(Collectors.joining(", "));
        }
        else {
            members = Lists.partition(value, arity).stream()
                    .map(tuple -> tuple.stream().map(Object::toString)
                            .collect(Collectors.joining(", ", "(", ")")))
                    .collect(Collectors.joining(", "));
        }
        return quantifier + "(" + members + ")";
    }

}
