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

import java.util.List;

/**
 * An {@link ExpressionSymbol} is a {@link Symbol} that describes a query operation
 * on a key with respect to one or more values (e.g. key = value, key >=
 * value, etc) at a certain timestamp.
 * <p>
 * This class is designed to make it easier to process the results of the
 * Shunting-Yard algorithm.
 * </p>
 * 
 * @author Jeff Nelson
 */
public interface ExpressionSymbol extends PostfixNotationSymbol {

    /**
     * Return the raw key associated with this {@link ExpressionSymbol}.
     * 
     * @return the key
     */
    public KeySymbol<?> key();

    /**
     * Return the operator associated with this {@link ExpressionSymbol}.
     * 
     * @return the operator
     */
    public OperatorSymbol operator();

    /**
     * Return the timestamp associated with this {@link ExpressionSymbol}.
     * 
     * @return the timestamp
     */
    public TimestampSymbol timestamp();


    /**
     * Return the values associated with this {@link ExpressionSymbol}.
     * 
     * @return the values
     */
    public List<ValueSymbol<?>> values();

}
