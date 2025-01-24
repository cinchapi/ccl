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

import com.cinchapi.ccl.grammar.KeyTokenSymbol;
import com.cinchapi.ccl.grammar.TimestampSymbol;

import javax.annotation.Nullable;

/**
 * A {@link CommandSymbol} that represents a SELECT command.
 */
public class SelectSymbol implements CommandSymbol {
    private final KeyTokenSymbol<?> key;
    private final long record;
    private final TimestampSymbol timestamp;

    /**
     * Construct a new instance.
     *
     * @param key the key to select
     * @param record the record identifier
     * @param timestamp optional timestamp for historical queries
     */
    public SelectSymbol(KeyTokenSymbol<?> key, long record, @Nullable TimestampSymbol timestamp) {
        this.key = key;
        this.record = record;
        this.timestamp = timestamp;
    }

    @Override
    public String type() {
        return "SELECT";
    }

    /**
     * Return the key to select.
     */
    public KeyTokenSymbol<?> key() {
        return key;
    }

    /**
     * Return the record identifier.
     */
    public long record() {
        return record;
    }

    /**
     * Return the timestamp for historical queries.
     */
    @Nullable
    public TimestampSymbol timestamp() {
        return timestamp;
    }
}