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

import com.cinchapi.ccl.grammar.ExpressionSymbol;
import com.cinchapi.ccl.grammar.KeySymbol;
import com.cinchapi.ccl.grammar.SimpleKeySymbol;
import com.cinchapi.ccl.grammar.ValueSymbol;
import com.cinchapi.ccl.grammar.OperatorSymbol;
import com.cinchapi.ccl.grammar.ScalarValueSymbol;
import com.cinchapi.concourse.thrift.Operator;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for the {@link Visitor} interface
 */
public class VisitorTest {

    @Test
    public void testVisitorPattern() {
        // Build tree
        KeySymbol<String> key = new SimpleKeySymbol("key");
        OperatorSymbol operator = new OperatorSymbol(Operator.EQUALS);
        ValueSymbol<Object> value = new ScalarValueSymbol("value");

        ExpressionSymbol expression = ExpressionSymbol.create(key, operator, value);
        ExpressionTree leftTree = new ExpressionTree(expression);

        key = new SimpleKeySymbol("key");
        operator = new OperatorSymbol(Operator.EQUALS);
        value = new ScalarValueSymbol("value");

        expression = ExpressionSymbol.create(key, operator, value);
        ExpressionTree rightTree = new ExpressionTree(expression);

        AndTree tree = new AndTree(leftTree, rightTree);

        // Test visitor
        Visitor<Object> visitor = new Visitor<Object>() {
            @Override
            public Object visit(ConjunctionTree tree, Object... data) {
                Assert.assertTrue(tree instanceof AndTree);
                return data;
            }

            @Override
            public Object visit(ExpressionTree tree, Object... data) {
                Assert.assertTrue(((ExpressionSymbol) tree.root()).key().toString()
                        .equals("key"));
                Assert.assertTrue(((ExpressionSymbol) tree.root()).operator()
                        .toString().equals("="));
                Assert.assertTrue(((ExpressionSymbol) tree.root()).values().get(0)
                        .toString().equals("value"));
                return data;
            }
            
        };
        tree.accept(visitor);
    }

}
