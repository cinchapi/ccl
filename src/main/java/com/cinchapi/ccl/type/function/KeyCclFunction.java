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
package com.cinchapi.ccl.type.function;

import com.cinchapi.ccl.syntax.AbstractSyntaxTree;
import com.cinchapi.ccl.syntax.ConjunctionTree;
import com.cinchapi.ccl.syntax.ExpressionTree;
import com.cinchapi.ccl.syntax.OrderTree;
import com.cinchapi.ccl.syntax.RootTree;
import com.cinchapi.ccl.syntax.Visitor;

/**
 * A function that is applied to a key across the records that match a condition
 * (represented by an {@link AbstractSyntaxTree}).
 */
public class KeyCclFunction
        extends ExplicitBinaryFunction<AbstractSyntaxTree> {
    /**
     * Constructs a new instance
     *
     * @param function the function
     * @param key the key
     * @param value the value
     */
    public KeyCclFunction(String function, String key,
            AbstractSyntaxTree value) {
        super(function, key, value);
    }

    @Override
    protected String _sourceToString() {
        return ((AbstractSyntaxTree) args[1]).accept(new Visitor<String>() {
            String string = "";

            @Override
            public String visit(RootTree tree, Object... data) {
                if (tree.parseTree() != null) {
                    tree.parseTree().accept(this, data);
                }
                if (tree.orderTree() != null) {
                    tree.orderTree().accept(this, data);
                }
                return string;
            }

            @Override
            public String visit(ConjunctionTree tree, Object... data) {
                tree.left().accept(this, data);
                string += " " + tree.root().toString();
                tree.right().accept(this, data);
                return string;
            }

            @Override
            public String visit(ExpressionTree tree, Object... data) {
                string += " " + tree.root().toString();
                return string;
            }

            @Override
            public String visit(OrderTree tree, Object... data) {
                string += " " + tree.root().toString();
                return string;
            }
        });
    }
}
