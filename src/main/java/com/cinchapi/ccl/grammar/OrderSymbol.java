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
package com.cinchapi.ccl.grammar;

import com.cinchapi.ccl.lang.order.Direction;
import com.cinchapi.ccl.lang.order.OrderClause;
import com.cinchapi.ccl.lang.order.OrderSpecification;
import com.cinchapi.concourse.Timestamp;

/**
 * A {@link Symbol} that represents an {@link com.cinchapi.ccl.lang.order.OrderSpecification}.
 */
public class OrderSymbol implements PostfixNotationSymbol {
    /**
     * The content of the symbol.
     */
    private OrderClause order;

    /**
     * Construct a new instance.
     *
     * @param order
     */
    public OrderSymbol(OrderClause order) {
        this.order = order;
    }

    /**
     * Construct a new instance.
     */
    public OrderSymbol() {
        this.order = new OrderClause();
    }

    /**
     * Add an new OrderSpecification
     *
     * @param orderSpecification
     */
    public void add(OrderSpecification orderSpecification) {
        this.order.add(orderSpecification);
    }

    /**
     * Add an new OrderSpecification
     *
     * @param key
     * @param direction
     * @param timestampString
     * @param timestampFormat
     * @param timestampNumber
     */
    public void add(String key, String direction, String timestampString,
            String timestampFormat, String timestampNumber) {
        if (direction == null || direction.equalsIgnoreCase("<")
                || direction.equalsIgnoreCase("ASC")) {
            if(timestampNumber != null) {
                long number = Long.valueOf(timestampNumber);
                this.order.add(new OrderSpecification(key,
                        Timestamp.fromMicros(number), Direction.ASCENDING));
            }
            else if(timestampString != null) {
                if(timestampFormat != null) {
                    this.order.add(new OrderSpecification(key,
                            Timestamp.parse(
                                    timestampString.replace("\"", ""),
                                    timestampFormat.replace("\"", "")),
                            Direction.ASCENDING));
                }
                else {
                    this.order.add(new OrderSpecification(key,
                            Timestamp.fromString(
                                    timestampString.replace("\"", "")),
                            Direction.ASCENDING));
                }
            }
            else {
                this.order.add(new OrderSpecification(key,
                        Direction.ASCENDING));
            }
        }
        else {
            if(timestampNumber != null) {
                long number = Long.valueOf(timestampNumber);
                this.order.add(new OrderSpecification(key,
                        Timestamp.fromMicros(number), Direction.DESCENDING));
            }
            else if(timestampString != null) {
                if(timestampFormat != null) {
                    this.order.add(new OrderSpecification(key,
                            Timestamp.parse(timestampString.replace("\"", ""),
                                    timestampFormat.replace("\"", "")),
                            Direction.DESCENDING));
                }
                else {
                    this.order.add(new OrderSpecification(key, Timestamp
                            .fromString(timestampString.replace("\"", "")),
                            Direction.DESCENDING));
                }
            }
            else {
                this.order.add(new OrderSpecification(key, Direction.DESCENDING));
            }
        }
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof OrderSymbol) {
            return order.equals(((OrderSymbol) obj).order);
        }
        else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return order.hashCode();
    }

    /**
     * Return the order represented by this {@link Symbol}.
     *
     * @return the order
     */
    public OrderClause order() {
        return order;
    }

    @Override
    public String toString() {
        return order.toString();
    }
}
