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
package com.cinchapi.ccl.v2.generated;

import com.cinchapi.ccl.grammar.Key;
import com.cinchapi.ccl.syntax.AbstractSyntaxTree;

import java.util.Collection;
import java.util.Collections;

/**
 *
 */
public abstract class ASTBaseKey<T> extends SimpleNode implements Key<T> {
    /**
     *
     */
    protected T key;

    /**
     * Constructs a new instance.
     *
     * @param id the id
     */
    public ASTBaseKey(int id) {
        super(id);
    }

    /**
     *
     * @param key
     */
    public void key(T key) {
        this.key = key;
    }

    /**
     * Convert the node a string representation
     *
     * @return the string
     */
    public String toString() {
        return key.toString();
    }
}
