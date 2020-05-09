/*
 * Copyright (c) 2013-2020 Cinchapi Inc.
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

import com.cinchapi.common.base.AnyStrings;
import com.cinchapi.concourse.Tag;
import com.cinchapi.concourse.util.Random;

/**
 * Unit tests for {@link ValueSymbol}
 *
 * @author Jeff Nelson
 */
public class ValueSymbolTest {
    
    @Test
    public void testStringWithEqualSignIsQuoted() {
        String value = Random.getSimpleString()+"="+Random.getSimpleString();
        System.out.println(value);
        ValueSymbol symbol = new ValueSymbol(value);
        Assert.assertTrue(AnyStrings.isWithinQuotes(symbol.toString()));
    }
    
    @Test
    public void testTagWithEqualSignIsQuoted() {
        Tag value = Tag.create(Random.getSimpleString()+"="+Random.getSimpleString());
        System.out.println(value);
        ValueSymbol symbol = new ValueSymbol(value);
        Assert.assertTrue(AnyStrings.isWithinQuotes(symbol.toString()));
    }

}
