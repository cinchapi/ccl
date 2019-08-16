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
     * The page number
     */
    private String number = null;

    /**
     * The page size
     */
    private String size = null;

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
     * Set the page number
     *
     * @param number the page number
     */
    public void number(String number) {
        this.number = number;
    }

    /**
     * Set the page size.
     *
     * @param size the page size
     */
    public void size(String size) {
        this.size = size;
    }

    /**
     * Get the page number
     */
    public String number() {
        return number;
    }

    /**
     * Get the page size
     */
    public String size() {
        return size;
    }

    /**
     * Convert the node a string representation.
     *
     * @return the node as a string
     */
    public String toString() {
        StringBuilder builder = new StringBuilder();
        if (number != null) {
            builder.append(number);
        }
        if (size != null) {
            if (builder.length() == 0) {
                builder.append(" ");
            }
            builder.append(number);
        }
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
