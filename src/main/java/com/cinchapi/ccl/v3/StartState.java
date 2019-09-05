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
package com.cinchapi.ccl.v3;

import com.cinchapi.ccl.grammar.KeySymbol;

/**
 * The {@link StartState} marks the logical beginning of a new {@link Condition}.
 * 
 * @author Jeff Nelson
 */
public class StartState extends State {

    /**
     * Construct a new instance.
     * 
     * @param condition
     */
    public StartState(BuiltCondition condition) {
        super(condition);
    }

    /**
     * Add a sub {@code condition} to the Condition that is building. A sub
     * condition is one that is wrapped in parenthesis.
     * 
     * @param condition
     * @return the builder
     */
    public BuildableStartState group(Condition condition) {
        this.condition.add(condition);
        return new BuildableStartState(this.condition);
    }

    /**
     * Add a sub {@code condition} to the Condition that is building. A sub
     * condition is one that is wrapped in parenthesis.
     * 
     * @param condition
     * @return the builder
     */
    // CON-131: account for cases when the caller forgets to "build" the
    // sub condition
    public BuildableStartState group(Object condition) {
        if(condition instanceof BuildableState) {
            return group(((BuildableState) condition).build());
        }
        else {
            throw new IllegalArgumentException(
                    condition + " is not a valid argument for the group method");
        }
    }

    /**
     * Add a {@code key} to the Condition that is building.
     * 
     * @param key
     * @return the builder
     */
    public KeyState key(String key) {
        condition.add(new KeySymbol(key));
        return new KeyState(condition);
    }

}
