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
package com.cinchapi.ccl.grammar;

import java.io.StringReader;
import java.util.Set;

import com.cinchapi.ccl.generated.GrammarConstants;
import com.cinchapi.ccl.generated.GrammarTokenManager;
import com.cinchapi.ccl.generated.SimpleCharStream;
import com.cinchapi.ccl.generated.Token;
import com.cinchapi.common.base.AnyStrings;
import com.cinchapi.common.reflect.Reflection;
import com.google.common.collect.ImmutableSet;

/**
 * A {@link Symbol} that represents a scalar value.
 * <p>
 * The text of a {@link ValueSymbol} is its value in CCL. A string prints as
 * text that the parser reads back as the same string, given a value transform
 * that reads quoted text as the string within the quotes. The string prints
 * unquoted when the parser reads it, unquoted, as the same one string. The
 * exception is a string that starts with {@code $} or {@code \$}, which the
 * parser reads as a local reference or as its escape. When the parser reads
 * such a string unquoted as one value, it prints unquoted, so the parser
 * resolves it the way a {@link com.cinchapi.ccl.Compiler Compiler} resolves
 * this symbol.
 * </p>
 * <p>
 * Any other string prints within quotes. So does a string that reads as a
 * number or that backticks wrap, so that it reads back as a string rather than
 * as a number or a Tag. The quotes are single quotes when they can hold the
 * string and either the string holds a double quote and no single quote, or
 * double quotes cannot hold it. Otherwise they are double quotes.
 * </p>
 * <p>
 * Within quotes, the parser reads {@code \"} as {@code "}, and the lexer reads
 * a backslash as an escape for the character after it. So a quoted string
 * prints with the backslashes that keep it intact. Double quotes cannot hold a
 * string in which a double quote follows an odd number of backslashes, or that
 * ends with an even, nonzero number of them. Single quotes cannot hold a string
 * that holds a single quote or that ends with an odd number of backslashes.
 * </p>
 * <p>
 * CCL cannot express the empty string, which prints as empty text and does not
 * parse. A string that neither quote character can hold, such as
 * {@code it's \\}, prints within double quotes, and its text does not parse
 * back as the string.
 * </p>
 *
 * @author Jeff Nelson
 */
public class ValueSymbol extends ValueTokenSymbol<Object> {

    /**
     * Return the CCL text for {@code value}, as the {@link ValueSymbol} class
     * documentation describes it.
     *
     * @param value the value to print
     * @return the text for {@code value}
     */
    private static Object escape(Object value) {
        if(value instanceof String && ((String) value).startsWith("`")
                && ((String) value).endsWith("`")) {
            // CON-167: surround by quotes so the backticks are not interpreted
            // as indicators of an encoded Tag. This case would happen if the
            // user manually placed text wrapped in backticks in the Criteria
            // instead of using the #Tag.create() method.
            return quote((String) value);
        }
        else if((value instanceof String || value.getClass().getName()
                .equals("com.cinchapi.concourse.Tag"))
                && AnyStrings.tryParseNumberStrict(value.toString()) != null) {
            // CON-628: Must wrap numeric strings/tags within quotes so they are
            // re-interpreted as the original type
            char wrap = value instanceof String ? '"' : '`';
            return new StringBuilder().append(wrap).append(value).append(wrap)
                    .toString();
        }
        else if(value.getClass().getName()
                .equals("com.cinchapi.concourse.Tag")) {
            return AnyStrings.format("`{}`", value);
        }
        else if(value.getClass().getName()
                .equals("com.cinchapi.concourse.Timestamp")) {
            // NOTE: See com.cinchapi.concourse.util.Convert to see the
            // conventions for the way that a Timestamp is parsed from a long
            // (microseconds) or string (natural language or date time format)
            // wrapped in vertical bars.
            long micros = Reflection.call(value, "getMicros"); // (Authorized)
            return AnyStrings.format("|{}|", micros);
        }
        else if(value instanceof String && ((String) value).isEmpty()) {
            // Quotes in CCL hold at least one character, so no text reads back
            // as the empty string. Empty text fails to parse instead of reading
            // back as another value.
            return value;
        }
        else if(value instanceof String) {
            String string = (String) value;
            return readsBackUnquoted(string) ? string : quote(string);
        }
        else {
            return value;
        }
    }

