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
import java.util.ListIterator;
import java.util.Queue;
import java.util.function.Function;

import javax.annotation.concurrent.ThreadSafe;

import com.cinchapi.ccl.grammar.Expression;
import com.cinchapi.ccl.grammar.KeySymbol;
import com.cinchapi.ccl.grammar.OperatorSymbol;
import com.cinchapi.ccl.grammar.PostfixNotationSymbol;
import com.cinchapi.ccl.grammar.Symbol;
import com.cinchapi.ccl.grammar.TimestampSymbol;
import com.cinchapi.ccl.grammar.ValueSymbol;
import com.cinchapi.ccl.syntax.AbstractSyntaxTree;
import com.cinchapi.ccl.type.Operator;
import com.cinchapi.common.reflect.Reflection;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
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
     * Go through a list of symbols and group the expressions together in a
     * {@link Expression} object.
     * 
     * @param symbols
     * @return the expression
     */
    public static List<Symbol> group(List<Symbol> symbols) { // visible for
                                                             // testing
        try {
            List<Symbol> grouped = Lists.newArrayList();
            ListIterator<Symbol> it = symbols.listIterator();
            while (it.hasNext()) {
                Symbol symbol = it.next();
                if(symbol instanceof KeySymbol) {
                    // NOTE: We are assuming that the list of symbols is well
                    // formed, and, as such, the next elements will be an
                    // operator and one or more symbols. If this is not the
                    // case, this method will throw a ClassCastException
                    OperatorSymbol operator = (OperatorSymbol) it.next();
                    ValueSymbol value = (ValueSymbol) it.next();
                    Expression expression;
                    if(operator.operator().operands() == 2) {
                        ValueSymbol value2 = (ValueSymbol) it.next();
                        expression = new Expression((KeySymbol) symbol,
                                operator, value, value2);
                    }
                    else {
                        expression = new Expression((KeySymbol) symbol,
                                operator, value);
                    }
                    grouped.add(expression);
                }
                else if(symbol instanceof TimestampSymbol) { // Add the
                                                             // timestamp to the
                                                             // previously
                                                             // generated
                                                             // Expression
                    Reflection.set("timestamp", symbol,
                            Iterables.getLast(grouped)); // (authorized)
                }
                else {
                    grouped.add(symbol);
                }
            }
            return grouped;
        }
        catch (ClassCastException e) {
            throw new SyntaxException(e.getMessage());
        }
    }

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
