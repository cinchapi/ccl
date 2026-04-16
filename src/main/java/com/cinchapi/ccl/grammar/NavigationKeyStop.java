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

/**
 * A {@link NavigationKeyStop} is a single stop along a
 * {@link NavigationKeySymbol} path. A stop has a name and may be marked as
 * transitive to indicate that the corresponding link should be followed
 * recursively rather than traversed a single time.
 *
 * @author Jeff Nelson
 */
public final class NavigationKeyStop {

    /**
     * The suffix character that marks a {@link NavigationKeyStop} as
     * transitive when it appears at the end of its raw component string.
     */
    static final char TRANSITIVE_SUFFIX = '*';

    /**
     * Return a {@link NavigationKeyStop} that corresponds to the given raw
     * navigation key {@code component} (e.g. {@code "children*"} or
     * {@code "name"}).
     *
     * @param component the raw component string
     * @return the {@link NavigationKeyStop}
     */
    public static NavigationKeyStop parse(String component) {
        int length = component.length();
        if(length > 0
                && component.charAt(length - 1) == TRANSITIVE_SUFFIX) {
            return new NavigationKeyStop(component.substring(0, length - 1),
                    true);
        }
        return new NavigationKeyStop(component, false);
    }

    /**
     * The base name of this {@link NavigationKeyStop}, without any
     * transitive suffix.
     */
    private final String name;

    /**
     * A flag that indicates whether this {@link NavigationKeyStop} is
     * transitive.
     */
    private final boolean isTransitive;

    /**
     * Construct a new {@link NavigationKeyStop}.
     *
     * @param name the base name of the stop, without any transitive suffix
     * @param isTransitive whether the stop is transitive
     */
    public NavigationKeyStop(String name, boolean isTransitive) {
        this.name = name;
        this.isTransitive = isTransitive;
    }

    /**
     * Return the base name of this {@link NavigationKeyStop}, without any
     * transitive suffix.
     *
     * @return the name
     */
    public String name() {
        return name;
    }

    /**
     * Return whether this {@link NavigationKeyStop} is transitive.
     *
     * @return {@code true} if transitive
     */
    public boolean isTransitive() {
        return isTransitive;
    }

    @Override
    public boolean equals(Object obj) {
        if(this == obj) {
            return true;
        }
        if(!(obj instanceof NavigationKeyStop)) {
            return false;
        }
        NavigationKeyStop other = (NavigationKeyStop) obj;
        return isTransitive == other.isTransitive && name.equals(other.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, isTransitive);
    }

    @Override
    public String toString() {
        return isTransitive ? name + TRANSITIVE_SUFFIX : name;
    }

}
