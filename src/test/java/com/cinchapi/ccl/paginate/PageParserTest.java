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
package com.cinchapi.ccl.paginate;

import com.cinchapi.concourse.lang.paginate.Page;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for the {@link PageParser}
 */
public class PageParserTest {

    // String constants
    static final String NUMBER = "number";
    static final String SIZE = "size";

    @Test
    public void testWithNumber() {
        int number = 3;
        StringBuilder builder = new StringBuilder();
        builder.append(NUMBER);
        builder.append(" ");
        builder.append(number);
        String input = builder.toString();

        Page expected = Page.number(number);

        PageParser parser = new PageParser(input);
        Page actual = parser.page();

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testWithNumberAndSize() {
        int number = 3;
        int size = 1;
        StringBuilder builder = new StringBuilder();
        builder.append(NUMBER);
        builder.append(" ");
        builder.append(number);
        builder.append(" ");
        builder.append(SIZE);
        builder.append(" ");
        builder.append(size);
        String input = builder.toString();

        Page expected = Page.sized(size).go(number);

        PageParser parser = new PageParser(input);
        Page actual = parser.page();

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testWithSize() {
        int size = 3;
        StringBuilder builder = new StringBuilder();
        builder.append(SIZE);
        builder.append(" ");
        builder.append(size);
        String input = builder.toString();

        Page expected = Page.sized(size);

        PageParser parser = new PageParser(input);
        Page actual = parser.page();

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testWithSizeAndNumber() {
        int size = 1;
        int number = 3;
        StringBuilder builder = new StringBuilder();
        builder.append(SIZE);
        builder.append(" ");
        builder.append(size);
        builder.append(" ");
        builder.append(NUMBER);
        builder.append(" ");
        builder.append(number);
        String input = builder.toString();

        Page expected = Page.sized(size).go(number);

        PageParser parser = new PageParser(input);
        Page actual = parser.page();

        Assert.assertEquals(expected, actual);
    }
}