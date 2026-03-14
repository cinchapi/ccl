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

/**
 * A {@link CommandSymbol} that represents an UNLINK command.
 */
public class UnlinkSymbol implements CommandSymbol {
    private final KeyTokenSymbol<?> key;
    private final long source;
    private final long destination;

    /**
     * Construct a new instance.
     *
     * @param key the key to unlink
     * @param source the source record
     * @param destination the destination record
     */
    public UnlinkSymbol(KeyTokenSymbol<?> key, long source, long destination) {
        this.key = key;
        this.source = source;
        this.destination = destination;
    }

    @Override
    public String type() {
        return "UNLINK";
    }

    /**
     * Return the key to unlink.
     */
    public KeyTokenSymbol<?> key() {
        return key;
    }

    /**
     * Return the source record.
     */
    public long source() {
        return source;
    }

    /**
     * Return the destination record.
     */
    public long destination() {
        return destination;
    }
}
