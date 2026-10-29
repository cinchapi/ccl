/*
 * Copyright (c) 2013-2020 Cinchapi Inc.
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

import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import com.cinchapi.ccl.Compiler;
import com.cinchapi.ccl.syntax.ConditionTree;
import com.cinchapi.common.base.AnyStrings;
import com.cinchapi.concourse.Tag;
import com.cinchapi.concourse.lang.Criteria;
import com.cinchapi.concourse.thrift.Operator;
import com.cinchapi.concourse.util.Convert;
import com.cinchapi.concourse.util.Random;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;

/**
 * Unit tests for {@link ValueSymbol}
 *
 * @author Jeff Nelson
 */
public class ValueSymbolTest {

    /**
     * Print a {@link ValueSymbol} for {@code value}, parse the condition
     * {@code name = } followed by that text, and return the values that the
     * condition holds.
     *
     * @param value the value to print and parse back
     * @return the values of the parsed condition
     */
    private static List<Object> readBack(Object value) {
        ConditionTree tree = (ConditionTree) COMPILER
                .parse("name = " + new ValueSymbol(value));
        return ((ExpressionSymbol) tree.root()).raw().values();
    }

    /**
     * A {@link Compiler} with the value and operator transforms the Concourse
     * driver uses.
     */
    private static final Compiler COMPILER = Compiler
            .create(Convert::stringToJava, Convert::stringToOperator);

    /**
     * <strong>Goal:</strong> Verify that a string value that starts with
     * {@code $} or {@code \$} prints unquoted, as a local reference or its
     * escape, which the parser resolves the way the {@link Compiler} resolves
     * the same symbol.
     * <p>
     * <strong>Start state:</strong> No prior state needed.
     * <p>
     * <strong>Workflow:</strong>
     * <ul>
     * <li>Build {@code name = $target or name = \$target or name = $a\@b} as a
     * {@link Criteria}, where each value is a string.</li>
     * <li>Parse {@link Criteria#ccl()} and {@link Criteria#symbols()}, each
     * with local data that binds {@code target} and {@code a\@b} to
     * {@code jeff}.</li>
     * </ul>
     * <p>
     * <strong>Expected:</strong> Each value prints unquoted, and the two
     * conditions are equal.
     */
    @Test
    public void testDollarSignStringPrintsAsLocalReference() {
        Criteria criteria = Criteria.where().key("name")
                .operator(Operator.EQUALS).value("$target").or().key("name")
                .operator(Operator.EQUALS).value("\\$target").or().key("name")
                .operator(Operator.EQUALS).value("$a\\@b").build();
        Multimap<String, Object> data = ImmutableMultimap.of("target", "jeff",
                "a\\@b", "jeff");
        Assert.assertEquals(
                "name = $target OR name = \\$target OR name = $a\\@b",
                criteria.ccl());
        Assert.assertEquals(COMPILER.parse(criteria.symbols(), data),
                COMPILER.parse(criteria.ccl(), data));
    }

    /**
     * <strong>Goal:</strong> Verify that the CCL of a {@link Criteria} whose
     * string values CCL reads as syntax parses to the condition of its symbols
     * (<a href="https://github.com/cinchapi/ccl/issues/79">GH-79</a>).
     * <p>
     * <strong>Start state:</strong> No prior state needed.
     * <p>
     * <strong>Workflow:</strong>
     * <ul>
     * <li>Build {@code name = a) or name = [a] or name = a;b or name = $id$ or
     * name = 'a'} as a {@link Criteria}, where each value is a string.</li>
     * <li>Parse {@link Criteria#ccl()} and {@link Criteria#symbols()}.</li>
     * </ul>
     * <p>
     * <strong>Expected:</strong> The two conditions are equal.
     */
    @Test
    public void testGH79CriteriaWithStringValuesParsesFromItsText() {
        Criteria criteria = Criteria.where().key("name")
                .operator(Operator.EQUALS).value("a)").or().key("name")
                .operator(Operator.EQUALS).value("[a]").or().key("name")
                .operator(Operator.EQUALS).value("a;b").or().key("name")
                .operator(Operator.EQUALS).value("$id$").or().key("name")
                .operator(Operator.EQUALS).value("'a'").build();
        Assert.assertEquals(COMPILER.parse(criteria.symbols()),
                COMPILER.parse(criteria.ccl()));
    }

    /**
     * <strong>Goal:</strong> Verify that the text of a string value that CCL
     * reads as syntax parses back as the same string (<a
     * href="https://github.com/cinchapi/ccl/issues/79">GH-79</a>).
     * <p>
     * <strong>Start state:</strong> No prior state needed.
     * <p>
     * <strong>Workflow:</strong>
     * <ul>
     * <li>Print a {@link ValueSymbol} for each string that holds a character
     * CCL reads as syntax, such as a parenthesis, a bracket, a semicolon, a
     * comma, whitespace or {@code \@}, or that CCL reads as one token other
     * than a value, such as {@code $id$}, {@code and}, {@code where},
     * {@code ->} or {@code 'a'}.</li>
     * <li>Parse {@code name = } followed by each text.</li>
     * </ul>
     * <p>
     * <strong>Expected:</strong> Each condition holds the one string that was
     * printed.
     */
    @Test
    public void testGH79StringValueThatReadsAsSyntaxParsesBack() {
        List<String> values = ImmutableList.of("a)", "[a]", "a;b", "$id$",
                "'a'", "a b", "a\tb", "a\nb", "a\rb", "\ra", "a,b", "(a)",
                "and", "where", "OR", "from", "contains", "->", "\"a\"",
                "\u201ca\u201d", "a\\@b");
        for (String value : values) {
            Assert.assertEquals(value, ImmutableList.of(value),
                    readBack(value));
        }
    }

