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

import com.cinchapi.concourse.lang.paginate.Page;

/**
 * A {@link Symbol} that represents a page.
 */
public class PageSymbol implements PostfixNotationSymbol {
    /**
     * The content of the {@link Symbol}.
     */
    private Page page;

    /**
     * Construct a new instance.
     *
     * @param
     */
    public PageSymbol(String number, String size) {
        if (number == null && size != null) {
            this.page = Page.sized(Integer.parseInt(size));
        }
        else if (size == null && number != null) {
            this.page = Page.number(Integer.parseInt(number));
        }
        else if (size != null & number != null) {
            this.page = Page.of(Integer.parseInt(number), Integer.parseInt(size));
        }
        else {
            this.page = Page.first();
        }
    }

    /**
     * Return the Page that this symbol expresses.
     *
     * @return the page
     */
    public Page page() {
        return page;
    }

    @Override
    public String toString() {
        return "number " + page.offset() + " size " + page.limit();
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof PageSymbol) {
            return page.equals(((PageSymbol) obj).page);
        }
        else {
            return false;
        }
    }
}
