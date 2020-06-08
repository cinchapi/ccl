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

import com.cinchapi.ccl.grammar.CommandSymbol;
import com.cinchapi.ccl.grammar.Symbol;
import com.google.common.collect.Lists;

import java.util.Collection;
import java.util.List;

import javax.annotation.Nullable;

/**
 * An abstraction for the overall {@link AbstractSyntaxTree} returned when
 * parsing a command.
 */
public class CommandTree extends BaseAbstractSyntaxTree {

    private CommandSymbol command;
    private ConditionTree conditionTree;
    private PageTree pageTree;
    private OrderTree orderTree;

    /**
     * Construct a new instance.
     *
     * @param conditionTree
     * @param pageTree
     */
    public CommandTree(ConditionTree conditionTree, PageTree pageTree,
            OrderTree orderTree) {
        this.command = CommandSymbol.FIND;
        this.conditionTree = conditionTree;
        this.pageTree = pageTree;
        this.orderTree = orderTree;
    }

    /**
     * Return a {@link ConditonTree tree} for the parsed command's condition, if
     * it exists.
     * 
     * @return the {@link ConditionTree}
     */
    @Nullable
    public ConditionTree conditionTree() {
        return conditionTree;
    }

    /**
     * Return a {@link PageTree tree} for the parsed command's page, if it
     * exists.
     * 
     * @return the {@link PageTree}
     */
    @Nullable
    public PageTree pageTree() {
        return pageTree;
    }

    /**
     * Return a {@link OrderTree tree} for the parsed command's order, if it
     * exists.
     *
     * @return the {@link OrderTree}
     */
    @Nullable
    public OrderTree orderTree() {
        return orderTree;
    }

    @Override
    public Collection<AbstractSyntaxTree> children() {
        List<AbstractSyntaxTree> children = Lists.newArrayList();
        if(conditionTree != null) {
            children.add(conditionTree);
        }
        if(pageTree != null) {
            children.add(pageTree);
        }
        if(orderTree != null) {
            children.add(orderTree);
        }
        return children;
    }

    @Override
    public Symbol root() {
        return command;
    }

    @Override
    public <T> T accept(Visitor<T> visitor, Object... data) {
        return visitor.visit(this, data);
    }
}
