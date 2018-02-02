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

import com.cinchapi.ccl.grammar.Symbol;
import com.cinchapi.ccl.syntax.Visitor;

/**
 *
 */
public class ASTValue extends ASTBaseValue<String> {
    /**
     * Constructs a new instance.
     *
     * @param id the id
     */
    public ASTValue(int id) {
        super(id);
    }

    @Override
    public String value() {
        return value;
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

    @Override public Symbol root() {
        return null;
    }

    @Override public <T> T accept(Visitor<T> visitor, Object... data) {
        return null;
    }
}
