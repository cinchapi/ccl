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

/**
 * A {@link Symbol} that contains a key.
 */
public abstract class BaseKeySymbol<T> extends BaseSymbol
        implements PostfixNotationSymbol {

    /**
     * The content of the {@link Symbol}.
     */
    protected final T key;

    /**
     * Construct a new instance.
     *
     * @param key
     */
    public BaseKeySymbol(T key) {
        this.key = key;
    }

    /**
     * Return the key associated with this {@link Symbol}.
     *
     * @return the key
     */
    public T key() {
        return key;
    }

    @Override
    public String toString() {
        return key.toString();
    }
}
