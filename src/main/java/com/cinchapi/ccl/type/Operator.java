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
package com.cinchapi.ccl.type;

/**
 * An interface representing the input to an {@link OperatorSymbol}.
 *
 * @author Jeff Nelson
 */
public interface Operator {

    /**
     * Return the number of operands operated on by this {@link Operator}.
     * 
     * @return the expected number of operands
     */
    public int operands();

    /**
     * Return the symbol that represents this {@link Operator}.
     * 
     * @return the operator's symbol
     */
    public String symbol();

}
