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
import javax.annotation.Nullable;
import com.cinchapi.ccl.grammar.KeyTokenSymbol;
import com.cinchapi.ccl.grammar.TimestampSymbol;

/**
 * A {@link CommandSymbol} that represents a BROWSE command
 */
public class BrowseSymbol implements CommandSymbol {
    private final Collection<KeyTokenSymbol<?>> keys;
    private final TimestampSymbol timestamp;

    /**
     * Construct a new instance for browsing current values.
     *
     * @param keys a collection of field names
     */
    public BrowseSymbol(Collection<KeyTokenSymbol<?>> keys) {
        this(keys, null);
    }

    /**
     * Construct a new instance for browsing historical values.
     *
     * @param keys a collection of field names
     * @param timestamp optional timestamp for historical browsing
     */
    public BrowseSymbol(Collection<KeyTokenSymbol<?>> keys, @Nullable TimestampSymbol timestamp) {
        this.keys = keys;
        this.timestamp = timestamp;
    }

    @Override
    public String type() {
        return "BROWSE";
    }

    /**
     * Return the keys to browse.
     *
     * @return the collection of keys
     */
    public Collection<KeyTokenSymbol<?>> keys() {
        return keys;
    }

    /**
     * Return the timestamp for historical browsing.
     *
     * @return the timestamp if specified, otherwise {@code null}
     */
    @Nullable
    public TimestampSymbol timestamp() {
        return timestamp;
    }
}