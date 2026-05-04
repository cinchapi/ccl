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

import com.google.common.base.Preconditions;

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
     * Return a new {@link NavigationKeySymbol} that mirrors {@code nav}
     * but folds {@code ts} onto the last stop. Used by the parser when a
     * trailing bracket-timestamp suffix follows a navigation token whose
     * last stop carried no inline annotation.
     *
     * @param nav the existing {@link NavigationKeySymbol}
     * @param ts the {@link TimestampSymbol} to fold onto the last stop
     * @return a new {@link NavigationKeySymbol} with the timestamp on
     *         the last stop
     * @throws IllegalArgumentException if the last stop already carries a
     *             bracket-timestamp annotation, or if the last stop is
     *             transitive (the canonical form is {@code key[t]*}, not
     *             {@code key*[t]})
     */
    public static NavigationKeySymbol withTimestampOnLastStop(
            NavigationKeySymbol nav, TimestampSymbol ts) {
        Preconditions.checkNotNull(nav,
                "navigation key symbol cannot be null");
        Preconditions.checkNotNull(ts,
                "timestamp symbol cannot be null");
        List<NavigationKeyStop> stops = nav.stops();
        NavigationKeyStop last = stops.get(stops.size() - 1);
        Preconditions.checkArgument(last.timestamp() == null,
                "navigation key cannot carry two bracket-timestamp "
                        + "annotations on the same stop");
        Preconditions.checkArgument(!last.isTransitive(),
                "navigation key cannot use the non-canonical "
                        + "'key*[t]' order; the canonical form binds "
                        + "the bracket to the key and lets the "
                        + "transitive marker terminate the stop "
                        + "('key[t]*')");
        List<NavigationKeyStop> updated = new ArrayList<>(stops);
        updated.set(updated.size() - 1, last.withTimestamp(ts));
        return new NavigationKeySymbol(updated);
    }

    /**
     * Return the canonical string representation of {@code stops}.
     *
     * @param stops the {@link NavigationKeyStop NavigationKeyStops}
     * @return the canonical raw string
     */
    private static String joinStops(List<NavigationKeyStop> stops) {
        StringBuilder sb = new StringBuilder();
        for (NavigationKeyStop stop : stops) {
            if(sb.length() > 0) {
                sb.append('.');
            }
            sb.append(stop.value());
        }
        return sb.toString();
    }

    /**
     * Split {@code raw} on top-level periods (those outside any bracket
     * annotation) and parse each piece into a {@link NavigationKeyStop}.
     *
     * @param raw the raw key string
     * @return the parsed stops, in path order
     */
    private static List<NavigationKeyStop> parseStops(String raw) {
        List<NavigationKeyStop> stops = new ArrayList<>();
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
                stops.add(NavigationKeyStop.parse(raw.substring(start, i)));
                start = i + 1;
            }
        }
        stops.add(NavigationKeyStop.parse(raw.substring(start)));
        return Collections.unmodifiableList(stops);
    }

    /**
     * The parsed {@link NavigationKeyStop NavigationKeyStops} that make
     * up this {@link NavigationKeySymbol}, in path order.
     */
    private final List<NavigationKeyStop> stops;

    /**
     * Construct a new {@link NavigationKeySymbol}. The stored
     * {@link #key()} is the canonical join of the parsed stops, so two
     * {@link NavigationKeySymbol NavigationKeySymbols} built from
     * different but semantically equivalent raw strings (e.g.
     * {@code "a[at 123].foo"} vs {@code "a[123].foo"}) expose the same
     * {@code key()} and therefore compare equal under the inherited
     * {@link KeyTokenSymbol#equals} contract. A single-stop
     * {@link NavigationKeySymbol} whose stop carries no timestamp or
     * transitive marker compares equal to a {@link KeySymbol} of the
     * same name for the same reason — both carry the same canonical
     * key string.
     *
     * @param key the raw key string
     */
    public NavigationKeySymbol(String key) {
        this(parseStops(key));
    }

    /**
     * Construct a new {@link NavigationKeySymbol} from already-parsed
     * {@link NavigationKeyStop NavigationKeyStops}, without re-parsing
     * a string. The raw {@link #key()} is the canonical join of
     * {@code stops}.
     *
     * @param stops the {@link NavigationKeyStop NavigationKeyStops}
     */
    private NavigationKeySymbol(List<NavigationKeyStop> stops) {
        super(joinStops(stops));
        this.stops = Collections
                .unmodifiableList(new ArrayList<>(stops));
    }

    /**
     * Return the canonical component strings that make up this
     * {@link NavigationKeySymbol}, in path order. Any transitive marker
     * or bracket-timestamp annotation is rendered in canonical form
     * (e.g. {@code "children[123]*"} regardless of whether the input
     * used {@code [at 123]}).
     *
     * @return the key components
     */
    public String[] components() {
        String[] result = new String[stops.size()];
        for (int i = 0; i < stops.size(); i++) {
            result[i] = stops.get(i).value();
        }
        return result;
    }

    @Override
    public String storageKey() {
        StringBuilder sb = new StringBuilder();
        for (NavigationKeyStop stop : stops) {
            if(sb.length() > 0) {
                sb.append('.');
            }
            sb.append(stop.storageValue());
        }
        return sb.toString();
    }

    @Override
    public boolean isTemporal() {
        for (NavigationKeyStop stop : stops) {
            if(stop.timestamp() != null) {
                return true;
            }
        }
        return false;
    }

    @Override
    public KeyTokenSymbol<?> untemporal() {
        if(!isTemporal()) {
            return this;
        }
        List<NavigationKeyStop> stripped = new ArrayList<>(stops.size());
        for (NavigationKeyStop stop : stops) {
            stripped.add(stop.timestamp() != null
                    ? stop.withTimestamp(null) : stop);
        }
        return new NavigationKeySymbol(stripped);
    }

    /**
     * Return the {@link NavigationKeyStop NavigationKeyStops} that make
     * up this {@link NavigationKeySymbol}, in path order.
     *
     * @return the stops
     */
    public List<NavigationKeyStop> stops() {
        return stops;
    }

}
