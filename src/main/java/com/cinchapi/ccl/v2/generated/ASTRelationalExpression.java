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

import com.google.common.collect.Lists;
import java.util.List;

/**
 * A node that representation a CCL expression
 */
public class ASTRelationalExpression extends SimpleNode {
    /**
     * The key
     */
    private String key = "";

    /**
     * The operator
     */
    private String operator = "";

    /**
     * The values
     */
    private List<String> values = Lists.newArrayList();

    /**
     * The timestamp
     */
    private String timestamp = null;

    /**
     * Construct a new instance
     *
     * @param id the id
     */
    public ASTRelationalExpression(int id) {
        super(id);
    }

    /**
     * Construct a new instance
     *
     * @param grammar the grammar
     * @param id the id
     */
    public ASTRelationalExpression(Grammar grammar, int id) {
        super(grammar, id);
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
    * Set the operator.
     *
    * @param operator operator
    */
    public void operator(String operator) {
        this.operator = operator;
    }

    /**
     * Add a value.
     *
     * @param value the value
     */
    public void addValue(String value) {
        this.values.add(value);
    }

    /**
     * Append a value to the first value
     *
     * @param value the value to append
     */
    public void appendValue(String value) {
        if (this.values.isEmpty()) {
            this.values.add(value);
        }
        else {
            this.values.add(0, this.values.get(0) + " " + value);
        }
    }

    /**
     * Set the timestamp.
     *
     * @param timestamp the timestamp
     */
    public void timestamp(String timestamp) {
        if (this.timestamp == null) {
            this.timestamp = timestamp;
        }
        else {
            this.timestamp += " " + timestamp;
        }
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
     * Get the operator
     *
     * @return the operator
     */
    public String operator() {
      return operator;
  }

    /**
     * Get the value
     *
     * @return the value
     */
    public List<String> value() {
      return values;
  }

    /**
     * Get the timestamp
     *
     * @return the timestamp
     */
    public String timestamp() {
      return  timestamp;
    }

    /**
     * Convert the node a string representation.
     *
     * @return the node as a string
     */
    public String toString() {
      String string = key + " " + operator;
      for (String value : values) {
            string += " " + value;
      }
      if (timestamp != null) {
          string += " at " + timestamp;
      }
      return string;
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
