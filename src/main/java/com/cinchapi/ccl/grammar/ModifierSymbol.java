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
 * A {@link Symbol} that identifies a modifier keyword applied to a
 * condition (e.g. {@code strict}).
 * <p>
 * A {@link ModifierSymbol} serves as the {@link Symbol} identity of an
 * AST node that wraps a condition with modified evaluation semantics. It
 * is not itself a {@link PostfixNotationSymbol}: the scope boundaries in
 * a linearized stream are carried by dedicated bracketing symbols (e.g.
 * {@link StrictSymbol#BEGIN} and {@link StrictSymbol#END}).
 * </p>
 *
 * @author Jeff Nelson
 */
public enum ModifierSymbol implements Symbol {
    STRICT;

    @Override
    public String toString() {
        return name().toLowerCase();
    }
}
