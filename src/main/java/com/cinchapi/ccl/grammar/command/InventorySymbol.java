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

/**
 * A {@link CommandSymbol} that represents an INVENTORY command. T
 */
public class InventorySymbol implements CommandSymbol {

    /**
     * The singleton instance for this command since it takes no parameters.
     */
    public static final InventorySymbol INSTANCE = new InventorySymbol();

    /**
     * Private constructor to enforce singleton pattern.
     */
    private InventorySymbol() {/* no-op */}

    @Override
    public String type() {
        return "INVENTORY";
    }
}