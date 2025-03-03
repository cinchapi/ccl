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
package com.cinchapi.ccl.generated;

import com.cinchapi.ccl.grammar.ConjunctionSymbol;
import com.cinchapi.ccl.grammar.Symbol;

/**
 * Represents an Or node in the CCL grammar.
 */
public class ASTOr extends SimpleNode {
    /**
     * Constructs a new instance.
     *
     * @param id the id
     */
    public ASTOr(int id) {
        super(id);
    }

    /**
     * Constructs a new instance.
     *
     * @param grammar the grammar
     * @param id the id
     */
    public ASTOr(Grammar grammar, int id) {
        super(grammar, id);
    }

    /**
     * Convert the node a string representation
     *
     * @return the string
     */
    public String toString() {
        return "or";
    }

    public Symbol root() {
        return ConjunctionSymbol.OR;
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
}
