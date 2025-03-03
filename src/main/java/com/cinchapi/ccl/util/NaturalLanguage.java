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

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;

import com.google.common.collect.ImmutableSet;
import com.google.common.primitives.Longs;
import com.joestelmach.natty.DateGroup;

/**
 * A collection of utility functions for processing natural language directives.
 * 
 * @author Jeff Nelson
 */
public final class NaturalLanguage {

    /**
     * The default formatter that is used to display objects of this class.
     */
    public static final DateTimeFormatter DEFAULT_FORMATTER = DateTimeFormat
            .forPattern("E MMM dd, yyyy @ h:mm:ss:SS a z");

    /**
     * A parser to convert natural language text strings to Timestamp objects.
     */
    private final static com.joestelmach.natty.Parser TIMESTAMP_PARSER = new com.joestelmach.natty.Parser();

    static {
        // Turn off logging in 3rd party code
        ((ch.qos.logback.classic.Logger) LoggerFactory
                .getLogger("com.joestelmach")).setLevel(Level.OFF);
        ((ch.qos.logback.classic.Logger) LoggerFactory.getLogger("net.fortuna"))
                .setLevel(Level.OFF);
    }

    // @formatter:off
    private static Set<DateTimeFormatter> DATETIME_FORMATTERS = ImmutableSet.of(
            DEFAULT_FORMATTER,
            DateTimeFormat.forPattern("MMM dd, yyyy"),
            DateTimeFormat.forPattern("MM/dd/yyyy"),
            DateTimeFormat.forPattern("MM-dd-yyyy")
            
    );
    // @formatter:on

    /**
     * Parse the number of microseconds from the UNIX epoch that are described
     * by {@code str}.
     * 
     * @param str
     * @return the microseconds
     */
    public static long parseMicros(String str) {
        // We should assume that the timestamp is in microseconds since
        // that is the output format used in ConcourseShell
        Long micros = Longs.tryParse(str);
        if(micros != null) {
            return micros;
        }
        else {
            Iterator<DateTimeFormatter> it = DATETIME_FORMATTERS.iterator();
            while (micros == null && it.hasNext()) {
                DateTimeFormatter formatter = it.next();
                try {
                    long millis = formatter.parseMillis(str);
                    micros = TimeUnit.MILLISECONDS.toMicros(millis);
                    break;
                }
                catch (IllegalArgumentException e) {/* no-op */}
            }
            if(micros != null) {
                return micros;
            }
            else {
                List<DateGroup> groups = TIMESTAMP_PARSER.parse(str);
                Date date = null;
                for (DateGroup group : groups) {
                    date = group.getDates().get(0);
                    break;
                }
                if(date != null) {
                    return TimeUnit.MILLISECONDS
                            .toMicros(new DateTime(date).getMillis());
                }
                else {
                    throw new IllegalStateException(
                            "Unrecognized date/time string '" + str + "'");
                }
            }
        }
    }

    // @formatter:off
    static {  // Warm up the NLP so the first invocation isn't slow
        NaturalLanguage.parseMicros("now"); 
    }
    // @formatter:on

    private NaturalLanguage() {/* noop */}

}
