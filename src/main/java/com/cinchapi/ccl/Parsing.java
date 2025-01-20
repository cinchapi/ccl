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

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Queue;

import com.cinchapi.ccl.grammar.ConjunctionSymbol;
import com.cinchapi.ccl.grammar.condition.ExpressionSymbol;
import com.cinchapi.ccl.grammar.KeyTokenSymbol;
import com.cinchapi.ccl.grammar.condition.OperatorSymbol;
import com.cinchapi.ccl.grammar.ParenthesisSymbol;
import com.cinchapi.ccl.grammar.PostfixNotationSymbol;
import com.cinchapi.ccl.grammar.TimestampSymbol;
import com.cinchapi.ccl.grammar.Symbol;
import com.cinchapi.ccl.grammar.ValueTokenSymbol;
import com.cinchapi.common.base.AnyStrings;
import com.cinchapi.common.base.Array;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

/**
 * Util functions for {@link Parser}s.
 * 
 * @author Jeff Nelson
 */
public final class Parsing {

    /**
     * Go through a list of symbols and group the expressions together in a
     * {@link ExpressionSymbol} object.
     * 
     * @param symbols
     * @return the expression
     */
    @SuppressWarnings("unchecked")
    public static List<Symbol> groupExpressions(List<Symbol> symbols) {
        try {
            List<Symbol> grouped = Lists.newArrayList();
            ListIterator<Symbol> it = symbols.listIterator();
            while (it.hasNext()) {
                Symbol symbol = it.next();
                if(symbol instanceof KeyTokenSymbol) {
                    KeyTokenSymbol<String> key = (KeyTokenSymbol<String>) symbol;
                    // NOTE: We are assuming that the list of symbols is well
                    // formed, and, as such, the next elements will be an
                    // operator and one or more symbols. If this is not the
                    // case, this method will throw a ClassCastException
                    OperatorSymbol operator = (OperatorSymbol) it.next();
                    ValueTokenSymbol<?> value = (ValueTokenSymbol<?>) it.next();
                    ExpressionSymbol expression;
                    if(operator.operator().operands() == 2) {
                        ValueTokenSymbol<?> value2 = (ValueTokenSymbol<?>) it.next();
                        expression = ExpressionSymbol.create(key, operator,
                                value, value2);
                    }
                    else {
                        expression = ExpressionSymbol.create(key, operator,
                                value);
                    }
                    grouped.add(expression);
                }
                else if(symbol instanceof TimestampSymbol) { // Add the
                                                             // timestamp to the
                                                             // previously
                                                             // generated
                                                             // ExpressionSymbol
                    ExpressionSymbol prev = (ExpressionSymbol) Iterables
                            .getLast(grouped);
                    grouped.set(grouped.size() - 1,
                            ExpressionSymbol.create((TimestampSymbol) symbol,
                                    prev.key(), prev.operator(),
                                    prev.values().toArray(Array.containing())));
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
     * Transform a sequential list of {@link Symbol} tokens to an {@link Queue}
     * of symbols in {@link PostfixNotationSymbol postfix notation} that are
     * sorted by the proper order of operations.
     * 
     * @param symbols a sequential list of tokens
     * @return a {@link Queue} of {@link PostfixNotationSymbol
     *         PostfixNotationSymbols}
     */
    public static Queue<PostfixNotationSymbol> toPostfixNotation(
            List<Symbol> symbols) {
        Preconditions.checkState(symbols.size() >= 3,
                "Not enough symbols to process. It should have at least 3 symbols but only has %s",
                symbols, symbols.size());
        Deque<Symbol> stack = new ArrayDeque<Symbol>();
        Queue<PostfixNotationSymbol> queue = new LinkedList<PostfixNotationSymbol>();
        symbols = Parsing.groupExpressions(symbols);
        for (Symbol symbol : symbols) {
            if(symbol instanceof ConjunctionSymbol) {
                while (!stack.isEmpty()) {
                    Symbol top = stack.peek();
                    if(symbol == ConjunctionSymbol.OR
                            && (top == ConjunctionSymbol.OR
                                    || top == ConjunctionSymbol.AND)) {
                        queue.add((PostfixNotationSymbol) stack.pop());
                    }
                    else {
                        break;
                    }
                }
                stack.push(symbol);
            }
            else if(symbol == ParenthesisSymbol.LEFT) {
                stack.push(symbol);
            }
            else if(symbol == ParenthesisSymbol.RIGHT) {
                boolean foundLeftParen = false;
                while (!stack.isEmpty()) {
                    Symbol top = stack.peek();
                    if(top == ParenthesisSymbol.LEFT) {
                        foundLeftParen = true;
                        break;
                    }
                    else {
                        queue.add((PostfixNotationSymbol) stack.pop());
                    }
                }
                if(!foundLeftParen) {
                    throw new SyntaxException(AnyStrings.format(
                            "Syntax error in {}: Mismatched parenthesis",
                            symbols));
                }
                else {
                    stack.pop();
                }
            }
            else {
                queue.add((PostfixNotationSymbol) symbol);
            }
        }
        while (!stack.isEmpty()) {
            Symbol top = stack.peek();
            if(top instanceof ParenthesisSymbol) {
                throw new SyntaxException(AnyStrings.format(
                        "Syntax error in {}: Mismatched parenthesis", symbols));
            }
            else {
                queue.add((PostfixNotationSymbol) stack.pop());
            }
        }
        return queue;
    }

    /**
     * Go through the list of symbols and break up any {@link ExpressionSymbol
     * expressions} into individual symbol tokens.
     * 
     * @param symbols
     * @return the list of symbols with no expressions
     */
    public static List<Symbol> ungroupExpressions(List<Symbol> symbols) {
        List<Symbol> ungrouped = Lists.newArrayList();
        symbols.forEach((symbol) -> {
            if(symbol instanceof ExpressionSymbol) {
                ExpressionSymbol expression = (ExpressionSymbol) symbol;
                ungrouped.add(expression.key());
                ungrouped.add(expression.operator());
                ungrouped.addAll(expression.values());
                if(expression.timestamp().timestamp() > 0) {
                    ungrouped.add(expression.timestamp());
                }
            }
            else {
                ungrouped.add(symbol);
            }
        });
        return ungrouped;
    }
}
