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
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package com.cinchapi.ccl.grammar;

import javax.annotation.Nullable;

/**
 * A {@link Symbol} that represents a page of results.
 * <p>
 * A {@link PageSymbol} can be constructed in two modes:
 * <ul>
 *   <li>{@link Mode#PAGE_NUMBER} &mdash; a 1-indexed page
 *       number and page size (e.g., page 3 size 10)</li>
 *   <li>{@link Mode#SKIP_LIMIT} &mdash; a 0-indexed skip
 *       offset and limit (e.g., skip 20 limit 10)</li>
 * </ul>
 * Both modes resolve to the same underlying skip/limit
 * representation.
 * </p>
 *
 * @author Jeff Nelson
 */
public class PageSymbol implements Symbol {

    /**
     * The default page number (1-indexed).
     */
    public static final int DEFAULT_PAGE_NUMBER = 1;

    /**
     * The default number of items per page.
     */
    public static final int DEFAULT_PAGE_SIZE = 20;

    /**
     * The mode in which this {@link PageSymbol} was
     * constructed.
     */
    public enum Mode {

        /**
         * Constructed from a 1-indexed page number and size.
         */
        PAGE_NUMBER,

        /**
         * Constructed from a 0-indexed skip offset and limit.
         */
        SKIP_LIMIT
    }

    /**
     * Create a {@link PageSymbol} from an explicit skip offset
     * and limit.
     *
     * @param skip the number of items to skip (0-indexed)
     * @param limit the maximum number of items to return
     * @return the {@link PageSymbol}
     */
    public static PageSymbol ofSkip(int skip, int limit) {
        return new PageSymbol(skip, limit, Mode.SKIP_LIMIT);
    }

    /**
     * The number of items to skip.
     */
    private final int skip;

    /**
     * The maximum number of items to return.
     */
    private final int limit;

    /**
     * The construction mode.
     */
    private final Mode mode;

    /**
     * Construct a new instance from a page number and size.
     *
     * @param number the 1-indexed page number, or
     *        {@code null} for the default
     * @param size the page size, or {@code null} for
     *        the default
     */
    public PageSymbol(@Nullable Integer number,
            @Nullable Integer size) {
        int n = number != null ? number : DEFAULT_PAGE_NUMBER;
        int s = size != null ? size : DEFAULT_PAGE_SIZE;
        this.skip = s * (n - 1);
        this.limit = s;
        this.mode = Mode.PAGE_NUMBER;
    }

    /**
     * Construct a new instance with explicit skip, limit, and
     * mode.
     *
     * @param skip the number of items to skip
     * @param limit the maximum number of items
     * @param mode the construction mode
     */
    private PageSymbol(int skip, int limit, Mode mode) {
        this.skip = skip;
        this.limit = limit;
        this.mode = mode;
    }

    /**
     * Return the 1-indexed page number.
     * <p>
     * For {@link Mode#SKIP_LIMIT} instances, this is
     * computed as {@code (skip / limit) + 1}.
     * </p>
     *
     * @return the page number
     */
    public int number() {
        return (skip / limit) + 1;
    }

    /**
     * Return the page size (maximum number of items per
     * page).
     *
     * @return the page size
     */
    public int size() {
        return limit;
    }

    /**
     * Return the number of items to skip before the first
     * item on this page.
     *
     * @return the offset
     */
    public int offset() {
        return skip;
    }

    /**
     * Alias for {@link #offset()}.
     *
     * @return the offset
     */
    public int skip() {
        return offset();
    }

    /**
     * Alias for {@link #size()}.
     *
     * @return the limit
     */
    public int limit() {
        return size();
    }

    /**
     * Return the {@link Mode} in which this
     * {@link PageSymbol} was constructed.
     *
     * @return the mode
     */
    public Mode mode() {
        return mode;
    }

    @Override
    public String toString() {
        if(mode == Mode.SKIP_LIMIT) {
            return "skip " + skip + " limit " + limit;
        }
        else {
            return "page " + number() + " size " + limit;
        }
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof PageSymbol) {
            PageSymbol other = (PageSymbol) obj;
            return skip == other.skip && limit == other.limit;
        }
        else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return 31 * skip + limit;
    }

}
