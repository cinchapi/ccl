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

import com.cinchapi.ccl.generated.ASTStart;
import com.cinchapi.ccl.grammar.TimestampSymbol;

import javax.annotation.Nullable;

/**
 * A {@link CommandSymbol} that represents a FIND_OR_INSERT command.
 * <p>
 * This command attempts to find a unique record matching specific criteria,
 * or creates a new record with the provided data if no matching record exists.
 * The command provides an atomic way to ensure data uniqueness while
 * allowing conditional record creation.
 * </p>
 */
public class FindOrInsertSymbol implements CommandSymbol {
    /**
     * The JSON data to insert if no matching record is found.
     * Represents the complete object to be added to a new record.
     */
    private final String json;

    /**
     * Optional timestamp for historical context of the find or insert operation.
     * Allows performing the operation at a specific point in time.
     */
    private final TimestampSymbol timestamp;

    /**
     * Construct a new FindOrInsert command symbol.
     *
     * @param json the JSON-formatted data to potentially insert
     * @param timestamp optional timestamp for historical context
     */
    public FindOrInsertSymbol(String json, TimestampSymbol timestamp) {
        this.json = json;
        this.timestamp = timestamp;
    }

    /**
     * Returns the command type identifier.
     *
     * @return the string "FIND_OR_INSERT" representing this command type
     */
    @Override
    public String type() {
        return "FIND_OR_INSERT";
    }

    /**
     * Get the JSON data for this command.
     *
     * @return the JSON string to be inserted if no matching record exists
     */
    public String json() {
        return json;
    }

    /**
     * Get the timestamp for this command.
     *
     * @return the timestamp for historical context, or null if not specified
     */
    @Nullable
    public TimestampSymbol timestamp() {
        return timestamp;
    }
}