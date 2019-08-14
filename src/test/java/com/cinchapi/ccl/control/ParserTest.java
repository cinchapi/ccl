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
package com.cinchapi.ccl.control;

import com.cinchapi.concourse.Timestamp;
import com.cinchapi.concourse.lang.sort.Order;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 */
public class ParserTest {

    @Test
    public void test() {
        String key = "age";
        StringBuilder builder = new StringBuilder();
        builder.append(key);
        String input = builder.toString();

        Order expected = Order.by("age").build();

        Parser parser = new Parser(input);
        Order actual = parser.order();

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void test2() {
        String key = "age";
        StringBuilder builder = new StringBuilder();
        builder.append("<");
        builder.append(key);
        String input = builder.toString();

        Order expected = Order.by("age").ascending().build();

        Parser parser = new Parser(input);
        Order actual = parser.order();

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void test3() {
        String key = "age";
        StringBuilder builder = new StringBuilder();
        builder.append(">");
        builder.append(key);
        String input = builder.toString();

        Order expected = Order.by("age").descending().build();

        Parser parser = new Parser(input);
        Order actual = parser.order();

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void test4() {
        String key = "age";
        long number = 122L;
        StringBuilder builder = new StringBuilder();
        builder.append(key);
        builder.append("@");
        builder.append(number);
        String input = builder.toString();

        Order expected = Order.by("age").at(Timestamp.fromMicros(number)).build();

        Parser parser = new Parser(input);
        Order actual = parser.order();

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void test5() {
        String key = "age";
        String timestamp = "\"1992-10-02\"";
        StringBuilder builder = new StringBuilder();
        builder.append(key);
        builder.append("@");
        builder.append(timestamp);
        String input = builder.toString();

        Order expected = Order.by("age").at(Timestamp
                .fromString(timestamp.replace("\"", ""))).build();

        Parser parser = new Parser(input);
        Order actual = parser.order();

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void test6() {
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

        Order expected = Order.by("age").at(Timestamp.parse(timestamp.replace("\"", ""), format.replace("\"", ""))).build();

        Parser parser = new Parser(input);
        Order actual = parser.order();

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void test7() {
        String key = "age";
        Long number = 122L;
        StringBuilder builder = new StringBuilder();
        builder.append("<");
        builder.append(key);
        builder.append("@");
        builder.append(number);
        String input = builder.toString();

        Order expected = Order.by("age").at(Timestamp.fromMicros(number)).ascending().build();

        Parser parser = new Parser(input);
        Order actual = parser.order();

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void test8() {
        String key = "age";
        String timestamp = "\"1992-10-02\"";
        StringBuilder builder = new StringBuilder();
        builder.append("<");
        builder.append(key);
        builder.append("@");
        builder.append(timestamp);
        String input = builder.toString();

        Order expected = Order.by("age").at(Timestamp
                .fromString(timestamp.replace("\"", ""))).ascending().build();

        Parser parser = new Parser(input);
        Order actual = parser.order();

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void test9() {
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

        Order expected = Order.by("age").at(Timestamp
                .parse(timestamp.replace("\"", ""),
                        format.replace("\"", ""))).ascending().build();

        Parser parser = new Parser(input);
        Order actual = parser.order();

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void test10() {
        String key = "age";
        Long number = 122L;
        StringBuilder builder = new StringBuilder();
        builder.append(">");
        builder.append(key);
        builder.append("@");
        builder.append(number);
        String input = builder.toString();

        Order expected = Order.by("age").at(Timestamp.fromMicros(number)).descending().build();

        Parser parser = new Parser(input);
        Order actual = parser.order();

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void test11() {
        String key = "age";
        String timestamp = "\"1992-10-02\"";
        StringBuilder builder = new StringBuilder();
        builder.append(">");
        builder.append(key);
        builder.append("@");
        builder.append(timestamp);
        String input = builder.toString();

        Order expected = Order.by("age").at(Timestamp
                .fromString(timestamp.replace("\"", ""))).descending().build();

        Parser parser = new Parser(input);
        Order actual = parser.order();

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void test12() {
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

        Order expected = Order.by("age").at(Timestamp
                .parse(timestamp.replace("\"", ""),
                        format.replace("\"", ""))).descending().build();

        Parser parser = new Parser(input);
        Order actual = parser.order();

        Assert.assertEquals(expected, actual);
    }
}
