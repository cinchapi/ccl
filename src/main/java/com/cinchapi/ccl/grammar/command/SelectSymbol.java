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
import com.cinchapi.ccl.grammar.TimestampSymbol;

/**
 * A {@link CommandSymbol} that represents a SELECT command.
 */
public class SelectSymbol implements CommandSymbol {
    private final Collection<KeyTokenSymbol<?>> keys;

    /**
     * Whether the command names {@link #keys} as a collection.
     */
    private final boolean hasKeysInCollection;
    private final Long record;
    private final Collection<Long> records;
    private final TimestampSymbol timestamp;

    /**
     * Construct a new instance for selecting from multiple records.
     *
     * @param keys the keys to select, or {@code null} to select every key
     * @param hasKeysInCollection whether the command names {@code keys} as a
     *        collection, as {@link #hasKeysInCollection()} defines it
     * @param records the records to select from
     * @param timestamp the timestamp for a historical select, or {@code null}
     *        to select the present state
     */
    public SelectSymbol(@Nullable Collection<KeyTokenSymbol<?>> keys,
            boolean hasKeysInCollection, Collection<Long> records,
            @Nullable TimestampSymbol timestamp) {
        this.keys = keys;
        this.hasKeysInCollection = hasKeysInCollection;
        this.record = null;
        this.records = records;
        this.timestamp = timestamp;
    }

    /**
     * Construct a new instance for selecting from a single record.
     *
     * @param keys the keys to select, or {@code null} to select every key
     * @param hasKeysInCollection whether the command names {@code keys} as a
     *        collection, as {@link #hasKeysInCollection()} defines it
     * @param record the record to select from
     * @param timestamp the timestamp for a historical select, or {@code null}
     *        to select the present state
     */
    public SelectSymbol(@Nullable Collection<KeyTokenSymbol<?>> keys,
            boolean hasKeysInCollection, long record,
            @Nullable TimestampSymbol timestamp) {
        this.keys = keys;
        this.hasKeysInCollection = hasKeysInCollection;
        this.record = record;
        this.records = null;
        this.timestamp = timestamp;
    }

    /**
     * Construct a new instance for expression-based selecting.
     *
     * @param keys the keys to select, or {@code null} to select every key
     * @param hasKeysInCollection whether the command names {@code keys} as a
     *        collection, as {@link #hasKeysInCollection()} defines it
     * @param timestamp the timestamp for a historical select, or {@code null}
     *        to select the present state
     */
    public SelectSymbol(@Nullable Collection<KeyTokenSymbol<?>> keys,
            boolean hasKeysInCollection, @Nullable TimestampSymbol timestamp) {
        this.keys = keys;
        this.hasKeysInCollection = hasKeysInCollection;
        this.timestamp = timestamp;
        this.record = null;
        this.records = null;
    }

    /**
     * Construct a new instance for selecting from multiple records.
     * {@link #hasKeysInCollection()} is {@code true} when {@code keys} holds
     * more than one key.
     *
     * @param keys the keys to select, or {@code null} to select every key
     * @param records the records to select from
     * @param timestamp the timestamp for a historical select, or {@code null}
     *        to select the present state
     */
    public SelectSymbol(@Nullable Collection<KeyTokenSymbol<?>> keys,
            Collection<Long> records, @Nullable TimestampSymbol timestamp) {
        this(keys, CommandSymbols.holdsMoreThanOne(keys), records, timestamp);
    }

    /**
     * Construct a new instance for selecting from a single record.
     * {@link #hasKeysInCollection()} is {@code true} when {@code keys} holds
     * more than one key.
     *
     * @param keys the keys to select, or {@code null} to select every key
     * @param record the record to select from
     * @param timestamp the timestamp for a historical select, or {@code null}
     *        to select the present state
     */
    public SelectSymbol(@Nullable Collection<KeyTokenSymbol<?>> keys,
            long record, @Nullable TimestampSymbol timestamp) {
        this(keys, CommandSymbols.holdsMoreThanOne(keys), record, timestamp);
    }

    /**
     * Construct a new instance for expression-based selecting.
     * {@link #hasKeysInCollection()} is {@code true} when {@code keys} holds
     * more than one key.
     *
     * @param keys the keys to select, or {@code null} to select every key
     * @param timestamp the timestamp for a historical select, or {@code null}
     *        to select the present state
     */
    public SelectSymbol(@Nullable Collection<KeyTokenSymbol<?>> keys,
            @Nullable TimestampSymbol timestamp) {
        this(keys, CommandSymbols.holdsMoreThanOne(keys), timestamp);
    }

    /**
     * Return whether the command names its {@link #keys() keys} as a
     * collection. A command names a collection when it lists its keys in
     * brackets, even one key as in {@code select [name] from 1}, or lists more
     * than one key, as in {@code select name, age from 1}. A command that names
     * one key without brackets, as in {@code select name from 1}, or names no
     * keys, does not.
     *
     * @return {@code true} if the command names its keys as a collection
     */
    public boolean hasKeysInCollection() {
        return hasKeysInCollection;
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

    @Override
    public String type() {
        return "SELECT";
    }
}
