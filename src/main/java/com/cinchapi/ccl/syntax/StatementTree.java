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
import java.util.List;

/**
 * An abstraction for the overall {@link AbstractSyntaxTree} returned when
 * parsing a statement.
 */
public class StatementTree extends BaseAbstractSyntaxTree {

    private ConditionTree conditionTree;
    private PageTree pageTree;

    /**
     * Construct a new instance.
     *
     * @param conditionTree
     * @param pageTree
     */
    public StatementTree(ConditionTree conditionTree, PageTree pageTree) {
        this.conditionTree = conditionTree;
        this.pageTree = pageTree;
    }

    public ConditionTree conditionTree() {
        return conditionTree;
    }

    public PageTree pageTree() {
        return pageTree;
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
        return children;
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
