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
package com.cinchapi.ccl.generated;

import com.cinchapi.ccl.grammar.KeyTokenSymbol;

/**
 * A node that represents a scoped CCL condition of the form
 * {@code prefix.(inner)} where {@code prefix} is a navigation path at
 * which the inner conditions must all be satisfied by the same
 * destination record.
 *
 * @author Jeff Nelson
 */
public class ASTScoped extends SimpleNode {

    /**
     * The navigation prefix that all inner conditions are evaluated
     * against.
     */
    private KeyTokenSymbol<?> prefix;

    public ASTScoped(int id) {
        super(id);
    }

    public ASTScoped(Grammar grammar, int id) {
        super(grammar, id);
    }

    /**
     * Get the navigation prefix at which inner conditions are evaluated.
     *
     * @return the prefix
     */
    public KeyTokenSymbol<?> prefix() {
        return prefix;
    }

    /**
     * Set the navigation prefix.
     *
     * @param prefix the prefix
     */
    public void prefix(KeyTokenSymbol<?> prefix) {
        this.prefix = prefix;
    }

    @Override
    public Object jjtAccept(GrammarVisitor visitor, Object data) {
        return visitor.visit(this, data);
    }
}
