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

import com.cinchapi.ccl.grammar.BaseExpression;
import com.cinchapi.ccl.grammar.BaseKeySymbol;
import com.cinchapi.ccl.grammar.BaseValueSymbol;
import com.cinchapi.ccl.grammar.Expression;
import com.cinchapi.ccl.grammar.OperatorSymbol;
import com.cinchapi.ccl.grammar.Symbol;
import com.cinchapi.ccl.grammar.TimestampSymbol;
import com.cinchapi.ccl.syntax.AbstractSyntaxTree;
import com.cinchapi.ccl.syntax.ExpressionTree;
import com.cinchapi.ccl.syntax.Visitor;
import com.google.common.collect.Lists;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * A node that representation a CCL expression
 */
public class ASTExpression extends SimpleNode implements BaseExpression,
        ExpressionTree {
    /**
     * The key
     */
    private BaseKeySymbol key;

    /**
     * The operator
     */
    private OperatorSymbol operator;

    /**
     * The values
     */
    private List<BaseValueSymbol> values = Lists.newArrayList();

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
     * Set the key.
     *
     * @param key the key
     */
    public void key(BaseKeySymbol key) {
        this.key = key;
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
     * Add a value.
     *
     * @param value the value
     */
    public void addValue(BaseValueSymbol value) {
        this.values.add(value);
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
     * Get the key
     *
     * @return the key
     */
    public BaseKeySymbol key() {
        return key;
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
     * Get the value
     *
     * @return the value
     */
    public List<BaseValueSymbol> values() {
        return values;
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
     * Convert the node a string representation.
     *
     * @return the node as a string
     */
    public String toString() {
        String string = key.toString() + " " + operator.toString();
        for (BaseValueSymbol value : values) {
            string += " " + value.toString();
        }
        if (timestamp != null) {
            string += " " + timestamp.toString();
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

    @Override
    public Collection<AbstractSyntaxTree> children() {
        return Collections.emptyList();
    }

    @Override
    public Symbol root() {
        if (this.timestamp != null) {
            return new Expression(timestamp, key, operator, values.toArray(new BaseValueSymbol[values.size()]));
        }
        else {
            return new Expression(key, operator, values.toArray(new BaseValueSymbol[values.size()]));
        }
    }

    @Override
    public <T> T accept(Visitor<T> visitor, Object... data) {
        return visitor.visit(this, data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(root(), children());
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof AbstractSyntaxTree) {
            AbstractSyntaxTree other = (AbstractSyntaxTree) obj;
            return Objects.equals(root(), other.root())
                    && Objects.equals(children(), other.children());
        }
        else {
            return false;
        }
    }
}