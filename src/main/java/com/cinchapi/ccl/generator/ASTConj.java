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
package com.cinchapi.ccl.generator;

/**
 * Represents a conjunction node in the CCL grammar.
 */
public class ASTConj extends SimpleNode {
    /**
     * The symbol
     */
    private String symbol = "";

    /**
     * Constructs a new instance.
     *
     * @param id the id
     */
    public ASTConj(int id) {
    super(id);
    }

    /**
     * Constructs a new instance.
     *
     * @param grammar the grammar
     * @param id the id
     */
    public ASTConj(Grammar grammar, int id) {
    super(grammar, id);
    }

    /**
     * Set the symbol
     *
     * @param symbol the symbol
     */
    public void symbol(String symbol) {
    this.symbol = symbol;
    }

    /**
     * Get the symbol
     *
     * @return the symbol
     */
    public String symbol() {
    return symbol;
    }

    /**
     * Convert the node a string representation
     *
     * @return the string
     */
    public String toString() {
    return symbol;
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
