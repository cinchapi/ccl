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
import java.math.BigInteger;
import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.NotThreadSafe;

import com.cinchapi.ccl.grammar.KeyTokenSymbol;
import com.cinchapi.ccl.grammar.PageSymbol;
import com.cinchapi.ccl.grammar.Symbol;
import com.cinchapi.ccl.grammar.TimestampSymbol;
import com.cinchapi.common.base.AnyStrings;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

/**
 * A {@link Symbol} for the {@code group by} clause of a calculate statement,
 * which calculates a result for each group of records instead of one result for
 * all of them.
 * <p>
 * The clause names one or more group keys, which may be navigation keys. The
 * rest of the clause is optional and comes in this order.
 * </p>
 * <ul>
 * <li>{@code per} groups the values of the last key into ranges. A number
 * gives the width of number ranges, and a quoted duration, such as
 * {@code "1 day"}, gives the period of time ranges.</li>
 * <li>{@code between <a> and <b>} follows {@code per} and bounds the ranges.
 * The bounds are numbers for number ranges and timestamps for time ranges.</li>
 * <li>{@code having} keeps the groups whose result satisfies a comparison, as
 * a {@link HavingSymbol}.</li>
 * <li>{@code order by} orders the groups by a result, as a
 * {@link ResultOrderSymbol}.</li>
 * <li>{@code offset} and {@code limit} page the groups, as a
 * {@link PageSymbol}.</li>
 * </ul>
 * <p>
 * Two groups are equal when their parts are equal, and numbers are equal when
 * they have the same value, such as {@code 10} and {@code 10.0}. The
 * {@link #toString() text} of a group is its CCL, with each duration in its
 * largest exact unit.
 * </p>
 *
 * @author Jeff Nelson
 */
@Immutable
public final class GroupSymbol implements Symbol {

    /**
     * Return a {@link Builder} for a group of {@code keys}.
     *
     * @param keys the group keys, as the statement lists them; must not be
     *            empty
     * @return the {@link Builder}
     * @throws IllegalArgumentException if {@code keys} is empty
     */
    public static Builder builder(
            Collection<? extends KeyTokenSymbol<?>> keys) {
        Preconditions.checkArgument(!keys.isEmpty(),
                "A group needs at least one key");
        return new Builder(keys);
    }

