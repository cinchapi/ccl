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
package com.cinchapi.ccl.grammar;

import java.util.Locale;

import javax.annotation.Nullable;

/**
 * A {@link Symbol} that says how the operator of an {@link ExpressionSymbol}
 * combines with a set of values: the comparison holds for
 * {@link #ANY at least one} member or for {@link #EVERY every} member.
 * <p>
 * In CCL a quantifier is written as a word before the bracketed value set, as
 * in {@code name = any["Jeff", "John"]}. The words {@code any}, {@code either}
 * and {@code some} mean {@link #ANY}; {@code every} and {@code all} mean
 * {@link #EVERY}. The words are matched without regard to letter case.
 * </p>
 *
 * @author Jeff Nelson
 */
public enum QuantifierSymbol implements Symbol {

    /**
     * The comparison holds when it holds for at least one member of the value
     * set, so {@code key op any[v1, v2]} means
     * {@code (key op v1) or (key op v2)}.
     */
    ANY,

    /**
     * The comparison holds when it holds for every member of the value set, so
     * {@code key op every[v1, v2]} means {@code (key op v1) and (key op v2)}.
     */
    EVERY;

    /**
     * Return the {@link QuantifierSymbol} that {@code word} names, or
     * {@code null} if {@code word} is not a quantifier word.
     *
     * @param word the word to interpret, in any letter case
     * @return the {@link QuantifierSymbol} or {@code null}
     */
    @Nullable
    public static QuantifierSymbol parse(String word) {
        switch (word.toLowerCase(Locale.ROOT)) {
        case "any":
        case "either":
        case "some":
            return ANY;
        case "every":
        case "all":
            return EVERY;
        default:
            return null;
        }
    }

    @Override
    public String toString() {
        return name().toLowerCase(Locale.ROOT);
    }

}
