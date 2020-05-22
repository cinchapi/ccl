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

import com.cinchapi.ccl.grammar.ExpressionSymbol;
import com.cinchapi.ccl.grammar.OperatorSymbol;
import com.cinchapi.ccl.grammar.TimestampSymbol;
import com.cinchapi.ccl.grammar.KeyTokenSymbol;
import com.cinchapi.ccl.grammar.ValueTokenSymbol;
import com.google.common.collect.Lists;

import java.util.List;

/**
 * A node that representation a CCL expression
 */
public class ASTExpression extends SimpleNode implements ExpressionSymbol {

    /**
     * The key
     */
    private KeyTokenSymbol<?> key;

    /**
     * The operator
     */
    private OperatorSymbol operator;

    /**
     * The values
     */
    private List<ValueTokenSymbol<?>> values = Lists.newArrayList();

    /**
     * The timestamp
     */
    private TimestampSymbol timestamp;

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
    public void addValue(ValueTokenSymbol<?> value) {
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
    public KeyTokenSymbol<?> key() {
        return key;
    }

    /**
     * Set the key.
     *
     * @param key the key
     */
    public void key(KeyTokenSymbol<?> key) {
        this.key = key;
    }

    /**
     * Get the operator
     *
     * @return the operator
     */
    public OperatorSymbol operator() {
        return operator;
    }

    /**
     * Set the operator.
     *
     * @param operator operator
     */
    public void operator(OperatorSymbol operator) {
        this.operator = operator;
    }

    /**
     * Get the timestamp
     *
     * @return the timestamp
     */
    public TimestampSymbol timestamp() {
        return timestamp;
    }

    /**
     * Set the timestamp.
     *
     * @param timestamp the timestamp
     */
    public void timestamp(TimestampSymbol timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Convert the node a string representation.
     *
     * @return the node as a string
     */
    public String toString() {
        String string = key.toString() + " " + operator.toString();
        for (ValueTokenSymbol<?> value : values) {
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
    public List<ValueTokenSymbol<?>> values() {
        return values;
    }
}
