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
        Assert.assertEquals("name", stop.name());
        Assert.assertFalse(stop.isTransitive());
    }

    @Test
    public void testParseTransitive() {
        NavigationKeyStop stop = NavigationKeyStop.parse("children*");
        Assert.assertEquals("children", stop.name());
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
        Assert.assertEquals(new NavigationKeyStop("children", true),
                NavigationKeyStop.parse("children*"));
        Assert.assertEquals(new NavigationKeyStop("name", false),
                NavigationKeyStop.parse("name"));
    }

    @Test
    public void testNotEqualsWhenTransitiveDiffers() {
        Assert.assertNotEquals(new NavigationKeyStop("children", true),
                new NavigationKeyStop("children", false));
    }

    @Test
    public void testNotEqualsWhenNameDiffers() {
        Assert.assertNotEquals(new NavigationKeyStop("children", true),
                new NavigationKeyStop("parents", true));
    }

    @Test
    public void testHashCodeConsistentWithEquals() {
        NavigationKeyStop a = new NavigationKeyStop("children", true);
        NavigationKeyStop b = NavigationKeyStop.parse("children*");
        Assert.assertEquals(a, b);
        Assert.assertEquals(a.hashCode(), b.hashCode());
    }

}
