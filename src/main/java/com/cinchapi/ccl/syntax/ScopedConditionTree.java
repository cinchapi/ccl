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

import com.cinchapi.ccl.grammar.KeyTokenSymbol;
import com.cinchapi.ccl.grammar.ScopeSymbol;
import com.cinchapi.ccl.grammar.Symbol;

/**
 * A {@link ConditionTree} that carries an explicit navigation
 * {@link #prefix() prefix} and an inner {@link ConditionTree} whose
 * conditions must all be satisfied by the <em>same</em> destination
 * record reachable via that prefix.
 * <p>
 * Produced by the CCL syntax {@code prefix.(inner)} (e.g.
 * {@code friend.(name = "Jeff" AND age > 30)}).
 * </p>
 *
 * @author Jeff Nelson
 */
public final class ScopedConditionTree extends BaseAbstractSyntaxTree
        implements ConditionTree {

    /**
     * The navigation prefix at which {@link #condition()} is evaluated.
     */
    private final KeyTokenSymbol<?> prefix;

    /**
     * The wrapped {@link ConditionTree}.
     */
    private final ConditionTree condition;

    /**
     * The {@link Symbol} returned by {@link #root()}. Cached so that
     * {@link #equals(Object)} and {@link #hashCode()} (both of which
     * consult {@link #root()}) do not allocate on every call.
     */
    private final ScopeSymbol root;

    /**
     * Construct a new instance.
     *
     * @param prefix the navigation prefix
     * @param condition the inner {@link ConditionTree}
     */
    public ScopedConditionTree(KeyTokenSymbol<?> prefix,
            ConditionTree condition) {
        this.prefix = prefix;
        this.condition = condition;
        this.root = new ScopeSymbol(prefix);
    }

    /**
     * Return the navigation prefix at which the inner {@link #condition()}
     * is evaluated.
     *
     * @return the prefix
     */
    public KeyTokenSymbol<?> prefix() {
        return prefix;
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
        return root;
    }

    @Override
    public <T> T accept(Visitor<T> visitor, Object... data) {
        return visitor.visit(this, data);
    }

}
