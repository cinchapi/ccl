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
package com.cinchapi.ccl.paginate.generated;

/**
 * A node that representation a Page
 */
public class ASTPage extends SimpleNode {
    /**
     * The operator
     */
    private String orderComponent = null;

    /**
     * The key
     */
    private String key = null;


    /**
     * Construct a new instance
     *
     * @param id the id
     */
    public ASTPage(int id) {
        super(id);
    }

    /**
     * Construct a new instance
     *
     * @param grammar the grammar
     * @param id the id
     */
    public ASTPage(PageGrammar grammar, int id) {
        super(grammar, id);
    }

    /**
     * Set the directional ordinal
     *
     * @param orderComponent the order component
     */
    public void orderComponent(String orderComponent) {
        this.orderComponent = orderComponent;
    }

    /**
     * Set the key.
     *
     * @param key the key
     */
    public void key(String key) {
        this.key = key;
    }

    /**
     * Get the order component
     */
    public String orderComponent() {
        return orderComponent;
    }

    /**
     * Get the key
     *
     * @return the key
     */
    public String key() {
        return key;
    }

    /**
     * Convert the node a string representation.
     *
     * @return the node as a string
     */
    public String toString() {
        StringBuilder builder = new StringBuilder();
        if (orderComponent != null) {
            builder.append(orderComponent);
        }
        builder.append(key);
        return builder.toString();
    }

    /**
     * Accept the visitor
     *
     * @param visitor the visitor
     * @param data the data
     * @return the result of the visit
     */
    public Object jjtAccept(PageGrammarVisitor visitor, Object data) {
        return visitor.visit(this, data);
    }
}
