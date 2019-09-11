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
package com.cinchapi.ccl.grammar;

import com.cinchapi.ccl.syntax.AbstractSyntaxTree;
import com.cinchapi.ccl.syntax.ConjunctionTree;
import com.cinchapi.ccl.syntax.ExpressionTree;
import com.cinchapi.ccl.syntax.Visitor;
import com.cinchapi.common.base.AnyStrings;

/**
 * Represents a {@link ExplicitValueFunction} where the value is a CCL represented
 * as a {@link AbstractSyntaxTree}
 */
public class ExplicitCclASTValueFunction extends ExplicitValueFunction<AbstractSyntaxTree> {
    /**
     * Constructs a new instance
     *
     * @param function the function
     * @param key      the key
     * @param value    the value
     */
    public ExplicitCclASTValueFunction(String function, String key,
            AbstractSyntaxTree value) {
        super(function, key, value);
    }

    @Override
    public String toString() {
        String string = AnyStrings.format("{} ({},", function, key);

        Visitor<String> visitor = new Visitor<String>() {
            String string = "";

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
        };

        string += value.accept(visitor) + ")";
        return string;
    }
}
