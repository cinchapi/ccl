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

import java.util.Collection;
import java.util.Collections;
import java.util.function.Function;

import com.cinchapi.ccl.grammar.Expression;
import com.cinchapi.ccl.grammar.Symbol;
import com.cinchapi.ccl.syntax.AbstractSyntaxTree;
import com.cinchapi.ccl.syntax.BaseAbstractSyntaxTree;
import com.cinchapi.ccl.syntax.ExpressionTree;
import com.cinchapi.ccl.syntax.Visitor;
import com.cinchapi.ccl.type.Operator;
import com.google.common.collect.Multimap;

/**
 * An {@link ConcourseExpressionTree} is an {@link AbstractSyntaxTree} whose root node is
 * an {@link Expression} and does not have any children.
 *
 * @author Jeff Nelson
 */
public class ConcourseExpressionTree extends BaseAbstractSyntaxTree implements
        ExpressionTree {

    /**
     * The root.
     */
    private final Expression expression;

    /**
     * Construct a new instance.
     * 
     * @param expression
     */
    public ConcourseExpressionTree(Expression expression) {
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
    public <T> T accept(Visitor<T> visitor, Object... data) {
        return visitor.visit(this, data);
    }

    @Override
    public void build(Function<String, Object> valueTransformFunction,
            Function<String, Operator> operatorTransformFunction,
            Multimap<String, Object> data) {

    }
}
