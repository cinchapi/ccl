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

import com.cinchapi.ccl.Parser;
import com.cinchapi.ccl.Parsing;
import com.cinchapi.ccl.grammar.Expression;
import com.cinchapi.ccl.grammar.ParenthesisSymbol;
import com.cinchapi.ccl.grammar.Symbol;
import com.cinchapi.ccl.grammar.TimestampSymbol;
import com.cinchapi.common.reflect.Reflection;
import com.cinchapi.concourse.Timestamp;
import com.cinchapi.concourse.util.Parsers;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * A {@link Condition} that has been {@link BuildableState#build()}.
 *
 * @author Jeff Nelson
 */
public class BuiltCondition implements Condition {

    /**
     * A flag that indicates whether this {@link Condition} has been built.
     */
    private boolean built = false;

    /**
     * The collection of {@link Symbol}s that make up this {@link Condition}.
     */
    List<Symbol> symbols;

    /**
     * Construct a new instance.
     */
    protected BuiltCondition() {
        this.symbols = Lists.newArrayList();
    }

    /**
     * Return this {@link Condition} with each expression (e.g. {key} {operator}
     * {values}) pinned to the specified {@code timestamp}.
     * 
     * <strong>NOTE:</strong> Any timestamps that are pinned to any expressions
     * within this Condition will be replaced by the specified {@code timestamp}.
     * 
     * @param timestamp the {@link Timestamp} to which the returned
     *            {@link Condition} is pinned
     * 
     * @return this {@link Condition} pinned to {@code timestamp}
     */
    public Condition at(Timestamp timestamp) {
        Parser parser = Parsers.create(ccl());
        List<Symbol> symbols = Parsing.groupExpressions(parser.tokenize());
        TimestampSymbol ts = new TimestampSymbol(timestamp.getMicros());
        symbols.forEach((symbol) -> {
            if(symbol instanceof Expression) {
                Expression expression = (Expression) symbol;
                Reflection.set("timestamp", ts, expression); // (authorized)
            }
        });
        BuiltCondition condition = new BuiltCondition();
        symbols = Parsing.ungroupExpressions(symbols);
        condition.symbols = symbols;
        return condition;
    }

    @Override
    public String ccl() {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (Symbol symbol : symbols) {
            if(!first) {
                sb.append(" ");
            }
            sb.append(symbol);
            first = false;
        }
        return sb.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Condition) {
            return Objects.equals(symbols, ((Condition) obj).symbols());
        }
        else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(symbols);
    }

    @Override
    public List<Symbol> symbols() {
        return Collections.unmodifiableList(symbols);
    }

    @Override
    public String toString() {
        return ccl();
    }

    /**
     * Expand any sub/grouped Condition.
     * 
     * @param symbols
     * @param expanded
     */
    private void expand(List<Symbol> symbols, List<Symbol> expanded) {
        for (Symbol symbol : symbols) {
            if(symbol instanceof Condition) {
                expanded.add(ParenthesisSymbol.LEFT);
                expand(((Condition) symbol).symbols(), expanded);
                expanded.add(ParenthesisSymbol.RIGHT);
            }
            else {
                expanded.add(symbol);
            }
        }
    }

    /**
     * Add a {@link Symbol} to this {@link Condition}.
     * 
     * @param symbol
     */
    protected void add(Symbol symbol) {
        Preconditions.checkState(!built,
                "Cannot add a symbol to a built Condition");
        symbols.add(symbol);
    }

    /**
     * Mark this {@link Condition} as {@code built}.
     */
    protected void close() {
        built = !built ? true : built;
        List<Symbol> expanded = Lists.newArrayList();
        expand(symbols, expanded);
        this.symbols = expanded;
    }

}
