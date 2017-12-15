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

import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.function.Function;

import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.ThreadSafe;

import com.cinchapi.ccl.grammar.Expression;
import com.cinchapi.ccl.grammar.KeySymbol;
import com.cinchapi.ccl.grammar.PostfixNotationSymbol;
import com.cinchapi.ccl.grammar.Symbol;
import com.cinchapi.ccl.syntax.AbstractSyntaxTree;
import com.cinchapi.ccl.type.Operator;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

/**
 * A {@link Parser} is a stateful object that transforms raw CCL strings into
 * organized structures that can be logically evaluated.
 *
 * @author Jeff Nelson
 */
@ThreadSafe
@Immutable
public abstract class Parser {

    /**
     * Return a new {@link Parser} for the {@code ccl} statement that uses the
     * {@code valueTransformFunction} and {@code operatorTransformFunction}.
     * 
     * @param ccl
     * @param valueTransformFunction
     * @param operatorTransformFunction
     * @return the {@link Parser}
     */
    public static Parser newParser(String ccl,
            Function<String, Object> valueTransformFunction,
            Function<String, Operator> operatorTransformFunction) {
        return newParser(ccl, ImmutableMultimap.of(), valueTransformFunction,
                operatorTransformFunction);
    }

    /**
     * Return a new {@link Parser} for the {@code ccl} statement that uses the
     * {@code data} for location resolution and the
     * {@code valueTransformFunction} and {@code operatorTransformFunction}.
     * 
     * @param ccl
     * @param data
     * @param valueTransformFunction
     * @param operatorTransformFunction
     * @return the {@link Parser}
     */
    public static Parser newParser(String ccl, Multimap<String, Object> data,
            Function<String, Object> valueTransformFunction,
            Function<String, Operator> operatorTransformFunction) {
        return new ConcourseParser(ccl, data, valueTransformFunction,
                operatorTransformFunction);
    }

    /**
     * The ccl statement being parsed.
     */
    protected final String ccl;

    /**
     * The dataset used for location resolution.
     */
    protected final Multimap<String, Object> data;

    /**
     * Construct a new instance.
     * 
     * @param ccl
     * @param data
     */
    public Parser(String ccl, Multimap<String, Object> data) {
        this.ccl = ccl;
        this.data = data;
    }

    /**
     * Return {@link Analysis} about the {@link #tokenize(String) tokenized} CCL
     * statement.
     * 
     * @param tokens
     * @return the {@link Analysis}
     */
    public Analysis analyze() {
        return new Analysis() {

            @Override
            public Set<String> keys() {
                List<Symbol> tokens = tokenize();
                Set<String> keys = Sets
                        .newLinkedHashSetWithExpectedSize(tokens.size());
                tokens.forEach((symbol) -> {
                    if(symbol instanceof Expression) {
                        keys.add(((Expression) symbol).raw().key());
                    }
                    else if(symbol instanceof KeySymbol) {
                        keys.add(((KeySymbol) symbol).key());
                    }
                });
                return Collections.unmodifiableSet(keys);
            }

        };
    }

    /**
     * Transform a sequential list of {@link Symbol} tokens to an {@link Queue}
     * of symbols in {@link PostfixNotationSymbol postfix notation} that are
     * sorted by the proper order of operations.
     * 
     * @return a {@link Queue} of {@link PostfixNotationSymbol
     *         PostfixNotationSymbols}
     */
    public abstract Queue<PostfixNotationSymbol> order();

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
    public abstract AbstractSyntaxTree parse();

    /**
     * Convert a CCL statement to a list of {@link Symbol} tokens and bind any
     * local variables using the provided {@code data}.
     * 
     * @param ccl the CCL statement
     * @param data the data to use for binding local variables
     * @return a list of {@link Symbol} tokens
     */
    public abstract List<Symbol> tokenize();

    /**
     * Implement a function that converts string operators to the appropriate
     * {@link Operator} object.
     * 
     * @return the transformed operator
     */
    protected abstract Operator transformOperator(String token);

    /**
     * Implement a function that converts string values to analogous java
     * objects.
     * 
     * @return the transformed value
     */
    protected abstract Object transformValue(String token);

    /**
     * A collection of insights about a CCL statement that is
     * {@link #analyze(String)} by this {@link Parser}.
     * 
     * @author Jeff Nelson
     */
    public interface Analysis {

        /**
         * Return an ordered list of keys that are included in the CCL
         * statement.
         * 
         * @return the included keys
         */
        public Set<String> keys();
    }

}
