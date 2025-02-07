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
import com.cinchapi.ccl.grammar.ValueTokenSymbol;

/**
 * A {@link CommandSymbol} that represents an ADD command.
 */
public class AddSymbol implements CommandSymbol {
    private final KeyTokenSymbol<?> key;
    private final ValueTokenSymbol<?> value;
    private final Long record;
    private final Collection<Long> records;

    /**
     * Construct a new instance for adding to a new record.
     *
     * @param key the key to add
     * @param value the value to add
     */
    public AddSymbol(KeyTokenSymbol<?> key, ValueTokenSymbol<?> value) {
        this.key = key;
        this.value = value;
        this.record = null;
        this.records = null;
    }

    /**
     * Construct a new instance for adding to a specific record.
     *
     * @param key the key to add
     * @param value the value to add
     * @param record the record identifier
     */
    public AddSymbol(KeyTokenSymbol<?> key, ValueTokenSymbol<?> value, long record) {
        this.key = key;
        this.value = value;
        this.record = record;
        this.records = null;
    }

    /**
     * Construct a new instance for adding to multiple records.
     *
     * @param key the key to add
     * @param value the value to add
     * @param records collection of record identifiers
     */
    public AddSymbol(KeyTokenSymbol<?> key, ValueTokenSymbol<?> value, Collection<Long> records) {
        this.key = key;
        this.value = value;
        this.record = null;
        this.records = records;
    }

    @Override
    public String type() {
        return "ADD";
    }

    /**
     * Return the key to add.
     */
    public KeyTokenSymbol<?> key() {
        return key;
    }

    /**
     * Return the value to add.
     */
    public ValueTokenSymbol<?> value() {
        return value;
    }

    /**
     * Return the record identifier if adding to a single record.
     * Returns null if adding to a new record or multiple records.
     */
    @Nullable
    public Long record() {
        return record;
    }

    /**
     * Return the collection of record identifiers if adding to multiple records.
     * Returns null if adding to a new record or single record.
     */
    @Nullable
    public Collection<Long> records() {
        return records;
    }
}