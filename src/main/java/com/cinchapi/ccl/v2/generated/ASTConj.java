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

import com.cinchapi.ccl.syntax.AbstractSyntaxTree;
import com.cinchapi.ccl.syntax.BaseConjunctionTree;
import com.cinchapi.ccl.syntax.Visitor;
import com.google.common.collect.Lists;

import java.util.Collection;

/**
 * Represents a conjunction node in the CCL grammar.
 */
public abstract class ASTConj extends SimpleNode implements BaseConjunctionTree {
    /**
     * Constructs a new instance.
     *
     * @param id the id
     */
    ASTConj(int id) {
        super(id);
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
    public <T> T accept(Visitor<T> visitor, Object... data) {
        return visitor.visit(this, data);
    }
}