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
package com.cinchapi.ccl.lang.order;

import com.cinchapi.ccl.lang.order.OrderParser;
import com.cinchapi.concourse.Timestamp;
import com.cinchapi.concourse.lang.sort.Order;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for the {@link OrderParser}
 */
public class OrderParserTest {

    @Test
    public void testKey() {
        String key = "age";
        StringBuilder builder = new StringBuilder();
        builder.append(key);
        String input = builder.toString();

        OrderClause expected = new OrderClause();
        expected.add(new OrderSpecification("age"));

        OrderParser orderParser = new OrderParser(input);
        OrderClause actual = orderParser.order();

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testKeyAscendingSymbol() {
        String key = "age";
        StringBuilder builder = new StringBuilder();
        builder.append("<");
        builder.append(key);
        String input = builder.toString();

        OrderClause expected = Order.by("age").ascending().build();

        OrderParser orderParser = new OrderParser(input);
        OrderClause actual = orderParser.order();

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testKeyAscendingWord() {
        String key = "age";
        StringBuilder builder = new StringBuilder();
        builder.append("ASC");
        builder.append(key);
        String input = builder.toString();

        OrderClause expected = Order.by("age").ascending().build();

        OrderParser orderParser = new OrderParser(input);
        OrderClause actual = orderParser.order();

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testKeyDescendingSymbol() {
        String key = "age";
        StringBuilder builder = new StringBuilder();
        builder.append(">");
        builder.append(key);
        String input = builder.toString();

        OrderClause expected = Order.by("age").descending().build();

        OrderParser orderParser = new OrderParser(input);
        OrderClause actual = orderParser.order();

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testKeyDescendingWord() {
        String key = "age";
        StringBuilder builder = new StringBuilder();
        builder.append("DESC");
        builder.append(key);
        String input = builder.toString();

        OrderClause expected = Order.by("age").descending().build();

        OrderParser orderParser = new OrderParser(input);
        OrderClause actual = orderParser.order();

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testKeyWithNumberTimestamp() {
        String key = "age";
        long number = 122L;
        StringBuilder builder = new StringBuilder();
        builder.append(key);
        builder.append("@");
        builder.append(number);
        String input = builder.toString();

        OrderClause expected = Order.by("age").at(Timestamp.fromMicros(number)).build();

        OrderParser orderParser = new OrderParser(input);
        OrderClause actual = orderParser.order();

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testKeyWithStringTimestamp() {
        String key = "age";
        String timestamp = "\"1992-10-02\"";
        StringBuilder builder = new StringBuilder();
        builder.append(key);
        builder.append("@");
        builder.append(timestamp);
        String input = builder.toString();

        OrderClause expected = Order.by("age").at(Timestamp
                .fromString(timestamp.replace("\"", ""))).build();

        OrderParser orderParser = new OrderParser(input);
        OrderClause actual = orderParser.order();

        Assert.assertEquals(expected.spec().size(), actual.spec().size());
        for(int i = 0; i < expected.spec().size(); i++) {
            Assert.assertEquals(expected.spec().get(i).key(), actual.spec().get(i).key());
            Assert.assertEquals(expected.spec().get(i).direction(), actual.spec().get(i).direction());
            Assert.assertEquals(expected.spec().get(i).timestamp().toString(), actual.spec().get(i).timestamp().toString());
        }
    }

    @Test
    public void testKeyWithStringAndFormatTimestamp() {
        String key = "age";
        String timestamp = "\"1992-10-02\"";
        String format = "\"yyyy-mm-dd\"";
        StringBuilder builder = new StringBuilder();
        builder.append(key);
        builder.append("@");
        builder.append(timestamp);
        builder.append("|");
        builder.append(format);
        String input = builder.toString();

        OrderClause expected = Order.by("age").at(Timestamp.parse(timestamp.replace("\"", ""), format.replace("\"", ""))).build();

        OrderParser orderParser = new OrderParser(input);
        OrderClause actual = orderParser.order();

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testKeyWithNumberTimestampAscending() {
        String key = "age";
        Long number = 122L;
        StringBuilder builder = new StringBuilder();
        builder.append("<");
        builder.append(key);
        builder.append("@");
        builder.append(number);
        String input = builder.toString();

        OrderClause expected = Order.by("age").at(Timestamp.fromMicros(number)).ascending().build();

        OrderParser orderParser = new OrderParser(input);
        OrderClause actual = orderParser.order();

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testKeyWithStringTimestampAscending() {
        String key = "age";
        String timestamp = "\"1992-10-02\"";
        StringBuilder builder = new StringBuilder();
        builder.append("<");
        builder.append(key);
        builder.append("@");
        builder.append(timestamp);
        String input = builder.toString();

        OrderClause expected = Order.by("age").at(Timestamp
                .fromString(timestamp.replace("\"", ""))).ascending().build();

        OrderParser orderParser = new OrderParser(input);
        OrderClause actual = orderParser.order();

        Assert.assertEquals(expected.spec().size(), actual.spec().size());
        for(int i = 0; i < expected.spec().size(); i++) {
            Assert.assertEquals(expected.spec().get(i).key(), actual.spec().get(i).key());
            Assert.assertEquals(expected.spec().get(i).direction(), actual.spec().get(i).direction());
            Assert.assertEquals(expected.spec().get(i).timestamp().toString(), actual.spec().get(i).timestamp().toString());
        }
    }

    @Test
    public void testKeyWithStringAndFormatTimestampAscending() {
        String key = "age";
        String timestamp = "\"1992-10-02\"";
        String format = "\"yyyy-mm-dd\"";
        StringBuilder builder = new StringBuilder();
        builder.append("<");
        builder.append(key);
        builder.append("@");
        builder.append(timestamp);
        builder.append("|");
        builder.append(format);
        String input = builder.toString();

        OrderClause expected = Order.by("age").at(Timestamp
                .parse(timestamp.replace("\"", ""),
                        format.replace("\"", ""))).ascending().build();

        OrderParser orderParser = new OrderParser(input);
        OrderClause actual = orderParser.order();

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testKeyWithNumberTimestampDescending() {
        String key = "age";
        Long number = 122L;
        StringBuilder builder = new StringBuilder();
        builder.append(">");
        builder.append(key);
        builder.append("@");
        builder.append(number);
        String input = builder.toString();

        OrderClause expected = Order.by("age").at(Timestamp.fromMicros(number)).descending().build();

        OrderParser orderParser = new OrderParser(input);
        OrderClause actual = orderParser.order();

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testKeyWithStringTimestampDescending() {
        String key = "age";
        String timestamp = "\"1992-10-02\"";
        StringBuilder builder = new StringBuilder();
        builder.append(">");
        builder.append(key);
        builder.append("@");
        builder.append(timestamp);
        String input = builder.toString();

        OrderClause expected = Order.by("age").at(Timestamp
                .fromString(timestamp.replace("\"", ""))).descending().build();

        OrderParser orderParser = new OrderParser(input);
        OrderClause actual = orderParser.order();

        Assert.assertEquals(expected.spec().size(), actual.spec().size());
        for(int i = 0; i < expected.spec().size(); i++) {
            Assert.assertEquals(expected.spec().get(i).key(), actual.spec().get(i).key());
            Assert.assertEquals(expected.spec().get(i).direction(), actual.spec().get(i).direction());
            Assert.assertEquals(expected.spec().get(i).timestamp().toString(), actual.spec().get(i).timestamp().toString());
        }
    }

    @Test
    public void testKeyWithStringAndFormatTimestampDescending() {
        String key = "age";
        String timestamp = "\"1992-10-02\"";
        String format = "\"yyyy-mm-dd\"";
        StringBuilder builder = new StringBuilder();
        builder.append(">");
        builder.append(key);
        builder.append("@");
        builder.append(timestamp);
        builder.append("|");
        builder.append(format);
        String input = builder.toString();

        OrderClause expected = Order.by("age").at(Timestamp
                .parse(timestamp.replace("\"", ""),
                        format.replace("\"", ""))).descending().build();

        OrderParser orderParser = new OrderParser(input);
        OrderClause actual = orderParser.order();

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testMultipleKeys() {
        String key1 = "age";
        String key2 = "salary";
        StringBuilder builder = new StringBuilder();
        builder.append(key1);
        builder.append(" ");
        builder.append(key2);
        String input = builder.toString();

        OrderClause expected = Order.by("age").then("salary").build();

        OrderParser orderParser = new OrderParser(input);
        OrderClause actual = orderParser.order();

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testMultipleKeysWithDirectional() {
        String key1 = "age";
        String key2 = "salary";
        StringBuilder builder = new StringBuilder();
        builder.append("<");
        builder.append(key1);
        builder.append(" ");
        builder.append(">");
        builder.append(key2);
        String input = builder.toString();

        OrderClause expected = Order.by("age").ascending().then("salary").descending().build();

        OrderParser orderParser = new OrderParser(input);
        OrderClause actual = orderParser.order();

        Assert.assertEquals(expected, actual);
    }
}
