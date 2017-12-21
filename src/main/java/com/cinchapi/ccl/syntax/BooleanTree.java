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
package com.cinchapi.ccl.syntax;

import java.util.Collection;
import java.util.Collections;

import com.cinchapi.ccl.grammar.Symbol;
import com.cinchapi.ccl.grammar.ValueSymbol;

/**
 * A {@link BooleanTree} contains a boolean.
 * 
 * @author Jeff Nelson
 */
public abstract class BooleanTree extends BaseAbstractSyntaxTree {

    @Override
    public Collection<AbstractSyntaxTree> children() {
        return Collections.emptyList();
    }

    @Override
    public Object accept(Visitor visitor, Object... data) {
        return visitor.visit(this, data);
    }

    @Override
    public final Symbol root() {
        return new ValueSymbol(value());
    }

    /**
     * Return the value expressed by this {@link BooleanTree}.
     * 
     * @return the value
     */
    public abstract boolean value();

}
