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

/**
 * A {@link Symbol} that contains a value.;
 *
 * @author Jeff Nelson
 */
public final class ValueSymbol extends BaseValueSymbol<Object> {

    /**
     * Construct a new instance.
     *
     * @param value
     */
    public ValueSymbol(Object value) {
        super(value);
    }

    /**
     * Do any escaping of the {@code value} in order to preserve it during the
     * translation.
     * 
     * @param value
     * @return the escaped value
     */
    private static Object escape(Object value) {
        if(value instanceof String && ((String) value).matches("`([^`]+)`")) {
            // CON-167: surround by quotes so the backticks are not interpreted
            // as indicators of an encoded Tag. This case would happen if the
            // user manually placed text wrapped in backticks in the Criteria
            // instead of using the #Tag.create() method.
            return "\"" + value + "\"";
        }
        else {
            return value;
        }
    }

    @Override
    public String toString() {
        return escape(value).toString();
    }

}
