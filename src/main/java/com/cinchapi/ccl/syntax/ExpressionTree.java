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
import java.util.Collections;

import afu.org.checkerframework.checker.oigj.qual.O;
import com.cinchapi.ccl.grammar.Expression;
import com.cinchapi.ccl.grammar.Symbol;

/**
 * An {@link ExpressionTree} is an {@link AbstractSyntaxTree} whose root node is
 * an {@link Expression} and does not have any children.
 *
 * @author Jeff Nelson
 */
public class ExpressionTree extends BaseAbstractSyntaxTree {

    /**
     * The root.
     */
    private final Expression expression;

    /**
     * Construct a new instance.
     * 
     * @param expression
     */
    public ExpressionTree(Expression expression) {
        this.expression = expression;
    }

    @Override
    public Collection<AbstractSyntaxTree> children() {
        return Collections.emptyList();
    }

    @Override
    public Symbol root() {
        return expression;
    }

    @Override
    public Object accept(Visitor visitor, Object data) {
        return visitor.visit(this, data);
    }

}
