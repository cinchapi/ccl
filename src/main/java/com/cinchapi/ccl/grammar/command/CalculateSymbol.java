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
import java.util.Set;
import javax.annotation.Nullable;
import com.cinchapi.ccl.grammar.KeyTokenSymbol;
import com.cinchapi.ccl.grammar.TimestampSymbol;
import com.google.common.collect.ImmutableSet;

/**
 * A {@link CommandSymbol} that represents a CALCULATE command.
 */
public class CalculateSymbol implements CommandSymbol {

    /**
     * The set of valid calculation function names.
     */
    private static final Set<String> VALID_FUNCTIONS = ImmutableSet.of(
            "sum", "average", "count", "min", "max");

    private final String function;
    private final KeyTokenSymbol<?> key;
    private final Collection<Long> records;
    private final TimestampSymbol timestamp;

    /**
     * Construct a new instance for calculating across specific records.
     *
     * @param function the calculation function (e.g., sum, average, count, min, max)
     * @param key the key to calculate on
     * @param records the records to include
     * @param timestamp optional timestamp for historical calculation
     * @throws IllegalArgumentException if the function is not a valid
     *             calculation function
     */
    public CalculateSymbol(String function, KeyTokenSymbol<?> key,
                           @Nullable Collection<Long> records,
                           @Nullable TimestampSymbol timestamp) {
        if(!VALID_FUNCTIONS.contains(function.toLowerCase())) {
            throw new IllegalArgumentException(
                    "Invalid calculation function '" + function
                            + "'. Must be one of: " + VALID_FUNCTIONS);
        }
        this.function = function.toLowerCase();
        this.key = key;
        this.records = records;
        this.timestamp = timestamp;
    }

    /**
     * Construct a new instance for calculating across all data or with
     * a condition (condition is captured in the AST, not in this symbol).
     *
     * @param function the calculation function (e.g., sum, average, count, min, max)
     * @param key the key to calculate on
     * @param timestamp optional timestamp for historical calculation
     */
    public CalculateSymbol(String function, KeyTokenSymbol<?> key,
                           @Nullable TimestampSymbol timestamp) {
        this(function, key, null, timestamp);
    }

    @Override
    public String type() {
        return "CALCULATE";
    }

    /**
     * Return the calculation function name.
     */
    public String function() {
        return function;
    }

    /**
     * Return the key to calculate on.
     */
    public KeyTokenSymbol<?> key() {
        return key;
    }

    /**
     * Return the records to include, or {@code null} if calculating across
     * all data or a condition.
     */
    @Nullable
    public Collection<Long> records() {
        return records;
    }

    /**
     * Return the timestamp for historical calculation.
     */
    @Nullable
    public TimestampSymbol timestamp() {
        return timestamp;
    }
}
