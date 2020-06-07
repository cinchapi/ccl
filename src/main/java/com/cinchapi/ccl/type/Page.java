/*
 * Copyright (c) 2013-2020 Cinchapi Inc.
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
package com.cinchapi.ccl.type;

import java.util.Objects;

/**
 * A {@link Page} encapsulates the limit and skip/offset parameters
 * that can be used to pagination through a result set.
 *
 * @author Jeff Nelson
 */
public interface Page {

    /**
     * Create a {@link Page} that encapsulates the appropriate {@link #offset()}
     * and {@link #limit()} for the {@code number}th page containing
     * {@code size} items.
     * 
     * @param number
     * @param size
     * @return the {@link Page}
     */
    public static Page create(int number, int size) {
        return new Page() {

            @Override
            public int offset() {
                return size * (number - 1);
            }

            @Override
            public int limit() {
                return size;
            }

            @Override
            public int hashCode() {
                return Objects.hash(limit(), offset());
            }

            @Override
            public boolean equals(Object obj) {
                if(obj instanceof Page) {
                    return offset() == ((Page) obj).offset()
                            && limit() == ((Page) obj).limit();
                }
                else {
                    return false;
                }
            }

        };
    }

    /**
     * Return the number of items to "skip" (inclusive) before adding items to
     * the page.
     * 
     * @return the offset
     */
    public int offset();

    /**
     * Return the maximum number of items on the page. The number of items
     * returned may be less than this value if there are fewer than
     * {@link #limit() limit} items remaining in the data set.
     * 
     * @return the limit
     */
    public int limit();

}
