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

/**
 * A {@link CommandSymbol} that represents a CLEAR command.
 */
public class ClearSymbol implements CommandSymbol {
    private final KeyTokenSymbol<?> key;
    private final Collection<KeyTokenSymbol<?>> keys;
    private final long record;
    private final Collection<Long> records;

    /**
     * Construct a new instance for single key and record.
     */
    public ClearSymbol(KeyTokenSymbol<?> key, long record) {
        this(key, null, record, null);
    }

    /**
     * Construct a new instance for multiple keys and single record.
     */
    public ClearSymbol(Collection<KeyTokenSymbol<?>> keys, long record) {
        this(null, keys, record, null);
    }

    /**
     * Construct a new instance for single key and multiple records.
     */
    public ClearSymbol(KeyTokenSymbol<?> key, Collection<Long> records) {
        this(key, null, -1, records);
    }

    /**
     * Construct a new instance for multiple keys and records.
     */
    public ClearSymbol(Collection<KeyTokenSymbol<?>> keys, Collection<Long> records) {
        this(null, keys, -1, records);
    }

    private ClearSymbol(@Nullable KeyTokenSymbol<?> key, @Nullable Collection<KeyTokenSymbol<?>> keys,
                        long record, @Nullable Collection<Long> records) {
        this.key = key;
        this.keys = keys;
        this.record = record;
        this.records = records;
    }

    @Override
    public String type() {
        return "CLEAR";
    }

    /**
     * Return the key to clear, if operating on a single key.
     */
    @Nullable
    public KeyTokenSymbol<?> key() {
        return key;
    }

    /**
     * Return the keys to clear, if operating on multiple keys.
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
}