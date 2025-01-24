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
 * A {@link CommandSymbol} that represents a SEARCH command.
 */
public class SearchSymbol implements CommandSymbol {
    private final KeyTokenSymbol<?> key;
    private final String query;

    /**
     * Construct a new instance.
     *
     * @param key the key to search
     * @param query the search query
     */
    public SearchSymbol(KeyTokenSymbol<?> key, String query) {
        this.key = key;
        this.query = query;
    }

    @Override
    public String type() {
        return "SEARCH";
    }

    /**
     * Return the key to search.
     */
    public KeyTokenSymbol<?> key() {
        return key;
    }

    /**
     * Return the search query.
     */
    public String query() {
        return query;
    }
}