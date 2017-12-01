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
package com.cinchapi.ccl;

import java.util.Collections;
import java.util.Set;

import com.cinchapi.ccl.grammar.Expression;
import com.google.common.collect.Sets;

/**
 * Analyzes a CCL statement and provides summary metrics.
 * 
 * @author Jeff Nelson
 */
public class Analyzer {

    /**
     * The provided CCL statement.
     */
    private final String ccl;

    /**
     * The keys that are used within the CCL statement.
     */
    private Set<String> keys = null;

    /**
     * Construct a new instance.
     * 
     * @param ccl
     */
    public Analyzer(String ccl) {
        this.ccl = ccl;
    }

    /**
     * Return the keys that are used within the CCL statement.
     * 
     * @return the keys.
     */
    public Set<String> keys() {
        if(keys == null) {
            keys = Sets.newHashSet();
            StaticParser.toPostfixNotation(ccl).forEach((symbol) -> {
                if(symbol instanceof Expression) {
                    keys.add(((Expression) symbol).key().key());
                }
            });
            keys = Collections.unmodifiableSet(keys);
        }
        return keys;
    }

}
