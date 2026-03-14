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
import com.cinchapi.ccl.grammar.ExpressionSymbol;
import com.cinchapi.ccl.grammar.TimestampSymbol;
import com.cinchapi.ccl.grammar.OrderSymbol;
import com.cinchapi.ccl.grammar.PageSymbol;

/**
 * A {@link CommandSymbol} that represents a SELECT command.
 */
public class SelectSymbol implements CommandSymbol {
    private final Collection<KeyTokenSymbol<?>> keys;
    private final Long record;
    private final Collection<Long> records;
    private final TimestampSymbol timestamp;

    /**
     * Construct a new instance for selecting from a single record.
     */
    public SelectSymbol(@Nullable Collection<KeyTokenSymbol<?>> keys, long record,
                        @Nullable TimestampSymbol timestamp) {
        this.keys = keys;
        this.record = record;
        this.records = null;
        this.timestamp = timestamp;
    }

    /**
     * Construct a new instance for selecting from multiple records.
     */
    public SelectSymbol(@Nullable Collection<KeyTokenSymbol<?>> keys, Collection<Long> records,
                        @Nullable TimestampSymbol timestamp) {
        this.keys = keys;
        this.record = null;
        this.records = records;
        this.timestamp = timestamp;
    }

    /**
     * Construct a new instance for expression-based selecting.
     */
    public SelectSymbol(@Nullable Collection<KeyTokenSymbol<?>> keys,
                        @Nullable TimestampSymbol timestamp) {
        this.keys = keys;
        this.timestamp = timestamp;
        this.record = null;
        this.records = null;
    }

    @Override
    public String type() {
        return "SELECT";
    }

    /**
     * Return the keys to select.
     */
    @Nullable
    public Collection<KeyTokenSymbol<?>> keys() {
        return keys;
    }

    /**
     * Return the record identifier if selecting from a single record.
     */
    @Nullable
    public Long record() {
        return record;
    }

    /**
     * Return the record identifiers if selecting from multiple records.
     */
    @Nullable
    public Collection<Long> records() {
        return records;
    }

    /**
     * Return the timestamp for historical select.
     */
    @Nullable
    public TimestampSymbol timestamp() {
        return timestamp;
    }
}