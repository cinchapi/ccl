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

/**
 * A {@link CommandSymbol} that represents a HOLDS command.
 */
public class HoldsSymbol implements CommandSymbol {
    private final Long record;
    private final Collection<Long> records;

    /**
     * Construct a new instance for checking a single record.
     *
     * @param record the record id
     */
    public HoldsSymbol(long record) {
        this(record, null);
    }

    /**
     * Construct a new instance for checking multiple records.
     *
     * @param records the record ids
     */
    public HoldsSymbol(Collection<Long> records) {
        this(null, records);
    }

    private HoldsSymbol(@Nullable Long record,
                        @Nullable Collection<Long> records) {
        this.record = record;
        this.records = records;
    }

    @Override
    public String type() {
        return "HOLDS";
    }

    /**
     * Return the record identifier, if operating on a single record.
     *
     * @return the record id, or {@code null} if operating on multiple records
     */
    @Nullable
    public Long record() {
        return record;
    }

    /**
     * Return the record identifiers, if operating on multiple records.
     *
     * @return the record ids, or {@code null} if operating on a single record
     */
    @Nullable
    public Collection<Long> records() {
        return records;
    }
}
