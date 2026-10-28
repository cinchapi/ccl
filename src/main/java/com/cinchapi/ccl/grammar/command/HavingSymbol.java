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

import com.cinchapi.ccl.grammar.OperatorSymbol;
import com.cinchapi.ccl.grammar.Symbol;
import com.cinchapi.ccl.grammar.ValueTokenSymbol;
import com.cinchapi.common.base.AnyStrings;

/**
 * A {@link Symbol} for the {@code having} clause of a grouped calculate
 * statement, which keeps the groups whose result for one calculation satisfies
 * a comparison, such as {@code having count >= 2}.
 * <p>
 * The clause names its calculation by function, as
 * {@link CalculationSymbol#resolve(String, java.util.List)} describes. Its
 * {@link #toString() text} is that CCL.
 * </p>
 *
 * @author Jeff Nelson
 */
@Immutable
public final class HavingSymbol implements Symbol {

    /**
     * The calculation whose result the comparison tests.
     */
    private final CalculationSymbol calculation;

    /**
     * The operator of the comparison.
     */
    private final OperatorSymbol operator;

    /**
     * The value that the result is compared to.
     */
    private final ValueTokenSymbol<?> value;

    /**
     * Construct a new instance.
     *
     * @param calculation the calculation whose result the comparison tests;
     *            must be one of the statement's calculations
     * @param operator the operator of the comparison
     * @param value the value that the result is compared to
     */
    public HavingSymbol(CalculationSymbol calculation, OperatorSymbol operator,
            ValueTokenSymbol<?> value) {
        this.calculation = calculation;
        this.operator = operator;
        this.value = value;
    }

    /**
     * Return the calculation whose result the comparison tests.
     *
     * @return the calculation
     */
    public CalculationSymbol calculation() {
        return calculation;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof HavingSymbol) {
            HavingSymbol other = (HavingSymbol) obj;
            return calculation.equals(other.calculation)
                    && operator.equals(other.operator)
                    && value.equals(other.value);
        }
        else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(calculation, operator, value);
    }

    /**
     * Return the operator of the comparison.
     *
     * @return the operator
     */
    public OperatorSymbol operator() {
        return operator;
    }

    @Override
    public String toString() {
        return AnyStrings.format("having {} {} {}", calculation.function(),
                operator, value);
    }

    /**
     * Return the value that the result is compared to.
     *
     * @return the value
     */
    public ValueTokenSymbol<?> value() {
        return value;
    }

}
