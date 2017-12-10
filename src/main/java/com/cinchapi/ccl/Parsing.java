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

import java.util.List;
import java.util.ListIterator;

import com.cinchapi.ccl.grammar.Expression;
import com.cinchapi.ccl.grammar.KeySymbol;
import com.cinchapi.ccl.grammar.OperatorSymbol;
import com.cinchapi.ccl.grammar.Symbol;
import com.cinchapi.ccl.grammar.TimestampSymbol;
import com.cinchapi.ccl.grammar.ValueSymbol;
import com.cinchapi.common.reflect.Reflection;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

/**
 * Util functions for {@link Parser}s.
 * 
 * @author Jeff Nelson
 */
public final class Parsing {

    /**
     * Go through a list of symbols and group the expressions together in a
     * {@link Expression} object.
     * 
     * @param symbols
     * @return the expression
     */
    public static List<Symbol> groupExpressions(List<Symbol> symbols) {
        try {
            List<Symbol> grouped = Lists.newArrayList();
            ListIterator<Symbol> it = symbols.listIterator();
            while (it.hasNext()) {
                Symbol symbol = it.next();
                if(symbol instanceof KeySymbol) {
                    // NOTE: We are assuming that the list of symbols is well
                    // formed, and, as such, the next elements will be an
                    // operator and one or more symbols. If this is not the
                    // case, this method will throw a ClassCastException
                    OperatorSymbol operator = (OperatorSymbol) it.next();
                    ValueSymbol value = (ValueSymbol) it.next();
                    Expression expression;
                    if(operator.operator().operands() == 2) {
                        ValueSymbol value2 = (ValueSymbol) it.next();
                        expression = new Expression((KeySymbol) symbol,
                                operator, value, value2);
                    }
                    else {
                        expression = new Expression((KeySymbol) symbol,
                                operator, value);
                    }
                    grouped.add(expression);
                }
                else if(symbol instanceof TimestampSymbol) { // Add the
                                                             // timestamp to the
                                                             // previously
                                                             // generated
                                                             // Expression
                    Reflection.set("timestamp", symbol,
                            Iterables.getLast(grouped)); // (authorized)
                }
                else {
                    grouped.add(symbol);
                }
            }
            return grouped;
        }
        catch (ClassCastException e) {
            throw new SyntaxException(e.getMessage());
        }
    }

    /**
     * Go through the list of symbols and break up any {@link Expression
     * expressions} into individual symbol tokens.
     * 
     * @param symbols
     * @return the list of symbols with no expressions
     */
    public static List<Symbol> ungroupExpressions(List<Symbol> symbols) {
        List<Symbol> ungrouped = Lists.newArrayList();
        symbols.forEach((symbol) -> {
            if(symbol instanceof Expression) {
                Expression expression = (Expression) symbol;
                ungrouped.add(expression.key());
                ungrouped.add(expression.operator());
                ungrouped.addAll(expression.values());
                if(expression.timestamp().timestamp() > 0) {
                    ungrouped.add(expression.timestamp());
                }
            }
            else {
                ungrouped.add(symbol);
            }
        });
        return ungrouped;
    }

}
