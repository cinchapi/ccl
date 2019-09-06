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

import com.cinchapi.ccl.Parsing;
import com.cinchapi.ccl.grammar.PostfixNotationSymbol;
import com.cinchapi.ccl.grammar.Symbol;

import javax.annotation.concurrent.Immutable;

import java.util.List;
import java.util.Queue;

/**
 * A {@link Condition} is an object that is used to encapsulate the semantics of
 * a complex query. Any any given time, objects of this class can exist in one
 * of two modes: {@code building} or {@code built}. When a Condition is
 * {@code built}, it is guaranteed to represent a fully and well formed query
 * that can be processed. On the other hand, when a Condition is {@code building}
 * it is in an incomplete state.
 * <p>
 * This class is the public interface to Condition construction. It is meant to
 * be used in a chained manner, where the caller initially calls
 * {@link Condition#where()} and continues to construct the Condition using the
 * options available from each subsequently returned state.
 * </p>
 *
 * @author Jeff Nelson
 */
/**
 * A {@link com.cinchapi.ccl.grammar.Symbol} is a terminal or non-terminal component in a grammar.
 *
 * @author Jeff Nelson
 */
@Immutable
public interface Condition extends Symbol {

    /**
     * Converts symbol list to Post-fix queue
     *
     * @param symbolList
     * @return
     */
    public static Queue<PostfixNotationSymbol> parse(List<Symbol> symbolList) {
        return Parsing.toPostfixNotation(symbolList);
    }

}
