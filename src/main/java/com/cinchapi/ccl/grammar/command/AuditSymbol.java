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
import com.cinchapi.ccl.grammar.KeySymbol;

/**
 * A {@link CommandSymbol} that represents an AUDIT command.
 */
public class AuditSymbol implements CommandSymbol {
    private final KeySymbol key;
    private final long record;

    /**
     * Construct a new instance.
     *
     * @param key optional key to audit, if null audits entire record
     * @param record the record identifier
     */
    public AuditSymbol(@Nullable KeySymbol key, long record) {
        this.key = key;
        this.record = record;
    }

    @Override
    public String type() {
        return "AUDIT";
    }

    /**
     * Return the key to audit, if specified.
     */
    @Nullable
    public KeySymbol key() {
        return key;
    }

    /**
     * Return the record identifier.
     */
    public long record() {
        return record;
    }
}