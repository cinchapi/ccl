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
package com.cinchapi.ccl.v2.generated.order;

import com.cinchapi.ccl.order.Direction;
import com.cinchapi.ccl.order.OrderClause;
import com.cinchapi.ccl.order.OrderSpecification;
import com.cinchapi.ccl.v3.Timestamp;

/**
 * A visitor pattern implementation of {@link OrderGrammarVisitor} that
 * generates an {@link Order} of the accepted string.
 */
public class OrderGrammarBasicVisitor implements OrderGrammarVisitor
{
    private OrderClause clause = new OrderClause();
    //private List<OrderSpecification> components = Lists.newArrayList();

    /**
     * Visitor for a {@link SimpleNode}
     *
     * @param node the node
     * @param data the data
     * @return the data
     */
    public Object visit(SimpleNode node, Object data) {
        System.out.println(node + ": acceptor not unimplemented in subclass?");
        node.childrenAccept(this, data);
        return clause;
    }

    /**
     * Visitor for a {@link ASTStart}
     *
     * @param node the node
     * @param data the data
     * @return the data
     */
    public Object visit(ASTStart node, Object data) {
        node.childrenAccept(this, data);
        return clause;
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
                this.clause.add(new OrderSpecification(node.key(),
                        Timestamp.fromMicros(number), Direction.ASCENDING));
            }
            else if(node.timestampString() != null) {
                if(node.timestampFormat() != null) {
                    this.clause.add(new OrderSpecification(node.key(),
                            Timestamp.parse(
                                    node.timestampString().replace("\"", ""),
                                    node.timestampFormat().replace("\"", "")),
                            Direction.ASCENDING));
                }
                else {
                    this.clause.add(new OrderSpecification(node.key(),
                            Timestamp.fromString(
                                    node.timestampString().replace("\"", "")),
                            Direction.ASCENDING));
                }
            }
            else {
                this.clause.add(new OrderSpecification(node.key(),
                        Direction.ASCENDING));
            }
        }
        else {
            if (node.timestampNumber() != null) {
                long number = Long.valueOf(node.timestampNumber());
                this.clause.add(new OrderSpecification(node.key(),
                        Timestamp.fromMicros(number), Direction.DESCENDING));
            }
            else if (node.timestampString() != null) {
                if (node.timestampFormat() != null) {
                    this.clause.add(new OrderSpecification(node.key(),
                            Timestamp.parse(
                                    node.timestampString().replace("\"", ""),
                                    node.timestampFormat().replace("\"", "")),
                            Direction.DESCENDING));
                }
                else {
                    this.clause.add(new OrderSpecification(node.key(),
                            Timestamp.fromString(
                                    node.timestampString().replace("\"", "")),
                            Direction.DESCENDING));
                }
            }
            else {
                this.clause.add(new OrderSpecification(node.key(),
                        Direction.DESCENDING));
            }
        }
        return clause;
    }
}
