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

import java.util.Objects;

import javax.annotation.concurrent.Immutable;

import com.cinchapi.ccl.syntax.CommandTree;

/**
 * A {@link ValueTokenSymbol} whose value is a read command whose result
 * supplies the value set of a quantified comparison, as in
 * {@code name = any[select name where group = "vip"]} or
 * {@code manager lnks2 any[trace manager from 42]}.
 * <p>
 * The {@link #value()} is the parsed {@link CommandTree} of the subquery,
 * including its condition, order and page. The subquery's condition belongs to
 * that tree alone and is not a child of the condition that encloses the
 * comparison. A consumer resolves the subquery into values, or into record ids
 * for a link operator, and then applies the quantifier.
 * </p>
 * <p>
 * {@link #toString()} is the CCL of the subquery with its tokens separated by
 * single spaces and each {@code $name} placeholder replaced by its bound value,
 * so a comparison that holds this symbol renders back to CCL that parses to an
 * equal comparison without the bindings. Two subqueries are equal when that CCL
 * is equal.
 * </p>
 *
 * @author Jeff Nelson
 */
@Immutable
public final class SubquerySymbol extends ValueTokenSymbol<CommandTree> {

    /**
     * The CCL of the subquery, with its tokens separated by single spaces.
     */
    private final String ccl;

    /**
     * Construct a new instance.
     *
     * @param command the parsed subquery
     * @param ccl the CCL of the subquery, with its tokens separated by single
     *            spaces
     */
    public SubquerySymbol(CommandTree command, String ccl) {
        super(Objects.requireNonNull(command));
        this.ccl = Objects.requireNonNull(ccl);
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof SubquerySymbol) {
            return ccl.equals(((SubquerySymbol) obj).ccl);
        }
        else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return ccl.hashCode();
    }

    @Override
    public String toString() {
        return ccl;
    }

}
