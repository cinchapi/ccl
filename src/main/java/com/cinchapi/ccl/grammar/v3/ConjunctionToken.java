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
package com.cinchapi.ccl.grammar.v3;

/**
 * A {@link Token} that represents a conjunction (e.g. "and" or "or").
 * 
 * @author Jeff Nelson
 */
public enum ConjunctionToken implements PostfixNotationToken {
    AND, OR;

    /***
     * Compares the precedence of this conjunction against another conjunction.
     *
     * @param symbol The conjunction to compare against.
     *
     * @return -1 if this conjunction is of lower precedence, 1 if it's of
     *         greater precedence, and 0 if they're of equal precedence. If two
     *         conjunctions are of equal precedence, right associativity and
     *         parenthetical groupings must be used to determine precedence.
     */
    public int comparePrecedence(ConjunctionToken symbol) {
        if(this == symbol) {
            return 0;
        }
        else if(this == AND && symbol == OR) {
            return 1;
        }
        else { // this == OR && symbol == AND
            return -1;
        }
    }

    /**
     * @return <code>true</code> if the conjunction is right associative, and
     *         <code>false</code> otherwise. By definition, any conjunction that
     *         isn't right associative is left associative.
     */
    public boolean isRightAssociative() {
        return false;
    }
}
