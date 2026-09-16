/*
 * Copyright (c) 2013-2026 Cinchapi Inc.
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

import org.junit.Assert;
import org.junit.Test;

/**
 * Unit tests for {@link TemporalRangeKeySymbol}.
 *
 * @author Jeff Nelson
 */
public class TemporalRangeKeySymbolTest {

    @Test(expected = NullPointerException.class)
    public void testConstructRejectsNullKey() {
        new TemporalRangeKeySymbol(null, new TimestampSymbol(1L),
                new TimestampSymbol(2L));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructRejectsBothEndpointsAbsent() {
        new TemporalRangeKeySymbol(new KeySymbol("foo"), null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructRejectsBackwardsRange() {
        new TemporalRangeKeySymbol(new KeySymbol("foo"),
                new TimestampSymbol(2L), new TimestampSymbol(1L));
    }

    @Test
    public void testConstructAcceptsEmptyRangeWhenStartEqualsEnd() {
        TemporalRangeKeySymbol symbol = new TemporalRangeKeySymbol(
                new KeySymbol("foo"), new TimestampSymbol(1L),
                new TimestampSymbol(1L));
        Assert.assertEquals(1L, symbol.start().timestamp());
        Assert.assertEquals(1L, symbol.end().timestamp());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructRejectsNavigationKeySymbolWrapping() {
        new TemporalRangeKeySymbol(new NavigationKeySymbol("a.b"),
                new TimestampSymbol(1L), new TimestampSymbol(2L));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructRejectsParameterizedKeyWrapping() {
        TemporalKeySymbol inner = new TemporalKeySymbol(new KeySymbol("foo"),
                new TimestampSymbol(1L));
        new TemporalRangeKeySymbol(inner, new TimestampSymbol(2L),
                new TimestampSymbol(3L));
    }

    @Test
    public void testEndpointAccessors() {
        TimestampSymbol start = new TimestampSymbol(1L);
        TimestampSymbol end = new TimestampSymbol(2L);
        TemporalRangeKeySymbol symbol = new TemporalRangeKeySymbol(
                new KeySymbol("foo"), start, end);
        Assert.assertSame(start, symbol.start());
        Assert.assertSame(end, symbol.end());
    }

    @Test
    public void testOpenStartHasNullStart() {
        TemporalRangeKeySymbol symbol = new TemporalRangeKeySymbol(
                new KeySymbol("foo"), null, new TimestampSymbol(2L));
        Assert.assertNull(symbol.start());
        Assert.assertEquals(2L, symbol.end().timestamp());
    }

    @Test
    public void testOpenEndHasNullEnd() {
        TemporalRangeKeySymbol symbol = new TemporalRangeKeySymbol(
                new KeySymbol("foo"), new TimestampSymbol(1L), null);
        Assert.assertEquals(1L, symbol.start().timestamp());
        Assert.assertNull(symbol.end());
    }

    @Test
    public void testToStringClosedRange() {
        TemporalRangeKeySymbol symbol = new TemporalRangeKeySymbol(
                new KeySymbol("foo"), new TimestampSymbol(1L),
                new TimestampSymbol(2L));
        Assert.assertEquals("foo[1...2]", symbol.toString());
    }

    @Test
    public void testToStringOpenStartRange() {
        TemporalRangeKeySymbol symbol = new TemporalRangeKeySymbol(
                new KeySymbol("foo"), null, new TimestampSymbol(2L));
        Assert.assertEquals("foo[...2]", symbol.toString());
    }

    @Test
    public void testToStringOpenEndRange() {
        TemporalRangeKeySymbol symbol = new TemporalRangeKeySymbol(
                new KeySymbol("foo"), new TimestampSymbol(1L), null);
        Assert.assertEquals("foo[1...]", symbol.toString());
    }

    @Test
    public void testEqualsWhenKeyAndEndpointsMatch() {
        TemporalRangeKeySymbol a = new TemporalRangeKeySymbol(
                new KeySymbol("foo"), new TimestampSymbol(1L),
                new TimestampSymbol(2L));
        TemporalRangeKeySymbol b = new TemporalRangeKeySymbol(
                new KeySymbol("foo"), new TimestampSymbol(1L),
                new TimestampSymbol(2L));
        Assert.assertEquals(a, b);
        Assert.assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    public void testNotEqualsWhenStartDiffers() {
        TemporalRangeKeySymbol a = new TemporalRangeKeySymbol(
                new KeySymbol("foo"), new TimestampSymbol(1L),
                new TimestampSymbol(3L));
        TemporalRangeKeySymbol b = new TemporalRangeKeySymbol(
                new KeySymbol("foo"), new TimestampSymbol(2L),
                new TimestampSymbol(3L));
        Assert.assertNotEquals(a, b);
    }

    @Test
    public void testNotEqualsWhenEndDiffers() {
        TemporalRangeKeySymbol a = new TemporalRangeKeySymbol(
                new KeySymbol("foo"), new TimestampSymbol(1L),
                new TimestampSymbol(2L));
        TemporalRangeKeySymbol b = new TemporalRangeKeySymbol(
                new KeySymbol("foo"), new TimestampSymbol(1L),
                new TimestampSymbol(3L));
        Assert.assertNotEquals(a, b);
    }

    @Test
    public void testNotEqualsWhenOpennessDiffers() {
        TemporalRangeKeySymbol closed = new TemporalRangeKeySymbol(
                new KeySymbol("foo"), new TimestampSymbol(1L),
                new TimestampSymbol(2L));
        TemporalRangeKeySymbol openStart = new TemporalRangeKeySymbol(
                new KeySymbol("foo"), null, new TimestampSymbol(2L));
        Assert.assertNotEquals(closed, openStart);
    }

    @Test
    public void testNotEqualsWhenKeyDiffers() {
        TemporalRangeKeySymbol a = new TemporalRangeKeySymbol(
                new KeySymbol("foo"), new TimestampSymbol(1L),
                new TimestampSymbol(2L));
        TemporalRangeKeySymbol b = new TemporalRangeKeySymbol(
                new KeySymbol("bar"), new TimestampSymbol(1L),
                new TimestampSymbol(2L));
        Assert.assertNotEquals(a, b);
    }

    @Test
    public void testNotEqualsToTemporalKeySymbol() {
        TemporalRangeKeySymbol range = new TemporalRangeKeySymbol(
                new KeySymbol("foo"), new TimestampSymbol(1L),
                new TimestampSymbol(2L));
        Assert.assertNotEquals(range,
                new TemporalKeySymbol(new KeySymbol("foo"),
                        new TimestampSymbol(1L)));
    }

    @Test
    public void testBaseKeyDelegatesToWrappedKey() {
        TemporalRangeKeySymbol symbol = new TemporalRangeKeySymbol(
                new KeySymbol("foo"), new TimestampSymbol(1L),
                new TimestampSymbol(2L));
        Assert.assertEquals("foo", symbol.baseKey());
    }

    @Test
    public void testIsParameterized() {
        TemporalRangeKeySymbol symbol = new TemporalRangeKeySymbol(
                new KeySymbol("foo"), new TimestampSymbol(1L),
                new TimestampSymbol(2L));
        Assert.assertTrue(symbol.isParameterized());
    }

    @Test
    public void testStripParametersReturnsWrappedKey() {
        KeySymbol inner = new KeySymbol("foo");
        TemporalRangeKeySymbol symbol = new TemporalRangeKeySymbol(inner,
                new TimestampSymbol(1L), new TimestampSymbol(2L));
        Assert.assertEquals(inner, symbol.stripParameters());
    }

}
