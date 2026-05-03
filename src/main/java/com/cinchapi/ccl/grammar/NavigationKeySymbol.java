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
 * A {@link NavigationKeySymbol} is a {@link KeyTokenSymbol} whose value
 * is a dotted path that traverses linked records. Any segment of the
 * path may be marked as transitive and may carry a bracket-timestamp
 * annotation that pins that segment's read.
 *
 * @author Jeff Nelson
 */
public class NavigationKeySymbol extends KeyTokenSymbol<String> {

    /**
     * Return the {@link KeyTokenSymbol} that represents the scope prefix
     * encoded by {@code path} (the navigation portion of a
     * {@code prefix.(inner)} construct, with the trailing {@code .(}
     * already stripped). A multi-stop or transitive path yields a
     * {@link NavigationKeySymbol}; a single non-transitive stop with a
     * bracket annotation yields a {@link TemporalKeySymbol} wrapping a
     * {@link KeySymbol}; a single non-transitive stop without an
     * annotation yields a plain {@link KeySymbol}.
     *
     * @param path the raw scope-prefix path
     * @return the {@link KeyTokenSymbol}
     */
    public static KeyTokenSymbol<?> parseScopePrefix(String path) {
        int dots = 0;
        boolean hasTransitive = false;
        int depth = 0;
        for (int i = 0; i < path.length(); i++) {
            char c = path.charAt(i);
            if(c == '[') {
                depth++;
            }
            else if(c == ']') {
                depth--;
            }
            else if(depth == 0) {
                if(c == '.') {
                    dots++;
                }
                else if(c == '*') {
                    hasTransitive = true;
                }
            }
        }
        if(dots > 0 || hasTransitive) {
            return new NavigationKeySymbol(path);
        }
        NavigationKeyStop stop = NavigationKeyStop.parse(path);
        KeySymbol base = new KeySymbol(stop.key());
        return stop.timestamp() == null ? base
                : new TemporalKeySymbol(base, stop.timestamp());
    }

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
     * {@link NavigationKeySymbol}, in path order. Any transitive marker
     * or bracket-timestamp annotation is preserved in the returned
     * component (e.g. {@code "children*[123]"}).
     *
     * @return the key components
     */
    public String[] components() {
        String raw = key();
        List<String> parts = new ArrayList<>();
        int start = 0;
        int depth = 0;
        for (int i = 0; i < raw.length(); i++) {
            char c = raw.charAt(i);
            if(c == '[') {
                depth++;
            }
            else if(c == ']') {
                depth--;
            }
            else if(c == '.' && depth == 0) {
                parts.add(raw.substring(start, i));
                start = i + 1;
            }
        }
        parts.add(raw.substring(start));
        return parts.toArray(new String[0]);
    }

    /**
     * Return the {@link NavigationKeyStop NavigationKeyStops} that make
     * up this {@link NavigationKeySymbol}, in path order.
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
