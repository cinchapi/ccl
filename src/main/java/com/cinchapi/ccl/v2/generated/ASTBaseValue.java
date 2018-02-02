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

import com.cinchapi.ccl.grammar.Value;
import com.cinchapi.ccl.syntax.AbstractSyntaxTree;

import java.util.Collection;
import java.util.Collections;

/**
 *
 */
public abstract class ASTBaseValue<T> extends SimpleNode implements Value<T>,
        AbstractSyntaxTree {
    /**
     *
     */
    protected T value;

    /**
     * Constructs a new instance.
     *
     * @param id the id
     */
    public ASTBaseValue(int id) {
        super(id);
    }

    /**
     *
     * @param value
     */
    public void value(T value) {
        this.value = value;
    }

    /**
     * Convert the node a string representation
     *
     * @return the string
     */
    public String toString() {
        return value.toString();
    }

    /**
     * Accept a visitor
     *
     * @param visitor the visitor
     * @param data the data
     * @return the result of the visit
     */
    public Object jjtAccept(GrammarVisitor visitor, Object data) {
        return visitor.visit(this, data);
    }

    @Override
    public Collection<AbstractSyntaxTree> children() {
        return Collections.emptyList();
    }
}
