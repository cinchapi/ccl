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

import com.cinchapi.ccl.grammar.KeyTokenSymbol;
import com.cinchapi.ccl.grammar.ValueTokenSymbol;

/**
 * A {@link CommandSymbol} that represents a FIND_OR_ADD command.
 * <p>
 * This command attempts to find a unique record where the specified key
 * equals the given value, or creates a new record with that key-value pair
 * if no matching record exists.
 * </p>
 * <p>
 * The command helps simulate a unique index by providing an atomic operation
 * to check for a condition and add data only if that condition isn't
 * currently satisfied.
 * </p>
 */
public class FindOrAddSymbol implements CommandSymbol {
    /**
     * The key (field name) to search or add.
     */
    private final KeyTokenSymbol<?> key;

    /**
     * The value to match or add to the specified key.
     */
    private final ValueTokenSymbol<?> value;

    /**
     * Construct a new FindOrAdd command symbol.
     *
     * @param key the field name to search or add
     * @param value the value to match or add to the field
     */
    public FindOrAddSymbol(KeyTokenSymbol<?> key, ValueTokenSymbol<?> value) {
        this.key = key;
        this.value = value;
    }

    /**
     * Returns the command type identifier.
     *
     * @return the string "FIND_OR_ADD" representing this command type
     */
    @Override
    public String type() {
        return "FIND_OR_ADD";
    }

    /**
     * Get the key (field name) for this command.
     *
     * @return the key token symbol representing the field name
     */
    public KeyTokenSymbol<?> key() {
        return key;
    }

    /**
     * Get the value for this command.
     *
     * @return the value token symbol to match or add
     */
    public ValueTokenSymbol<?> value() {
        return value;
    }
}