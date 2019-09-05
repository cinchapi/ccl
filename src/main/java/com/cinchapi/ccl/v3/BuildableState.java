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

import com.cinchapi.ccl.grammar.ConjunctionSymbol;
import com.cinchapi.ccl.grammar.Symbol;

import java.util.List;

/**
 * The base class for a language state that can be transformed into a complete
 * and well-formed {@link Condition}.
 * 
 * @author Jeff Nelson
 */
public abstract class BuildableState extends State implements Condition {

    /**
     * Construct a new instance.
     * 
     * @param condition
     */
    protected BuildableState(BuiltCondition condition) {
        super(condition);
    }

    /**
     * Build and return the {@link Condition}.
     * 
     * @return the built Condition
     */
    public final Condition build() {
        condition.close();
        return condition;
    }

    /**
     * Build a conjunctive clause onto the {@link Condition} that is building.
     * 
     * @return the builder
     */
    public StartState and() {
        condition.add(ConjunctionSymbol.AND);
        return new StartState(condition);
    }

    /**
     * Build a disjunctive clause onto the {@link Condition} that is building.
     * 
     * @return the builder
     */
    public StartState or() {
        condition.add(ConjunctionSymbol.OR);
        return new StartState(condition);
    }

    @Override
    public Condition at(Timestamp timestamp) {
        return build().at(timestamp);
    }

    @Override
    public final String ccl() {
        return build().ccl();
    }

    @Override
    public final List<Symbol> symbols() {
        return build().symbols();
    }

    @Override
    public final String toString() {
        return build().toString();
    }

}
