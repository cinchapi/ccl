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

import javax.annotation.Nullable;
import com.cinchapi.ccl.grammar.ExpressionSymbol;
import com.cinchapi.ccl.grammar.TimestampSymbol;
import com.cinchapi.ccl.grammar.OrderSymbol;
import com.cinchapi.ccl.grammar.PageSymbol;

/**
 * A {@link CommandSymbol} that represents a FIND command.
 */
public class FindSymbol implements CommandSymbol {
    private final TimestampSymbol timestamp;

    /**
     * Construct a new instance for expression-based finding.
     */
    public FindSymbol(@Nullable TimestampSymbol timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String type() {
        return "FIND";
    }

    /**
     * Return the timestamp for historical search.
     */
    @Nullable
    public TimestampSymbol timestamp() {
        return timestamp;
    }
}