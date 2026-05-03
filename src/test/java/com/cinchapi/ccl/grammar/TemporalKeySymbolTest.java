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
 * Unit tests for {@link TemporalKeySymbol}.
 *
 * @author Jeff Nelson
 */
public class TemporalKeySymbolTest {

    @Test(expected = NullPointerException.class)
    public void testConstructRejectsNullKey() {
        new TemporalKeySymbol(null, new TimestampSymbol(123L));
    }

    @Test(expected = NullPointerException.class)
    public void testConstructRejectsNullTimestamp() {
        new TemporalKeySymbol(new KeySymbol("foo"), null);
    }

    @Test
    public void testKeyAccessor() {
        KeySymbol inner = new KeySymbol("foo");
        TimestampSymbol ts = new TimestampSymbol(123L);
        TemporalKeySymbol symbol = new TemporalKeySymbol(inner, ts);
        Assert.assertSame(inner, symbol.key());
    }

    @Test
    public void testTimestampAccessor() {
        TimestampSymbol ts = new TimestampSymbol(123L);
        TemporalKeySymbol symbol = new TemporalKeySymbol(new KeySymbol("foo"),
                ts);
        Assert.assertSame(ts, symbol.timestamp());
    }

    @Test
    public void testToStringWrappingKeySymbol() {
        TemporalKeySymbol symbol = new TemporalKeySymbol(new KeySymbol("foo"),
                new TimestampSymbol(123L));
        Assert.assertEquals("foo[123]", symbol.toString());
    }

    @Test
    public void testToStringWrappingNavigationKeySymbol() {
        TemporalKeySymbol symbol = new TemporalKeySymbol(
                new NavigationKeySymbol("a.b"), new TimestampSymbol(456L));
        Assert.assertEquals("a.b[456]", symbol.toString());
    }

    @Test
    public void testEqualsWhenKeyAndTimestampMatch() {
        TemporalKeySymbol a = new TemporalKeySymbol(new KeySymbol("foo"),
                new TimestampSymbol(123L));
        TemporalKeySymbol b = new TemporalKeySymbol(new KeySymbol("foo"),
                new TimestampSymbol(123L));
        Assert.assertEquals(a, b);
        Assert.assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    public void testNotEqualsWhenTimestampDiffers() {
        TemporalKeySymbol a = new TemporalKeySymbol(new KeySymbol("foo"),
                new TimestampSymbol(123L));
        TemporalKeySymbol b = new TemporalKeySymbol(new KeySymbol("foo"),
                new TimestampSymbol(456L));
        Assert.assertNotEquals(a, b);
    }

    @Test
    public void testNotEqualsWhenKeyDiffers() {
        TemporalKeySymbol a = new TemporalKeySymbol(new KeySymbol("foo"),
                new TimestampSymbol(123L));
        TemporalKeySymbol b = new TemporalKeySymbol(new KeySymbol("bar"),
                new TimestampSymbol(123L));
        Assert.assertNotEquals(a, b);
    }

    @Test
    public void testNotEqualsToPlainKeySymbol() {
        TemporalKeySymbol a = new TemporalKeySymbol(new KeySymbol("foo"),
                new TimestampSymbol(123L));
        Assert.assertNotEquals(a, new KeySymbol("foo"));
    }

}
