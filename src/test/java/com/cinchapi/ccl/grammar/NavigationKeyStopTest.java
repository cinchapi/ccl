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

}
