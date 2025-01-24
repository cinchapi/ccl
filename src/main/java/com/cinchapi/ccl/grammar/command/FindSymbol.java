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
import com.cinchapi.ccl.type.Operator;

/**
 * A {@link CommandSymbol} that represents a FIND command.
 */
public class FindSymbol implements CommandSymbol {
    private final KeyTokenSymbol<?> key;
    private final Operator operator;
    private final ValueTokenSymbol<?> value;
    private final ValueTokenSymbol<?> value2;
    private final TimestampSymbol timestamp;

    /**
     * Construct a new instance for unary operation.
     *
     * @param key the key to find
     * @param operator the operator to use
     * @param value the value to compare
     * @param timestamp optional timestamp for historical search
     */
    public FindSymbol(KeyTokenSymbol<?> key, Operator operator, ValueTokenSymbol<?> value,
                      @Nullable TimestampSymbol timestamp) {
        this(key, operator, value, null, timestamp);
    }

    /**
     * Construct a new instance for binary operation.
     *
     * @param key the key to find
     * @param operator the operator to use
     * @param value the first value to compare
     * @param value2 the second value to compare
     * @param timestamp optional timestamp for historical search
     */
    public FindSymbol(KeyTokenSymbol<?> key, Operator operator, ValueTokenSymbol<?> value,
                      @Nullable ValueTokenSymbol<?> value2, @Nullable TimestampSymbol timestamp) {
        this.key = key;
        this.operator = operator;
        this.value = value;
        this.value2 = value2;
        this.timestamp = timestamp;
    }

    @Override
    public String type() {
        return "FIND";
    }

    /**
     * Return the key to find.
     */
    public KeyTokenSymbol<?> key() {
        return key;
    }

    /**
     * Return the operator to use.
     */
    public Operator operator() {
        return operator;
    }

    /**
     * Return the value to compare.
     */
    public ValueTokenSymbol<?> value() {
        return value;
    }

    /**
     * Return the second value for binary operations.
     */
    @Nullable
    public ValueTokenSymbol<?> value2() {
        return value2;
    }

    /**
     * Return the timestamp for historical search.
     */
    @Nullable
    public TimestampSymbol timestamp() {
        return timestamp;
    }
}