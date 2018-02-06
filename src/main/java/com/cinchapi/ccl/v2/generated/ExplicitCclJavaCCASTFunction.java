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

import com.cinchapi.ccl.grammar.ExplicitFunction;
import com.cinchapi.common.base.AnyStrings;

/**
 * Represents a {@link ExplicitFunction} where the value is a CCL represented
 * as a {@link ASTStart}
 */
public class ExplicitCclJavaCCASTFunction extends ExplicitFunction<ASTStart> {
    /**
     * Constructs a new instance
     *
     * @param function the function
     * @param key      the key
     * @param value    the value
     */
    public ExplicitCclJavaCCASTFunction(String function, String key,
            ASTStart value) {
        super(function, key, value);
    }

    @Override
    public String toString() {
        String string = AnyStrings.format("{} ({},", function, key);

        //string += value.accept(visitor) + ")";
        return string;
    }
}