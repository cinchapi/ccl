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
import javax.annotation.Nullable;
import com.cinchapi.ccl.grammar.KeyTokenSymbol;
import com.cinchapi.ccl.grammar.TimestampSymbol;

/**
 * A {@link CommandSymbol} that represents a CALCULATE command.
 */
public class CalculateSymbol implements CommandSymbol {
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
     */
    public CalculateSymbol(String function, KeyTokenSymbol<?> key,
                           @Nullable Collection<Long> records,
                           @Nullable TimestampSymbol timestamp) {
        this.function = function;
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
