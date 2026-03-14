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
import com.cinchapi.ccl.grammar.TimestampSymbol;

/**
 * A {@link CommandSymbol} that represents a JSONIFY command.
 */
public class JsonifySymbol implements CommandSymbol {
    private final Long record;
    private final Collection<Long> records;
    private final TimestampSymbol timestamp;
    private final boolean identifier;

    /**
     * Construct a new instance for jsonifying a single record.
     *
     * @param record the record id
     * @param identifier whether to include a special key for record id
     */
    public JsonifySymbol(long record, boolean identifier) {
        this(record, null, null, identifier);
    }

    /**
     * Construct a new instance for jsonifying multiple records.
     *
     * @param records collection of record ids
     * @param identifier whether to include a special key for record ids
     */
    public JsonifySymbol(Collection<Long> records, boolean identifier) {
        this(null, records, null, identifier);
    }

    /**
     * Construct a new instance for jsonifying historical data.
     *
     * @param record single record id or {@code null} if using multiple records
     * @param records collection of record ids or {@code null} if using single record
     * @param timestamp the historical instant to use for data retrieval
     * @param identifier whether to include a special key for record id(s)
     */
    public JsonifySymbol(@Nullable Long record, @Nullable Collection<Long> records,
                         @Nullable TimestampSymbol timestamp, boolean identifier) {
        this.record = record;
        this.records = records;
        this.timestamp = timestamp;
        this.identifier = identifier;
    }

    @Override
    public String type() {
        return "JSONIFY";
    }

    /**
     * Return the single record identifier if specified.
     *
     * @return the record id or {@code null} if using multiple records
     */
    @Nullable
    public Long record() {
        return record;
    }

    /**
     * Return the collection of record identifiers if specified.
     *
     * @return the record ids or {@code null} if using single record
     */
    @Nullable
    public Collection<Long> records() {
        return records;
    }

    /**
     * Return the timestamp for historical data retrieval.
     *
     * @return the timestamp if specified, otherwise {@code null}
     */
    @Nullable
    public TimestampSymbol timestamp() {
        return timestamp;
    }

    /**
     * Return whether to include record identifiers in JSON output.
     *
     * @return {@code true} if identifiers should be included
     */
    public boolean includeIdentifier() {
        return identifier;
    }
}