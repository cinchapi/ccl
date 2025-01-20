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

import com.cinchapi.ccl.grammar.command.CommandSymbol;
import com.cinchapi.ccl.grammar.KeySymbol;
import com.cinchapi.ccl.grammar.ValueSymbol;

/**
 * A {@link CommandSymbol} that represents an ADD command.
 */
public class AddSymbol implements CommandSymbol {
    private final KeySymbol key;
    private final ValueSymbol value;
    private final long record;

    /**
     * Construct a new instance.
     *
     * @param key the key to add
     * @param value the value to add
     * @param record the record identifier
     */
    public AddSymbol(KeySymbol key, ValueSymbol value, long record) {
        this.key = key;
        this.value = value;
        this.record = record;
    }

    @Override
    public String type() {
        return "ADD";
    }

    /**
     * Return the key to add.
     */
    public KeySymbol key() {
        return key;
    }

    /**
     * Return the value to add.
     */
    public ValueSymbol value() {
        return value;
    }

    /**
     * Return the record identifier.
     */
    public long record() {
        return record;
    }
}