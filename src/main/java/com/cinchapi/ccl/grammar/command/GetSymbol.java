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
 * <p>
 * When {@link #keys()} returns {@code null}, all keys are implied
 * (equivalent to a SELECT without explicit keys).
 */
public class GetSymbol implements CommandSymbol {
    private final Collection<KeyTokenSymbol<?>> keys;
    private final Long record;
    private final Collection<Long> records;
    private final TimestampSymbol timestamp;

    /**
     * Construct a new instance for getting from a single record.
     */
    public GetSymbol(@Nullable Collection<KeyTokenSymbol<?>> keys, long record,
                     @Nullable TimestampSymbol timestamp) {
        this.keys = keys;
        this.record = record;
        this.records = null;
        this.timestamp = timestamp;
    }

    /**
     * Construct a new instance for getting from multiple records.
     */
    public GetSymbol(@Nullable Collection<KeyTokenSymbol<?>> keys, Collection<Long> records,
                     @Nullable TimestampSymbol timestamp) {
        this.keys = keys;
        this.record = null;
        this.records = records;
        this.timestamp = timestamp;
    }

    /**
     * Construct a new instance for expression-based getting (WHERE clause).
     */
    public GetSymbol(@Nullable Collection<KeyTokenSymbol<?>> keys,
                     @Nullable TimestampSymbol timestamp) {
        this.keys = keys;
        this.timestamp = timestamp;
        this.record = null;
        this.records = null;
    }

    @Override
    public String type() {
        return "GET";
    }

    /**
     * Return the single key if exactly one was specified, otherwise
     * {@code null}. This is a convenience accessor; prefer {@link #keys()}.
     */
    @Nullable
    public KeyTokenSymbol<?> key() {
        return keys != null && keys.size() == 1
                ? keys.iterator().next() : null;
    }

    /**
     * Return the keys to get, or {@code null} if all keys are implied.
     */
    @Nullable
    public Collection<KeyTokenSymbol<?>> keys() {
        return keys;
    }

    /**
     * Return the record identifier if getting from a single record.
     */
    @Nullable
    public Long record() {
        return record;
    }

    /**
     * Return the record identifiers if getting from multiple records.
     */
    @Nullable
    public Collection<Long> records() {
        return records;
    }

    /**
     * Return the timestamp for historical get.
     */
    @Nullable
    public TimestampSymbol timestamp() {
        return timestamp;
    }
}
