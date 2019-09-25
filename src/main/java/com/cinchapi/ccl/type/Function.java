/*
 * Copyright (c) 2013-2019 Cinchapi Inc.
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

import com.cinchapi.common.base.ArrayBuilder;
import com.cinchapi.common.base.Verify;
import com.cinchapi.common.collect.Sequences;
import com.cinchapi.common.describe.Empty;

/**
 * A {@link Function} is an operation that is applied to a key in one or more
 * records. Functions take, as input, the associated values and produces an
 * output value.
 *
 * @author Jeff Nelson
 */
public abstract class Function {

    /**
     * Construct a new instance.
     * 
     * @param name
     * @param key
     * @param args
     */
    protected Function(String name, String key, Object... args) {
        Verify.thatArgument(!Empty.ness().describes(name));
        Verify.thatArgument(!Empty.ness().describes(key));
        ArrayBuilder<Object> $args = ArrayBuilder.builder();
        $args.add(key);
        Sequences.forEach(args, $arg -> {
            Verify.thatArgument(!Empty.ness().describes($arg));
            $args.add($arg);
        });
        this.name = name;
        this.args = $args.build();
    }

    /**
     * The name of the function.
     */
    private final String name;

    /**
     * The arguments that are supplied to the function.
     */
    protected final Object[] args;

    /**
     * Return the name of the function.
     * 
     * @return the name
     */
    public String name() {
        return name;
    }

    /**
     * Return the key of the index to which the function is applied.
     * 
     * @return the index key
     */
    public final String key() {
        return (String) args[0];
    }

}