    /**
     * Return {@code number} as a {@link BigDecimal}.
     *
     * @param number the number
     * @return the {@link BigDecimal} with the value of {@code number}
     * @throws IllegalArgumentException if {@code number} is not finite
     */
    private static BigDecimal decimal(Number number) {
        try {
            return new BigDecimal(number.toString());
        }
        catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    AnyStrings.format("{} is not a finite number", number));
        }
    }

    /**
     * Return the CCL for {@code period}, as a quoted amount of its largest
     * exact unit, such as {@code "1 day"} or {@code "90 minutes"}.
     *
     * @param period the period
     * @return the quoted duration
     */
    private static String format(Duration period) {
        BigInteger nanos = BigInteger.valueOf(period.getSeconds())
                .multiply(BigInteger.valueOf(1_000_000_000))
                .add(BigInteger.valueOf(period.getNano()));
        Map.Entry<String, BigInteger> unit = UNITS.entrySet().stream()
                .filter(entry -> nanos.mod(entry.getValue()).signum() == 0)
                .findFirst().get();
        BigInteger amount = nanos.divide(unit.getValue());
        return AnyStrings.format("\"{} {}{}\"", amount, unit.getKey(),
                amount.equals(BigInteger.ONE) ? "" : "s");
    }

    /**
     * Return the hash code of the value of {@code number}, which is the same
     * for numbers with the same value.
     *
     * @param number the number
     * @return the hash code, or 0 if {@code number} is {@code null}
     */
    private static int hashOfValue(@Nullable Number number) {
        if(number == null) {
            return 0;
        }
        else {
            BigDecimal value = decimal(number);
            return value.signum() == 0 ? 0
                    : value.stripTrailingZeros().hashCode();
        }
    }

    /**
     * Return the CCL for {@code number}, without an exponent.
     *
     * @param number the number
     * @return the text of the number
     */
    private static String plain(Number number) {
        return decimal(number).toPlainString();
    }

    /**
     * Return whether {@code a} and {@code b} have the same value.
     *
     * @param a a number
     * @param b another number
     * @return {@code true} if both are {@code null} or both have the same value
     */
    private static boolean sameValue(@Nullable Number a, @Nullable Number b) {
        if(a == null || b == null) {
            return a == b;
        }
        else {
            return decimal(a).compareTo(decimal(b)) == 0;
        }
    }

    /**
     * The units that a duration's text can use, from largest to smallest, with
     * the number of nanoseconds in each.
     */
    private static final Map<String, BigInteger> UNITS = ImmutableMap
            .<String, BigInteger> builder()
            .put("day", BigInteger.valueOf(86_400_000_000_000L))
            .put("hour", BigInteger.valueOf(3_600_000_000_000L))
            .put("minute", BigInteger.valueOf(60_000_000_000L))
            .put("second", BigInteger.valueOf(1_000_000_000L))
            .put("millisecond", BigInteger.valueOf(1_000_000L))
            .put("microsecond", BigInteger.valueOf(1_000L))
            .put("nanosecond", BigInteger.ONE).build();

    /**
     * The group keys, as the statement lists them.
     */
    private final List<KeyTokenSymbol<?>> keys;

    /**
     * The width of number ranges of the last key, or {@code null}.
     */
    @Nullable
    private final Number width;

    /**
     * The period of time ranges of the last key, or {@code null}.
     */
    @Nullable
    private final Duration period;

    /**
     * The lower bound of number ranges, or {@code null}.
     */
    @Nullable
    private final Number lowerBound;

    /**
     * The upper bound of number ranges, or {@code null}.
     */
    @Nullable
    private final Number upperBound;

    /**
     * The start of time ranges, or {@code null}.
     */
    @Nullable
    private final TimestampSymbol start;

    /**
     * The end of time ranges, or {@code null}.
     */
    @Nullable
    private final TimestampSymbol end;

    /**
     * The {@code having} clause, or {@code null}.
     */
    @Nullable
    private final HavingSymbol having;

    /**
     * The order of the groups by a result, or {@code null}.
     */
    @Nullable
    private final ResultOrderSymbol order;

    /**
     * The page of groups, or {@code null}.
     */
    @Nullable
    private final PageSymbol page;

    /**
     * Construct a new instance from the parts that {@code builder} holds.
     *
     * @param builder the {@link Builder} with consistent parts
     */
    private GroupSymbol(Builder builder) {
        this.keys = builder.keys;
        this.width = builder.width;
        this.period = builder.period;
        this.lowerBound = builder.lowerBound;
        this.upperBound = builder.upperBound;
        this.start = builder.start;
        this.end = builder.end;
        this.having = builder.having;
        this.order = builder.order;
        this.page = builder.page;
    }

    /**
     * Return the upper bound of the time ranges, which a {@code between} clause
     * gives.
     *
     * @return the end, or {@code null} if the group has no time ranges or no
     *         bounds
     */
    @Nullable
    public TimestampSymbol end() {
        return end;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof GroupSymbol) {
            GroupSymbol other = (GroupSymbol) obj;
            return keys.equals(other.keys) && sameValue(width, other.width)
                    && Objects.equals(period, other.period)
                    && sameValue(lowerBound, other.lowerBound)
                    && sameValue(upperBound, other.upperBound)
                    && Objects.equals(start, other.start)
                    && Objects.equals(end, other.end)
                    && Objects.equals(having, other.having)
                    && Objects.equals(order, other.order)
                    && Objects.equals(page, other.page);
        }
        else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(keys, hashOfValue(width), period,
                hashOfValue(lowerBound), hashOfValue(upperBound), start, end,
                having, order, page);
    }

    /**
     * Return the {@code having} clause, which keeps the groups whose result
     * satisfies a comparison.
     *
     * @return the {@link HavingSymbol}, or {@code null} if the group keeps
     *         every group
     */
    @Nullable
    public HavingSymbol having() {
        return having;
    }

    /**
     * Return the group keys, as the statement lists them. A range groups the
     * values of the last one.
     *
     * @return an unmodifiable {@link List} of at least one key
     */
    public List<KeyTokenSymbol<?>> keys() {
        return keys;
    }

    /**
     * Return the lower bound of the number ranges, which a {@code between}
     * clause gives.
     *
     * @return the lower bound, or {@code null} if the group has no number
     *         ranges or no bounds
     */
    @Nullable
    public Number lowerBound() {
        return lowerBound;
    }

    /**
     * Return the order of the groups by the result of one calculation.
     *
     * @return the {@link ResultOrderSymbol}, or {@code null} if the statement
     *         does not order its groups
     */
    @Nullable
    public ResultOrderSymbol order() {
        return order;
    }

    /**
     * Return the page of groups.
     *
     * @return the {@link PageSymbol}, or {@code null} if the statement does not
     *         page its groups
     */
    @Nullable
    public PageSymbol page() {
        return page;
    }

    /**
     * Return the period of the time ranges that group the values of the last
     * key.
     *
     * @return the period, or {@code null} if the group has no time ranges
     */
    @Nullable
    public Duration period() {
        return period;
    }

    /**
     * Return the lower bound of the time ranges, which a {@code between} clause
     * gives.
     *
     * @return the start, or {@code null} if the group has no time ranges or no
     *         bounds
     */
    @Nullable
    public TimestampSymbol start() {
        return start;
    }

    @Override
    public String toString() {
        StringBuilder text = new StringBuilder("group by ");
        text.append(keys.stream().map(Object::toString)
                .collect(Collectors.joining(", ")));
        if(width != null) {
            text.append(" per ").append(plain(width));
        }
        else if(period != null) {
            text.append(" per ").append(format(period));
        }
        if(lowerBound != null) {
            text.append(" between ").append(plain(lowerBound)).append(" and ")
                    .append(plain(upperBound));
        }
        else if(start != null) {
            text.append(" between ").append(start.timestamp()).append(" and ")
                    .append(end.timestamp());
        }
        if(having != null) {
            text.append(' ').append(having);
        }
        if(order != null) {
            text.append(' ').append(order);
        }
        if(page != null) {
            text.append(' ').append(page);
        }
        return text.toString();
    }

    /**
     * Return the upper bound of the number ranges, which a {@code between}
     * clause gives.
     *
     * @return the upper bound, or {@code null} if the group has no number
     *         ranges or no bounds
     */
    @Nullable
    public Number upperBound() {
        return upperBound;
    }

    /**
     * Return the width of the number ranges that group the values of the last
     * key.
     *
     * @return the width, or {@code null} if the group has no number ranges
     */
    @Nullable
    public Number width() {
        return width;
    }

    /**
     * A builder of a {@link GroupSymbol}. Each part is optional, and setting a
     * part again replaces it.
     *
     * @author Jeff Nelson
     */
    @NotThreadSafe
    public static final class Builder {

        /**
         * The group keys, as the statement lists them.
         */
        private final List<KeyTokenSymbol<?>> keys;

        /**
         * The width of number ranges, or {@code null}.
         */
        @Nullable
        private Number width;

        /**
         * The period of time ranges, or {@code null}.
         */
        @Nullable
        private Duration period;

        /**
         * The lower bound of number ranges, or {@code null}.
         */
        @Nullable
        private Number lowerBound;

        /**
         * The upper bound of number ranges, or {@code null}.
         */
        @Nullable
        private Number upperBound;

        /**
         * The start of time ranges, or {@code null}.
         */
        @Nullable
        private TimestampSymbol start;

        /**
         * The end of time ranges, or {@code null}.
         */
        @Nullable
        private TimestampSymbol end;

        /**
         * The {@code having} clause, or {@code null}.
         */
        @Nullable
        private HavingSymbol having;

        /**
         * The order of the groups by a result, or {@code null}.
         */
        @Nullable
        private ResultOrderSymbol order;

        /**
         * The page of groups, or {@code null}.
         */
        @Nullable
        private PageSymbol page;

        /**
         * Construct a new instance.
         *
         * @param keys the group keys, as the statement lists them; must not be
         *            empty
         */
        private Builder(Collection<? extends KeyTokenSymbol<?>> keys) {
            this.keys = ImmutableList.copyOf(keys);
        }

        /**
         * Bound the number ranges by {@code lowerBound} and {@code upperBound},
         * which asks for every range between them, including an empty one. The
         * group must also have a {@link #per(Number) width}.
         *
         * @param lowerBound the lower bound
         * @param upperBound the upper bound; must be greater than
         *            {@code lowerBound}
         * @return this {@link Builder}
         * @throws IllegalArgumentException if a bound is not finite or
         *             {@code upperBound} is not greater than {@code lowerBound}
         */
        public Builder between(Number lowerBound, Number upperBound) {
            Preconditions.checkArgument(
                    decimal(lowerBound).compareTo(decimal(upperBound)) < 0,
                    "The lower bound of the ranges must be less than the "
                            + "upper bound, but got %s and %s",
                    lowerBound, upperBound);
            this.lowerBound = lowerBound;
            this.upperBound = upperBound;
            return this;
        }

        /**
         * Bound the time ranges by {@code start} and {@code end}, which asks
         * for every range between them, including an empty one. The group must
         * also have a {@link #per(Duration) period}.
         *
         * @param start the start
         * @param end the end; must be after {@code start}
         * @return this {@link Builder}
         * @throws IllegalArgumentException if {@code end} is not after
         *             {@code start}
         */
        public Builder between(TimestampSymbol start, TimestampSymbol end) {
            Preconditions.checkArgument(start.timestamp() < end.timestamp(),
                    "The start of the ranges must be before the end, but got "
                            + "%s and %s",
                    start.timestamp(), end.timestamp());
            this.start = start;
            this.end = end;
            return this;
        }

        /**
         * Return a {@link GroupSymbol} with the parts of this {@link Builder}.
         *
         * @return the {@link GroupSymbol}
         * @throws IllegalStateException if the group has both a width and a
         *             period, number bounds without a width, or timestamp
         *             bounds without a period
         */
        public GroupSymbol build() {
            Preconditions.checkState(width == null || period == null,
                    "A group has a width or a period, but not both");
            Preconditions.checkState(lowerBound == null || width != null,
                    "Number bounds need a width");
            Preconditions.checkState(start == null || period != null,
                    "Timestamp bounds need a period");
            return new GroupSymbol(this);
        }

        /**
         * Keep only the groups whose result satisfies the comparison of
         * {@code having}.
         *
         * @param having the {@link HavingSymbol}
         * @return this {@link Builder}
         */
        public Builder having(HavingSymbol having) {
            this.having = having;
            return this;
        }

        /**
         * Order the groups by the result of one calculation.
         *
         * @param order the {@link ResultOrderSymbol}
         * @return this {@link Builder}
         */
        public Builder order(ResultOrderSymbol order) {
            this.order = order;
            return this;
        }

        /**
         * Keep only the groups on {@code page}.
         *
         * @param page the {@link PageSymbol}
         * @return this {@link Builder}
         */
        public Builder page(PageSymbol page) {
            this.page = page;
            return this;
        }

        /**
         * Group the values of the last key into time ranges of {@code period}.
         *
         * @param period the period; must be positive
         * @return this {@link Builder}
         * @throws IllegalArgumentException if {@code period} is not positive
         */
        public Builder per(Duration period) {
            Preconditions.checkArgument(
                    !period.isNegative() && !period.isZero(),
                    "The period of a range must be positive, but got %s",
                    period);
            this.period = period;
            return this;
        }

        /**
         * Group the values of the last key into number ranges of {@code width}.
         *
         * @param width the width; must be positive
         * @return this {@link Builder}
         * @throws IllegalArgumentException if {@code width} is not a positive
         *             finite number
         */
        public Builder per(Number width) {
            Preconditions.checkArgument(decimal(width).signum() > 0,
                    "The width of a range must be a positive number, but got "
                            + "%s",
                    width);
            this.width = width;
            return this;
        }

    }

}
