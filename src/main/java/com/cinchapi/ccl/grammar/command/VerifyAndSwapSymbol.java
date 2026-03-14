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
 * A {@link CommandSymbol} that represents a VERIFY AND SWAP command.
 */
public class VerifyAndSwapSymbol implements CommandSymbol {
    private final KeyTokenSymbol<?> key;
    private final ValueTokenSymbol<?> expected;
    private final ValueTokenSymbol<?> replacement;
    private final long record;

    /**
     * Construct a new instance.
     *
     * @param key the key to verify and swap
     * @param expected the expected value
     * @param replacement the replacement value
     * @param record the record identifier
     */
    public VerifyAndSwapSymbol(KeyTokenSymbol<?> key, ValueTokenSymbol<?> expected,
                               ValueTokenSymbol<?> replacement, long record) {
        this.key = key;
        this.expected = expected;
        this.replacement = replacement;
        this.record = record;
    }

    @Override
    public String type() {
        return "VERIFY_AND_SWAP";
    }

    /**
     * Return the key to verify and swap.
     */
    public KeyTokenSymbol<?> key() {
        return key;
    }

    /**
     * Return the expected value.
     */
    public ValueTokenSymbol<?> expected() {
        return expected;
    }

    /**
     * Return the replacement value.
     */
    public ValueTokenSymbol<?> replacement() {
        return replacement;
    }

    /**
     * Return the record identifier.
     */
    public long record() {
        return record;
    }
}