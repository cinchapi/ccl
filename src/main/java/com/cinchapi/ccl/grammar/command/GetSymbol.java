/*
 * Copyright (c) 2013-2017 Cinchapi Inc.
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
 * A {@link CommandSymbol} that represents a GET command.
 */
public class GetSymbol implements CommandSymbol {
    private final KeyTokenSymbol<?> key;
    private final Collection<KeyTokenSymbol<?>> keys;
    private final long record;
    private final Collection<Long> records;
    private final TimestampSymbol timestamp;

    /**
     * Construct a new instance for keys with optional where clause and timestamp.
     */
    public GetSymbol(Collection<KeyTokenSymbol<?>> keys, TimestampSymbol timestamp) {
        this(null, keys, -1, null, timestamp);
    }

    /**
     * Construct a new instance for single key and record.
     */
    public GetSymbol(KeyTokenSymbol<?> key, long record, TimestampSymbol timestamp) {
        this(key, null, record, null, timestamp);
    }

    /**
     * Construct a new instance for single key and multiple records.
     */
    public GetSymbol(KeyTokenSymbol<?> key, Collection<Long> records, TimestampSymbol timestamp) {
        this(key, null, -1, records, timestamp);
    }

    /**
     * Construct a new instance for multiple keys and single record.
     */
    public GetSymbol(Collection<KeyTokenSymbol<?>> keys, long record, TimestampSymbol timestamp) {
        this(null, keys, record, null, timestamp);
    }

    /**
     * Construct a new instance for multiple keys and records.
     */
    public GetSymbol(Collection<KeyTokenSymbol<?>> keys, Collection<Long> records, TimestampSymbol timestamp) {
        this(null, keys, -1, records, timestamp);
    }

    /**
     * Comprehensive constructor to handle all variations.
     */
    private GetSymbol(KeyTokenSymbol<?> key,
                      Collection<KeyTokenSymbol<?>> keys,
                      long record,
                      Collection<Long> records,
                      TimestampSymbol timestamp) {
        this.key = key;
        this.keys = keys;
        this.record = record;
        this.records = records;
        this.timestamp = timestamp;
    }

    @Override
    public String type() {
        return "GET";
    }

    @Nullable
    public KeyTokenSymbol<?> key() {
        return key;
    }

    @Nullable
    public Collection<KeyTokenSymbol<?>> keys() {
        return keys;
    }

    public long record() {
        return record;
    }

    @Nullable
    public Collection<Long> records() {
        return records;
    }

    @Nullable
    public TimestampSymbol timestamp() {
        return timestamp;
    }
}