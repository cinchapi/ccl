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

import java.util.Collection;
import com.cinchapi.ccl.grammar.KeySymbol;

/**
 * A {@link CommandSymbol} that represents a LINK command.
 */
public class LinkSymbol implements CommandSymbol {
    private final KeySymbol key;
    private final long source;
    private final Collection<Long> destinations;

    /**
     * Construct a new instance.
     *
     * @param key the key to link
     * @param source the source record
     * @param destinations the destination records
     */
    public LinkSymbol(KeySymbol key, long source, Collection<Long> destinations) {
        this.key = key;
        this.source = source;
        this.destinations = destinations;
    }

    @Override
    public String type() {
        return "LINK";
    }

    /**
     * Return the key to link.
     */
    public KeySymbol key() {
        return key;
    }

    /**
     * Return the source record.
     */
    public long source() {
        return source;
    }

    /**
     * Return the destination records.
     */
    public Collection<Long> destinations() {
        return destinations;
    }
}