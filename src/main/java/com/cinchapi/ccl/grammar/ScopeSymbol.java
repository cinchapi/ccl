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

/**
 * A {@link PostfixNotationSymbol} that opens a scoped condition group of
 * the form {@code prefix.(inner)}. The {@link #prefix() prefix} identifies
 * the navigation path at which the inner conditions must all be satisfied
 * by the same destination record.
 *
 * @author Jeff Nelson
 */
public final class ScopeSymbol implements PostfixNotationSymbol {

    /**
     * The navigation prefix at which the inner conditions are evaluated.
     */
    private final KeyTokenSymbol<?> prefix;

    /**
     * Construct a new instance.
     *
     * @param prefix the navigation prefix
     */
    public ScopeSymbol(KeyTokenSymbol<?> prefix) {
        this.prefix = prefix;
    }

    /**
     * Return the navigation prefix at which the inner conditions are
     * evaluated.
     *
     * @return the prefix
     */
    public KeyTokenSymbol<?> prefix() {
        return prefix;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof ScopeSymbol) {
            return prefix.equals(((ScopeSymbol) obj).prefix);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return prefix.hashCode();
    }

    @Override
    public String toString() {
        return prefix + ".(";
    }

}
