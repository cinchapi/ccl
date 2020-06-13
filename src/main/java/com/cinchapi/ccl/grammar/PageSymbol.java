/*
 * Copyright (c) 2013-2017 Cinchapi Inc.
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

import javax.annotation.Nullable;

/**
 * A {@link Symbol} that represents a page.
 */
public class PageSymbol implements Symbol {

    public static Integer DEFAULT_PAGE_NUMBER = 1;
    public static Integer DEFAULT_PAGE_SIZE = 20;

    private final int number;
    private final int size;

    /**
     * Construct a new instance.
     * 
     * @param number
     * @param size
     */
    public PageSymbol(@Nullable Integer number, @Nullable Integer size) {
        this.number = number != null ? number : DEFAULT_PAGE_NUMBER;
        this.size = size != null ? size : DEFAULT_PAGE_SIZE;
    }

    /**
     * Return the page number.
     * 
     * @return the page number
     */
    public int number() {
        return number;
    }

    /**
     * Return the upper limit on the number of items to include on the page
     * 
     * @return the page size
     */
    public int size() {
        return size;
    }

    /**
     * Return the number of items to skip before adding items to the page.
     * 
     * @return the offset
     */
    public int offset() {
        return size * (number - 1);
    }

    /**
     * Alias for {@link #offset()}.
     */
    public int skip() {
        return offset();
    }

    /**
     * Alias for {@link #size()}
     */
    public int limit() {
        return size();
    }

    @Override
    public String toString() {
        return "number " + number + " size " + size;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof PageSymbol) {
            return number == ((PageSymbol) obj).number
                    && size == ((PageSymbol) obj).size;
        }
        else {
            return false;
        }
    }
}
