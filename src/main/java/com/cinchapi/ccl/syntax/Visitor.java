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

/**
 * An implementation of the visitor pattern. This interface should be
 * implemented by any class attempting to visit an {@link AbstractSyntaxTree}.
 * In order to do so, the {@link AbstractSyntaxTree#accept(Visitor, Object)}
 * method must be called with a {@link Visitor} as a parameter.
 */
public interface Visitor<T> {

    public T visit(CommandTree tree, Object... data);

    public default T visit(ConditionTree tree, Object... data) {
        if(tree instanceof ConjunctionTree) {
            return visit((ConjunctionTree) tree, data);
        }
        else if(tree instanceof ExpressionTree) {
            return visit((ExpressionTree) tree, data);
        }
        else {
            throw new UnsupportedOperationException("Unsupported ConditionTree type");
        }
    }

    public T visit(ConjunctionTree tree, Object... data);
    
    public T visit(ExpressionTree tree, Object... data);

    public T visit(OrderTree tree, Object... data);

    public T visit(PageTree tree, Object... data);

    public T visit(FunctionTree tree, Object... data);
}
