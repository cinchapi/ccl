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

import com.google.common.base.Preconditions;

/**
 * A {@link NavigationKeyStop} is a single stop along a
 * {@link NavigationKeySymbol} path. A stop has a {@link #key() key} and may
 * be marked as {@link #isTransitive() transitive} to indicate that the
 * corresponding link should be followed recursively rather than traversed a
 * single time.
 *
 * @author Jeff Nelson
 */
public final class NavigationKeyStop {

    /**
     * Return the {@link #key() key} of the {@link NavigationKeyStop}
     * represented by {@code value}.
     *
     * @param value the raw value
     * @return the key
     */
    public static String extractKeyAtPossiblyTransitiveStop(String value) {
        int length = value.length();
        if(length > 0 && value.charAt(length - 1) == TRANSITIVE_SUFFIX) {
            return value.substring(0, length - 1);
        }
        return value;
    }

    /**
     * Return {@code true} if {@code value} represents a transitive
     * {@link NavigationKeyStop}.
     *
     * @param value the raw value
     * @return {@code true} if {@code value} is a transitive stop
     */
    public static boolean isTransitiveStop(String value) {
        return !extractKeyAtPossiblyTransitiveStop(value).equals(value);
    }

    /**
     * Return the {@link NavigationKeyStop} that corresponds to the given raw
     * {@code value} (e.g. {@code "children*"} or {@code "name"}).
     *
     * @param value the raw value string; must be non-{@code null},
     *            non-empty, and must contain at least one character before
     *            any trailing {@link #TRANSITIVE_SUFFIX}
     * @return the {@link NavigationKeyStop}
     * @throws NullPointerException if {@code value} is {@code null}
     * @throws IllegalArgumentException if {@code value} is empty or
     *             consists solely of the {@link #TRANSITIVE_SUFFIX}
     */
    public static NavigationKeyStop parse(String value) {
        Preconditions.checkNotNull(value,
                "navigation key stop value cannot be null");
        Preconditions.checkArgument(!value.isEmpty(),
                "navigation key stop value cannot be empty");
        String key = extractKeyAtPossiblyTransitiveStop(value);
        boolean isTransitive = !key.equals(value);
        Preconditions.checkArgument(!isTransitive || !key.isEmpty(),
                "navigation key stop value cannot consist solely of the "
                        + "transitive suffix '%s'",
                TRANSITIVE_SUFFIX);
        return new NavigationKeyStop(key, isTransitive);
    }

    /**
     * The suffix character that marks a {@link NavigationKeyStop} as
     * transitive when it appears at the end of its raw component string.
     */
    public static final char TRANSITIVE_SUFFIX = '*';

    /**
     * The key of this {@link NavigationKeyStop}, without any trailing
     * {@link #TRANSITIVE_SUFFIX}.
     */
    private final String key;

    /**
     * A flag that indicates whether this {@link NavigationKeyStop} is
     * transitive.
     */
    private final boolean isTransitive;

    /**
     * Construct a new {@link NavigationKeyStop}.
     *
     * @param key the key of the stop, without any trailing
     *            {@link #TRANSITIVE_SUFFIX}
     * @param isTransitive whether the stop is transitive
     */
    private NavigationKeyStop(String key, boolean isTransitive) {
        this.key = key;
        this.isTransitive = isTransitive;
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
            return isTransitive == other.isTransitive && key.equals(other.key);
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, isTransitive);
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
     * Return the key of this {@link NavigationKeyStop}, without any trailing
     * {@link #TRANSITIVE_SUFFIX}.
     *
     * @return the key
     */
    public String key() {
        return key;
    }

    @Override
    public String toString() {
        return value();
    }

    /**
     * Return the raw value of this {@link NavigationKeyStop}, which is the
     * {@link #key() key} with the {@link #TRANSITIVE_SUFFIX} appended when
     * this stop {@link #isTransitive() is transitive}.
     *
     * @return the value
     */
    public String value() {
        return isTransitive ? key + TRANSITIVE_SUFFIX : key;
    }

}
