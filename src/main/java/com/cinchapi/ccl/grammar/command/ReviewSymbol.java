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

import javax.annotation.Nullable;
import com.cinchapi.ccl.grammar.KeyTokenSymbol;
import com.cinchapi.ccl.grammar.TimestampSymbol;

/**
 * A {@link CommandSymbol} that represents a REVIEW command. T
 */
public class ReviewSymbol implements CommandSymbol {
    private final KeyTokenSymbol<?> key;
    private final long record;
    private final TimestampSymbol start;
    private final TimestampSymbol end;

    /**
     * Construct a new instance for reviewing all changes to a record.
     *
     * @param record the record id
     */
    public ReviewSymbol(long record) {
        this(null, record, null, null);
    }

    /**
     * Construct a new instance for reviewing all changes to a specific field.
     *
     * @param key the field name
     * @param record the record id
     */
    public ReviewSymbol(KeyTokenSymbol<?> key, long record) {
        this(key, record, null, null);
    }

    /**
     * Construct a new instance for reviewing changes after a start time.
     *
     * @param key the field name if reviewing specific field
     * @param record the record id
     * @param start the inclusive start timestamp
     */
    public ReviewSymbol(@Nullable KeyTokenSymbol<?> key, long record,
                        TimestampSymbol start) {
        this(key, record, start, null);
    }

    /**
     * Construct a new instance for reviewing changes in a time range.
     *
     * @param key the field name if reviewing specific field
     * @param record the record id
     * @param start the inclusive start timestamp
     * @param end the non-inclusive end timestamp
     */
    public ReviewSymbol(@Nullable KeyTokenSymbol<?> key, long record,
                        @Nullable TimestampSymbol start, @Nullable TimestampSymbol end) {
        this.key = key;
        this.record = record;
        this.start = start;
        this.end = end;
    }

    @Override
    public String type() {
        return "REVIEW";
    }

    /**
     * Return the key to review if specified.
     *
     * @return the field name or {@code null} if reviewing entire record
     */
    @Nullable
    public KeyTokenSymbol<?> key() {
        return key;
    }

    /**
     * Return the record identifier.
     *
     * @return the record id
     */
    public long record() {
        return record;
    }

    /**
     * Return the start timestamp if specified.
     *
     * @return the inclusive start timestamp or {@code null} if reviewing from beginning
     */
    @Nullable
    public TimestampSymbol start() {
        return start;
    }

    /**
     * Return the end timestamp if specified.
     *
     * @return the non-inclusive end timestamp or {@code null} if reviewing to present
     */
    @Nullable
    public TimestampSymbol end() {
        return end;
    }
}