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

import java.util.Objects;

import com.cinchapi.ccl.util.NaturalLanguage;
import com.google.common.base.Preconditions;

/**
 * A {@link NavigationKeyStop} is a single stop along a
 * {@link NavigationKeySymbol} path. A stop has a {@link #key() key}, may
 * be marked as {@link #isTransitive() transitive} to indicate that the
 * corresponding link should be followed recursively rather than traversed
 * a single time, and may carry a {@link #timestamp() timestamp} that pins
 * the stop's read.
 *
 * @author Jeff Nelson
 */
public final class NavigationKeyStop {

    /**
     * Return the bare key of the {@link NavigationKeyStop} represented
     * by {@code value}, with any {@link #TRANSITIVE_SUFFIX} or
     * bracket-timestamp annotation stripped off.
     *
     * @param value the raw value
     * @return the key
     */
    public static String extractBaseKey(String value) {
        String stripped = stripBracketAnnotation(value);
        int length = stripped.length();
        if(length > 0 && stripped.charAt(length - 1) == TRANSITIVE_SUFFIX) {
            return stripped.substring(0, length - 1);
        }
        return stripped;
    }

    /**
     * Return {@code true} if {@code value} represents a transitive
     * {@link NavigationKeyStop}.
     *
     * @param value the raw value
     * @return {@code true} if {@code value} is a transitive stop
     */
    public static boolean isTransitiveStop(String value) {
        return !extractBaseKey(value).equals(stripBracketAnnotation(value));
    }

    /**
     * Return the {@link NavigationKeyStop} that corresponds to the given
     * raw {@code value} (e.g. {@code "children*"}, {@code "name"},
     * {@code "a[123]"}, {@code "a*[at \"yesterday\"]"}).
     *
     * @param value the raw value string; must be non-{@code null},
     *            non-empty, and must contain at least one character
     *            before any trailing {@link #TRANSITIVE_SUFFIX}
     * @return the {@link NavigationKeyStop}
     * @throws NullPointerException if {@code value} is {@code null}
     * @throws IllegalArgumentException if {@code value} is empty,
     *             consists solely of the {@link #TRANSITIVE_SUFFIX}, or
     *             carries a malformed bracket-timestamp annotation
     */
    public static NavigationKeyStop parse(String value) {
        Preconditions.checkNotNull(value,
                "navigation key stop value cannot be null");
        Preconditions.checkArgument(!value.isEmpty(),
                "navigation key stop value cannot be empty");
        TimestampSymbol timestamp = parseTrailingBracketTimestamp(value);
        String stripped = stripBracketAnnotation(value);
        String key = extractBaseKey(stripped);
        boolean isTransitive = !key.equals(stripped);
        Preconditions.checkArgument(!isTransitive || !key.isEmpty(),
                "navigation key stop value cannot consist solely of the "
                        + "transitive suffix '%s'",
                TRANSITIVE_SUFFIX);
        Preconditions.checkArgument(!key.isEmpty(),
                "navigation key stop value cannot consist solely of a "
                        + "bracket annotation");
        return new NavigationKeyStop(key, isTransitive, timestamp);
    }

    /**
     * Return {@code value} with any trailing bracket-timestamp annotation
     * removed.
     *
     * @param value the raw value
     * @return {@code value} without its bracket annotation
     */
    private static String stripBracketAnnotation(String value) {
        if(!value.isEmpty()
                && value.charAt(value.length() - 1) == BRACKET_CLOSE) {
            int open = value.lastIndexOf(BRACKET_OPEN);
            if(open >= 0) {
                value = value.substring(0, open);
            }
        }
        return value;
    }

    /**
     * Return the {@link TimestampSymbol} encoded by the trailing
     * {@code [...]} of {@code value}, or {@code null} when {@code value}
     * carries no bracket annotation.
     *
     * @param value the raw value
     * @return the {@link TimestampSymbol} or {@code null}
     */
    private static TimestampSymbol parseTrailingBracketTimestamp(
            String value) {
        if(value.isEmpty()
                || value.charAt(value.length() - 1) != BRACKET_CLOSE) {
            return null;
        }
        int open = value.lastIndexOf(BRACKET_OPEN);
        if(open < 0) {
            return null;
        }
        String content = value.substring(open + 1, value.length() - 1);
        return parseBracketContent(content);
    }

