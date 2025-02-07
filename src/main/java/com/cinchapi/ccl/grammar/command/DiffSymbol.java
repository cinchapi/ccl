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
 * A {@link CommandSymbol} that represents a DIFF command.
 */
public class DiffSymbol implements CommandSymbol {
    private final KeyTokenSymbol<?> key;
    private final Long record;
    private final TimestampSymbol start;
    private final TimestampSymbol end;

    /**
     * Construct a new instance for diffing changes to a record.
     *
     * @param record the record id
     * @param start the base timestamp for comparison
     */
    public DiffSymbol(long record, TimestampSymbol start) {
        this(null, record, start, null);
    }

    /**
     * Construct a new instance for diffing changes to a specific key in a record.
     *
     * @param key the field name
     * @param record the record id
     * @param start the base timestamp for comparison
     */
    public DiffSymbol(KeyTokenSymbol<?> key, long record, TimestampSymbol start) {
        this(key, record, start, null);
    }

    /**
     * Construct a new instance for diffing changes between two timestamps.
     *
     * @param key the field name, if diffing a specific field
     * @param record the record id
     * @param start the base timestamp for comparison
     * @param end the comparison timestamp
     */
    public DiffSymbol(@Nullable KeyTokenSymbol<?> key, long record,
                      TimestampSymbol start, @Nullable TimestampSymbol end) {
        this.key = key;
        this.record = record;
        this.start = start;
        this.end = end;
    }

    @Override
    public String type() {
        return "DIFF";
    }

    /**
     * Return the key to diff if specified.
     *
     * @return the field name or {@code null} if diffing entire record
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
     * Return the base timestamp for comparison.
     *
     * @return the start timestamp
     */
    public TimestampSymbol start() {
        return start;
    }

    /**
     * Return the comparison timestamp.
     *
     * @return the end timestamp if specified, otherwise {@code null}
     */
    @Nullable
    public TimestampSymbol end() {
        return end;
    }
}
