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
package com.cinchapi.ccl.v2.generated;

import com.cinchapi.ccl.JavaCCParser;
import com.cinchapi.ccl.SyntaxException;
import com.cinchapi.ccl.grammar.ConjunctionSymbol;
import com.cinchapi.ccl.grammar.Expression;
import com.cinchapi.ccl.grammar.KeySymbol;
import com.cinchapi.ccl.grammar.OperatorSymbol;
import com.cinchapi.ccl.grammar.Symbol;
import com.cinchapi.ccl.grammar.TimestampSymbol;
import com.cinchapi.ccl.grammar.ValueSymbol;
import com.cinchapi.ccl.util.NaturalLanguage;
import com.cinchapi.common.base.AnyStrings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

import java.util.List;
import java.util.NoSuchElementException;

/**
 * A visitor pattern implementation of {@link GrammarVisitor} that
 * generates a list of the symbols in the accepted string.
 */
public class GrammarTokenizeVisitor implements GrammarVisitor
{
    /**
     * The parser used to parse the string.
     */
    private final JavaCCParser parser;

    /**
     * The data for local resolution
     */
    private final Multimap<String, Object> data;

    /**
     * Constructs a new instance
     *
     * @param parser the parser
     * @param data the local data
     */
    public GrammarTokenizeVisitor(JavaCCParser parser, Multimap<String, Object> data) {
        super();
        this.parser = parser;
        this.data = data;
    }

    /**
     * Visitor for a {@link SimpleNode}
     *
     * @param node the node
     * @param data the data
     * @return the data
     */
    public Object visit(SimpleNode node, Object data) {
        System.out.println(node + ": acceptor not unimplemented in subclass?");
        data = node.childrenAccept(this, data);
        return data;
    }

    /**
     * Visitor for a {@link SimpleNode}
     *
     * @param node the node
     * @param data the data
     * @return the list of symbols
     */
    public Object visit(ASTStart node, Object data) {
        List<Symbol> symbols = Lists.newArrayList();
        data = node.childrenAccept(this, symbols);
        return data;
    }

    /**
     * Visitor for a {@link ASTAnd}
     *
     * @param node the node
     * @param data a reference to the list of symbols
     * @return the list of symbols
     */
    @SuppressWarnings("unchecked")
    public Object visit(ASTAnd node, Object data) {
        List<Symbol> symbols = (List<Symbol>) node.jjtGetChild(0).jjtAccept(this, data);
        symbols.add(ConjunctionSymbol.AND);
        symbols = (List<Symbol>) node.jjtGetChild(1).jjtAccept(this, data);
        return symbols;
    }

    /**
     * Visitor for a {@link ASTOr}
     *
     * @param node the node
     * @param data a reference to the list of symbols
     * @return the list of symbols
     */
    @SuppressWarnings("unchecked")
    public Object visit(ASTOr node, Object data) {
        List<Symbol> symbols = (List<Symbol>) node.jjtGetChild(0).jjtAccept(this, data);
        symbols.add(ConjunctionSymbol.OR);
        symbols = (List<Symbol>) node.jjtGetChild(1).jjtAccept(this, data);
        return symbols;
    }

    /**
     * Visitor for a {@link ASTRelationalExpression}
     *
     * @param node the node
     * @param data a reference to the list of symbols
     * @return the list of symbols
     */
    @SuppressWarnings("unchecked")
    public Object visit(ASTRelationalExpression node, Object data) {
        KeySymbol key = new KeySymbol(node.key());
        OperatorSymbol operator = new OperatorSymbol(parser.transformOperator(node.operator()));

        // Perform local resolution for variable
        List<ValueSymbol> values = Lists.newArrayList();
        for(String value : node.value()) {
            if(value.charAt(0) == '$') {
                String var = value.substring(1);
                try {
                    value = Iterables.getOnlyElement(this.data.get(var)).toString();
                }
                catch (IllegalArgumentException e) {
                    String err = "Unable to resolve variable {} because multiple values exist locally: {}";
                    throw new SyntaxException(AnyStrings.format(err, value, this.data.get(var)));
                }
                catch (NoSuchElementException e) {
                    String err = "Unable to resolve variable {} because no values exist locally";
                    throw new SyntaxException(AnyStrings.format(err, value));
                }
            }
            else if(value.length() > 2 && value.charAt(0) == '\\'
                    && value.charAt(1) == '$') {
                value = value.substring(1);
            }

            values.add(new ValueSymbol(parser.transformValue(value)));
        }


        Expression expression;
        if (node.timestamp() != null) {
            long ts = NaturalLanguage.parseMicros(node.timestamp());
            TimestampSymbol timestamp = new TimestampSymbol(ts);

            expression = new Expression(timestamp, key, operator, values.toArray(new ValueSymbol[0]));
        }
        else {
            expression = new Expression(key, operator, values.toArray(new ValueSymbol[0]));
        }

        ((List<Symbol>) data).add(expression);

        return data;
    }
}
