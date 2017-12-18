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
package com.cinchapi.ccl.syntax;

import com.cinchapi.ccl.grammar.Expression;
import com.cinchapi.ccl.grammar.KeySymbol;
import com.cinchapi.ccl.grammar.OperatorSymbol;
import com.cinchapi.ccl.grammar.ValueSymbol;
import com.cinchapi.ccl.type.Operator;
import com.cinchapi.ccl.Parser;
import com.cinchapi.concourse.Tag;
import com.cinchapi.concourse.util.Strings;
import com.google.common.base.MoreObjects;
import org.junit.Assert;
import org.junit.Test;

import java.util.function.Function;

/**
 * Tests for the {@link Visitor} interface
 */
public class VisitorTest {

    @Test
    public void testVisitorPattern() {
        // Build tree
        KeySymbol key = new KeySymbol("key");
        OperatorSymbol operator = new OperatorSymbol(PARSER_TRANSFORM_OPERATOR_FUNCTION.apply("="));
        ValueSymbol value = new ValueSymbol(PARSER_TRANSFORM_VALUE_FUNCTION.apply("value"));

        Expression expression = new Expression(key, operator, value);
        ExpressionTree leftTree = new ExpressionTree(expression);

        key = new KeySymbol("key");
        operator = new OperatorSymbol(PARSER_TRANSFORM_OPERATOR_FUNCTION.apply("="));
        value = new ValueSymbol(PARSER_TRANSFORM_VALUE_FUNCTION.apply("value"));

        expression = new Expression(key, operator, value);
        ExpressionTree rightTree = new ExpressionTree(expression);

        AndTree tree = new AndTree(leftTree, rightTree);

        // Test visitor
        Visitor visitor = new Visitor() {
            @Override
            public Object visit(ConjunctionTree tree, Object data) {
                Assert.assertTrue(tree instanceof AndTree);
                return data;
            }

            @Override
            public Object visit(ExpressionTree tree, Object data) {
                Assert.assertTrue(((Expression) tree.root()).key().toString().equals("key"));
                Assert.assertTrue(((Expression) tree.root()).operator().toString().equals("="));
                Assert.assertTrue(((Expression) tree.root()).values().get(0).toString().equals("value"));
                return data;
            }
        };
        tree.accept(visitor, null);
    }

    /**
     * Converts a string value to a java object
     *
     * @param value
     * @return the converted value
     */
    public static Object stringToJava(String value) {
        if(value.isEmpty()) {
            return value;
        }
        char first = value.charAt(0);
        char last = value.charAt(value.length() - 1);
        Long record;
        if(Strings.isWithinQuotes(value)) {
            // keep value as string since its between single or double quotes
            return value.substring(1, value.length() - 1);
        }
        else if(value.equalsIgnoreCase("true")) {
            return true;
        }
        else if(value.equalsIgnoreCase("false")) {
            return false;
        }
        else if(first == '`' && last == '`') {
            return Tag.create(value.substring(1, value.length() - 1));
        }
        else {
            return MoreObjects.firstNonNull(Strings.tryParseNumber(value),
                    value);
        }
    }

    /**
     * Convert the {@code symbol} into the appropriate {@link Operator}.
     *
     * @param symbol - the string form of a symbol (i.e. =, >, >=, etc) or a
     *            CaSH shortcut (i.e. eq, gt, gte, etc)
     * @return the {@link Operator} that is parsed from the string
     *         {@code symbol}
     */
    public Operator stringToOperator(String symbol) {
        return new DummyOperator(symbol);
    }

    /**
     * The canonical function to transform strings to java values in a
     * {@link Parser}.
     */
    public final Function<String, Object> PARSER_TRANSFORM_VALUE_FUNCTION =
            value -> stringToJava(value);

    /**
     * The canonical function to transform strings to operators in a
     * {@link Parser}.
     */
    public final Function<String, Operator> PARSER_TRANSFORM_OPERATOR_FUNCTION =
            operator -> stringToOperator(operator);

    /**
     * Dummy operator
     */
    private class DummyOperator implements Operator {
        String symbol;

        DummyOperator(String symbol) {
            this.symbol = symbol;
        }

        @Override public int operands() {
            return 0;
        }

        @Override public String symbol() {
            return symbol;
        }
    }
}