    /**
     * Parse the {@code content} between {@code [} and {@code ]} into a
     * {@link TimestampSymbol}. The leading {@code at} / {@code on} /
     * {@code during} keyword is optional.
     *
     * @param content the bracket content
     * @return the {@link TimestampSymbol}
     * @throws IllegalArgumentException if {@code content} is empty or
     *             carries a keyword with no following value
     */
    private static TimestampSymbol parseBracketContent(String content) {
        String trimmed = content.trim();
        Preconditions.checkArgument(!trimmed.isEmpty(),
                "bracket-timestamp annotation cannot be empty");
        String[] parts = trimmed.split("\\s+");
        int start = 0;
        String first = parts[0].toLowerCase();
        if(first.equals("at") || first.equals("on")
                || first.equals("during")) {
            start = 1;
        }
        Preconditions.checkArgument(start < parts.length,
                "bracket-timestamp annotation has no value after keyword");
        StringBuilder joined = new StringBuilder();
        for (int i = start; i < parts.length; i++) {
            if(joined.length() > 0) {
                joined.append(' ');
            }
            joined.append(parts[i]);
        }
        return new TimestampSymbol(
                NaturalLanguage.parseMicros(joined.toString()));
    }

    /**
     * The suffix character that marks a {@link NavigationKeyStop} as
     * transitive when it appears at the end of its raw component string.
     */
    public static final char TRANSITIVE_SUFFIX = '*';

    /**
     * The opening delimiter of a bracket-timestamp annotation.
     */
    private static final char BRACKET_OPEN = '[';

    /**
     * The closing delimiter of a bracket-timestamp annotation.
     */
    private static final char BRACKET_CLOSE = ']';

    /**
     * The key of this {@link NavigationKeyStop}, without any
     * {@link #TRANSITIVE_SUFFIX} or bracket-timestamp annotation.
     */
    private final String key;

    /**
     * A flag that indicates whether this {@link NavigationKeyStop} is
     * transitive.
     */
    private final boolean isTransitive;

    /**
     * The {@link TimestampSymbol} that pins this
     * {@link NavigationKeyStop}'s read, or {@code null} if the stop is
     * unstamped.
     */
    private final TimestampSymbol timestamp;

    /**
     * Construct a new {@link NavigationKeyStop}.
     *
     * @param key the key of the stop, without any
     *            {@link #TRANSITIVE_SUFFIX} or bracket annotation
     * @param isTransitive whether the stop is transitive
     * @param timestamp the {@link TimestampSymbol} pinned to the stop, or
     *            {@code null}
     */
    private NavigationKeyStop(String key, boolean isTransitive,
            TimestampSymbol timestamp) {
        this.key = key;
        this.isTransitive = isTransitive;
        this.timestamp = timestamp;
    }

    @Override
    public boolean equals(Object obj) {
        if(this == obj) {
            return true;
        }
        else if(!(obj instanceof NavigationKeyStop)) {
            return false;
        }
        else {
            NavigationKeyStop other = (NavigationKeyStop) obj;
            return isTransitive == other.isTransitive && key.equals(other.key)
                    && Objects.equals(timestamp, other.timestamp);
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, isTransitive, timestamp);
    }

    /**
     * Return whether this {@link NavigationKeyStop} is transitive.
     *
     * @return {@code true} if transitive
     */
    public boolean isTransitive() {
        return isTransitive;
    }

    /**
     * Return the key of this {@link NavigationKeyStop}, without any
     * trailing {@link #TRANSITIVE_SUFFIX} or bracket annotation.
     *
     * @return the key
     */
    public String key() {
        return key;
    }

    /**
     * Return the {@link TimestampSymbol} that pins this
     * {@link NavigationKeyStop}'s read, or {@code null} when the stop is
     * unstamped.
     *
     * @return the {@link TimestampSymbol} or {@code null}
     */
    public TimestampSymbol timestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return value();
    }

    /**
     * Return the canonical raw value of this {@link NavigationKeyStop} —
     * the {@link #key() key}, optionally followed by the
     * {@link #TRANSITIVE_SUFFIX} when the stop
     * {@link #isTransitive() is transitive}, and a keyword-less bracket
     * annotation when the stop carries a {@link #timestamp() timestamp}.
     *
     * @return the value
     */
    public String value() {
        StringBuilder sb = new StringBuilder(key);
        if(isTransitive) {
            sb.append(TRANSITIVE_SUFFIX);
        }
        if(timestamp != null) {
            sb.append(BRACKET_OPEN);
            sb.append(timestamp.timestamp());
            sb.append(BRACKET_CLOSE);
        }
        return sb.toString();
    }

}
