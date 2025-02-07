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

import javax.annotation.Nullable;
import com.cinchapi.ccl.grammar.KeyTokenSymbol;
import com.cinchapi.ccl.grammar.ValueTokenSymbol;

import java.util.Collection;

/**
 * A {@link CommandSymbol} that represents a REMOVE command.
 */
public class RemoveSymbol implements CommandSymbol {
    private final KeyTokenSymbol<?> key;
    private final ValueTokenSymbol<?> value;
    private final long record;
    private final Collection<Long> records;
    private final boolean isMultiRecord;

    /**
     * Construct a new instance for single record removal.
     *
     * @param key the key to remove
     * @param value the value to remove, if specified
     * @param record the record identifier
     */
    public RemoveSymbol(KeyTokenSymbol<?> key, @Nullable ValueTokenSymbol<?> value, long record) {
        this.key = key;
        this.value = value;
        this.record = record;
        this.records = null;
        this.isMultiRecord = false;
    }

    /**
     * Construct a new instance for multiple record removal.
     *
     * @param key the key to remove
     * @param value the value to remove, if specified
     * @param records a collection of record identifiers
     */
    public RemoveSymbol(KeyTokenSymbol<?> key, @Nullable ValueTokenSymbol<?> value, Collection<Long> records) {
        this.key = key;
        this.value = value;
        this.record = -1;
        this.records = records;
        this.isMultiRecord = true;
    }

    @Override
    public String type() {
        return "REMOVE";
    }

    /**
     * Return the key to remove.
     *
     * @return the key symbol
     */
    public KeyTokenSymbol<?> key() {
        return key;
    }

    /**
     * Return the value to remove, if specified.
     *
     * @return the value symbol or null if not specified
     */
    @Nullable
    public ValueTokenSymbol<?> value() {
        return value;
    }

    /**
     * Return the record identifier for single record operations.
     * Returns -1 if this is a multi-record operation.
     *
     * @return the record id
     */
    public long record() {
        return record;
    }

    /**
     * Return the collection of record identifiers for multi-record operations.
     * Returns null if this is a single record operation.
     *
     * @return the collection of record ids
     */
    public Collection<Long> records() {
        return records;
    }

    /**
     * Indicates whether this is a multi-record operation.
     *
     * @return true if operating on multiple records, false otherwise
     */
    public boolean isMultiRecord() {
        return isMultiRecord;
    }
}