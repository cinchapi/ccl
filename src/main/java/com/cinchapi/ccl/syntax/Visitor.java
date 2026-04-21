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
 * A {@link Visitor} performs an operation over an
 * {@link AbstractSyntaxTree} by dispatching on the concrete subtree type.
 *
 * @author Jeff Nelson
 */
public interface Visitor<T> {

    /**
     * Visit a {@link CommandTree}.
     *
     * @param tree
     * @param data
     * @return the result of visiting {@code tree}
     */
    public T visit(CommandTree tree, Object... data);

    /**
     * Dispatch to the {@code visit} method for the concrete
     * {@link ConditionTree} subtype of {@code tree}.
     *
     * @param tree
     * @param data
     * @return the result of visiting {@code tree}
     */
    public default T visit(ConditionTree tree, Object... data) {
        if(tree instanceof ConjunctionTree) {
            return visit((ConjunctionTree) tree, data);
        }
        else if(tree instanceof ExpressionTree) {
            return visit((ExpressionTree) tree, data);
        }
        else if(tree instanceof ScopedConditionTree) {
            return visit((ScopedConditionTree) tree, data);
        }
        else {
            throw new UnsupportedOperationException(
                    "Unsupported ConditionTree type");
        }
    }

    /**
     * Visit a {@link ConjunctionTree}.
     *
     * @param tree
     * @param data
     * @return the result of visiting {@code tree}
     */
    public T visit(ConjunctionTree tree, Object... data);

    /**
     * Visit an {@link ExpressionTree}.
     *
     * @param tree
     * @param data
     * @return the result of visiting {@code tree}
     */
    public T visit(ExpressionTree tree, Object... data);

    /**
     * Visit a {@link FunctionTree}.
     *
     * @param tree
     * @param data
     * @return the result of visiting {@code tree}
     */
    public T visit(FunctionTree tree, Object... data);

    /**
     * Visit an {@link OrderTree}.
     *
     * @param tree
     * @param data
     * @return the result of visiting {@code tree}
     */
    public T visit(OrderTree tree, Object... data);

    /**
     * Visit a {@link PageTree}.
     *
     * @param tree
     * @param data
     * @return the result of visiting {@code tree}
     */
    public T visit(PageTree tree, Object... data);

    /**
     * Visit a {@link ScopedConditionTree}.
     * <p>
     * The default implementation throws {@link UnsupportedOperationException}.
     * {@link ScopedConditionTree} carries semantics (same-destination
     * evaluation at a navigation prefix) that cannot be silently unwrapped
     * without risking incorrect results, so every {@link Visitor} must
     * explicitly decide how to handle it &mdash; either by implementing
     * scope-aware behavior, or by delegating to
     * {@link ScopedConditionTree#condition()} with an intentional choice.
     * </p>
     *
     * @param tree
     * @param data
     * @return the result of visiting {@code tree}
     */
    public default T visit(ScopedConditionTree tree, Object... data) {
        throw new UnsupportedOperationException(
                "This Visitor does not handle ScopedConditionTree. Override "
                        + "visit(ScopedConditionTree) to honor or explicitly "
                        + "delegate its semantics.");
    }
}
