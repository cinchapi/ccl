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
package com.cinchapi.ccl.type.function;

import com.cinchapi.ccl.syntax.AbstractSyntaxTree;
import com.cinchapi.ccl.syntax.ConditionTree;

/**
 * A function that is applied to a key across the records that match a condition
 * (represented by an {@link ConditionTree}).
 *
 * @author Jeff Nelson
 * @deprecated use {@link KeyConditionFunction} instead
 */
@Deprecated
public final class KeyCclFunction extends KeyConditionFunction {

    /**
     * Construct a new instance.
     * @param function
     * @param key
     * @param value
     */
    public KeyCclFunction(String function, String key, AbstractSyntaxTree value) {
        super(function, key, (ConditionTree) value);
    }

}
