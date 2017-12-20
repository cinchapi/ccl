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

import java.util.Collection;
import javax.annotation.concurrent.Immutable;

import com.cinchapi.ccl.grammar.Symbol;

/**
 * An {@link AbstractSyntaxTree} is a generic structure that can be used to
 * represent a statement in a formal language.
 * 
 * <p>
 * Each {@link AbstractSyntaxTree} contains a single {@link Symbol}, which
 * contains any number of {@link #children} nodes. If the tree contains no
 * children, it is considered a "leaf". This property can be checked in the
 * {@link #isLeaf()} method.
 * </p>
 *
 * @author Jeff Nelson
 */
@Immutable
public interface AbstractSyntaxTree {

    /**
     * Return an collection that contains all the children nodes of this
     * {@link AbstractSyntaxTree} from "left" to "right".
     * 
     * <p>
     * NOTE: Since this tree is generic, there is no semantic meaning defined
     * for the "left" to "right" order in which the children are sorted, at this
     * level. Implementations of this class may define a meaning to the order.
     * </p>
     * 
     * @return an collection of the children
     */
    public Collection<AbstractSyntaxTree> children();

    /**
     * Return {@code true} if this {@link AbstractSyntaxTree} has no children
     * and is therefore considered a leaf node.
     * 
     * @return {@code true} if this tree is considered a leaf node
     */
    public default boolean isLeaf() {
        return children().isEmpty();
    }

    /**
     * Return the {@link Symbol} contained in the "root" node of the tree.
     * 
     * @return the root node {@link Symbol}
     */
    public Symbol root();

    public default Object accept(Visitor visitor) {
        return accept(visitor, new Object[] {});
    }

    /**
     * This method is part of the visitor design pattern It accepts an
     * {@link Visitor} as {@code visitor} and some data {@code data}.
     *
     * @param visitor the visitor
     * @param data the data
     * @return the data
     */
    public Object accept(Visitor visitor, Object... data);
}
