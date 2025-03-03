/*
 * Copyright (c) 2013-2017 Cinchapi Inc.
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
package com.cinchapi.ccl.grammar;

import com.cinchapi.common.base.AnyStrings;

/**
 * A {@link Symbol} that represents either an opened or closed parenthesis.
 * 
 * @author Jeff Nelson
 */
public enum ParenthesisSymbol implements Symbol {
    LEFT, RIGHT;

    /**
     * Return the {@link ParenthesisSymbol} that is parsed from {@code string}.
     * 
     * @param string
     * @return the symbol
     */
    public static ParenthesisSymbol parse(String string) {
        if(string.equalsIgnoreCase("(")
                || string.equalsIgnoreCase(LEFT.name())) {
            return LEFT;
        }
        else if(string.equalsIgnoreCase(")")
                || string.equalsIgnoreCase(RIGHT.name())) {
            return RIGHT;
        }
        throw new RuntimeException(
                AnyStrings.format("Cannot parse {} into a {}", string,
                        ParenthesisSymbol.class.getSimpleName()));
    }

    @Override
    public String toString() {
        return this == LEFT ? "(" : ")";
    }
}
