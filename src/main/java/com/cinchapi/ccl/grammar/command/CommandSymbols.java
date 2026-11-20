/*
 * Copyright (c) 2013-2026 Cinchapi Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.cinchapi.ccl.grammar.command;

import java.util.Collection;

import javax.annotation.Nullable;

/**
 * Utilities that the {@link CommandSymbol CommandSymbols} in this package
 * share.
 *
 * @author Jeff Nelson
 */
final class CommandSymbols {

    /**
     * Return whether {@code items} holds more than one item. A command can name
     * more than one item only as a collection, so a constructor that is not
     * told how the command names its items treats them as a collection exactly
     * when this is {@code true}.
     *
     * @param items the items that a command names, or {@code null} when it
     *            names none
     * @return {@code true} if {@code items} holds more than one item
     */
    static boolean holdsMoreThanOne(@Nullable Collection<?> items) {
        return items != null && items.size() > 1;
    }

    /**
     * Prevent instantiation of this utility class.
     */
    private CommandSymbols() {/* no-init */}

}
