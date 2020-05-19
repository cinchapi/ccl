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
package com.cinchapi.ccl.lang.order;

import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;

import java.util.List;

/**
 * A class to abstract away a list of orders as in concourse Order
 */
public class OrderClause {

    /**
     * List of order specifications
     */
    private List<OrderSpecification> specifications;

    public OrderClause() {
        specifications = Lists.newArrayList();
    }

    /**
     * Add a new {@code orderSpecification} to the clause
     *
     * @param orderSpecification
     */
    public void add(OrderSpecification orderSpecification) {
        specifications.add(orderSpecification);
    }

    public List<OrderSpecification> spec() {
        return specifications;
    }

    @Override
    public String toString() {
        return StringUtils.join(specifications, " ");
    }
}
