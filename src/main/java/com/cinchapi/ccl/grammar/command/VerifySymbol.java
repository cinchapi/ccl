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

import javax.annotation.Nullable;
import com.cinchapi.ccl.grammar.KeyTokenSymbol;
import com.cinchapi.ccl.grammar.ValueTokenSymbol;
import com.cinchapi.ccl.grammar.TimestampSymbol;

/**
 * A {@link CommandSymbol} that represents a VERIFY command.
 */
public class VerifySymbol implements CommandSymbol {
    private final KeyTokenSymbol<?> key;
    private final ValueTokenSymbol<?> value;
    private final long record;
    private final TimestampSymbol timestamp;

    /**
     * Construct a new instance.
     *
     * @param key the key to verify
     * @param value the value to verify
     * @param record the record identifier
     * @param timestamp optional timestamp for historical verification
     */
    public VerifySymbol(KeyTokenSymbol<?> key, ValueTokenSymbol<?> value, long record,
                        @Nullable TimestampSymbol timestamp) {
        this.key = key;
        this.value = value;
        this.record = record;
        this.timestamp = timestamp;
    }

    @Override
    public String type() {
        return "VERIFY";
    }

    /**
     * Return the key to verify.
     */
    public KeyTokenSymbol<?> key() {
        return key;
    }

    /**
     * Return the value to verify.
     */
    public ValueTokenSymbol<?> value() {
        return value;
    }

    /**
     * Return the record identifier.
     */
    public long record() {
        return record;
    }

    /**
     * Return the timestamp for historical verification.
     */
    @Nullable
    public TimestampSymbol timestamp() {
        return timestamp;
    }
}