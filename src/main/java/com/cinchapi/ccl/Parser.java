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
package com.cinchapi.ccl;

import java.util.List;
import java.util.Queue;
import java.util.function.Function;

import javax.annotation.concurrent.ThreadSafe;

import com.cinchapi.ccl.grammar.PostfixNotationSymbol;
import com.cinchapi.ccl.grammar.Symbol;
import com.cinchapi.ccl.syntax.AbstractSyntaxTree;
import com.cinchapi.ccl.type.Operator;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;

/**
 * A {@link Parser} transforms raw CCL strings and groups of {@link Symbol
 * tokens} into organized structures that can be logically evaluated.
 *
 * @author Jeff Nelson
 */
@ThreadSafe
public interface Parser {

    /**
     * Return a {@link Parser} instance.
     * 
     * @return a {@link Parser}
     */
    public static Parser instance(
            Function<String, Object> valueTransformFunction,
            Function<String, Operator> operatorTransformFunction) {
        return new CustomParser(valueTransformFunction,
                operatorTransformFunction);
    }

    /**
     * Transform a sequential list of {@link Symbol} tokens to an {@link Queue}
     * of symbols in {@link PostfixNotationSymbol postfix notation} that are
     * sorted by the proper order of operations.
     * 
     * @param symbols a sequential list of tokens
     * @return a {@link Queue} of {@link PostfixNotationSymbol
     *         PostfixNotationSymbols}
     */
    public Queue<PostfixNotationSymbol> order(List<Symbol> symbols);

    /**
     * Transform a sequential list of {@link Symbol} tokens to an
     * {@link AbstractSyntaxTree}.
     * 
     * <p>
     * NOTE: This method will group non-conjunctive symbols into
     * {@link Expression} objects.
     * </p>
     * 
     * @param symbols a sequential list of tokens
     * @return an {@link AbstractSyntaxTree} containing the parsed structure
     *         inherent in the symbols
     */
    public AbstractSyntaxTree parse(List<Symbol> symbols);

    /**
     * Convert a CCL statement to a list of {@link Symbol} tokens.
     * 
     * @param ccl the CCL statement
     * @return a list of {@link Symbol} tokens
     */
    public default List<Symbol> tokenize(String ccl) {
        return tokenize(ccl, ImmutableMultimap.of());
    }

    /**
     * Convert a CCL statement to a list of {@link Symbol} tokens and bind any
     * local variables using the provided {@code data}.
     * 
     * @param ccl the CCL statement
     * @param data the data to use for binding local variables
     * @return a list of {@link Symbol} tokens
     */
    public List<Symbol> tokenize(String ccl, Multimap<String, Object> data);

    /**
     * Implement a function that converts string operators to the appropriate
     * {@link Operator} object.
     * 
     * @return the transformed operator
     */
    public Operator transformOperator(String token);

    /**
     * Implement a function that converts string values to analogous java
     * objects.
     * 
     * @return the transformed value
     */
    public Object transformValue(String token);

}
