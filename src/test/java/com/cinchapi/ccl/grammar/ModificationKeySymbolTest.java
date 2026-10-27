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
 * Unit tests for {@link ModificationKeySymbol}.
 *
 * @author Jeff Nelson
 */
public class ModificationKeySymbolTest {

    @Test(expected = NullPointerException.class)
    public void testConstructRejectsNullKey() {
        new ModificationKeySymbol(null, new TimestampSymbol(1L));
    }

    @Test(expected = NullPointerException.class)
    public void testConstructRejectsNullTimestamp() {
        new ModificationKeySymbol(new KeySymbol("foo"), null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructRejectsNavigationKeySymbolWrapping() {
        new ModificationKeySymbol(new NavigationKeySymbol("a.b"),
                new TimestampSymbol(1L));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructRejectsParameterizedKeyWrapping() {
        TemporalKeySymbol inner = new TemporalKeySymbol(new KeySymbol("foo"),
                new TimestampSymbol(1L));
        new ModificationKeySymbol(inner, new TimestampSymbol(2L));
    }

    @Test
    public void testTimestampAccessor() {
        TimestampSymbol ts = new TimestampSymbol(1L);
        ModificationKeySymbol symbol = new ModificationKeySymbol(
                new KeySymbol("foo"), ts);
        Assert.assertSame(ts, symbol.timestamp());
    }

    @Test
    public void testToString() {
        ModificationKeySymbol symbol = new ModificationKeySymbol(
                new KeySymbol("foo"), new TimestampSymbol(123L));
        Assert.assertEquals("foo[123~]", symbol.toString());
    }

    @Test
    public void testEqualsWhenKeyAndTimestampMatch() {
        ModificationKeySymbol a = new ModificationKeySymbol(
                new KeySymbol("foo"), new TimestampSymbol(1L));
        ModificationKeySymbol b = new ModificationKeySymbol(
                new KeySymbol("foo"), new TimestampSymbol(1L));
        Assert.assertEquals(a, b);
        Assert.assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    public void testNotEqualsWhenTimestampDiffers() {
        ModificationKeySymbol a = new ModificationKeySymbol(
                new KeySymbol("foo"), new TimestampSymbol(1L));
        ModificationKeySymbol b = new ModificationKeySymbol(
                new KeySymbol("foo"), new TimestampSymbol(2L));
        Assert.assertNotEquals(a, b);
    }

    @Test
    public void testNotEqualsWhenKeyDiffers() {
        ModificationKeySymbol a = new ModificationKeySymbol(
                new KeySymbol("foo"), new TimestampSymbol(1L));
        ModificationKeySymbol b = new ModificationKeySymbol(
                new KeySymbol("bar"), new TimestampSymbol(1L));
        Assert.assertNotEquals(a, b);
    }

    @Test
    public void testNotEqualsToTemporalKeySymbol() {
        ModificationKeySymbol modification = new ModificationKeySymbol(
                new KeySymbol("foo"), new TimestampSymbol(1L));
        Assert.assertNotEquals(modification,
                new TemporalKeySymbol(new KeySymbol("foo"),
                        new TimestampSymbol(1L)));
    }

    @Test
    public void testNotEqualsToTemporalRangeKeySymbol() {
        ModificationKeySymbol modification = new ModificationKeySymbol(
                new KeySymbol("foo"), new TimestampSymbol(1L));
        Assert.assertNotEquals(modification,
                new TemporalRangeKeySymbol(new KeySymbol("foo"),
                        new TimestampSymbol(1L), null));
    }

    @Test
    public void testBaseKeyDelegatesToWrappedKey() {
        ModificationKeySymbol symbol = new ModificationKeySymbol(
                new KeySymbol("foo"), new TimestampSymbol(1L));
        Assert.assertEquals("foo", symbol.baseKey());
    }

    @Test
    public void testIsParameterized() {
        ModificationKeySymbol symbol = new ModificationKeySymbol(
                new KeySymbol("foo"), new TimestampSymbol(1L));
        Assert.assertTrue(symbol.isParameterized());
    }

    @Test
    public void testStripParametersReturnsWrappedKey() {
        KeySymbol inner = new KeySymbol("foo");
        ModificationKeySymbol symbol = new ModificationKeySymbol(inner,
                new TimestampSymbol(1L));
        Assert.assertEquals(inner, symbol.stripParameters());
    }

}
