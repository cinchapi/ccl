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

import java.util.Set;

import com.cinchapi.ccl.type.Operator;

/**
 * The {@link StatementAnalysis} provides metatadata about the nature of a CCL
 * statement.
 *
 * @author Jeff Nelson
 */
public interface StatementAnalysis {

    /**
     * Return an ordered collection of keys that are included in the CCL
     * statement.
     * 
     * @return the included keys
     */
    public Set<String> keys();

    /**
     * Return an ordered collection of keys that are included in the CCL
     * statement in an expression that contains the specified
     * {@code operator}.
     * 
     * @return the included keys that are evaluated against the
     *         {@code operator}
     */
    public Set<String> keys(Operator operator);

    /**
     * Return all the operators used in the CCL statement.
     * 
     * @return the included operators
     */
    public Set<Operator> operators();

}
