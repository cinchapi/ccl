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

/**
 * A {@link Symbol} that represents a key (e.g. selection key or evaluation key).
 */
public abstract class KeyTokenSymbol<T> implements PostfixNotationSymbol {

    /**
     * Throw an {@link IllegalArgumentException} when {@code key} carries any
     * read-time annotation. Called from grammar actions for commands whose
     * semantics forbid per-key annotations (writes and range-history reads) so
     * the rejection happens at parse time.
     *
     * @param key the {@link KeyTokenSymbol} to verify
     * @param context a short label naming the rejecting context, used in the
     *            exception message
     * @throws IllegalArgumentException when {@code key} is annotated
     */
    public static void requireBaseKey(KeyTokenSymbol<?> key, String context) {
        if(key.isAnnotated()) {
            throw new IllegalArgumentException(String.format(
                    "%s does not accept a bracket-timestamp annotation on "
                            + "the key; got: %s",
                    context, key));
        }
    }

    /**
     * Apply {@link #requireBaseKey} to every {@link KeyTokenSymbol} in
     * {@code keys}.
     *
     * @param keys the {@link KeyTokenSymbol KeyTokenSymbols} to verify
     * @param context a short label naming the rejecting context
     * @throws IllegalArgumentException when any element is annotated
     */
    public static void requireBaseKeys(
            Iterable<? extends KeyTokenSymbol<?>> keys, String context) {
        for (KeyTokenSymbol<?> key : keys) {
            requireBaseKey(key, context);
        }
    }


    /**
     * The content of the {@link Symbol}.
     */
    protected final T key;

    /**
     * Construct a new instance.
     *
     * @param key
     */
    public KeyTokenSymbol(T key) {
        this.key = key;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof KeyTokenSymbol) {
            return key.equals(((KeyTokenSymbol<?>) obj).key);
        }
        else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return key.hashCode();
    }

    /**
     * Return the key that this symbol expresses.
     *
     * @return the key
     */
    public T key() {
        return key;
    }

    /**
     * Return the canonical key string this {@link KeyTokenSymbol} represents
     * with all read-time annotations stripped. Subclasses that carry
     * annotations override to return the annotation-free form; the default
     * returns {@link #key()} as a string.
     *
     * @return the base key string
     */
    public String baseKey() {
        return key.toString();
    }

    /**
     * Return {@code true} when this {@link KeyTokenSymbol} carries a read-time
     * annotation anywhere in its structure (the leaf, a navigation stop, or a
     * wrapped key). Used by command grammars to reject annotations where they
     * are semantically invalid (writes) and by analysis tools that surface
     * which keys are annotated.
     *
     * @return {@code true} if any annotation is present
     */
    public boolean isAnnotated() {
        return false;
    }

    /**
     * Return a {@link KeyTokenSymbol} equivalent to this one with every
     * read-time annotation removed. Returns {@code this} when there is no
     * annotation to strip.
     *
     * @return the {@link KeyTokenSymbol} in its base form
     */
    public KeyTokenSymbol<?> unannotated() {
        return this;
    }

    @Override
    public String toString() {
        return key.toString();
    }
}
