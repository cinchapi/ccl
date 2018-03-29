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
package com.cinchapi.ccl.v1;

import com.cinchapi.ccl.grammar.ConjunctionSymbol;
import com.cinchapi.ccl.grammar.Symbol;
import com.cinchapi.ccl.syntax.AbstractSyntaxTree;
import com.cinchapi.ccl.syntax.BaseAbstractSyntaxTree;
import com.cinchapi.ccl.syntax.BaseConjunctionTree;
import com.cinchapi.ccl.syntax.Visitor;
import com.google.common.collect.Lists;

import java.util.Collection;

/**
 * A {@link ConjunctionTree} contains a {@link ConjunctionSymbol} and is flanked
 * on the left and right, by exactly two other {@link AbstractSyntaxTree} nodes.
 */
 public class ConjunctionTree extends BaseAbstractSyntaxTree implements BaseConjunctionTree {
    private final ConjunctionSymbol conjunction;
    private final AbstractSyntaxTree left;
    private final AbstractSyntaxTree right;

    /**
     * Construct a new instance.
     *
     * @param conjunction
     * @param left
     * @param right
     */
    public ConjunctionTree(ConjunctionSymbol conjunction,
            AbstractSyntaxTree left, AbstractSyntaxTree right) {
        this.conjunction = conjunction;
        this.left = left;
        this.right = right;
    }

    @Override
    public Collection<AbstractSyntaxTree> children() {
        return Lists.newArrayList(left, right);
    }

    /**
     * Return the left child of this {@link ConjunctionTree}.
     *
     * @return the left child
     */
    @Override
    public AbstractSyntaxTree left() {
        return left;
    }

    /**
     * Return the right child of this {@link ConjunctionTree}.
     *
     * @return the right child
     */
    @Override
    public AbstractSyntaxTree right() {
        return right;
    }

    @Override
    public Symbol root() {
        return conjunction;
    }

    @Override
    public <T> T accept(Visitor<T> visitor, Object... data) {
        return visitor.visit(this, data);
    }
}
