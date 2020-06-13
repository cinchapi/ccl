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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;

import java.util.List;

/**
 * A class to abstract away a list of orders as in concourse Order
 */
public class OrderSymbol implements Symbol {

    /**
     * List of order specifications
     */
    private List<OrderComponentSymbol> components;


    public OrderSymbol() {
        this.components = Lists.newArrayList();
    }

    @Override
    public String toString() {
        return StringUtils.join(components, " ");
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof OrderSymbol) {
            return components.equals(((OrderSymbol) obj).components);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return components.hashCode();
    }

    public void add(OrderComponentSymbol symbol) {
        components.add(symbol);
    }
    
    public List<OrderComponentSymbol> components(){
        return ImmutableList.copyOf(components);
    }
}