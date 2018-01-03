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
 * Represents an explicit function of type {@code T}
 */
public abstract class ExplicitFunction<T> extends Function {
    /**
     * The value of the function
     */
    protected final T value;

    /**
     * Constructs a new instance
     *
     * @param function the function
     * @param key the key
     * @param value the value
     */
    protected ExplicitFunction(String function, String key, T value) {
        super(function, key);
        this.value = value;
    }

    /**
     *
     * @return
     */
    public T value() {
        return value;
    }
}
