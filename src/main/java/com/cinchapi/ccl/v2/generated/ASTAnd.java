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

/**
 * Represents a And node in the CCL grammar.
 */
public class ASTAnd extends SimpleNode {
    /**
     * Constructs a new instance.
     *
     * @param id the id
     */
    public ASTAnd(int id) {
        super(id);
    }

    /**
     * Constructs a new instance.
     *
     * @param grammar the grammar
     * @param id the id
     */
    public ASTAnd(CriteriaGrammar grammar, int id) {
        super(grammar, id);
    }

    /**
     * Convert the node a string representation
     *
     * @return the string
     */
    public String toString() {
        return "and";
    }

    /**
     * Accept a visitor
     *
     * @param visitor the visitor
     * @param data the data
     * @return the result of the visit
     */
    public Object jjtAccept(CriteriaGrammarVisitor visitor, Object data) {
        return visitor.visit(this, data);
    }
}
