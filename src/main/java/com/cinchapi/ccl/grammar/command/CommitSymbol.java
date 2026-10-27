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
 * A {@link CommandSymbol} that represents a COMMIT command.
 */
public class CommitSymbol implements CommandSymbol {
    /**
     * Singleton instance.
     */
    public static final CommitSymbol INSTANCE = new CommitSymbol();

    private CommitSymbol() {
        // Use INSTANCE
    }

    @Override
    public String type() {
        return "COMMIT";
    }
}