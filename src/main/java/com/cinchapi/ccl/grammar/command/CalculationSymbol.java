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

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import com.cinchapi.ccl.grammar.KeyTokenSymbol;
import com.cinchapi.ccl.grammar.Symbol;
import com.cinchapi.common.base.AnyStrings;
import com.google.common.collect.ImmutableMap;

/**
 * A {@link Symbol} for one calculation in a calculate statement, which applies
 * a function to the values of a key, such as {@code sum price} or
 * {@code percentile 0.99 latency}.
 * <p>
 * The {@code percentile} function takes a fraction from 0 to 1, which comes
 * before the key in CCL. No other function takes one. The
 * {@link #toString() text} of a calculation is its CCL, which parses to an
 * equal symbol.
 * </p>
 *
 * @author Jeff Nelson
 */
@Immutable
public final class CalculationSymbol implements Symbol {

    /**
     * Return the one calculation among {@code calculations} whose function
     * {@code function} names. A name matches in any letter case, and the names
     * {@code avg} and {@code average} name the same function.
     * <p>
     * A clause that refers to the result of one of a statement's calculations,
     * such as {@code having} or {@code order by}, names the calculation this
     * way.
     * </p>
     *
     * @param function the name of a function
     * @param calculations the calculations of a statement
     * @return the calculation that {@code function} names
     * @throws IllegalArgumentException if {@code function} names none of
     *             {@code calculations} or more than one of them
     */
    public static CalculationSymbol resolve(String function,
            List<CalculationSymbol> calculations) {
        List<CalculationSymbol> named = calculations.stream()
                .filter(calculation -> calculation.isNamedBy(function))
                .collect(Collectors.toList());
        if(named.size() == 1) {
            return named.get(0);
        }
        else if(named.isEmpty()) {
            throw new IllegalArgumentException(
                    AnyStrings.format("{} names none of the calculations {}",
                            function, calculations));
        }
        else {
            throw new IllegalArgumentException(AnyStrings.format(
                    "{} names more than one of the calculations {}", function,
                    calculations));
        }
    }

    /**
     * Return the identity of the function that {@code name} names. Two names
     * have the same identity exactly when they name the same function.
     *
     * @param name the name of a function, in any letter case
     * @return the identity of the function
     */
    private static String identify(String name) {
        String lower = name.toLowerCase(Locale.ROOT);
        return lower.equals("avg") ? "average" : lower;
    }

    /**
     * The canonical name of each function, keyed by the name in lower case.
     */
    private static final Map<String, String> FUNCTIONS = ImmutableMap
            .<String, String> builder().put("sum", "sum").put("avg", "avg")
            .put("average", "average").put("count", "count").put("min", "min")
            .put("max", "max").put("countdistinct", "countDistinct")
            .put("median", "median").put("percentile", "percentile")
            .put("stddev", "stddev").put("stddevpopulation", "stddevPopulation")
            .put("variance", "variance")
            .put("variancepopulation", "variancePopulation").build();

    /**
     * The canonical name of the function.
     */
    private final String function;

    /**
     * The fraction of a {@code percentile}, or {@code null} for any other
     * function.
     */
    @Nullable
    private final Double fraction;

    /**
     * The key whose values the function applies to.
     */
    private final KeyTokenSymbol<?> key;

    /**
     * Construct a new instance.
     *
     * @param function the name of the function, in any letter case
     * @param fraction the fraction of a {@code percentile}, from 0 to 1; must
     *            be {@code null} for any other function
     * @param key the key whose values the function applies to
     * @throws IllegalArgumentException if {@code function} is not a supported
     *             function, if a {@code percentile} has no fraction or one
     *             outside 0 to 1, or if another function has a fraction
     */
    public CalculationSymbol(String function, @Nullable Double fraction,
            KeyTokenSymbol<?> key) {
        String canonical = FUNCTIONS.get(function.toLowerCase(Locale.ROOT));
        if(canonical == null) {
            throw new IllegalArgumentException(AnyStrings.format(
                    "Invalid calculation function '{}'. Must be one of: {}",
                    function, FUNCTIONS.values()));
        }
        else if(canonical.equals("percentile")
                && (fraction == null || !(fraction >= 0 && fraction <= 1))) {
            throw new IllegalArgumentException(AnyStrings.format(
                    "A percentile needs a fraction from 0 to 1, but got {}",
                    fraction));
        }
        else if(!canonical.equals("percentile") && fraction != null) {
            throw new IllegalArgumentException(AnyStrings.format(
                    "Only a percentile takes a fraction, but {} got {}",
                    canonical, fraction));
        }
        else {
            this.function = canonical;
            this.fraction = fraction;
            this.key = key;
        }
    }

    /**
     * Construct a new instance for a function that takes no fraction.
     *
     * @param function the name of the function, in any letter case; must not be
     *            {@code percentile}
     * @param key the key whose values the function applies to
     * @throws IllegalArgumentException if {@code function} is not a supported
     *             function or is {@code percentile}
     */
    public CalculationSymbol(String function, KeyTokenSymbol<?> key) {
        this(function, null, key);
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof CalculationSymbol) {
            CalculationSymbol other = (CalculationSymbol) obj;
            return function.equals(other.function)
                    && Objects.equals(fraction, other.fraction)
                    && key.equals(other.key);
        }
        else {
            return false;
        }
    }

    /**
     * Return the fraction of a {@code percentile}, from 0 to 1.
     *
     * @return the fraction, or {@code null} for any function other than
     *         {@code percentile}
     */
    @Nullable
    public Double fraction() {
        return fraction;
    }

    /**
     * Return the canonical name of the function. A function name parses in any
     * letter case. The canonical name is lower case for {@code sum},
     * {@code avg}, {@code average}, {@code count}, {@code min} and {@code max}.
     * It is camelCase for every other function, such as {@code countDistinct},
     * because that is the name of the Concourse calculation it asks for, so a
     * caller can look the calculation up by name.
     *
     * @return the canonical name
     */
    public String function() {
        return function;
    }

    @Override
    public int hashCode() {
        return Objects.hash(function, fraction, key);
    }

    /**
     * Return the key whose values the function applies to.
     *
     * @return the key
     */
    public KeyTokenSymbol<?> key() {
        return key;
    }

    @Override
    public String toString() {
        if(fraction != null) {
            return AnyStrings.format("{} {} {}", function,
                    BigDecimal.valueOf(fraction).toPlainString(), key);
        }
        else {
            return AnyStrings.format("{} {}", function, key);
        }
    }

    /**
     * Return whether {@code name} names this calculation's function. A name
     * matches in any letter case, and the names {@code avg} and {@code average}
     * name the same function.
     *
     * @param name the name of a function
     * @return {@code true} if {@code name} names the function
     */
    private boolean isNamedBy(String name) {
        return identify(name).equals(identify(function));
    }

}
