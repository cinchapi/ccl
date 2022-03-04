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
package com.cinchapi.ccl.util;

import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Test;

import com.cinchapi.concourse.Timestamp;

/**
 * Unit tests for the {@link NaturalLanguage} utils.
 *
 * @author Jeff Nelson
 */
public class NaturalLanguageTest  {

    @Test
    public void testParseTimeStringInDefaultFormat() {
        String str = "Sat Mar 07, 2015 @ 8:11:35:36 PM EST";
        long expected = Timestamp.parse(str, Timestamp.DEFAULT_FORMATTER)
                .getMicros();
        Assert.assertEquals(expected, NaturalLanguage.parseMicros(str));
    }
    
    @Test
    public void testParseTimeStringFormat1() {
        String string = "December 30, 1987";
        Timestamp timestamp = Timestamp.fromMicros(NaturalLanguage.parseMicros(string));
        DateTime joda = timestamp.getJoda();
        Assert.assertEquals(12, joda.getMonthOfYear());
        Assert.assertEquals(30, joda.getDayOfMonth());
        Assert.assertEquals(1987, joda.getYear());
        Assert.assertEquals(0, joda.getHourOfDay());
        Assert.assertEquals(0, joda.getMinuteOfHour());
        Assert.assertEquals(0, joda.getSecondOfMinute());
    }
    
    @Test
    public void testParseTimeStringFormat2() {
        String string = "Dec 30, 1987";
        Timestamp timestamp = Timestamp.fromMicros(NaturalLanguage.parseMicros(string));
        DateTime joda = timestamp.getJoda();
        Assert.assertEquals(12, joda.getMonthOfYear());
        Assert.assertEquals(30, joda.getDayOfMonth());
        Assert.assertEquals(1987, joda.getYear());
        Assert.assertEquals(0, joda.getHourOfDay());
        Assert.assertEquals(0, joda.getMinuteOfHour());
        Assert.assertEquals(0, joda.getSecondOfMinute());
    }
    
    @Test
    public void testParseTimeStringFormat3() {
        String string = "12/30/1987";
        Timestamp timestamp = Timestamp.fromMicros(NaturalLanguage.parseMicros(string));
        DateTime joda = timestamp.getJoda();
        Assert.assertEquals(12, joda.getMonthOfYear());
        Assert.assertEquals(30, joda.getDayOfMonth());
        Assert.assertEquals(1987, joda.getYear());
        Assert.assertEquals(0, joda.getHourOfDay());
        Assert.assertEquals(0, joda.getMinuteOfHour());
        Assert.assertEquals(0, joda.getSecondOfMinute());
    }
    
    @Test
    public void testParseTimeStringFormat10() {
        String string = "01-03-1987";
        Timestamp timestamp = Timestamp.fromMicros(NaturalLanguage.parseMicros(string));
        DateTime joda = timestamp.getJoda();
        Assert.assertEquals(1, joda.getMonthOfYear());
        Assert.assertEquals(3, joda.getDayOfMonth());
        Assert.assertEquals(1987, joda.getYear());
        Assert.assertEquals(0, joda.getHourOfDay());
        Assert.assertEquals(0, joda.getMinuteOfHour());
        Assert.assertEquals(0, joda.getSecondOfMinute());
    }
    
    @Test
    public void testParseTimeStringFormat11() {
        String string = "1-03-1987";
        Timestamp timestamp = Timestamp.fromMicros(NaturalLanguage.parseMicros(string));
        DateTime joda = timestamp.getJoda();
        Assert.assertEquals(1, joda.getMonthOfYear());
        Assert.assertEquals(3, joda.getDayOfMonth());
        Assert.assertEquals(1987, joda.getYear());
        Assert.assertEquals(0, joda.getHourOfDay());
        Assert.assertEquals(0, joda.getMinuteOfHour());
        Assert.assertEquals(0, joda.getSecondOfMinute());
    }
    
    @Test
    public void testParseTimeStringFormat12() {
        String string = "1-3-1987";
        Timestamp timestamp = Timestamp.fromMicros(NaturalLanguage.parseMicros(string));
        DateTime joda = timestamp.getJoda();
        Assert.assertEquals(1, joda.getMonthOfYear());
        Assert.assertEquals(3, joda.getDayOfMonth());
        Assert.assertEquals(1987, joda.getYear());
        Assert.assertEquals(0, joda.getHourOfDay());
        Assert.assertEquals(0, joda.getMinuteOfHour());
        Assert.assertEquals(0, joda.getSecondOfMinute());
    }
    
    @Test
    public void testParseTimeStringFormat13() {
        String string = "01-3-1987";
        Timestamp timestamp = Timestamp.fromMicros(NaturalLanguage.parseMicros(string));
        DateTime joda = timestamp.getJoda();
        Assert.assertEquals(1, joda.getMonthOfYear());
        Assert.assertEquals(3, joda.getDayOfMonth());
        Assert.assertEquals(1987, joda.getYear());
        Assert.assertEquals(0, joda.getHourOfDay());
        Assert.assertEquals(0, joda.getMinuteOfHour());
        Assert.assertEquals(0, joda.getSecondOfMinute());
    }

}