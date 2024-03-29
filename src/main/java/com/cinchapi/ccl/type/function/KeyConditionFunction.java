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

import com.cinchapi.ccl.ConditionTreeVisitor;
import com.cinchapi.ccl.syntax.ConjunctionTree;
import com.cinchapi.ccl.syntax.ExpressionTree;
import com.cinchapi.ccl.syntax.ConditionTree;

/**
 * A function that is applied to a key across the records that match a condition
 * (represented by a {@link ConditionTree}).
 */
public class KeyConditionFunction
        extends ExplicitBinaryFunction<ConditionTree> {
    /**
     * Constructs a new instance
     *
     * @param function the function
     * @param key the key
     * @param value the value
     */
    public KeyConditionFunction(String function, String key,
            ConditionTree value) {
        super(function, key, value);
    }

    /**
     * Constructs a new instance
     *
     * @param function the function
     * @param key the key
     * @param timestamp the timestamp
     * @param value the value
     */
    public KeyConditionFunction(String function, String key,
            ConditionTree value, long timestamp) {
        super(function, key, value, timestamp);
    }

    @Override
    protected String _sourceToString() {
        return ((ConditionTree) args[1])
                .accept(new ConditionTreeVisitor<String>() {
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

                });
    }
}
