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
 * An abstraction for a condition tree in a {@link AbstractSyntaxTree}
 */
public class ConditionTree extends BaseAbstractSyntaxTree {

    /**
     * The root.
     */
    private ConditionNode conditionNode;

    /**
     * Construct a new instance.
     *
     * @param conditionNode
     */
    public ConditionTree(ConditionNode conditionNode) {
        this.conditionNode = conditionNode;
    }

    public AbstractSyntaxTree condition() {
        return conditionNode;
    }

    @Override
    public Collection<AbstractSyntaxTree> children() {
        return Lists.newArrayList(conditionNode);
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
