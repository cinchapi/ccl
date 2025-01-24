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
 * A {@link CommandSymbol} that represents a FETCH command.
 */
public class FetchSymbol implements CommandSymbol {
    private final KeyTokenSymbol<?> key;
    private final Collection<KeyTokenSymbol<?>> keys;
    private final long record;
    private final Collection<Long> records;
    private final TimestampSymbol timestamp;

    /**
     * Construct a new instance for single key and record.
     */
    public FetchSymbol(KeyTokenSymbol<?> key, long record, @Nullable TimestampSymbol timestamp) {
        this(key, null, record, null, timestamp);
    }

    /**
     * Construct a new instance for multiple keys and single record.
     */
    public FetchSymbol(Collection<KeyTokenSymbol<?>> keys, long record, @Nullable TimestampSymbol timestamp) {
        this(null, keys, record, null, timestamp);
    }

    /**
     * Construct a new instance for single key and multiple records.
     */
    public FetchSymbol(KeyTokenSymbol<?> key, Collection<Long> records, @Nullable TimestampSymbol timestamp) {
        this(key, null, -1, records, timestamp);
    }

    /**
     * Construct a new instance for multiple keys and records.
     */
    public FetchSymbol(Collection<KeyTokenSymbol<?>> keys, Collection<Long> records,
                       @Nullable TimestampSymbol timestamp) {
        this(null, keys, -1, records, timestamp);
    }

    private FetchSymbol(@Nullable KeyTokenSymbol<?> key, @Nullable Collection<KeyTokenSymbol<?>> keys,
                        long record, @Nullable Collection<Long> records, @Nullable TimestampSymbol timestamp) {
        this.key = key;
        this.keys = keys;
        this.record = record;
        this.records = records;
        this.timestamp = timestamp;
    }

    @Override
    public String type() {
        return "FETCH";
    }

    /**
     * Return the key to fetch, if operating on a single key.
     */
    @Nullable
    public KeyTokenSymbol<?> key() {
        return key;
    }

    /**
     * Return the keys to fetch, if operating on multiple keys.
     */
    @Nullable
    public Collection<KeyTokenSymbol<?>> keys() {
        return keys;
    }

    /**
     * Return the record identifier, if operating on a single record.
     */
    public long record() {
        return record;
    }

    /**
     * Return the record identifiers, if operating on multiple records.
     */
    @Nullable
    public Collection<Long> records() {
        return records;
    }

    /**
     * Return the timestamp for historical fetch.
     */
    @Nullable
    public TimestampSymbol timestamp() {
        return timestamp;
    }
}