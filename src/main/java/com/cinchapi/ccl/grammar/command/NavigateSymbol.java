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
import com.cinchapi.concourse.lang.BuildableState;

/**
 * A {@link CommandSymbol} that represents a NAVIGATE command.
 */
public class NavigateSymbol implements CommandSymbol {
    private final Collection<KeyTokenSymbol<?>> keys;
    private final Long record;
    private final Collection<Long> records;
    private final String ccl;
    private final BuildableState criteria;
    private final TimestampSymbol timestamp;

    /**
     * Construct a new instance for navigating from a single record.
     *
     * @param keys navigation keys
     * @param record the starting record id
     * @param timestamp optional timestamp for historical navigation
     */
    public NavigateSymbol(Collection<KeyTokenSymbol<?>> keys, long record,
                          @Nullable TimestampSymbol timestamp) {
        this(keys, record, null, null, null, timestamp);
    }

    /**
     * Construct a new instance for navigating from multiple records.
     *
     * @param keys navigation keys
     * @param records the starting record ids
     * @param timestamp optional timestamp for historical navigation
     */
    public NavigateSymbol(Collection<KeyTokenSymbol<?>> keys, Collection<Long> records,
                          @Nullable TimestampSymbol timestamp) {
        this(keys, null, records, null, null, timestamp);
    }

    /**
     * Construct a new instance for navigating from records matching criteria.
     *
     * @param keys navigation keys
     * @param criteria the criteria for selecting starting records
     * @param timestamp optional timestamp for historical navigation
     */
    public NavigateSymbol(Collection<KeyTokenSymbol<?>> keys, BuildableState criteria,
                          @Nullable TimestampSymbol timestamp) {
        this(keys, null, null, null, criteria, timestamp);
    }

    /**
     * Construct a new instance for navigating from records matching CCL.
     *
     * @param keys navigation keys
     * @param ccl the CCL for selecting starting records
     * @param timestamp optional timestamp for historical navigation
     */
    public NavigateSymbol(Collection<KeyTokenSymbol<?>> keys, String ccl,
                          @Nullable TimestampSymbol timestamp) {
        this(keys, null, null, ccl, null, timestamp);
    }

    private NavigateSymbol(Collection<KeyTokenSymbol<?>> keys, @Nullable Long record,
                           @Nullable Collection<Long> records, @Nullable String ccl,
                           @Nullable BuildableState criteria, @Nullable TimestampSymbol timestamp) {
        this.keys = keys;
        this.record = record;
        this.records = records;
        this.ccl = ccl;
        this.criteria = criteria;
        this.timestamp = timestamp;
    }

    @Override
    public String type() {
        return "NAVIGATE";
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
    public BuildableState criteria() {
        return criteria;
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
}