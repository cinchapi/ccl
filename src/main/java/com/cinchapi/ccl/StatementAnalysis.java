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
package com.cinchapi.ccl;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.cinchapi.ccl.type.Operator;

/**
 * The {@link StatementAnalysis} provides metatadata about the nature of a CCL
 * statement.
 *
 * @author Jeff Nelson
 */
public interface StatementAnalysis {

    /**
     * Return an ordered collection of keys that are included in the CCL
     * statement (a flat key, a navigation path, or a scope-pivot path),
     * each rendered in storage form (no bracket-timestamp annotations).
     * To recover the per-key bracket annotations, use
     * {@link #temporalKeys()}.
     *
     * @return the included keys
     */
    public Set<String> keys();

    /**
     * Return an ordered collection of keys that are included in the CCL
     * statement in an expression that contains the specified
     * {@code operator}, each rendered without bracket-timestamp
     * annotations.
     *
     * @return the included keys that are evaluated against the
     *         {@code operator}
     */
    public Set<String> keys(Operator operator);

    /**
     * Return all the operators used in the CCL statement.
     *
     * @return the included operators
     */
    public Set<Operator> operators();

    /**
     * Return every distinct storage-level key the statement touches. A
     * flat key contributes itself; a navigation key contributes each
     * stop's storage key; a scope prefix contributes the prefix's
     * storage key(s). Bracket annotations and transitive markers are
     * stripped.
     *
     * @return the set of storage keys
     */
    public Set<String> storageKeys();

    /**
     * Return the {@link #storageKeys()} that appear in an expression
     * evaluated against the specified {@code operator}.
     *
     * @param operator the {@link Operator} to filter on
     * @return the matching storage keys
     */
    public Set<String> storageKeys(Operator operator);

    /**
     * Return the storage keys that carry a bracket-timestamp annotation
     * anywhere they appear, paired with the distinct microsecond
     * timestamps each is pinned at. The same storage key can appear at
     * multiple timestamps (e.g. {@code name[t1] = X AND name[t2] = Y}
     * or {@code a[t1].b.a[t2] = X}); the value set captures every
     * distinct pin.
     *
     * @return a {@link Map} from storage key to its distinct pinned
     *         microsecond timestamps
     */
    public Map<String, Set<Long>> temporalKeys();

    /**
     * Return the {@link #temporalKeys()} that appear in an expression
     * evaluated against the specified {@code operator}.
     *
     * @param operator the {@link Operator} to filter on
     * @return a {@link Map} from storage key to its distinct pinned
     *         microsecond timestamps for the matching expressions
     */
    public Map<String, Set<Long>> temporalKeys(Operator operator);

    /**
     * Return the storage keys that appear as a transitive navigation
     * stop ({@code key*}) anywhere in the statement.
     *
     * @return the transitive storage keys
     */
    public Set<String> transitiveNavigationKeys();

    /**
     * Return the {@link #transitiveNavigationKeys()} that appear in an
     * expression evaluated against the specified {@code operator}.
     *
     * @param operator the {@link Operator} to filter on
     * @return the matching transitive storage keys
     */
    public Set<String> transitiveNavigationKeys(Operator operator);

    /**
     * Return the distinct navigation key paths referenced by the
     * statement, each rendered in canonical storage form (no brackets,
     * no transitive markers, dot-separated).
     *
     * @return the navigation key paths
     */
    public Set<String> navigationKeys();

    /**
     * Return the {@link #navigationKeys()} that appear in an expression
     * evaluated against the specified {@code operator}.
     *
     * @param operator the {@link Operator} to filter on
     * @return the matching navigation key paths
     */
    public Set<String> navigationKeys(Operator operator);

    /**
     * Return the storage keys that participate in any navigation key
     * path. This is the subset of {@link #storageKeys()} reachable only
     * through a {@link #navigationKeys() navigation key}, useful for
     * detecting which keys are involved in path traversal so callers
     * can apply navigation-specific optimizations.
     *
     * @return the storage keys that appear as a navigation stop
     */
    public Set<String> navigationKeyStops();

    /**
     * Return the {@link #navigationKeyStops()} that appear in an
     * expression evaluated against the specified {@code operator}.
     *
     * @param operator the {@link Operator} to filter on
     * @return the matching storage keys that appear as a navigation
     *         stop
     */
    public Set<String> navigationKeyStops(Operator operator);

    /**
     * Return a {@link Map} from each scope-pivot key (the prefix of a
     * {@code prefix.(...)} construct, in canonical storage form) to the
     * direct child keys evaluated within that scope. A nested scope
     * contributes its own pivot to the outer scope's child list and
     * carries its own entry in the {@link Map}.
     *
     * @return a {@link Map} from scope pivot to the storage keys (or
     *         nested pivot keys) directly inside that scope
     */
    public Map<String, List<String>> scopedKeys();

    /**
     * Return the {@link #scopedKeys()} entries whose direct children
     * include at least one expression evaluated against the specified
     * {@code operator}. Pivots whose only matching expressions are
     * inside nested scopes are not reported here.
     *
     * @param operator the {@link Operator} to filter on
     * @return a {@link Map} from scope pivot to the storage keys (or
     *         nested pivot keys) directly inside that scope, restricted
     *         to scopes containing the {@code operator}
     */
    public Map<String, List<String>> scopedKeys(Operator operator);

}
