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
 * A {@link CommandSymbol} that represents a NAVIGATE command.
 */
public class NavigateSymbol implements CommandSymbol {
    private final Collection<KeyTokenSymbol<?>> keys;

    /**
     * Whether the command names {@link #keys} as a collection.
     */
    private final boolean hasKeysInCollection;
    private final Long record;
    private final Collection<Long> records;
    private final String ccl;
    private final Object criteria;
    private final TimestampSymbol timestamp;

    /**
     * Construct a new instance for navigating from multiple records.
     *
     * @param keys navigation keys
     * @param hasKeysInCollection whether the command names {@code keys} as a
     *        collection, as {@link #hasKeysInCollection()} defines it
     * @param records the starting record ids
     * @param timestamp optional timestamp for historical navigation
     */
    public NavigateSymbol(Collection<KeyTokenSymbol<?>> keys,
            boolean hasKeysInCollection, Collection<Long> records,
            @Nullable TimestampSymbol timestamp) {
        this(keys, hasKeysInCollection, null, records, null, null, timestamp);
    }

    /**
     * Construct a new instance for navigating from a single record.
     *
     * @param keys navigation keys
     * @param hasKeysInCollection whether the command names {@code keys} as a
     *        collection, as {@link #hasKeysInCollection()} defines it
     * @param record the starting record id
     * @param timestamp optional timestamp for historical navigation
     */
    public NavigateSymbol(Collection<KeyTokenSymbol<?>> keys,
            boolean hasKeysInCollection, long record,
            @Nullable TimestampSymbol timestamp) {
        this(keys, hasKeysInCollection, record, null, null, null, timestamp);
    }

    /**
     * Construct a new instance for navigating from records matching criteria.
     *
     * @param keys navigation keys
     * @param hasKeysInCollection whether the command names {@code keys} as a
     *        collection, as {@link #hasKeysInCollection()} defines it
     * @param criteria the criteria for selecting starting records
     * @param timestamp optional timestamp for historical navigation
     */
    public NavigateSymbol(Collection<KeyTokenSymbol<?>> keys,
            boolean hasKeysInCollection, Object criteria,
            @Nullable TimestampSymbol timestamp) {
        this(keys, hasKeysInCollection, null, null, null, criteria, timestamp);
    }

    /**
     * Construct a new instance for navigating from records matching CCL.
     *
     * @param keys navigation keys
     * @param hasKeysInCollection whether the command names {@code keys} as a
     *        collection, as {@link #hasKeysInCollection()} defines it
     * @param ccl the CCL for selecting starting records
     * @param timestamp optional timestamp for historical navigation
     */
    public NavigateSymbol(Collection<KeyTokenSymbol<?>> keys,
            boolean hasKeysInCollection, String ccl,
            @Nullable TimestampSymbol timestamp) {
        this(keys, hasKeysInCollection, null, null, ccl, null, timestamp);
    }

    /**
     * Construct a new instance for navigating from multiple records.
     * {@link #hasKeysInCollection()} is {@code true} when {@code keys} holds
     * more than one key.
     *
     * @param keys navigation keys
     * @param records the starting record ids
     * @param timestamp optional timestamp for historical navigation
     */
    public NavigateSymbol(Collection<KeyTokenSymbol<?>> keys,
            Collection<Long> records, @Nullable TimestampSymbol timestamp) {
        this(keys, CommandSymbols.holdsMoreThanOne(keys), records, timestamp);
    }

    /**
     * Construct a new instance for navigating from a single record.
     * {@link #hasKeysInCollection()} is {@code true} when {@code keys} holds
     * more than one key.
     *
     * @param keys navigation keys
     * @param record the starting record id
     * @param timestamp optional timestamp for historical navigation
     */
    public NavigateSymbol(Collection<KeyTokenSymbol<?>> keys, long record,
            @Nullable TimestampSymbol timestamp) {
        this(keys, CommandSymbols.holdsMoreThanOne(keys), record, timestamp);
    }

