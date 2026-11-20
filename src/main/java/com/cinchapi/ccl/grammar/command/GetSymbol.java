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

    /**
     * Whether the command names {@link #keys} as a collection.
     */
    private final boolean hasKeysInCollection;
    private final Long record;
    private final Collection<Long> records;
    private final TimestampSymbol timestamp;

    /**
     * Construct a new instance for getting from multiple records.
     *
     * @param keys the keys to get, or {@code null} to get every key
     * @param hasKeysInCollection whether the command names {@code keys} as a
     *        collection, as {@link #hasKeysInCollection()} defines it
     * @param records the records to get from
     * @param timestamp the timestamp for a historical get, or {@code null} to
     *        get the present state
     */
    public GetSymbol(@Nullable Collection<KeyTokenSymbol<?>> keys,
            boolean hasKeysInCollection, Collection<Long> records,
            @Nullable TimestampSymbol timestamp) {
        this.keys = keys;
        this.hasKeysInCollection = hasKeysInCollection;
        this.record = null;
        this.records = records;
        this.timestamp = timestamp;
    }

    /**
     * Construct a new instance for getting from a single record.
     *
     * @param keys the keys to get, or {@code null} to get every key
     * @param hasKeysInCollection whether the command names {@code keys} as a
     *        collection, as {@link #hasKeysInCollection()} defines it
     * @param record the record to get from
     * @param timestamp the timestamp for a historical get, or {@code null} to
     *        get the present state
     */
    public GetSymbol(@Nullable Collection<KeyTokenSymbol<?>> keys,
            boolean hasKeysInCollection, long record,
            @Nullable TimestampSymbol timestamp) {
        this.keys = keys;
        this.hasKeysInCollection = hasKeysInCollection;
        this.record = record;
        this.records = null;
        this.timestamp = timestamp;
    }

    /**
     * Construct a new instance for expression-based getting (WHERE clause).
     *
     * @param keys the keys to get, or {@code null} to get every key
     * @param hasKeysInCollection whether the command names {@code keys} as a
     *        collection, as {@link #hasKeysInCollection()} defines it
     * @param timestamp the timestamp for a historical get, or {@code null} to
     *        get the present state
     */
    public GetSymbol(@Nullable Collection<KeyTokenSymbol<?>> keys,
            boolean hasKeysInCollection, @Nullable TimestampSymbol timestamp) {
        this.keys = keys;
        this.hasKeysInCollection = hasKeysInCollection;
        this.timestamp = timestamp;
        this.record = null;
        this.records = null;
    }

    /**
     * Construct a new instance for getting from multiple records.
     * {@link #hasKeysInCollection()} is {@code true} when {@code keys} holds
     * more than one key.
     *
     * @param keys the keys to get, or {@code null} to get every key
     * @param records the records to get from
     * @param timestamp the timestamp for a historical get, or {@code null} to
     *        get the present state
     */
    public GetSymbol(@Nullable Collection<KeyTokenSymbol<?>> keys,
            Collection<Long> records, @Nullable TimestampSymbol timestamp) {
        this(keys, CommandSymbols.holdsMoreThanOne(keys), records, timestamp);
    }

    /**
     * Construct a new instance for getting from a single record.
     * {@link #hasKeysInCollection()} is {@code true} when {@code keys} holds
     * more than one key.
     *
     * @param keys the keys to get, or {@code null} to get every key
     * @param record the record to get from
     * @param timestamp the timestamp for a historical get, or {@code null} to
     *        get the present state
     */
    public GetSymbol(@Nullable Collection<KeyTokenSymbol<?>> keys, long record,
            @Nullable TimestampSymbol timestamp) {
        this(keys, CommandSymbols.holdsMoreThanOne(keys), record, timestamp);
    }

    /**
     * Construct a new instance for expression-based getting (WHERE clause).
     * {@link #hasKeysInCollection()} is {@code true} when {@code keys} holds
     * more than one key.
     *
     * @param keys the keys to get, or {@code null} to get every key
     * @param timestamp the timestamp for a historical get, or {@code null} to
     *        get the present state
     */
    public GetSymbol(@Nullable Collection<KeyTokenSymbol<?>> keys,
            @Nullable TimestampSymbol timestamp) {
        this(keys, CommandSymbols.holdsMoreThanOne(keys), timestamp);
    }

    /**
     * Return whether the command names its {@link #keys() keys} as a
     * collection. A command names a collection when it lists its keys in
     * brackets, even one key as in {@code get [name] from 1}, or lists more
     * than one key, as in {@code get name, age from 1}. A command that names
     * one key without brackets, as in {@code get name from 1}, or names no
     * keys, does not.
     *
     * @return {@code true} if the command names its keys as a collection
     */
    public boolean hasKeysInCollection() {
        return hasKeysInCollection;
    }

    /**
     * Return the single key if exactly one was specified, otherwise
     * {@code null}. This is a convenience accessor; prefer {@link #keys()}.
     */
    @Nullable
    public KeyTokenSymbol<?> key() {
        return keys != null && keys.size() == 1 ? keys.iterator().next() : null;
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

    @Override
    public String type() {
        return "GET";
    }
}
