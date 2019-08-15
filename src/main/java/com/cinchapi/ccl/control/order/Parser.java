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
package com.cinchapi.ccl.control.order;

import com.cinchapi.ccl.control.order.generated.OrderGrammar;
import com.cinchapi.ccl.control.order.generated.OrderGrammarBasicVisitor;
import com.cinchapi.ccl.control.order.generated.PropagatedSyntaxException;
import com.cinchapi.ccl.control.order.generated.SimpleNode;
import com.cinchapi.concourse.lang.sort.Order;

import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.ThreadSafe;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * A {@link Parser} is a stateful object that transforms raw input strings into
 * organized structures that can be logically evaluated.
 */
@ThreadSafe
@Immutable
public class Parser {

    /**
     * The string statement being parsed.
     */
    protected final String input;

    /**
     * Construct a new instance.
     *
     * @param input
     */
    public Parser(String input) {
        this.input = input;
    }

    /**
     * Return the input statement that was parsed.
     *
     * @return the input string
     */
    public String input() {
        return input;
    }

    /**
     * Transform a input string into an {@link Order}.
     *
     * @return a {@link Order}
     */
    public Order order() {
        try {
            InputStream stream = new ByteArrayInputStream(
                    input.getBytes(StandardCharsets.UTF_8.name()));
            OrderGrammar grammar = new OrderGrammar(stream);

            SimpleNode start = grammar.Start();

            OrderGrammarBasicVisitor visitor = new OrderGrammarBasicVisitor();
            return (Order) start.jjtAccept(visitor, null);
        }
        catch (Exception exception) {
            throw new PropagatedSyntaxException(exception, this);
        }
    }

    @Override
    public String toString() {
        return input;
    }

}
