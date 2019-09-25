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
package com.cinchapi.ccl.type.function;

import com.cinchapi.ccl.type.Function;
import com.cinchapi.common.base.AnyStrings;

/**
 *
 *
 * @author Jeff Nelson
 */
public abstract class AbstractKeyExplicitSourceFunction<S> extends Function {

    /**
     * Construct a new instance.
     * @param name
     * @param arg
     * @param args
     */
    protected AbstractKeyExplicitSourceFunction(String name, String key,
            S source) {
        super(name, key, source);
    }
    
    @Override
    public final String toString() {
        return AnyStrings.format("{}({},{})", name(), key(), _sourceToString());
    }
    
    protected abstract String _sourceToString();

}
