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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import com.cinchapi.ccl.grammar.ConjunctionSymbol;
import com.cinchapi.ccl.grammar.ExpressionSymbol;
import com.cinchapi.ccl.grammar.KeyTokenSymbol;
import com.cinchapi.ccl.grammar.OperatorSymbol;
import com.cinchapi.ccl.grammar.ParenthesisSymbol;
import com.cinchapi.ccl.grammar.PostfixNotationSymbol;
import com.cinchapi.ccl.grammar.ScopeEndSymbol;
import com.cinchapi.ccl.grammar.ScopeSymbol;
import com.cinchapi.ccl.grammar.TimestampSymbol;
import com.cinchapi.ccl.grammar.Symbol;
import com.cinchapi.ccl.grammar.ValueTokenSymbol;
import com.cinchapi.common.base.AnyStrings;
import com.cinchapi.common.base.Array;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

/**
 * Util functions for {@link Parser}s.
 * 
 * @author Jeff Nelson
 */
public final class Parsing {

    /**
     * The form of a {@code within} duration: a whole or decimal amount,
     * optional whitespace, then a unit name.
     */
    private static final Pattern DURATION = Pattern
            .compile("(\\d+(?:\\.\\d+)?)\\s*(\\p{L}+)");

    /**
     * The exact number of nanoseconds in one unit, for each lower case unit
     * name that a {@code within} duration accepts.
     */
    private static final Map<String, BigDecimal> DURATION_UNIT_NANOS =
            ImmutableMap.<String, BigDecimal> builder()
                    .putAll(aliases(ChronoUnit.NANOS, "ns", "nsec", "nsecs",
                            "nano", "nanos", "nanosecond", "nanoseconds"))
                    .putAll(aliases(ChronoUnit.MICROS, "us", "\u00b5s",
                            "\u03bcs", "usec", "usecs", "micro", "micros",
                            "microsecond", "microseconds"))
                    .putAll(aliases(ChronoUnit.MILLIS, "ms", "msec", "msecs",
                            "milli", "millis", "millisecond", "milliseconds"))
                    .putAll(aliases(ChronoUnit.SECONDS, "s", "sec", "secs",
                            "second", "seconds"))
                    .putAll(aliases(ChronoUnit.MINUTES, "m", "min", "mins",
                            "minute", "minutes"))
                    .putAll(aliases(ChronoUnit.HOURS, "h", "hr", "hrs",
                            "hour", "hours"))
                    .putAll(aliases(ChronoUnit.DAYS, "d", "day", "days"))
                    .putAll(aliases(ChronoUnit.WEEKS, "w", "wk", "wks",
                            "week", "weeks"))
                    .putAll(aliases(ChronoUnit.MONTHS, "mo", "mos", "month",
                            "months"))
                    .putAll(aliases(ChronoUnit.YEARS, "y", "yr", "yrs",
                            "year", "years"))
                    .build();

    /**
     * The number of nanoseconds in {@link Long#MAX_VALUE} milliseconds, which
     * is the longest duration that a {@code within} clause resolves to.
     */
    private static final BigInteger MAX_DURATION_NANOS = BigInteger
            .valueOf(Long.MAX_VALUE).multiply(BigInteger.valueOf(1_000_000));

