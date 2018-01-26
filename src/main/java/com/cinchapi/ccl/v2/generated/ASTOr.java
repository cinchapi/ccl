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
package com.cinchapi.ccl.v2.generated;

import com.cinchapi.ccl.grammar.ConjunctionSymbol;
import com.cinchapi.ccl.grammar.Symbol;
import com.cinchapi.ccl.syntax.AbstractSyntaxTree;
import com.cinchapi.ccl.syntax.BaseConjunctionNode;
import com.cinchapi.ccl.syntax.Visitor;
import com.google.common.collect.Lists;

import java.util.Collection;

/**
 * Represents a Or node in the CCL grammar.
 */
public class ASTOr extends SimpleNode implements BaseConjunctionNode {
    /**
     * Constructs a new instance.
     *
     * @param id the id
     */
    public ASTOr(int id) {
        super(id);
    }

    /**
     * Convert the node a string representation
     *
     * @return the string
     */
    public String toString() {
        return "or";
    }

    /**
     * Accept a visitor
     *
     * @param visitor the visitor
     * @param data the data
     * @return the result of the visit
     */
    public Object jjtAccept(GrammarVisitor visitor, Object data) {
        return visitor.visit(this, data);
    }

    @Override
    public AbstractSyntaxTree left() {
        return (AbstractSyntaxTree) jjtGetChild(0);
    }

    @Override
    public AbstractSyntaxTree right() {
        return (AbstractSyntaxTree) jjtGetChild(1);
    }

    @Override
    public Collection<AbstractSyntaxTree> children() {
        return Lists.newArrayList((AbstractSyntaxTree) jjtGetChild(0),
        (AbstractSyntaxTree) jjtGetChild(1));
    }

    @Override
    public Symbol root() {
        return ConjunctionSymbol.OR;
    }

    @Override
    public <T> T accept(Visitor<T> visitor, Object... data) {
        return null;
    }
}