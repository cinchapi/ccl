/*
 * Copyright (c) 2013-2024 Cinchapi Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package com.cinchapi.ccl.grammar.command;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import com.cinchapi.ccl.grammar.KeyTokenSymbol;
import com.cinchapi.ccl.grammar.TimestampSymbol;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

/**
 * A {@link CommandSymbol} that represents a CALCULATE command.
 * <p>
 * The command applies one or more {@link CalculationSymbol calculations} to
 * every record, to a list of records, or to the records that match a condition,
 * at the present or at a timestamp. A statement with a
 * {@link GroupSymbol group} calculates a result for each group, and only such a
 * statement may list several calculations. The condition is a child of the
 * {@link com.cinchapi.ccl.syntax.CommandTree CommandTree} whose root is this
 * symbol.
 * </p>
 * <p>
 * Two symbols are equal when their calculations, timestamp and group are equal
 * and they list the same records in the same order. The
 * {@link #toString() text} of a symbol is its statement without the condition,
 * which parses to an equal symbol.
 * </p>
 *
 * @author Jeff Nelson
 */
public class CalculateSymbol implements CommandSymbol {

    /**
     * Throw an {@link IllegalArgumentException} unless {@code calculation} is
     * the one calculation among {@code calculations} that its function names.
     *
     * @param calculation the calculation that a clause refers to
     * @param calculations the calculations of the statement
     * @throws IllegalArgumentException if the function of {@code calculation}
     *             names none of {@code calculations}, more than one of them, or
     *             one that is not {@code calculation}
     */
    private static void requireNamedByFunction(CalculationSymbol calculation,
            List<CalculationSymbol> calculations) {
        Preconditions.checkArgument(
                CalculationSymbol.resolve(calculation.function(), calculations)
                        .equals(calculation),
                "%s is not one of the calculations %s", calculation,
                calculations);
    }

    /**
     * The calculations, as the statement lists them.
     */
    private final List<CalculationSymbol> calculations;

    /**
     * The records to calculate across, or {@code null}.
     */
    @Nullable
    private final Collection<Long> records;

    /**
     * The timestamp to calculate at, or {@code null}.
     */
    @Nullable
    private final TimestampSymbol timestamp;

    /**
     * The group, or {@code null}.
     */
    @Nullable
    private final GroupSymbol group;

    /**
     * Construct a new instance.
     *
     * @param calculations the calculations, as the statement lists them; must
     *            not be empty, and must have one element unless {@code group}
     *            is not {@code null}
     * @param records the records to include, or {@code null} to calculate
     *            across all data or a condition
     * @param timestamp the timestamp to calculate at, or {@code null} for the
     *            present
     * @param group the group, or {@code null} for one result; its having clause
     *            and order must each name one of {@code calculations} by
     *            function, as {@link CalculationSymbol#resolve(String, List)}
     *            describes
     * @throws IllegalArgumentException if {@code calculations} is empty, if it
     *             has several elements and {@code group} is {@code null}, or if
     *             the having clause or order of {@code group} does not name one
     *             of {@code calculations}
     */
    public CalculateSymbol(List<CalculationSymbol> calculations,
            @Nullable Collection<Long> records,
            @Nullable TimestampSymbol timestamp, @Nullable GroupSymbol group) {
        Preconditions.checkArgument(!calculations.isEmpty(),
                "A calculate statement needs at least one calculation");
        Preconditions.checkArgument(calculations.size() == 1 || group != null,
                "Several calculations need a group by clause, but got %s",
                calculations);
        if(group != null && group.having() != null) {
            requireNamedByFunction(group.having().calculation(), calculations);
        }
        if(group != null && group.order() != null) {
            requireNamedByFunction(group.order().calculation(), calculations);
        }
        this.calculations = ImmutableList.copyOf(calculations);
        this.records = records;
        this.timestamp = timestamp;
        this.group = group;
    }

    /**
     * Construct a new instance for one ungrouped calculation across specific
     * records.
     *
     * @param function the name of the function, as
     *            {@link CalculationSymbol#function()} describes; must not be
     *            {@code percentile}
     * @param key the key to calculate on
     * @param records the records to include
     * @param timestamp optional timestamp for historical calculation
     * @throws IllegalArgumentException if the function is not a valid
     *             calculation function or is {@code percentile}
     */
    public CalculateSymbol(String function, KeyTokenSymbol<?> key,
            @Nullable Collection<Long> records,
            @Nullable TimestampSymbol timestamp) {
        this(ImmutableList.of(new CalculationSymbol(function, key)), records,
                timestamp, null);
    }

    /**
     * Construct a new instance for one ungrouped calculation across all data or
     * with a condition (condition is captured in the AST, not in this symbol).
     *
     * @param function the name of the function, as
     *            {@link CalculationSymbol#function()} describes; must not be
     *            {@code percentile}
     * @param key the key to calculate on
     * @param timestamp optional timestamp for historical calculation
     * @throws IllegalArgumentException if the function is not a valid
     *             calculation function or is {@code percentile}
     */
    public CalculateSymbol(String function, KeyTokenSymbol<?> key,
            @Nullable TimestampSymbol timestamp) {
        this(function, key, null, timestamp);
    }

    /**
     * Return the calculations, as the statement lists them.
     *
     * @return an unmodifiable {@link List} of at least one
     *         {@link CalculationSymbol}
     */
    public List<CalculationSymbol> calculations() {
        return calculations;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof CalculateSymbol) {
            CalculateSymbol other = (CalculateSymbol) obj;
            return calculations.equals(other.calculations)
                    && Objects.equals(recordList(), other.recordList())
                    && Objects.equals(timestamp, other.timestamp)
                    && Objects.equals(group, other.group);
        }
        else {
            return false;
        }
    }

    /**
     * Return the canonical name of the function of the first calculation, as
     * {@link CalculationSymbol#function()} describes.
     *
     * @return the function name
     */
    public String function() {
        return calculations.get(0).function();
    }

    /**
     * Return the group, which calculates a result for each group of records.
     *
     * @return the {@link GroupSymbol}, or {@code null} if the statement
     *         calculates one result
     */
    @Nullable
    public GroupSymbol group() {
        return group;
    }

    @Override
    public int hashCode() {
        return Objects.hash(calculations, recordList(), timestamp, group);
    }

    /**
     * Return the key of the first calculation.
     *
     * @return the key
     */
    public KeyTokenSymbol<?> key() {
        return calculations.get(0).key();
    }

    /**
     * Return the records to include, or {@code null} if calculating across
     * all data or a condition.
     *
     * @return the records, or {@code null}
     */
    @Nullable
    public Collection<Long> records() {
        return records;
    }

    /**
     * Return the timestamp for historical calculation.
     *
     * @return the timestamp, or {@code null} for the present
     */
    @Nullable
    public TimestampSymbol timestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        StringBuilder text = new StringBuilder("calculate ");
        text.append(calculations.stream().map(Object::toString)
                .collect(Collectors.joining(", ")));
        if(records != null) {
            text.append(" in [").append(records.stream().map(Object::toString)
                    .collect(Collectors.joining(", "))).append(']');
        }
        if(timestamp != null) {
            text.append(' ').append(timestamp);
        }
        if(group != null) {
            text.append(' ').append(group);
        }
        return text.toString();
    }

    @Override
    public String type() {
        return "CALCULATE";
    }

    /**
     * Return the records as a {@link List}, in the order the statement lists
     * them.
     *
     * @return the records, or {@code null} if the statement has none
     */
    @Nullable
    private List<Long> recordList() {
        return records != null ? ImmutableList.copyOf(records) : null;
    }

}
