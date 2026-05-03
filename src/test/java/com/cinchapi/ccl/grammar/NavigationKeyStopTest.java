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
 * Unit tests for {@link NavigationKeyStop}.
 *
 * @author Jeff Nelson
 */
public class NavigationKeyStopTest {

    @Test
    public void testParseNonTransitive() {
        NavigationKeyStop stop = NavigationKeyStop.parse("name");
        Assert.assertEquals("name", stop.key());
        Assert.assertFalse(stop.isTransitive());
    }

    @Test
    public void testParseTransitive() {
        NavigationKeyStop stop = NavigationKeyStop.parse("children*");
        Assert.assertEquals("children", stop.key());
        Assert.assertTrue(stop.isTransitive());
    }

    @Test
    public void testToStringRoundTripNonTransitive() {
        NavigationKeyStop stop = NavigationKeyStop.parse("name");
        Assert.assertEquals("name", stop.toString());
    }

    @Test
    public void testToStringRoundTripTransitive() {
        NavigationKeyStop stop = NavigationKeyStop.parse("children*");
        Assert.assertEquals("children*", stop.toString());
    }

    @Test
    public void testEquals() {
        Assert.assertEquals(NavigationKeyStop.parse("children*"),
                NavigationKeyStop.parse("children*"));
        Assert.assertEquals(NavigationKeyStop.parse("name"),
                NavigationKeyStop.parse("name"));
    }

    @Test
    public void testNotEqualsWhenTransitiveDiffers() {
        Assert.assertNotEquals(NavigationKeyStop.parse("children*"),
                NavigationKeyStop.parse("children"));
    }

    @Test
    public void testNotEqualsWhenKeyDiffers() {
        Assert.assertNotEquals(NavigationKeyStop.parse("children*"),
                NavigationKeyStop.parse("parents*"));
    }

    @Test
    public void testHashCodeConsistentWithEquals() {
        NavigationKeyStop a = NavigationKeyStop.parse("children*");
        NavigationKeyStop b = NavigationKeyStop.parse("children*");
        Assert.assertEquals(a, b);
        Assert.assertEquals(a.hashCode(), b.hashCode());
    }

    @Test(expected = NullPointerException.class)
    public void testParseRejectsNull() {
        NavigationKeyStop.parse(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseRejectsEmpty() {
        NavigationKeyStop.parse("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseRejectsBareTransitiveSuffix() {
        NavigationKeyStop.parse("*");
    }

    @Test
    public void testParseStampedNonTransitive() {
        NavigationKeyStop stop = NavigationKeyStop.parse("name[123]");
        Assert.assertEquals("name", stop.key());
        Assert.assertFalse(stop.isTransitive());
        Assert.assertNotNull(stop.timestamp());
        Assert.assertEquals(123L, stop.timestamp().timestamp());
    }

    @Test
    public void testParseStampedTransitive() {
        NavigationKeyStop stop = NavigationKeyStop.parse("children[456]*");
        Assert.assertEquals("children", stop.key());
        Assert.assertTrue(stop.isTransitive());
        Assert.assertEquals(456L, stop.timestamp().timestamp());
    }

    @Test
    public void testParseStampedWithKeywordEqualsKeywordless() {
        Assert.assertEquals(NavigationKeyStop.parse("name[123]"),
                NavigationKeyStop.parse("name[at 123]"));
        Assert.assertEquals(NavigationKeyStop.parse("name[123]"),
                NavigationKeyStop.parse("name[on 123]"));
        Assert.assertEquals(NavigationKeyStop.parse("name[123]"),
                NavigationKeyStop.parse("name[during 123]"));
    }

    @Test
    public void testParseUnstampedTimestampIsNull() {
        Assert.assertNull(NavigationKeyStop.parse("name").timestamp());
        Assert.assertNull(NavigationKeyStop.parse("children*").timestamp());
    }

    @Test
    public void testValueRoundTripStamped() {
        Assert.assertEquals("name[123]",
                NavigationKeyStop.parse("name[123]").value());
        Assert.assertEquals("children[456]*",
                NavigationKeyStop.parse("children[456]*").value());
    }

    @Test
    public void testValueCanonicalizesKeywordForm() {
        Assert.assertEquals("name[123]",
                NavigationKeyStop.parse("name[at 123]").value());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseRejectsBareBracketAnnotation() {
        NavigationKeyStop.parse("[123]");
    }

    @Test
    public void testNotEqualsWhenTimestampDiffers() {
        Assert.assertNotEquals(NavigationKeyStop.parse("name[123]"),
                NavigationKeyStop.parse("name[456]"));
    }

    @Test
    public void testNotEqualsWhenOneStampedAndOtherNot() {
        Assert.assertNotEquals(NavigationKeyStop.parse("name[123]"),
                NavigationKeyStop.parse("name"));
    }

    @Test
    public void testParseStampedTransitivePutsBracketBeforeAsterisk() {
        NavigationKeyStop stop = NavigationKeyStop.parse("children[456]*");
        Assert.assertEquals("children", stop.key());
        Assert.assertTrue(stop.isTransitive());
        Assert.assertEquals(456L, stop.timestamp().timestamp());
    }

}
