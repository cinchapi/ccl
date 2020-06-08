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

import com.cinchapi.ccl.grammar.OrderSymbol;

/**
 * A node that representation a CCL Order
 */
public class ASTOrder extends SimpleNode {
    /**
     * The operator
     */
    private OrderSymbol order;

    /**
     * Construct a new instance
     *
     * @param id the id
     */
    public ASTOrder(int id) {
        super(id);
    }

    /**
     * Construct a new instance
     *
     * @param grammar the grammar
     * @param id the id
     */
    public ASTOrder(Grammar grammar, int id) {
        super(grammar, id);
    }

    /**
     * Set the order
     *
     * @param order the order
     */
    public void order(OrderSymbol order) {
        this.order = order;
    }

    /**
     * Get the order
     */
    public OrderSymbol order() {
        return order;
    }

    /**
     * Convert the node a string representation.
     *
     * @return the node as a string
     */
    public String toString() {
        return order.toString();
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
