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

/**
 * A {@link CommandSymbol} that represents an ABORT command.
 */
public class AbortSymbol implements CommandSymbol {
    /**
     * Singleton instance.
     */
    public static final AbortSymbol INSTANCE = new AbortSymbol();

    private AbortSymbol() {
        // Use INSTANCE
    }

    @Override
    public String type() {
        return "ABORT";
    }
}