/*
 * Copyright (c) 2013-2019 Cinchapi Inc.
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
package com.cinchapi.ccl.v3;

import com.cinchapi.ccl.grammar.OperatorSymbol;

/**
 * The {@link State} that expects the next token to be an operator.
 * 
 * @author Jeff Nelson
 */
public class KeyState extends State {

    /**
     * Construct a new instance.
     * 
     * @param condition
     */
    protected KeyState(BuiltCondition condition) {
        super(condition);
    }

    /**
     * Add the specified {@code operator} to the {@link Condition} that is
     * building.
     * 
     * @param operator
     * @return the builder
     */
    public OperatorState operator(Operator operator) {
        condition.add(new OperatorSymbol(operator));

        return new OperatorState(condition);
    }

    /**
     * Add the specified {@code operator} to the {@link Condition} that is
     * building.
     * 
     * @param operator
     * @return the builder
     */
    public OperatorState operator(String operator) {
        return operator(Convert.stringToOperator(operator));
    }

}
