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
package com.cinchapi.ccl.order.generated;

/**
 * A node that representation a CCL Order
 */
public class ASTOrder extends SimpleNode {
    /**
     * The operator
     */
    private String orderComponent = null;

    /**
     * The key
     */
    private String key = null;

    /**
     * The timestamp as a string
     */
    private String timestampString = null;

    /**
     * The timestamp format
     */
    private String timestampFormat = null;

    /**
     * The timestamp as a number
     */
    private String timestampNumber = null;

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
    public ASTOrder(OrderGrammar grammar, int id) {
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
     * Set the timestamp.
     *
     * @param timestampString the timestamp
     */
    public void timestampString(String timestampString) {
        this.timestampString = timestampString;
    }

    /**
     * Set the timestamp.
     *
     * @param timestampFormat the timestamp
     */
    public void timestampFormat(String timestampFormat) {
        this.timestampFormat = timestampFormat;
    }

    /**
     * Set the timestamp.
     *
     * @param timestampNumber the timestamp
     */
    public void timestampNumber(String timestampNumber) {
        this.timestampNumber = timestampNumber;
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
     * Get the timestamp
     *
     * @return the timestamp
     */
    public String timestampString() {
        return  timestampString;
    }

    /**
     * Get the timestamp
     *
     * @return the timestamp
     */
    public String timestampFormat() {
        return timestampFormat;
    }

    /**
     * Get the timestamp
     *
     * @return the timestamp
     */
    public String timestampNumber() {
        return  timestampNumber;
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
        if (timestampNumber != null) {
            builder.append("@");
            builder.append(timestampNumber);
        }
        else if (timestampString != null) {
            builder.append("@");
            builder.append(timestampString);
            if (timestampFormat != null) {
                builder.append("|");
                builder.append(timestampFormat);
            }
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
    public Object jjtAccept(OrderGrammarVisitor visitor, Object data) {
        return visitor.visit(this, data);
    }
}
