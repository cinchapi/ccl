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

import com.cinchapi.ccl.grammar.v3.ConjunctionToken;
import com.cinchapi.ccl.grammar.v3.Token;
import com.google.common.collect.Lists;

import java.util.Collection;

/**
 * An abstraction for a conjunction node in an {@link AbstractSyntaxTree}
 */
public class ConjunctionTree extends BaseAbstractSyntaxTree {

    private final ConjunctionToken conjunction;
    private final AbstractSyntaxTree left;
    private final AbstractSyntaxTree right;

    /**
     * Construct a new instance.
     *
     * @param conjunction
     * @param left
     * @param right
     */
    public ConjunctionTree(ConjunctionToken conjunction,
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
    public AbstractSyntaxTree left() {
        return left;
    }

    /**
     * Return the right child of this {@link ConjunctionTree}.
     *
     * @return the right child
     */
    public AbstractSyntaxTree right() {
        return right;
    }

    @Override
    public Token root() {
        return conjunction;
    }

    @Override
    public <T> T accept(Visitor<T> visitor, Object... data) {
        return visitor.visit(this, data);
    }

}
