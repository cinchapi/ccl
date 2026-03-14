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

/**
 * A {@link CommandSymbol} that represents an INSERT command.
 */
public class InsertSymbol implements CommandSymbol {
    private final String json;
    private final Long record;
    private final Collection<Long> records;

    /**
     * Construct a new instance for insertion into a new record.
     *
     * @param json the JSON formatted data to insert
     */
    public InsertSymbol(String json) {
        this(json, null, null);
    }

    /**
     * Construct a new instance for insertion into a single record.
     *
     * @param json the JSON formatted data to insert
     * @param record the record identifier
     */
    public InsertSymbol(String json, long record) {
        this(json, record, null);
    }

    /**
     * Construct a new instance for insertion into multiple records.
     *
     * @param json the JSON formatted data to insert
     * @param records the record identifiers
     */
    public InsertSymbol(String json, Collection<Long> records) {
        this(json, null, records);
    }

    private InsertSymbol(String json, @Nullable Long record,
                         @Nullable Collection<Long> records) {
        this.json = json;
        this.record = record;
        this.records = records;
    }

    @Override
    public String type() {
        return "INSERT";
    }

    /**
     * Return the JSON formatted data to insert.
     */
    public String json() {
        return json;
    }

    /**
     * Return the record identifier if inserting into a single record.
     */
    @Nullable
    public Long record() {
        return record;
    }

    /**
     * Return the record identifiers if inserting into multiple records.
     */
    @Nullable
    public Collection<Long> records() {
        return records;
    }
}