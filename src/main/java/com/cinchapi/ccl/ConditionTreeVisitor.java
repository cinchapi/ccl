/*
 * Copyright (c) 2013-2020 Cinchapi Inc.
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
package com.cinchapi.ccl;

import com.cinchapi.ccl.syntax.CommandTree;
import com.cinchapi.ccl.syntax.ConditionTree;
import com.cinchapi.ccl.syntax.FunctionTree;
import com.cinchapi.ccl.syntax.OrderTree;
import com.cinchapi.ccl.syntax.PageTree;
import com.cinchapi.ccl.syntax.Visitor;

/**
 * A {@link Visitor} that can only visit possible subtree types of a
 * {@link ConditionTree}.
 *
 *
 * @author Jeff Nelson
 */
public abstract class ConditionTreeVisitor<T> implements Visitor<T> {

    @Override
    public final T visit(CommandTree tree, Object... data) {
        throw new UnsupportedOperationException();
    }

    @Override
    public final T visit(OrderTree tree, Object... data) {
        throw new UnsupportedOperationException();
    }

    @Override
    public final T visit(PageTree tree, Object... data) {
        throw new UnsupportedOperationException();
    }

    @Override
    public final T visit(FunctionTree tree, Object... data) {
        throw new UnsupportedOperationException();
    }

}