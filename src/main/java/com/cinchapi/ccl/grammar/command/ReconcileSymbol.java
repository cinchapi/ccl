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
import com.cinchapi.ccl.grammar.KeyTokenSymbol;
import com.cinchapi.ccl.grammar.ValueTokenSymbol;

/**
 * A {@link CommandSymbol} that represents a RECONCILE command.
 */
public class ReconcileSymbol implements CommandSymbol {
    private final KeyTokenSymbol<?> key;
    private final long record;
    private final Collection<ValueTokenSymbol<?>> values;

    /**
     * Construct a new instance.
     *
     * @param key the field name to reconcile
     * @param record the record id
     * @param values the collection of desired values
     */
    public ReconcileSymbol(KeyTokenSymbol<?> key, long record,
                           Collection<ValueTokenSymbol<?>> values) {
        this.key = key;
        this.record = record;
        this.values = values;
    }

    @Override
    public String type() {
        return "RECONCILE";
    }

    /**
     * Return the key to reconcile.
     *
     * @return the field name
     */
    public KeyTokenSymbol<?> key() {
        return key;
    }

    /**
     * Return the record identifier.
     *
     * @return the record id
     */
    public long record() {
        return record;
    }

    /**
     * Return the desired values.
     *
     * @return collection of values the field should contain
     */
    public Collection<ValueTokenSymbol<?>> values() {
        return values;
    }
}