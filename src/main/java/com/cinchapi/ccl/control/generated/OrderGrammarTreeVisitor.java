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
package com.cinchapi.ccl.control.generated;

import com.cinchapi.concourse.Timestamp;
import com.cinchapi.concourse.lang.sort.Order;

/**
 * A visitor pattern implementation of {@link OrderGrammarVisitor} that
 * generates an abstract syntax tree of the accepted string.
 */
public class OrderGrammarTreeVisitor implements OrderGrammarVisitor
{
    /**
     * Visitor for a {@link SimpleNode}
     *
     * @param node the node
     * @param data the data
     * @return the data
     */
    public Object visit(SimpleNode node, Object data) {
        System.out.println(node + ": acceptor not unimplemented in subclass?");
        data = node.childrenAccept(this, data);
        return data;
    }

    /**
     * Visitor for a {@link ASTStart}
     *
     * @param node the node
     * @param data the data
     * @return the data
     */
    public Object visit(ASTStart node, Object data) {
        data = node.jjtGetChild(0).jjtAccept(this, data);
        return data;
    }


    /**
     * Visitor for a {@link ASTOrder}
     *
     * @param node the node
     * @param data a reference to the tree
     * @return the expression tree
     */
    public Object visit(ASTOrder node, Object data) {
        if (node.orderComponent() == null || node.orderComponent().equalsIgnoreCase("<")) {
            if(node.timestampNumber() != null) {
                long number = Long.valueOf(node.timestampNumber());
                return Order.by(node.key()).at(Timestamp.fromMicros(number)).ascending().build();
            }
            else if(node.timestampString() != null) {
                if(node.timestampFormat() != null) {
                    return Order.by(node.key()).at(Timestamp
                            .parse(node.timestampString().replace("\"", ""),
                                    node.timestampFormat().replace("\"", "")))
                                    .ascending()
                            .build();
                }
                else {
                    return Order.by(node.key()).at(Timestamp.
                            fromString(node.timestampString().replace("\"", "")))
                            .ascending().build();
                }
            }
            else {
                return Order.by(node.key()).ascending().build();
            }
        }
        else {
            if (node.timestampNumber() != null) {
                long number = Long.valueOf(node.timestampNumber());
                return Order.by(node.key()).at(Timestamp.fromMicros(number)).descending().build();
            }
            else if (node.timestampString() != null) {
                if (node.timestampFormat() != null) {
                    return Order.by(node.key()).at(Timestamp
                            .parse(node.timestampString().replace("\"", ""),
                                    node.timestampFormat().replace("\"", "")))
                                    .descending()
                            .build();
                }
                else {
                    return Order.by(node.key()).at(Timestamp
                            .fromString(node.timestampString().replace("\"", "")))
                            .descending().build();
                }
            }
            else {
                return Order.by(node.key()).descending().build();
            }
        }
    }
}
