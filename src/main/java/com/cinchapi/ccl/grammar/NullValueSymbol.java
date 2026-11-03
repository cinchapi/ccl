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

import javax.annotation.concurrent.Immutable;

/**
 * A {@link ValueTokenSymbol} for the CCL literal {@code null}, which stands for
 * the absence of a value: {@code key = null} describes records that hold no
 * value for {@code key} and {@code key != null} describes records that hold at
 * least one.
 * <p>
 * The literal is the unquoted word {@code null} in any letter case. The quoted
 * string {@code "null"} is an ordinary {@link ValueSymbol} whose value is that
 * string. The grammar accepts the literal only with the {@code =} and
 * {@code !=} operators, alone or as a member of a quantified value set.
 * </p>
 * <p>
 * The symbol's {@link #value()} is Java {@code null}, so a consumer that maps
 * values with the Concourse driver receives its null sentinel.
 * </p>
 *
 * @author Jeff Nelson
 */
@Immutable
public final class NullValueSymbol extends ValueTokenSymbol<Object> {

    /**
     * The only instance.
     */
    public static final NullValueSymbol INSTANCE = new NullValueSymbol();

    /**
     * Construct the single instance.
     */
    private NullValueSymbol() {
        super(null);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof NullValueSymbol;
    }

    @Override
    public int hashCode() {
        return NullValueSymbol.class.hashCode();
    }

    @Override
    public String toString() {
        return "null";
    }

}
