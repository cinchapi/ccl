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
 *
 * @author Jeff Nelson
 */
public class PageSymbol implements Symbol {

    /**
     * Create a {@link PageSymbol} from an explicit skip
     * offset without a limit.
     *
     * @param skip the number of items to skip (0-indexed)
     * @return the {@link PageSymbol}
     */
    public static PageSymbol fromSkip(int skip) {
        return new PageSymbol(skip, null);
    }

    /**
     * Create a {@link PageSymbol} from a limit with the
     * default skip offset.
     *
     * @param limit the maximum number of items to return
     * @return the {@link PageSymbol}
     */
    public static PageSymbol fromLimit(int limit) {
        return new PageSymbol(0, limit);
    }

    /**
     * Create a {@link PageSymbol} from an explicit skip
     * offset and an optional limit.
     *
     * @param skip the number of items to skip (0-indexed)
     * @param limit the maximum number of items to return,
     *            or {@code null} for no limit
     * @return the {@link PageSymbol}
     */
    public static PageSymbol fromSkipLimit(int skip, @Nullable Integer limit) {
        return new PageSymbol(skip, limit);
    }

    /**
     * The number of items to skip.
     */
    private final int skip;

    /**
     * The maximum number of items to return.
     */
    private final Integer limit;

    /**
     * Construct a new instance.
     *
     * @param skip the number of items to skip
     * @param limit the maximum number of items to return,
     *            or {@code null} for no limit
     */
    private PageSymbol(int skip, @Nullable Integer limit) {
        this.skip = skip;
        this.limit = limit;
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
     * Return the maximum number of items to return, if one
     * was specified.
     *
     * @return the limit, or {@code null}
     */
    @Nullable
    public Integer limit() {
        return limit;
    }

    @Override
    public String toString() {
        if(limit == null) {
            return "skip " + skip;
        }
        else if(skip == 0) {
            return "limit " + limit;
        }
        else {
            return "skip " + skip + " limit " + limit;
        }
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof PageSymbol) {
            PageSymbol other = (PageSymbol) obj;
            return skip == other.skip
                    && java.util.Objects.equals(limit, other.limit);
        }
        else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(skip, limit);
    }

}
