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
package com.cinchapi.ccl.order;

import com.cinchapi.ccl.order.generated.OrderGrammar;
import com.cinchapi.ccl.order.generated.OrderGrammarBasicVisitor;
import com.cinchapi.ccl.order.generated.PropagatedSyntaxException;
import com.cinchapi.ccl.order.generated.SimpleNode;
import com.cinchapi.ccl.order.generated.ParseException;
import com.cinchapi.concourse.Timestamp;
import com.cinchapi.concourse.lang.sort.BuildableOrderState;
import com.cinchapi.concourse.lang.sort.Direction;
import com.cinchapi.concourse.lang.sort.Order;
import com.cinchapi.concourse.lang.sort.OrderAtState;
import com.cinchapi.concourse.lang.sort.OrderByState;
import com.cinchapi.concourse.lang.sort.OrderComponent;
import com.cinchapi.concourse.lang.sort.OrderDirectionState;
import com.cinchapi.concourse.lang.sort.OrderThenState;

import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.ThreadSafe;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * A {@link OrderParser} is a stateful object that transforms raw input strings into
 * organized structures that can be logically evaluated.
 */
@ThreadSafe
@Immutable
public class OrderParser {

    /**
     * The string statement being parsed.
     */
    protected final String input;

    /**
     * Construct a new instance.
     *
     * @param input
     */
    public OrderParser(String input) {
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
            List<OrderComponent> orderComponents =
                    (List<OrderComponent>) start.jjtAccept(visitor, null);

            // Reflection to dynamically build the chained order invocation
            Method byMethod = Order.class.getDeclaredMethod("by", String.class);
            Method atMethod = OrderByState.class.getDeclaredMethod("at", Timestamp.class);
            Method thenMethod = OrderDirectionState.class.getDeclaredMethod("then");
            Method thenByMethod = OrderThenState.class.getDeclaredMethod("by", String.class);
            Method orderAtStateAscendingMethod = OrderAtState.class.getMethod("ascending");
            orderAtStateAscendingMethod.setAccessible(true);
            Method orderAtStateDescendingMethod = OrderAtState.class.getMethod("descending");
            orderAtStateDescendingMethod.setAccessible(true);

            Object result = null;

            for (int i = 0; i < orderComponents.size(); i++) {
                Object byStateResult;
                if (i == 0) {
                    Object byResult = byMethod.invoke(null, orderComponents.get(i).key());
                    byStateResult = atMethod.invoke(byResult, orderComponents.get(i).timestamp());

                } else {
                    Object thenResult = thenMethod.invoke(result);
                    Object byResult = thenByMethod.invoke(thenResult, orderComponents.get(i).key());

                    byStateResult = atMethod.invoke(byResult, orderComponents.get(i).timestamp());
                }

                if (orderComponents.get(i).direction() == Direction.ASCENDING) {
                    result = orderAtStateAscendingMethod.invoke(byStateResult);
                }
                else {
                    result = orderAtStateDescendingMethod.invoke(byStateResult);
                }
            }
            Method buildMethod = BuildableOrderState.class.getMethod("build");
            return (Order) buildMethod.invoke(result);

        }
        catch (ParseException exception) {
            throw new PropagatedSyntaxException(exception, this);
        }
        catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    @Override
    public String toString() {
        return input;
    }

}
