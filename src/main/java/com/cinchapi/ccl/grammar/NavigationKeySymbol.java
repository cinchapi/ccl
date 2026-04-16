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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A {@link NavigationKeySymbol} is a {@link KeyTokenSymbol} whose value is
 * a dotted path that traverses linked records. Any segment of the path may
 * be marked as transitive.
 *
 * @author Jeff Nelson
 */
public class NavigationKeySymbol extends KeyTokenSymbol<String> {

    /**
     * Construct a new {@link NavigationKeySymbol}.
     *
     * @param key the raw key string
     */
    public NavigationKeySymbol(String key) {
        super(key);
    }

    /**
     * Return the raw component strings that make up this
     * {@link NavigationKeySymbol}, in path order. Any transitive marker is
     * preserved in the returned component (e.g. {@code "children*"}).
     *
     * @return the key components
     */
    public String[] components() {
        return key().split("\\.");
    }

    /**
     * Return the {@link NavigationKeyStop NavigationKeyStops} that make up
     * this {@link NavigationKeySymbol}, in path order.
     *
     * @return the stops
     */
    public List<NavigationKeyStop> stops() {
        String[] parts = components();
        List<NavigationKeyStop> stops = new ArrayList<>(parts.length);
        for (String part : parts) {
            stops.add(NavigationKeyStop.parse(part));
        }
        return Collections.unmodifiableList(stops);
    }

}
