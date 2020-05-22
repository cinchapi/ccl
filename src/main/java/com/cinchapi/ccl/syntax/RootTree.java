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

import com.cinchapi.ccl.grammar.Symbol;
import com.google.common.collect.Lists;

import java.util.Collection;

/**
 * An abstraction for a root node in a {@link AbstractSyntaxTree}
 */
public class RootTree extends BaseAbstractSyntaxTree {
    private AbstractSyntaxTree parseTree;
    private OrderTree orderTree;

    /**
     * Construct a new instance.
     *
     * @param parseTree
     * @param orderTree
     */
    public RootTree(AbstractSyntaxTree parseTree, OrderTree orderTree) {
        this.parseTree = parseTree;
        this.orderTree = orderTree;
    }

    public AbstractSyntaxTree parseTree() {
        return parseTree;
    }

    public OrderTree orderTree() {
        return orderTree;
    }

    @Override
    public Collection<AbstractSyntaxTree> children() {
        return Lists.newArrayList(parseTree, orderTree);
    }

    @Override
    public Symbol root() {
        return null;
    }

    @Override
    public <T> T accept(Visitor<T> visitor, Object... data) {
        return visitor.visit(this, data);
    }
}

