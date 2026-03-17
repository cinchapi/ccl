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
 * Regardless of how the pagination was expressed in CCL
 * (page-number or skip/limit), a {@link PageSymbol} always
 * normalizes to a skip and limit representation.
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
     * Create a {@link PageSymbol} from a 1-indexed page
     * number and page size, applying defaults for
     * {@code null} values.
     *
     * @param number the 1-indexed page number, or
     *        {@code null} for the default
     * @param size the page size, or {@code null} for
     *        the default
     * @return the {@link PageSymbol}
     */
    public static PageSymbol fromPageNumberAndSize(
            @Nullable Integer number, @Nullable Integer size) {
        int n = number != null ? number : DEFAULT_PAGE_NUMBER;
        int s = size != null ? size : DEFAULT_PAGE_SIZE;
        return new PageSymbol(s * (n - 1), s);
    }

    /**
     * Create a {@link PageSymbol} from a 1-indexed page number
     * and the default page size.
     *
     * @param number the 1-indexed page number
     * @return the {@link PageSymbol}
     */
    public static PageSymbol fromPageNumber(int number) {
        return fromPageNumberAndSize(number, null);
    }

    /**
     * Create a {@link PageSymbol} for the first page with a
     * specific page size.
     *
     * @param size the page size
     * @return the {@link PageSymbol}
     */
    public static PageSymbol firstPageOfSize(int size) {
        return fromPageNumberAndSize(null, size);
    }

    /**
     * Create a {@link PageSymbol} from an explicit skip
     * offset and limit.
     *
     * @param skip the number of items to skip (0-indexed)
     * @param limit the maximum number of items to return
     * @return the {@link PageSymbol}
     */
    public static PageSymbol fromSkipLimit(int skip, int limit) {
        return new PageSymbol(skip, limit);
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
     * Construct a new instance.
     *
     * @param skip the number of items to skip
     * @param limit the maximum number of items to return
     */
    private PageSymbol(int skip, int limit) {
        this.skip = skip;
        this.limit = limit;
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

    @Override
    public String toString() {
        return "skip " + skip + " limit " + limit;
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
