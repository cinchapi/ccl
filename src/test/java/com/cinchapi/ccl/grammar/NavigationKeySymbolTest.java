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
 * Unit tests for {@link NavigationKeySymbol}.
 *
 * @author Jeff Nelson
 */
public class NavigationKeySymbolTest {

    @Test
    public void testKeyIsCanonicalForUnstampedInput() {
        NavigationKeySymbol nav = new NavigationKeySymbol("a.b.c");
        Assert.assertEquals("a.b.c", nav.key());
    }

    @Test
    public void testKeyCanonicalizesKeywordBracketForm() {
        NavigationKeySymbol nav = new NavigationKeySymbol("a[at 123].foo");
        Assert.assertEquals("a[123].foo", nav.key());
    }

    @Test
    public void testKeyCanonicalizesAcrossKeywordVariants() {
        NavigationKeySymbol canonical = new NavigationKeySymbol(
                "a[123].b[456]");
        for (String keyword : new String[] { "at", "on", "during" }) {
            NavigationKeySymbol variant = new NavigationKeySymbol(
                    String.format("a[%s 123].b[%s 456]", keyword, keyword));
            Assert.assertEquals(
                    "key() must canonicalize keyword form '" + keyword + "'",
                    canonical.key(), variant.key());
        }
    }

    @Test
    public void testEqualsAcrossKeywordAndCanonicalForms() {
        Assert.assertEquals(new NavigationKeySymbol("a[123].foo"),
                new NavigationKeySymbol("a[at 123].foo"));
    }

    @Test
    public void testStorageKeyStripsBracketAnnotations() {
        Assert.assertEquals("a.b.foo", new NavigationKeySymbol(
                "a[1].b[2].foo[3]").storageKey());
    }

    @Test
    public void testStorageKeyPreservesTransitiveMarker() {
        Assert.assertEquals("a*.b",
                new NavigationKeySymbol("a[1]*.b[2]").storageKey());
    }

    @Test
    public void testStorageKeyOnUnstampedNavigationIsIdentity() {
        Assert.assertEquals("a.b.c",
                new NavigationKeySymbol("a.b.c").storageKey());
    }

}
