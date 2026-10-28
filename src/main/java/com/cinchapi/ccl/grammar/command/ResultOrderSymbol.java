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
package com.cinchapi.ccl.grammar.command;

import java.util.Objects;

import javax.annotation.concurrent.Immutable;

import com.cinchapi.ccl.grammar.DirectionSymbol;
import com.cinchapi.ccl.grammar.Symbol;
import com.cinchapi.common.base.AnyStrings;

/**
 * A {@link Symbol} for the {@code order by} clause of a grouped calculate
 * statement, which orders the groups by their result for one calculation, such
 * as {@code order by count desc}.
 * <p>
 * The clause names its calculation by function, as
 * {@link CalculationSymbol#resolve(String, java.util.List)} describes. Its
 * {@link #toString() text} is that CCL, with the direction spelled out.
 * </p>
 *
 * @author Jeff Nelson
 */
@Immutable
public final class ResultOrderSymbol implements Symbol {

    /**
     * The calculation whose result orders the groups.
     */
    private final CalculationSymbol calculation;

    /**
     * The direction of the order.
     */
    private final DirectionSymbol direction;

    /**
     * Construct a new instance.
     *
     * @param calculation the calculation whose result orders the groups; must
     *            be one of the statement's calculations
     * @param direction the direction of the order
     */
    public ResultOrderSymbol(CalculationSymbol calculation,
            DirectionSymbol direction) {
        this.calculation = calculation;
        this.direction = direction;
    }

    /**
     * Return the calculation whose result orders the groups.
     *
     * @return the calculation
     */
    public CalculationSymbol calculation() {
        return calculation;
    }

    /**
     * Return the direction of the order.
     *
     * @return the direction
     */
    public DirectionSymbol direction() {
        return direction;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof ResultOrderSymbol) {
            ResultOrderSymbol other = (ResultOrderSymbol) obj;
            return calculation.equals(other.calculation)
                    && direction == other.direction;
        }
        else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(calculation, direction);
    }

    @Override
    public String toString() {
        return AnyStrings.format("order by {} {}", calculation.function(),
                direction == DirectionSymbol.DESCENDING ? "desc" : "asc");
    }

}
