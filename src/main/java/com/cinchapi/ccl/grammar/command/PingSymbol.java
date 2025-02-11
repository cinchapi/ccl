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
 * A {@link CommandSymbol} that represents a PING command.
 */
public class PingSymbol implements CommandSymbol {
    private final long record;
    private final Collection<Long> records;

    /**
     * Construct a new instance for a single record.
     *
     * @param record the record identifier
     */
    public PingSymbol(long record) {
        this(record, null);
    }

    /**
     * Construct a new instance for multiple records.
     *
     * @param records the record identifiers
     */
    public PingSymbol(Collection<Long> records) {
        this(-1, records);
    }

    private PingSymbol(long record, @Nullable Collection<Long> records) {
        this.record = record;
        this.records = records;
    }

    @Override
    public String type() {
        return "PING";
    }

    /**
     * Return the record identifier if pinging a single record.
     */
    public long record() {
        return record;
    }

    /**
     * Return the record identifiers if pinging multiple records.
     */
    @Nullable
    public Collection<Long> records() {
        return records;
    }
}