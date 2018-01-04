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
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.Set;
import java.util.function.Function;

import javax.annotation.concurrent.ThreadSafe;

import org.apache.commons.lang.StringUtils;

import com.cinchapi.ccl.grammar.ConjunctionSymbol;
import com.cinchapi.ccl.grammar.KeySymbol;
import com.cinchapi.ccl.grammar.OperatorSymbol;
import com.cinchapi.ccl.grammar.ParenthesisSymbol;
import com.cinchapi.ccl.grammar.PostfixNotationSymbol;
import com.cinchapi.ccl.grammar.Symbol;
import com.cinchapi.ccl.grammar.TimestampSymbol;
import com.cinchapi.ccl.grammar.ValueSymbol;
import com.cinchapi.ccl.syntax.AbstractSyntaxTree;
import com.cinchapi.ccl.type.Operator;
import com.cinchapi.ccl.util.NaturalLanguage;
import com.cinchapi.common.base.AnyStrings;
import com.cinchapi.common.base.QuoteAwareStringSplitter;
import com.cinchapi.common.base.SplitOption;
import com.cinchapi.common.base.StringSplitter;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

/**
 * A {@link Parser} implemented using custom logic.
 *
 * @author Jeff Nelson
 * @deprecated Deprecated since version 2.2.0 in favor of {@link JavaCCParser}.
 */
@ThreadSafe
@Deprecated
final class ConcourseParser extends Parser {

    /**
     * A collection of tokens that indicate the parser should pivot to expecting
     * a timestamp token.
     */
    private final static Set<String> TIMESTAMP_PIVOT_TOKENS = Sets
            .newHashSet("at", "on", "during", "in");

    private final Function<String, Object> valueTransformFunction;
    private final Function<String, Operator> operatorTransformFunction;

    /**
     * Construct a new instance.
     * 
     * @param ccl
     * @param data
     * @param valueTransformFunction
     * @param operatorTransformFunction
     */
    protected ConcourseParser(String ccl, Multimap<String, Object> data,
            Function<String, Object> valueTransformFunction,
            Function<String, Operator> operatorTransformFunction) {
        super(ccl, data);
        this.valueTransformFunction = valueTransformFunction;
        this.operatorTransformFunction = operatorTransformFunction;
    }

    @Override
    public Queue<PostfixNotationSymbol> order() {
        return Parsing.toPostfixNotation(tokenize());
    }

    @Override
    public AbstractSyntaxTree parse() {
        return Parsing.toAbstractSyntaxTree(tokenize());
    }

    @Override
    public List<Symbol> tokenize() {
        // This method uses a value buffer to correct cases when a string value
        // is specified without quotes (because its a common mistake to make).
        // If an operator other than BETWEEN is specified, we use logic that
        // will buffer all the subsequent tokens until we reach a (parenthesis),
        // (conjunction) or (at) and assume that the tokens belong to the same
        // value.
        StringSplitter toks = new QuoteAwareStringSplitter(ccl, ' ',
                SplitOption.TOKENIZE_PARENTHESIS);
        List<Symbol> symbols = Lists.newArrayList();
        TokenTypeGuess guess = TokenTypeGuess.KEY;
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
                guess = TokenTypeGuess.KEY;
            }
            else if(tok.equalsIgnoreCase("||") || tok.equalsIgnoreCase("or")) {
                addBufferedValue(buffer, symbols);
                addBufferedTime(timeBuffer, symbols);
                symbols.add(ConjunctionSymbol.OR);
                guess = TokenTypeGuess.KEY;
            }
            else if(TIMESTAMP_PIVOT_TOKENS.contains(tok.toLowerCase())) {
                addBufferedValue(buffer, symbols);
                guess = TokenTypeGuess.TIMESTAMP;
                timeBuffer = new StringBuilder();
            }
            else if(tok.equalsIgnoreCase("where")) {
                continue;
            }
            else if(StringUtils.isBlank(tok)) {
                continue;
            }
            else if(guess == TokenTypeGuess.KEY) {
                symbols.add(new KeySymbol(tok));
                guess = TokenTypeGuess.OPERATOR;
            }
            else if(guess == TokenTypeGuess.OPERATOR) {
                OperatorSymbol symbol = new OperatorSymbol(
                        transformOperator(tok));
                symbols.add(symbol);
                if(symbol.operator().operands() == 1) {
                    buffer = new StringBuilder();
                }
                guess = TokenTypeGuess.VALUE;
            }
            else if(guess == TokenTypeGuess.VALUE) {
                // CON-321: Perform local resolution for variable
                if(tok.charAt(0) == '$') {
                    String var = tok.substring(1);
                    try {
                        tok = Iterables.getOnlyElement(data.get(var))
                                .toString();
                    }
                    catch (IllegalArgumentException e) {
                        String err = "Unable to resolve variable {} because multiple values exist locally: {}";
                        throw new SyntaxException(
                                AnyStrings.format(err, tok, data.get(var)));
                    }
                    catch (NoSuchElementException e) {
                        String err = "Unable to resolve variable {} because no values exist locally";
                        throw new SyntaxException(AnyStrings.format(err, tok));
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
                    symbols.add(new ValueSymbol(transformValue(tok)));
                }
            }
            else if(guess == TokenTypeGuess.TIMESTAMP) {
                timeBuffer.append(tok).append(" ");
            }
            else {
                throw new SyntaxException("Cannot properly parse " + tok);
            }
        }
        addBufferedValue(buffer, symbols);
        addBufferedTime(timeBuffer, symbols);
        return symbols;
    }

    @Override
    public Operator transformOperator(String token) {
        return operatorTransformFunction.apply(token);
    }

    @Override
    public Object transformValue(String token) {
        return valueTransformFunction.apply(token);
    }

    /**
     * This is a helper method for {@link #tokenize(String, Multimap)} that
     * contains logic to create a {@link TimestampSymbol} from a buffered value.
     * 
     * @param buffer
     * @param symbols
     */
    private void addBufferedTime(StringBuilder buffer, List<Symbol> symbols) {
        if(buffer != null && buffer.length() > 0) {
            buffer.delete(buffer.length() - 1, buffer.length());
            long ts = NaturalLanguage.parseMicros(buffer.toString());
            symbols.add(new TimestampSymbol(ts));
            buffer.delete(0, buffer.length());
        }
    }

    /**
     * This is a helper method for {@link #tokenize(String, Multimap)} that
     * contains the logic to create a {@link ValueSymbol} from a buffered value.
     * 
     * @param buffer
     * @param symbols
     */
    private void addBufferedValue(StringBuilder buffer, List<Symbol> symbols) {
        if(buffer != null && buffer.length() > 0) {
            buffer.delete(buffer.length() - 1, buffer.length());
            symbols.add(new ValueSymbol(transformValue(buffer.toString())));
            buffer.delete(0, buffer.length());
        }
    }

    /**
     * An enum that tracks what the parser guesses the next token to be in the
     * {@link #toPostfixNotation(String)} method.
     * 
     * @author Jeff Nelson
     */
    private enum TokenTypeGuess {
        KEY, OPERATOR, TIMESTAMP, VALUE
    }

}
