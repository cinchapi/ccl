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
import com.cinchapi.ccl.syntax.AbstractSyntaxTree;
import com.cinchapi.ccl.syntax.AndTree;

/**
 * An {@link AbstractSyntaxTree} that represents a logical AND.
 *
 * @author Jeff Nelson
 */
public class ConcourseAndTree extends ConcourseConjunctionTree implements
        AndTree {
    /**
     * Construct a new instance.
     *
     * @param left
     * @param right
     */
    public ConcourseAndTree(AbstractSyntaxTree left, AbstractSyntaxTree right) {
        super(ConjunctionSymbol.AND, left, right);
    }
}