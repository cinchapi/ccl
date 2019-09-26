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

import com.cinchapi.ccl.grammar.v3.ConjunctionToken;
import com.cinchapi.ccl.grammar.v3.ExpressionToken;
import com.cinchapi.ccl.grammar.v3.KeyToken;
import com.cinchapi.ccl.grammar.v3.OperatorToken;
import com.cinchapi.ccl.grammar.v3.ParenthesisToken;
import com.cinchapi.ccl.grammar.v3.PostfixNotationToken;
import com.cinchapi.ccl.grammar.v3.TimestampToken;
import com.cinchapi.ccl.grammar.v3.Token;
import com.cinchapi.ccl.grammar.v3.ValueToken;
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
     * {@link ExpressionToken} object.
     * 
     * @param symbols
     * @return the expression
     */
    @SuppressWarnings("unchecked")
    public static List<Token> groupExpressions(List<Token> symbols) {
        try {
            List<Token> grouped = Lists.newArrayList();
            ListIterator<Token> it = symbols.listIterator();
            while (it.hasNext()) {
                Token symbol = it.next();
                if(symbol instanceof KeyToken) {
                    KeyToken<String> key = (KeyToken<String>) symbol;
                    // NOTE: We are assuming that the list of symbols is well
                    // formed, and, as such, the next elements will be an
                    // operator and one or more symbols. If this is not the
                    // case, this method will throw a ClassCastException
                    OperatorToken operator = (OperatorToken) it.next();
                    ValueToken<?> value = (ValueToken<?>) it.next();
                    ExpressionToken expression;
                    if(operator.operator().operands() == 2) {
                        ValueToken<?> value2 = (ValueToken<?>) it.next();
                        expression = ExpressionToken.create(key, operator,
                                value, value2);
                    }
                    else {
                        expression = ExpressionToken.create(key, operator,
                                value);
                    }
                    grouped.add(expression);
                }
                else if(symbol instanceof TimestampToken) { // Add the
                                                             // timestamp to the
                                                             // previously
                                                             // generated
                                                             // ExpressionSymbol
                    ExpressionToken prev = (ExpressionToken) Iterables
                            .getLast(grouped);
                    grouped.set(grouped.size() - 1,
                            ExpressionToken.create((TimestampToken) symbol,
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
     * Transform a sequential list of {@link Token} tokens to an {@link Queue}
     * of symbols in {@link PostfixNotationToken postfix notation} that are
     * sorted by the proper order of operations.
     * 
     * @param symbols a sequential list of tokens
     * @return a {@link Queue} of {@link PostfixNotationToken
     *         PostfixNotationSymbols}
     */
    public static Queue<PostfixNotationToken> toPostfixNotation(
            List<Token> symbols) {
        Preconditions.checkState(symbols.size() >= 3,
                "Not enough symbols to process. It should have at least 3 symbols but only has %s",
                symbols, symbols.size());
        Deque<Token> stack = new ArrayDeque<Token>();
        Queue<PostfixNotationToken> queue = new LinkedList<PostfixNotationToken>();
        symbols = Parsing.groupExpressions(symbols);
        for (Token symbol : symbols) {
            if(symbol instanceof ConjunctionToken) {
                while (!stack.isEmpty()) {
                    Token top = stack.peek();
                    if(symbol == ConjunctionToken.OR
                            && (top == ConjunctionToken.OR
                                    || top == ConjunctionToken.AND)) {
                        queue.add((PostfixNotationToken) stack.pop());
                    }
                    else {
                        break;
                    }
                }
                stack.push(symbol);
            }
            else if(symbol == ParenthesisToken.LEFT) {
                stack.push(symbol);
            }
            else if(symbol == ParenthesisToken.RIGHT) {
                boolean foundLeftParen = false;
                while (!stack.isEmpty()) {
                    Token top = stack.peek();
                    if(top == ParenthesisToken.LEFT) {
                        foundLeftParen = true;
                        break;
                    }
                    else {
                        queue.add((PostfixNotationToken) stack.pop());
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
                queue.add((PostfixNotationToken) symbol);
            }
        }
        while (!stack.isEmpty()) {
            Token top = stack.peek();
            if(top instanceof ParenthesisToken) {
                throw new SyntaxException(AnyStrings.format(
                        "Syntax error in {}: Mismatched parenthesis", symbols));
            }
            else {
                queue.add((PostfixNotationToken) stack.pop());
            }
        }
        return queue;
    }

    /**
     * Go through the list of symbols and break up any {@link ExpressionToken
     * expressions} into individual symbol tokens.
     * 
     * @param symbols
     * @return the list of symbols with no expressions
     */
    public static List<Token> ungroupExpressions(List<Token> symbols) {
        List<Token> ungrouped = Lists.newArrayList();
        symbols.forEach((symbol) -> {
            if(symbol instanceof ExpressionToken) {
                ExpressionToken expression = (ExpressionToken) symbol;
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