    /**
     * Return whether the CCL lexer reads all of {@code text} as one token of a
     * kind in {@code kinds}.
     *
     * @param text the text to read
     * @param kinds kinds of token from {@link GrammarConstants}
     * @return {@code true} if the first token that the lexer reads from
     *         {@code text} has a kind in {@code kinds} and spans all of
     *         {@code text}
     */
    private static boolean isOneToken(String text, Set<Integer> kinds) {
        if(text.isEmpty() || text.charAt(0) == ' ' || text.charAt(0) == '\t'
                || text.charAt(0) == '\r') {
            // The lexer skips a leading space or tab, so no token spans the
            // text, and it throws on a carriage return, which starts no token.
            return false;
        }
        else {
            // The extra character of buffer lets the lexer reach the end of the
            // text without growing its buffer.
            Token token = new GrammarTokenManager(new SimpleCharStream(
                    new StringReader(text), 1, 1, text.length() + 1))
                            .getNextToken();
            return kinds.contains(token.kind) && token.image.equals(text);
        }
    }

    /**
     * Return {@code string} within the quotes that the {@link ValueSymbol}
     * class documentation chooses for it.
     *
     * @param string a nonempty string
     * @return the quoted text for {@code string}
     */
    private static String quote(String string) {
        String text = within(string, '"');
        String single = within(string, '\'');
        boolean holdsOnlyDoubleQuotes = string.indexOf('"') >= 0
                && string.indexOf('\'') < 0;
        if(isOneToken(single, QUOTED_VALUE_TOKENS) && (holdsOnlyDoubleQuotes
                || !isOneToken(text, QUOTED_VALUE_TOKENS))) {
            text = single;
        }
        return text;
    }

    /**
     * Return whether {@code string} prints unquoted, because the parser reads
     * it, unquoted, as the same one string or as the same local reference.
     *
     * @param string the string to print
     * @return {@code true} if {@code string} can print unquoted
     */
    private static boolean readsBackUnquoted(String string) {
        // The parser reads \@ as @ in an unquoted value that is not a local
        // reference, and the value transform reads text within quotes as the
        // text inside them.
        return isOneToken(string, UNQUOTED_VALUE_TOKENS)
                && !AnyStrings.isWithinQuotes(string)
                && (string.startsWith("$") || string.startsWith("\\$")
                        || !string.contains("\\@"));
    }

    /**
     * Return {@code string} between two {@code quote} characters, with the
     * backslashes that let the parser read it back. The parser drops a
     * backslash that comes before a double quote. So the text adds one before
     * each double quote that follows a backslash or sits within double quotes.
     * It also adds one at the end of a double-quoted string that ends with a
     * backslash, which would otherwise escape the closing quote.
     *
     * @param string the string to quote
     * @param quote the quote character, {@code "} or {@code '}
     * @return the quoted text, which the lexer reads as one quoted value only
     *         if {@code quote} can hold {@code string}
     */
    private static String within(String string, char quote) {
        StringBuilder text = new StringBuilder(string.length() + 3);
        text.append(quote);
        for (int i = 0; i < string.length(); ++i) {
            char c = string.charAt(i);
            if(c == '"' && (quote == '"'
                    || (i > 0 && string.charAt(i - 1) == '\\'))) {
                text.append('\\');
            }
            text.append(c);
        }
        if(quote == '"' && string.endsWith("\\")) {
            text.append('\\');
        }
        return text.append(quote).toString();
    }

    /**
     * The kinds of token that the grammar reads as one unquoted value, such as
     * {@code jeff} in {@code name = jeff}.
     */
    private static final Set<Integer> UNQUOTED_VALUE_TOKENS = ImmutableSet.of(
            GrammarConstants.NUMERIC, GrammarConstants.SIGNED_INTEGER,
            GrammarConstants.SIGNED_DECIMAL, GrammarConstants.ALPHANUMERIC,
            GrammarConstants.PERIOD_SEPARATED_STRING,
            GrammarConstants.ASTERISK_SUFFIXED_STRING,
            GrammarConstants.NON_ALPHANUMERIC_AND_ALPHANUMERIC);

    /**
     * The kinds of token that the grammar reads as one quoted value, such as
     * {@code "jeff nelson"} in {@code name = "jeff nelson"}.
     */
    private static final Set<Integer> QUOTED_VALUE_TOKENS = ImmutableSet
            .of(GrammarConstants.QUOTED_STRING);

    /**
     * Construct a new instance.
     *
     * @param value
     */
    public ValueSymbol(Object value) {
        super(value);
    }

    @Override
    public String toString() {
        return escape(value).toString();
    }

}
