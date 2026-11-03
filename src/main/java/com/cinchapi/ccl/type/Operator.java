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
package com.cinchapi.ccl.type;

import java.util.List;

/**
 * An interface representing the input to an {@link OperatorSymbol}.
 *
 * @author Jeff Nelson
 */
public interface Operator {

    /**
     * Return the most operands this {@link Operator} accepts.
     *
     * @return the maximum number of operands, {@link #operands()} by default
     */
    public default int maxOperands() {
        return operands();
    }

    /**
     * Return the fewest operands this {@link Operator} accepts.
     *
     * @return the minimum number of operands, {@link #operands()} by default
     */
    public default int minOperands() {
        return operands();
    }

    /**
     * Return the number of operands operated on by this {@link Operator}.
     * 
     * @return the expected number of operands
     */
    public int operands();

    /**
     * Return the symbol that represents this {@link Operator}.
     * 
     * @return the operator's symbol
     */
    public String symbol();

    /**
     * Check that {@code operands} are valid for this {@link Operator}. The
     * parser calls this after it checks the number of operands against
     * {@link #minOperands()} and {@link #maxOperands()}.
     *
     * @param operands the parsed values of the operands, in order
     * @throws IllegalArgumentException if the operands are not valid
     */
    public default void validate(List<Object> operands) {}

}
