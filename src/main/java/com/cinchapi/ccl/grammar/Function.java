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
 *
 */
public abstract class Function {
    /**
     *
     */
    protected final String function;

    /**
     *
     */
    protected final String key;

    /**
     * Creates a new instances
     *
     * @param function the function
     * @param key the key
     */
    protected Function(String function, String key) {
        this.function = function;
        this.key = key;
    }

    /**
     *
     * @return
     */
    public String function() {
        return function;
    }

    /**
     *
     * @return
     */
    public String key() {
        return key;
    }

    /**
     * Return a string representation
     *
     * @return the string representation
     */
    public abstract String toString();
}
