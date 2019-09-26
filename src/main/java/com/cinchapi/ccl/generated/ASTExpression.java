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

import com.cinchapi.ccl.grammar.v3.ExpressionToken;
import com.cinchapi.ccl.grammar.v3.KeyToken;
import com.cinchapi.ccl.grammar.v3.OperatorToken;
import com.cinchapi.ccl.grammar.v3.TimestampToken;
import com.cinchapi.ccl.grammar.v3.ValueToken;
import com.google.common.collect.Lists;

import java.util.List;

/**
 * A node that representation a CCL expression
 */
public class ASTExpression extends SimpleNode implements ExpressionToken {
    
    /**
     * The key
     */
    private KeyToken<?> key;

    /**
     * The operator
     */
    private OperatorToken operator;

    /**
     * The values
     */
    private List<ValueToken<?>> values = Lists.newArrayList();

    /**
     * The timestamp
     */
    private TimestampToken timestamp;

    /**
     * Construct a new instance
     *
     * @param id the id
     */
    public ASTExpression(int id) {
        super(id);
    }

    /**
     * Add a value.
     *
     * @param value the value
     */
    public void addValue(ValueToken<?> value) {
        this.values.add(value);
    }

    @Override
    public boolean equals(Object obj) {
        if(obj.getClass() == getClass()) {
            return toString().equals(obj.toString());
        }
        else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
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

    /**
     * Get the key
     *
     * @return the key
     */
    @SuppressWarnings("unchecked")
    public KeyToken<?> key() {
        return key;
    }

    /**
     * Set the key.
     *
     * @param key the key
     */
    public void key(KeyToken<?> key) {
        this.key = key;
    }

    /**
     * Get the operator
     *
     * @return the operator
     */
    public OperatorToken operator() {
        return operator;
    }

    /**
     * Set the operator.
     *
     * @param operator operator
     */
    public void operator(OperatorToken operator) {
        this.operator = operator;
    }

    /**
     * Get the timestamp
     *
     * @return the timestamp
     */
    public TimestampToken timestamp() {
        return timestamp;
    }

    /**
     * Set the timestamp.
     *
     * @param timestamp the timestamp
     */
    public void timestamp(TimestampToken timestamp) {
        this.timestamp = timestamp;
    }
    
    /**
     * Convert the node a string representation.
     *
     * @return the node as a string
     */
    public String toString() {
        String string = key.toString() + " " + operator.toString();
        for (ValueToken<?> value : values) {
            string += " " + value.toString();
        }
        if (timestamp != null) {
            string += " " + timestamp.toString();
        }
        return string;
    }

    /**
     * Get the value
     *
     * @return the value
     */
    public List<ValueToken<?>> values() {
        return values;
    }
}