    /**
     * <strong>Goal:</strong> Verify that the text of a string value that needs
     * quotes and holds quote characters or backslashes parses back as the same
     * string (<a href="https://github.com/cinchapi/ccl/issues/79">GH-79</a>).
     * <p>
     * <strong>Start state:</strong> No prior state needed.
     * <p>
     * <strong>Workflow:</strong>
     * <ul>
     * <li>Print a {@link ValueSymbol} for strings with a space that hold a
     * double quote, a single quote, both, a backslash before a double quote, or
     * one or two backslashes at the end.</li>
     * <li>Parse {@code name = } followed by each text.</li>
     * </ul>
     * <p>
     * <strong>Expected:</strong> Each condition holds the one string that was
     * printed.
     */
    @Test
    public void testGH79StringValueWithQuotesOrBackslashesParsesBack() {
        List<String> values = ImmutableList.of("say \"hi\"", "it's a",
                "it's \"x\"", "a \\\"b", "it's \\\\\"x", "C:\\Program Files\\",
                "a \\\\", "\"a\" \\");
        for (String value : values) {
            Assert.assertEquals(value, ImmutableList.of(value),
                    readBack(value));
        }
    }

    /**
     * <strong>Goal:</strong> Verify that a string value that CCL cannot express
     * prints as the text that {@link ValueSymbol} documents for it.
     * <p>
     * <strong>Start state:</strong> No prior state needed.
     * <p>
     * <strong>Workflow:</strong>
     * <ul>
     * <li>Print a {@link ValueSymbol} for the empty string.</li>
     * <li>Print a {@link ValueSymbol} for {@code it's \\}, which needs quotes,
     * holds a single quote and ends with two backslashes, and parse
     * {@code name = } followed by its text.</li>
     * </ul>
     * <p>
     * <strong>Expected:</strong> The empty string prints as empty text.
     * {@code it's \\} prints within double quotes, with one more backslash at
     * its end, and the condition holds a different string.
     */
    @Test
    public void testStringValueThatCclCannotExpressDoesNotParseBack() {
        Assert.assertEquals("", new ValueSymbol("").toString());
        String value = "it's \\\\";
        Assert.assertEquals("\"it's \\\\\\\"",
                new ValueSymbol(value).toString());
        Assert.assertNotEquals(ImmutableList.of(value), readBack(value));
    }

    /**
     * <strong>Goal:</strong> Verify that a string value whose text already
     * parses back keeps that text.
     * <p>
     * <strong>Start state:</strong> No prior state needed.
     * <p>
     * <strong>Workflow:</strong>
     * <ul>
     * <li>Print a {@link ValueSymbol} for words, paths and other strings that
     * CCL reads unquoted as one value.</li>
     * <li>Print a {@link ValueSymbol} for numeric strings and for strings that
     * hold a space, an equals sign, quote characters or backticks.</li>
     * <li>Parse {@code name = } followed by each text.</li>
     * </ul>
     * <p>
     * <strong>Expected:</strong> A string that CCL reads unquoted prints as is.
     * A numeric string prints within double quotes. Any other string prints
     * within double quotes, or within single quotes when it holds a double
     * quote and no single quote. A double quote within double quotes prints
     * with a backslash before it. Each condition holds the one string that was
     * printed.
     */
    @Test
    public void testStringValueThatParsesBackKeepsItsText() {
        Map<String, String> texts = ImmutableMap.<String, String> builder()
                .put("jeff", "jeff").put("a.b", "a.b").put("a*", "a*")
                .put("_", "_").put("+1", "+1").put("order", "order")
                .put("jeff@cinchapi.com", "jeff@cinchapi.com")
                .put("C:\\Users\\", "C:\\Users\\").put("it's", "it's")
                .put("1", "\"1\"").put("1.5", "\"1.5\"")
                .put("a b", "\"a b\"").put("a=b", "\"a=b\"")
                .put("it's a", "\"it's a\"")
                .put("say \"hi\"", "'say \"hi\"'")
                .put("it's \"x\"", "\"it's \\\"x\\\"\"")
                .put("`a b`", "\"`a b`\"").build();
        texts.forEach((value, text) -> {
            Assert.assertEquals(value, text, new ValueSymbol(value).toString());
            Assert.assertEquals(value, ImmutableList.of(value),
                    readBack(value));
        });
    }

    @Test
    public void testStringWithEqualSignIsQuoted() {
        String value = Random.getSimpleString()+"="+Random.getSimpleString();
        System.out.println(value);
        ValueSymbol symbol = new ValueSymbol(value);
        Assert.assertTrue(AnyStrings.isWithinQuotes(symbol.toString()));
    }
    
    @Test
    public void testTagWithEqualSignIsQuoted() {
        Tag value = Tag.create(Random.getSimpleString()+"="+Random.getSimpleString());
        System.out.println(value);
        ValueSymbol symbol = new ValueSymbol(value);
        Assert.assertTrue(AnyStrings.isWithinQuotes(symbol.toString()));
    }

}
