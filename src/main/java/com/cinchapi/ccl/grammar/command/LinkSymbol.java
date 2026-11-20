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
import com.cinchapi.ccl.grammar.KeyTokenSymbol;

/**
 * A {@link CommandSymbol} that represents a LINK command.
 */
public class LinkSymbol implements CommandSymbol {
    private final KeyTokenSymbol<?> key;
    private final long source;
    private final Collection<Long> destinations;

    /**
     * Whether the command names {@link #destinations} as a collection.
     */
    private final boolean hasDestinationsInCollection;

    /**
     * Construct a new instance. {@link #hasDestinationsInCollection()} is
     * {@code true} when {@code destinations} holds more than one record.
     *
     * @param key the key to link
     * @param source the source record
     * @param destinations the destination records
     */
    public LinkSymbol(KeyTokenSymbol<?> key, long source,
            Collection<Long> destinations) {
        this(key, source, destinations,
                CommandSymbols.holdsMoreThanOne(destinations));
    }

    /**
     * Construct a new instance.
     *
     * @param key the key to link
     * @param source the source record
     * @param destinations the destination records
     * @param hasDestinationsInCollection whether the command names
     *        {@code destinations} as a collection, as
     *        {@link #hasDestinationsInCollection()} defines it
     */
    public LinkSymbol(KeyTokenSymbol<?> key, long source,
            Collection<Long> destinations,
            boolean hasDestinationsInCollection) {
        this.key = key;
        this.source = source;
        this.destinations = destinations;
        this.hasDestinationsInCollection = hasDestinationsInCollection;
    }

    /**
     * Return the destination records.
     */
    public Collection<Long> destinations() {
        return destinations;
    }

    /**
     * Return whether the command names its {@link #destinations() destinations}
     * as a collection. A command names a collection when it lists its
     * destinations in brackets, even one destination as in
     * {@code link friends from 1 to [2]}, or lists more than one destination,
     * as in {@code link friends from 1 to 2, 3}. A command that names one
     * destination without brackets, as in {@code link friends from 1 to 2},
     * does not.
     *
     * @return {@code true} if the command names its destinations as a
     *         collection
     */
    public boolean hasDestinationsInCollection() {
        return hasDestinationsInCollection;
    }

    /**
     * Return the key to link.
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

    @Override
    public String type() {
        return "LINK";
    }
}
