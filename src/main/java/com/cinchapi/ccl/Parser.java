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
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import com.cinchapi.ccl.grammar.ConjunctionSymbol;
import com.cinchapi.ccl.grammar.Expression;
import com.cinchapi.ccl.grammar.KeySymbol;
import com.cinchapi.ccl.grammar.OperatorSymbol;
import com.cinchapi.ccl.grammar.ParenthesisSymbol;
import com.cinchapi.ccl.grammar.PostfixNotationSymbol;
import com.cinchapi.ccl.grammar.Symbol;
import com.cinchapi.ccl.grammar.TimestampSymbol;
import com.cinchapi.ccl.grammar.ValueSymbol;
import com.cinchapi.ccl.syntax.AbstractSyntaxTree;
import com.cinchapi.ccl.syntax.AndTree;
import com.cinchapi.ccl.syntax.ExpressionTree;
import com.cinchapi.ccl.syntax.OrTree;
import com.cinchapi.ccl.util.NaturalLanguage;
import com.cinchapi.common.base.AnyStrings;
import com.cinchapi.common.reflect.Reflection;
import com.cinchapi.concourse.thrift.Operator;
import com.cinchapi.concourse.util.Convert;
import com.cinchapi.concourse.util.StringSplitter;
import com.cinchapi.concourse.util.QuoteAwareStringSplitter;
import com.cinchapi.concourse.util.SplitOption;
import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

/**
 * The {@link Parser} is a tool that operates on various aspects of the
 * language.
 * 
 * @author Jeff Nelson
 */
public final class Parser {

    /**
     * Convert a valid and well-formed list of {@link Symbol} objects into a
     * an {@link AbstractSyntaxTree}.
     * <p>
     * NOTE: This method will group non-conjunctive symbols into
     * {@link Expression} objects.
     * </p>
     * 
     * @param symbols
     * @return the symbols in an AbstractSyntaxTree
     */
    public static AbstractSyntaxTree toAbstractSyntaxTree(
            List<Symbol> symbols) {
        Deque<Symbol> operatorStack = new ArrayDeque<Symbol>();
        Deque<AbstractSyntaxTree> operandStack = new ArrayDeque<AbstractSyntaxTree>();
        symbols = groupExpressions(symbols);
        main: for (Symbol symbol : symbols) {
            if(symbol == ParenthesisSymbol.LEFT) {
                operatorStack.push(symbol);
            }
            else if(symbol == ParenthesisSymbol.RIGHT) {
                while (!operatorStack.isEmpty()) {
                    Symbol popped = operatorStack.pop();
                    if(popped == ParenthesisSymbol.LEFT) {
                        continue main;
                    }
                    else {
                        addAbstractSyntaxTreeNode(operandStack, popped);
                    }
                }
                throw new SyntaxException(AnyStrings.format(
                        "Syntax error in {}: Mismatched parenthesis", symbols));
            }
            else if(symbol instanceof Expression) {
                operandStack.add(new ExpressionTree((Expression) symbol));
            }
            else {
                operatorStack.push(symbol);
            }
        }
        while (!operatorStack.isEmpty()) {
            addAbstractSyntaxTreeNode(operandStack, operatorStack.pop());
        }
        return operandStack.pop();
    }