    /**
     * The number of nanoseconds in one second.
     */
    private static final BigInteger NANOS_PER_SECOND = BigInteger
            .valueOf(1_000_000_000);

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
                        ValueTokenSymbol<?> value2 = (ValueTokenSymbol<?>) it
                                .next();
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
     * Return the duration of a {@code within} clause, such as
     * {@code "5 seconds"}, {@code "500ms"} or {@code "1.5 h"}.
     * <p>
     * A duration is a whole or decimal amount, optional whitespace, and one
     * unit name in any letter case. The units run from nanoseconds to years,
     * and each has several names, such as {@code s}, {@code sec} and
     * {@code seconds}. The name {@code m} means minutes and {@code mo} means
     * months. A month and a year have the estimated lengths that
     * {@link ChronoUnit#getDuration()} gives them. Whitespace around the
     * duration is ignored. The result drops any fraction of a nanosecond, and a
     * duration longer than {@link Long#MAX_VALUE} milliseconds resolves to
     * {@link Long#MAX_VALUE} milliseconds.
     * </p>
     *
     * @param token the image of a quoted string token, including its enclosing
     *            quotes
     * @return the {@link Duration}, always at least 1 nanosecond
     * @throws SyntaxException if the duration does not have that form, names an
     *             unknown unit, or is shorter than 1 nanosecond
     */
    public static Duration parseDuration(String token) {
        String duration = token.substring(1, token.length() - 1).trim();
        Matcher matcher = DURATION.matcher(duration);
        BigDecimal unitNanos = matcher.matches()
                ? DURATION_UNIT_NANOS
                        .get(matcher.group(2).toLowerCase(Locale.ROOT))
                : null;
        BigInteger nanos = unitNanos != null
                ? new BigDecimal(matcher.group(1)).multiply(unitNanos)
                        .toBigInteger()
                : BigInteger.ZERO;
        if(nanos.signum() > 0) {
            // The cap keeps Duration#toMillis, which turns a timeout into the
            // milliseconds that the Concourse server takes, from overflowing.
            BigInteger[] secondsAndNanos = nanos.min(MAX_DURATION_NANOS)
                    .divideAndRemainder(NANOS_PER_SECOND);
            return Duration.ofSeconds(secondsAndNanos[0].longValue(),
                    secondsAndNanos[1].longValue());
        }
        else {
            throw new SyntaxException(AnyStrings.format(
                    "A within duration must be an amount and a unit of time, "
                            + "such as \"5 s\", of at least 1 ns, but got "
                            + "\"{}\"",
                    duration));
        }
    }

    /**
     * Resolve a CCL local-reference value &mdash; either a {@code $name}
     * placeholder substituted from {@code data} or a {@code \$name} escape
     * unwrapped to its literal form.
     * <p>
     * Returns the substituted/unescaped value when {@code value} matches
     * one of those two forms, and {@code null} when {@code value} is
     * neither &mdash; so callers can discriminate "resolved" from "left
     * alone" without re-checking the prefix.
     * </p>
     *
     * @param value the raw value text
     * @param data the locally-bound values
     * @return the substituted value for {@code $name}, the literal
     *         {@code $name} for {@code \$name}, or {@code null} when
     *         {@code value} is not a local reference
     * @throws SyntaxException if {@code value} is a {@code $name}
     *             reference and {@code data} does not contain exactly
     *             one binding for {@code name}
     */
    @Nullable
    public static String resolveLocalReference(String value,
            Multimap<String, Object> data) {
        if(!value.isEmpty() && value.charAt(0) == '$') {
            String var = value.substring(1);
            try {
                return Iterables.getOnlyElement(data.get(var)).toString();
            }
            catch (IllegalArgumentException e) {
                throw new SyntaxException(AnyStrings.format(
                        "Unable to resolve variable {} because multiple "
                                + "values exist locally: {}",
                        value, data.get(var)));
            }
            catch (NoSuchElementException e) {
                throw new SyntaxException(AnyStrings.format(
                        "Unable to resolve variable {} because no values "
                                + "exist locally",
                        value));
            }
        }
        else if(value.length() > 2 && value.charAt(0) == '\\'
                && value.charAt(1) == '$') {
            return value.substring(1);
        }
        else {
            return null;
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
            else if(symbol instanceof ScopeSymbol) {
                // A ScopeSymbol acts as a precedence boundary (like
                // LEFT_PAREN) so operators inside the scoped group cannot
                // be popped out by later operators of lower precedence,
                // while still flowing into the output queue in structural
                // position.
                stack.push(symbol);
                queue.add((PostfixNotationSymbol) symbol);
            }
            else if(symbol == ScopeEndSymbol.INSTANCE) {
                boolean foundBegin = false;
                while (!stack.isEmpty()) {
                    Symbol top = stack.peek();
                    if(top instanceof ScopeSymbol) {
                        foundBegin = true;
                        break;
                    }
                    else if(top instanceof ParenthesisSymbol) {
                        // An unmatched LEFT paren inside a scope means the
                        // scope bracket closes before the paren group does;
                        // report as a scope mismatch rather than letting the
                        // subsequent PostfixNotationSymbol cast fail with
                        // ClassCastException.
                        throw new SyntaxException(AnyStrings.format(
                                "Syntax error in {}: Mismatched scope bracket",
                                symbols));
                    }
                    else {
                        queue.add((PostfixNotationSymbol) stack.pop());
                    }
                }
                if(!foundBegin) {
                    throw new SyntaxException(AnyStrings.format(
                            "Syntax error in {}: Mismatched scope bracket",
                            symbols));
                }
                stack.pop();
                queue.add((PostfixNotationSymbol) symbol);
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
            else if(top instanceof ScopeSymbol) {
                throw new SyntaxException(AnyStrings.format(
                        "Syntax error in {}: Mismatched scope bracket",
                        symbols));
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

    /**
     * Return a {@link Map} from each of {@code names} to the exact number of
     * nanoseconds in one {@code unit}. A month and a year have the estimated
     * lengths that {@link ChronoUnit#getDuration()} gives them.
     *
     * @param unit the {@link ChronoUnit} that each name means
     * @param names the lower case names of {@code unit}
     * @return an immutable {@link Map} from each name to the nanoseconds in one
     *         {@code unit}
     * @throws IllegalArgumentException if {@code names} repeats a name
     */
    private static Map<String, BigDecimal> aliases(ChronoUnit unit,
            String... names) {
        Duration length = unit.getDuration();
        BigDecimal nanos = BigDecimal.valueOf(length.getSeconds())
                .movePointRight(9).add(BigDecimal.valueOf(length.getNano()));
        return Arrays.stream(names).collect(
                ImmutableMap.toImmutableMap(name -> name, name -> nanos));
    }
}
