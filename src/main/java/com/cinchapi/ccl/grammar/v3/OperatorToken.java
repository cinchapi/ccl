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
package com.cinchapi.ccl.grammar.v3;

import com.cinchapi.ccl.type.Operator;

/**
 * A {@link Token} that represents an {@link Operator}.
 *
 * @author Jeff Nelson
 */
public class OperatorToken implements PostfixNotationToken {

    /**
     * The content of the symbol.
     */
    private final Operator operator;

    /**
     * Construct a new instance.
     * 
     * @param operator
     */
    public OperatorToken(Operator operator) {
        this.operator = operator;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof OperatorToken) {
            return operator.equals(((OperatorToken) obj).operator);
        }
        else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return operator.hashCode();
    }

    /**
     * Return the operator represented by this {@link Token}.
     * 
     * @return the operator
     */
    public Operator operator() {
        return operator;
    }
    
    @Override
    public String toString() {
        return operator.symbol();
    }

}
