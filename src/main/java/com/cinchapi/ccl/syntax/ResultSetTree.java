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
import java.util.Set;

import com.cinchapi.ccl.grammar.Symbol;
import com.cinchapi.ccl.grammar.ValueSymbol;

/**
 * A {@link ResultSetTree} contains records that are the result set of a query.
 * 
 * @author Jeff Nelson
 */
public class ResultSetTree extends BaseAbstractSyntaxTree {

    private final Set<Long> records;

    /**
     * Construct a new instance.
     * 
     * @param records
     */
    public ResultSetTree(Set<Long> records) {
        this.records = records;
    }

    @Override
    public Collection<AbstractSyntaxTree> children() {
        return Collections.emptyList();
    }

    @Override
    public Symbol root() {
        return new ValueSymbol(records());
    }

    /**
     * Return the records contained in this {@link ResultSetTree}.
     * 
     * @return the records
     */
    public Set<Long> records() {
        return records;
    }

    @Override
    public <T> T accept(Visitor<T> visitor, Object... data) {
        return visitor.visit(this, data);
    }

}
