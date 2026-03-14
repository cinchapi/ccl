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
import com.cinchapi.ccl.grammar.ValueTokenSymbol;

/**
 * A {@link CommandSymbol} that represents a SET command.
 */
public class SetSymbol implements CommandSymbol {
    private final KeyTokenSymbol<?> key;
    private final ValueTokenSymbol<?> value;
    private final long record;
    private final Collection<Long> records;

    /**
     * Construct a new instance for setting in a single record.
     *
     * @param key the key to set
     * @param value the value to set
     * @param record the record identifier
     */
    public SetSymbol(KeyTokenSymbol<?> key, ValueTokenSymbol<?> value, long record) {
        this.key = key;
        this.value = value;
        this.record = record;
        this.records = null;
    }

    /**
     * Construct a new instance for setting in multiple records.
     *
     * @param key the key to set
     * @param value the value to set
     * @param records the record identifiers
     */
    public SetSymbol(KeyTokenSymbol<?> key, ValueTokenSymbol<?> value, Collection<Long> records) {
        this.key = key;
        this.value = value;
        this.record = -1;
        this.records = records;
    }

    @Override
    public String type() {
        return "SET";
    }

    /**
     * Return the key to set.
     */
    public KeyTokenSymbol<?> key() {
        return key;
    }

    /**
     * Return the value to set.
     */
    public ValueTokenSymbol<?> value() {
        return value;
    }

    /**
     * Return the record identifier.
     */
    public long record() {
        return record;
    }

    /**
     * Return the record identifiers if setting in multiple records.
     *
     * @return the record ids or {@code null} if using single record
     */
    @Nullable
    public Collection<Long> records() {
        return records;
    }
}
