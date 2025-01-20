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
import com.cinchapi.ccl.grammar.KeySymbol;
import com.cinchapi.ccl.grammar.TimestampSymbol;

/**
 * A {@link CommandSymbol} that represents a REVERT command.
 */
public class RevertSymbol implements CommandSymbol {
    private final KeySymbol key;
    private final Collection<KeySymbol> keys;
    private final long record;
    private final Collection<Long> records;
    private final TimestampSymbol timestamp;

    /**
     * Construct a new instance for single key and record.
     *
     * @param key the key to revert
     * @param record the record identifier
     * @param timestamp the timestamp to revert to
     */
    public RevertSymbol(KeySymbol key, long record, TimestampSymbol timestamp) {
        this(key, null, record, null, timestamp);
    }

    /**
     * Construct a new instance for multiple keys and single record.
     *
     * @param keys the keys to revert
     * @param record the record identifier
     * @param timestamp the timestamp to revert to
     */
    public RevertSymbol(Collection<KeySymbol> keys, long record, TimestampSymbol timestamp) {
        this(null, keys, record, null, timestamp);
    }

    /**
     * Construct a new instance for single key and multiple records.
     *
     * @param key the key to revert
     * @param records the record identifiers
     * @param timestamp the timestamp to revert to
     */
    public RevertSymbol(KeySymbol key, Collection<Long> records, TimestampSymbol timestamp) {
        this(key, null, -1, records, timestamp);
    }

    /**
     * Construct a new instance for multiple keys and records.
     *
     * @param keys the keys to revert
     * @param records the record identifiers
     * @param timestamp the timestamp to revert to
     */
    public RevertSymbol(Collection<KeySymbol> keys, Collection<Long> records,
                        TimestampSymbol timestamp) {
        this(null, keys, -1, records, timestamp);
    }

    private RevertSymbol(@Nullable KeySymbol key, @Nullable Collection<KeySymbol> keys,
                         long record, @Nullable Collection<Long> records, TimestampSymbol timestamp) {
        this.key = key;
        this.keys = keys;
        this.record = record;
        this.records = records;
        this.timestamp = timestamp;
    }

    @Override
    public String type() {
        return "REVERT";
    }

    /**
     * Return the key to revert, if operating on a single key.
     */
    @Nullable
    public KeySymbol key() {
        return key;
    }

    /**
     * Return the keys to revert, if operating on multiple keys.
     */
    @Nullable
    public Collection<KeySymbol> keys() {
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
     * Return the timestamp to revert to.
     */
    public TimestampSymbol timestamp() {
        return timestamp;
    }
}