    /**
     * Construct a new instance for navigating from records matching criteria.
     * {@link #hasKeysInCollection()} is {@code true} when {@code keys} holds
     * more than one key.
     *
     * @param keys navigation keys
     * @param criteria the criteria for selecting starting records
     * @param timestamp optional timestamp for historical navigation
     */
    public NavigateSymbol(Collection<KeyTokenSymbol<?>> keys, Object criteria,
            @Nullable TimestampSymbol timestamp) {
        this(keys, CommandSymbols.holdsMoreThanOne(keys), criteria, timestamp);
    }

    /**
     * Construct a new instance for navigating from records matching CCL.
     * {@link #hasKeysInCollection()} is {@code true} when {@code keys} holds
     * more than one key.
     *
     * @param keys navigation keys
     * @param ccl the CCL for selecting starting records
     * @param timestamp optional timestamp for historical navigation
     */
    public NavigateSymbol(Collection<KeyTokenSymbol<?>> keys, String ccl,
            @Nullable TimestampSymbol timestamp) {
        this(keys, CommandSymbols.holdsMoreThanOne(keys), ccl, timestamp);
    }

    /**
     * Construct a new instance that starts from at most one of a record,
     * records, criteria or CCL.
     *
     * @param keys navigation keys
     * @param hasKeysInCollection whether the command names {@code keys} as a
     *        collection, as {@link #hasKeysInCollection()} defines it
     * @param record the starting record id, or {@code null}
     * @param records the starting record ids, or {@code null}
     * @param ccl the CCL for selecting starting records, or {@code null}
     * @param criteria the criteria for selecting starting records, or
     *        {@code null}
     * @param timestamp optional timestamp for historical navigation
     */
    private NavigateSymbol(Collection<KeyTokenSymbol<?>> keys,
            boolean hasKeysInCollection, @Nullable Long record,
            @Nullable Collection<Long> records, @Nullable String ccl,
            @Nullable Object criteria, @Nullable TimestampSymbol timestamp) {
        this.keys = keys;
        this.hasKeysInCollection = hasKeysInCollection;
        this.record = record;
        this.records = records;
        this.ccl = ccl;
        this.criteria = criteria;
        this.timestamp = timestamp;
    }

    /**
     * Return the CCL query if specified.
     *
     * @return the CCL string or {@code null} if using record ids or criteria
     */
    @Nullable
    public String ccl() {
        return ccl;
    }

    /**
     * Return the criteria if specified.
     *
     * @return the criteria or {@code null} if using record ids or CCL
     */
    @Nullable
    public Object criteria() {
        return criteria;
    }

    /**
     * Return whether the command names its {@link #keys() keys} as a
     * collection. A command names a collection when it lists its keys in
     * brackets, even one key as in {@code navigate [friends.name] from 1}, or
     * lists more than one key, as in
     * {@code navigate friends.name, friends.age from 1}. A command that names
     * one key without brackets, as in {@code navigate friends.name from 1},
     * does not.
     *
     * @return {@code true} if the command names its keys as a collection
     */
    public boolean hasKeysInCollection() {
        return hasKeysInCollection;
    }

    /**
     * Return the navigation keys.
     *
     * @return the collection of keys to navigate along
     */
    public Collection<KeyTokenSymbol<?>> keys() {
        return keys;
    }

    /**
     * Return the single record identifier if specified.
     *
     * @return the starting record id or {@code null} if using multiple records
     */
    @Nullable
    public Long record() {
        return record;
    }

    /**
     * Return the collection of record identifiers if specified.
     *
     * @return the starting record ids or {@code null} if using single record
     */
    @Nullable
    public Collection<Long> records() {
        return records;
    }

    /**
     * Return the timestamp for historical navigation.
     *
     * @return the timestamp if specified, otherwise {@code null}
     */
    @Nullable
    public TimestampSymbol timestamp() {
        return timestamp;
    }

    @Override
    public String type() {
        return "NAVIGATE";
    }
}
