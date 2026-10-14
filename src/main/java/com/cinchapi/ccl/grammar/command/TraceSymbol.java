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
 * A {@link CommandSymbol} that represents a TRACE command. This command locates and
 * returns all incoming links to specified records.
 * <p>
 * The trace operation maps each record to another mapping from keys to all the records
 * where those keys were stored as links to the specified record. This provides a way
 * to find all references to particular records in the document-graph.
 * </p>
 * <p>
 * A trace may name a {@link #key()}, as in {@code trace manager from 42}, in
 * which case it locates only the records that link to the specified records
 * through that key.
 * </p>
 */
public class TraceSymbol implements CommandSymbol {
    private final KeyTokenSymbol<?> key;
    private final Long record;
    private final Collection<Long> records;
    private final TimestampSymbol timestamp;

    /**
     * Construct a new instance for tracing links to a single record.
     *
     * @param record the record id
     */
    public TraceSymbol(long record) {
        this(null, record, null, null);
    }

    /**
     * Construct a new instance for tracing historical links to a single record.
     *
     * @param record the record id
     * @param timestamp the historical instant to trace
     */
    public TraceSymbol(long record, @Nullable TimestampSymbol timestamp) {
        this(null, record, null, timestamp);
    }

    /**
     * Construct a new instance for tracing links to multiple records.
     *
     * @param records collection of record ids
     */
    public TraceSymbol(Collection<Long> records) {
        this(null, null, records, null);
    }

    /**
     * Construct a new instance for tracing historical links to multiple records.
     *
     * @param records collection of record ids
     * @param timestamp the historical instant to trace
     */
    public TraceSymbol(Collection<Long> records, @Nullable TimestampSymbol timestamp) {
        this(null, null, records, timestamp);
    }

    /**
     * Construct a new instance for tracing the links to a single record through
     * one key.
     *
     * @param key the key through which the incoming links are traced
     * @param record the record id
     * @param timestamp the historical instant to trace, or {@code null} for the
     *            present
     */
    public TraceSymbol(KeyTokenSymbol<?> key, long record,
            @Nullable TimestampSymbol timestamp) {
        this(key, record, null, timestamp);
    }

    /**
     * Construct a new instance for tracing the links to multiple records
     * through one key.
     *
     * @param key the key through which the incoming links are traced
     * @param records collection of record ids
     * @param timestamp the historical instant to trace, or {@code null} for the
     *            present
     */
    public TraceSymbol(KeyTokenSymbol<?> key, Collection<Long> records,
            @Nullable TimestampSymbol timestamp) {
        this(key, null, records, timestamp);
    }

    /**
     * Private constructor for all trace scenarios.
     *
     * @param key the key through which links are traced, or null for every key
     * @param record single record id or null if using multiple records
     * @param records collection of record ids or null if using single record
     * @param timestamp optional historical instant to trace
     */
    private TraceSymbol(@Nullable KeyTokenSymbol<?> key, @Nullable Long record,
            @Nullable Collection<Long> records,
            @Nullable TimestampSymbol timestamp) {
        this.key = key;
        this.record = record;
        this.records = records;
        this.timestamp = timestamp;
    }

    @Override
    public String type() {
        return "TRACE";
    }

    /**
     * Return the key through which the incoming links are traced.
     *
     * @return the key, or {@code null} if links through every key are traced
     */
    @Nullable
    public KeyTokenSymbol<?> key() {
        return key;
    }

    /**
     * Return the single record identifier if specified.
     *
     * @return the record id or {@code null} if tracing multiple records
     */
    @Nullable
    public Long record() {
        return record;
    }

    /**
     * Return the collection of record identifiers if specified.
     *
     * @return the record ids or {@code null} if tracing single record
     */
    @Nullable
    public Collection<Long> records() {
        return records;
    }

    /**
     * Return the timestamp for historical tracing.
     *
     * @return the timestamp if specified, otherwise {@code null}
     */
    @Nullable
    public TimestampSymbol timestamp() {
        return timestamp;
    }
}
