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
import com.cinchapi.ccl.grammar.TimestampSymbol;

/**
 * A {@link CommandSymbol} that represents a DESCRIBE command.
 */
public class DescribeSymbol implements CommandSymbol {
    private final long record;
    private final Collection<Long> records;
    private final TimestampSymbol timestamp;

    /**
     * Construct a new instance for a single record.
     */
    public DescribeSymbol(long record, @Nullable TimestampSymbol timestamp) {
        this(record, null, timestamp);
    }

    /**
     * Construct a new instance for multiple records.
     */
    public DescribeSymbol(Collection<Long> records, @Nullable TimestampSymbol timestamp) {
        this(-1, records, timestamp);
    }

    private DescribeSymbol(long record, @Nullable Collection<Long> records,
                           @Nullable TimestampSymbol timestamp) {
        this.record = record;
        this.records = records;
        this.timestamp = timestamp;
    }

    @Override
    public String type() {
        return "DESCRIBE";
    }

    /**
     * Return the record identifier, if operating on a single record.
     */
    public long record() {
        return record;
    }

    /**
     * Return the record identifiers, if operating on multiple records.
     */
    @Nullable
    public Collection<Long> records() {
        return records;
    }

    /**
     * Return the timestamp for historical describe.
     */
    @Nullable
    public TimestampSymbol timestamp() {
        return timestamp;
    }
}