    /**
     * Convert a valid and well-formed list of {@link Symbol} objects into a
     * Queue in postfix notation.
     * <p>
     * NOTE: This method will group non-conjunctive symbols into
     * {@link Expression} objects.
     * </p>
     * 
     * @param symbols
     * @return the symbols in postfix notation
     */
    public static Queue<PostfixNotationSymbol> toPostfixNotation(
            List<Symbol> symbols) {
        Preconditions.checkState(symbols.size() >= 3,
                "The parsed query %s does not have"
                        + "enough symbols to process. It should have at least 3 symbols but "
                        + "only has %s",
                symbols, symbols.size());
        Deque<Symbol> stack = new ArrayDeque<Symbol>();
        Queue<PostfixNotationSymbol> queue = new LinkedList<PostfixNotationSymbol>();
        symbols = groupExpressions(symbols);
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
     * Convert a valid and well-formed CCL string into aQueue in postfix
     * notation.
     * <p>
     * NOTE: This method will group non-conjunctive symbols into
     * {@link Expression} objects.
     * </p>
     * 
     * @param ccl the string to parse into postfix notation
     * @return the queue in postfix notation
     */
    public static Queue<PostfixNotationSymbol> toPostfixNotation(String ccl) {
        return toPostfixNotation(ccl, null);
    }

    /**
     * Convert a valid and well-formed CCL string into a {@link Queue} in
     * postfix notation. This function will also resolve local references from
     * the CCL string into a {@link Multimap} passed in.
     * <p>
     * NOTE: This method will group non-conjunctive symbols into
     * {@link Expression} objects.
     * </p>
     *
     * @param ccl the CCL string to convert
     * @param data the data to use for local references
     * @return the queue in postfix notation
     */
    public static Queue<PostfixNotationSymbol> toPostfixNotation(String ccl,
            Multimap<String, Object> data) {
        // This method uses a value buffer to correct cases when a string value
        // is specified without quotes (because its a common mistake to make).
        // If an operator other than BETWEEN is specified, we use logic that
        // will buffer all the subsequent tokens until we reach a (parenthesis),
        // (conjunction) or (at) and assume that the tokens belong to the same
        // value.
        data = data == null ? EMPTY_MULTIMAP : data;
        StringSplitter toks = new QuoteAwareStringSplitter(ccl, ' ',
                SplitOption.TOKENIZE_PARENTHESIS);
        List<Symbol> symbols = Lists.newArrayList();
        GuessState guess = GuessState.KEY;
        StringBuilder buffer = null;
        StringBuilder timeBuffer = null;
        while (toks.hasNext()) {
            String tok = toks.next();
            if(tok.equals("(") || tok.equals(")")) {
                addBufferedValue(buffer, symbols);
                addBufferedTime(timeBuffer, symbols);
                symbols.add(ParenthesisSymbol.parse(tok));
            }
            else if(tok.equalsIgnoreCase("&&") || tok.equalsIgnoreCase("&")
                    || tok.equalsIgnoreCase("and")) {
                addBufferedValue(buffer, symbols);
                addBufferedTime(timeBuffer, symbols);
                symbols.add(ConjunctionSymbol.AND);
                guess = GuessState.KEY;
            }
            else if(tok.equalsIgnoreCase("||") || tok.equalsIgnoreCase("or")) {
                addBufferedValue(buffer, symbols);
                addBufferedTime(timeBuffer, symbols);
                symbols.add(ConjunctionSymbol.OR);
                guess = GuessState.KEY;
            }
            else if(TIMESTAMP_PIVOT_TOKENS.contains(tok.toLowerCase())) {
                addBufferedValue(buffer, symbols);
                guess = GuessState.TIMESTAMP;
                timeBuffer = new StringBuilder();
            }
            else if(tok.equalsIgnoreCase("where")) {
                continue;
            }
            else if(StringUtils.isBlank(tok)) {
                continue;
            }
            else if(guess == GuessState.KEY) {
                symbols.add(new KeySymbol(tok));
                guess = GuessState.OPERATOR;
            }
            else if(guess == GuessState.OPERATOR) {
                OperatorSymbol symbol = new OperatorSymbol(
                        Convert.stringToOperator(tok));
                symbols.add(symbol);
                if(symbol.operator() != Operator.BETWEEN) {
                    buffer = new StringBuilder();
                }
                guess = GuessState.VALUE;
            }
            else if(guess == GuessState.VALUE) {
                // CON-321: Perform local resolution for variable
                if(tok.charAt(0) == '$') {
                    String var = tok.substring(1);
                    try {
                        tok = Iterables.getOnlyElement(data.get(var))
                                .toString();
                    }
                    catch (IllegalArgumentException e) {
                        String err = "Unable to resolve variable {} because multiple values exist locally: {}";
                        throw new IllegalStateException(
                                AnyStrings.format(err, tok, data.get(var)));
                    }
                    catch (NoSuchElementException e) {
                        String err = "Unable to resolve variable {} because no values exist locally";
                        throw new IllegalStateException(
                                AnyStrings.format(err, tok));
                    }
                }
                else if(tok.length() > 2 && tok.charAt(0) == '\\'
                        && tok.charAt(1) == '$') {
                    tok = tok.substring(1);
                }
                if(buffer != null) {
                    buffer.append(tok).append(" ");
                }
                else {
                    symbols.add(new ValueSymbol(
                            Convert.javaToThrift(Convert.stringToJava(tok))));
                }
            }
            else if(guess == GuessState.TIMESTAMP) {
                timeBuffer.append(tok).append(" ");
            }
            else {
                throw new IllegalStateException("Cannot properly parse " + tok);
            }
        }
        addBufferedValue(buffer, symbols);
        addBufferedTime(timeBuffer, symbols);
        return toPostfixNotation(symbols);
    }

    /**
     * Go through a list of symbols and group the expressions together in a
     * {@link Expression} object.
     * 
     * @param symbols
     * @return the expression
     */
    protected static List<Symbol> groupExpressions(List<Symbol> symbols) { // visible
                                                                           // for
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
                    if(operator.operator() == Operator.BETWEEN) {
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
     * An the appropriate {@link AbstractSyntaxTree} node to the {@code stack}
     * based on
     * {@code operator}.
     * 
     * @param stack
     * @param operator
     */
    private static void addAbstractSyntaxTreeNode(
            Deque<AbstractSyntaxTree> stack, Symbol operator) {
        AbstractSyntaxTree right = stack.pop();
        AbstractSyntaxTree left = stack.pop();
        if(operator == ConjunctionSymbol.AND) {
            stack.push(new AndTree(left, right));
        }
        else {
            stack.push(new OrTree(left, right));
        }
    }

    /**
     * This is a helper method for {@link #toPostfixNotation(String)} that
     * contains the logic to create a ValueSymbol from a buffered value.
     * 
     * @param buffer
     * @param symbols
     */
    private static void addBufferedValue(StringBuilder buffer,
            List<Symbol> symbols) {
        if(buffer != null && buffer.length() > 0) {
            buffer.delete(buffer.length() - 1, buffer.length());
            symbols.add(new ValueSymbol(Convert
                    .javaToThrift(Convert.stringToJava(buffer.toString()))));
            buffer.delete(0, buffer.length());
        }
    }

    private static void addBufferedTime(StringBuilder buffer,
            List<Symbol> symbols) {
        if(buffer != null && buffer.length() > 0) {
            buffer.delete(buffer.length() - 1, buffer.length());
            long ts = NaturalLanguage.parseMicros(buffer.toString());
            symbols.add(new TimestampSymbol(ts));
            buffer.delete(0, buffer.length());
        }
    }

    /**
     * A collection of tokens that indicate the parser should pivot to expecting
     * a timestamp token.
     */
    private final static Set<String> TIMESTAMP_PIVOT_TOKENS = Sets
            .newHashSet("at", "on", "during", "in");

    /**
     * An empty multimap to use in {@link #toPostfixNotation(String, Multimap)}
     * when there is no local data provided against which to resolve.
     */
    private final static Multimap<String, Object> EMPTY_MULTIMAP = HashMultimap
            .create();

    private Parser() {/* noop */}

    /**
     * An enum that tracks what the parser guesses the next token to be in the
     * {@link #toPostfixNotation(String)} method.
     * 
     * @author Jeff Nelson
     */
    private enum GuessState {
        KEY, OPERATOR, TIMESTAMP, VALUE
    }
}
