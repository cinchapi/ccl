/*
 * Copyright (c) 2013-2026 Cinchapi Inc.
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

import java.util.Collection;
import java.util.Collections;

import com.cinchapi.ccl.grammar.ModifierSymbol;
import com.cinchapi.ccl.grammar.Symbol;

/**
 * A {@link ConditionTree} that wraps an inner {@link ConditionTree} whose
 * conjuncts must be satisfied together, rather than independently.
 *
 * @author Jeff Nelson
 */
public final class StrictConditionTree extends BaseAbstractSyntaxTree
        implements ConditionTree {

    /**
     * The wrapped {@link ConditionTree}.
     */
    private final ConditionTree condition;

    /**
     * Construct a new instance.
     *
     * @param condition
     */
    public StrictConditionTree(ConditionTree condition) {
        this.condition = condition;
    }

    /**
     * Return the wrapped {@link ConditionTree}.
     *
     * @return the inner {@link ConditionTree}
     */
    public ConditionTree condition() {
        return condition;
    }

    @Override
    public Collection<AbstractSyntaxTree> children() {
        return Collections.singletonList(condition);
    }

    @Override
    public Symbol root() {
        return ModifierSymbol.STRICT;
    }

    @Override
    public <T> T accept(Visitor<T> visitor, Object... data) {
        return visitor.visit(this, data);
    }

}
