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

import com.cinchapi.ccl.SyntaxException;
import com.cinchapi.ccl.grammar.BaseKeySymbol;
import com.cinchapi.ccl.grammar.BaseValueSymbol;
import com.cinchapi.ccl.grammar.Expression;
import com.cinchapi.ccl.grammar.KeySymbol;
import com.cinchapi.ccl.grammar.OperatorSymbol;
import com.cinchapi.ccl.grammar.Symbol;
import com.cinchapi.ccl.grammar.TimestampSymbol;
import com.cinchapi.ccl.grammar.ValueSymbol;
import com.cinchapi.ccl.syntax.AbstractSyntaxTree;
import com.cinchapi.ccl.syntax.BaseExpressionTree;
import com.cinchapi.ccl.syntax.Visitor;
import com.cinchapi.ccl.type.Operator;
import com.cinchapi.ccl.util.NaturalLanguage;
import com.cinchapi.common.base.AnyStrings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Function;

/**
 * A node that represents a CCL expression
 */
public class ASTExpression extends SimpleNode implements BaseExpressionTree {
    private String key = null;
    private String operator = null;
    private List<String> values = Lists.newArrayList();
    private String timestamp = null;

    /**
     * Construct a new instance
     *
     * @param id the id
     */
    public ASTExpression(int id) {
        super(id);
    }

    /**
     * Convert the node a string representation.
     *
     * @return the node as a string
     */
    public String toString() {
        String string = AnyStrings.format("{} {}", key, operator);
        for (String value : values) {
            string += " " + value;
        }
        if(timestamp != null) {
            string += " " + timestamp;
        }
        return string;
    }

    /**
     * Sets the key
     *
     * @param key the key
     */
    public void key(String key) {
        this.key = key;
    }

    /** Sets the operator
     *
     * @param operator the operator
     */
    public void operator(String operator) {
        this.operator = operator;
    }

    /**
     * Adds a value
     *
     * @param value the value to add
     */
    public void addValue(String value) {
        this.values.add(value);
    }

    /**
     * Sets the timestamp
     *
     * @param timestamp the timestamp
     */
    public void timestamp(String timestamp) {
        this.timestamp = timestamp;
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
        return null;
    }

    @Override
    public Symbol root(
            Function<String, Object> valueTransformFunction,
            Function<String, Operator> operatorTransformFunction,
            Multimap<String, Object> data) {

        BaseKeySymbol key = new KeySymbol(this.key);
        OperatorSymbol operator = new OperatorSymbol(operatorTransformFunction.apply(this.operator));
        List<BaseValueSymbol> values = Lists.newArrayList();

        for(String value : this.values) {
            if(value.charAt(0) == '$') {
                String var = value.substring(1);
                try {
                    value = Iterables.getOnlyElement(data.get(var)).toString();
                }
                catch (IllegalArgumentException e) {
                    String err = "Unable to resolve variable {} because multiple values exist locally: {}";
                    throw new SyntaxException(AnyStrings.format(err, value, data.get(var)));
                }
                catch (NoSuchElementException e) {
                    String err = "Unable to resolve variable {} because no values exist locally";
                    throw new SyntaxException(AnyStrings.format(err, value));
                }
            }
            else if(value.length() > 2 && value.charAt(0) == '\\'
                    && value.charAt(1) == '$') {
                value = value.substring(1);
            }

            values.add(new ValueSymbol(valueTransformFunction.apply(value)));
        }

        if (this.timestamp != null && !this.timestamp.trim().isEmpty()) {
            TimestampSymbol timestamp = new TimestampSymbol(NaturalLanguage.parseMicros(this.timestamp));
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
}