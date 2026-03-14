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

/**
 * A {@link CommandSymbol} that represents a CONSOLIDATE command.
 */
public class ConsolidateSymbol implements CommandSymbol {
    private final long first;
    private final Collection<Long> remaining;

    /**
     * Construct a new instance.
     *
     * @param first the primary record to consolidate into
     * @param remaining the records to merge into the first
     */
    public ConsolidateSymbol(long first, Collection<Long> remaining) {
        this.first = first;
        this.remaining = remaining;
    }

    @Override
    public String type() {
        return "CONSOLIDATE";
    }

    /**
     * Return the primary record to consolidate into.
     */
    public long first() {
        return first;
    }

    /**
     * Return the records to merge into the first.
     */
    public Collection<Long> remaining() {
        return remaining;
    }
}
