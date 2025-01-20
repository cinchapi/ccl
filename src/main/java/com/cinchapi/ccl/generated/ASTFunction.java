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

import com.cinchapi.ccl.grammar.condition.FunctionTokenSymbol;

/**
 * A node that representation a CCL function
 */
public class ASTFunction extends SimpleNode {
    /**
     * The function
     */
    private FunctionTokenSymbol function;

    /**
     * Construct a new instance
     *
     * @param id the id
     */
    public ASTFunction(int id) {
        super(id);
    }

    /**
     * Construct a new instance
     *
     * @param grammar the grammar
     * @param id the id
     */
    public ASTFunction(Grammar grammar, int id) {
        super(grammar, id);
    }

    /**
     * Set the function
     *
     * @param function
     */
    public void function(FunctionTokenSymbol function) {
        this.function = function;
    }

    /**
     * Get the function
     */
    public FunctionTokenSymbol function() {
        return function;
    }

    /**
     * Convert the node a string representation.
     *
     * @return the node as a string
     */
    public String toString() {
        return function.toString();
    }

    /**
     * Accept the visitor
     *
     * @param visitor the visitor
     * @param data the data
     * @return the result of the visit
     */
    public Object jjtAccept(GrammarVisitor visitor, Object data) {
        return visitor.visit(this, data);
    }
}
