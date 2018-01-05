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

import com.cinchapi.ccl.grammar.BaseValueSymbol;
import com.cinchapi.ccl.grammar.ConjunctionSymbol;
import com.cinchapi.ccl.grammar.Expression;
import com.cinchapi.ccl.grammar.PostfixNotationSymbol;

import java.util.LinkedList;
import java.util.Queue;

/**
 * A visitor pattern implementation of {@link GrammarVisitor} that
 * generates a postfix notation queue of the accepted string.
 */
public class GrammarPostfixVisitor implements GrammarVisitor
{
    /**
     * Visitor for a {@link SimpleNode}
     *
     * @param node the node
     * @param data the data
     * @return the data
     */
    public Object visit(SimpleNode node, Object data) {
        System.out.println(node +
                ": acceptor not unimplemented in subclass?");
        data = node.childrenAccept(this, data);
        return data;
    }

    /**
     * Visitor for a {@link ASTStart}
     *
     * @param node the node
     * @param data the data
     * @return the queue of postfix symbols
     */
    public Object visit(ASTStart node, Object data) {
        Queue<PostfixNotationSymbol> symbols = new LinkedList<>();
        data = node.childrenAccept(this, symbols);
        return data;
    }

    /**
     * Visitor for a {@link ASTAnd}
     *
     * @param node the node
     * @param data the data
     * @return the queue of postfix symbols
     */
    @SuppressWarnings({ "unchecked", "unused" })
    public Object visit(ASTAnd node, Object data) {
        // Return value isn't needed
        node.jjtGetChild(0).jjtAccept(this, data);
        Queue<PostfixNotationSymbol> symbols = (Queue<PostfixNotationSymbol>) node.jjtGetChild(1).jjtAccept(this, data);
        symbols.add(ConjunctionSymbol.AND);
        return symbols;
    }

    /**
     * Visitor for a {@link ASTOr}
     *
     * @param node the node
     * @param data the data
     * @return the queue of postfix symbols
     */
    @SuppressWarnings({ "unchecked", "unused" })
    public Object visit(ASTOr node, Object data) {
        // Return value isn't needed
        node.jjtGetChild(0).jjtAccept(this, data);
        Queue<PostfixNotationSymbol> symbols = (Queue<PostfixNotationSymbol>) node.jjtGetChild(1).jjtAccept(this, data);
        symbols.add(ConjunctionSymbol.OR);
        return symbols;
    }

    /**
     * Visitor for a {@link ASTRelationalExpression}
     *
     * @param node the node
     * @param data the data
     * @return the queue of postfix symbols
     */
    @SuppressWarnings("unchecked")
    public Object visit(ASTRelationalExpression node, Object data) {

        Expression expression;
        if (node.timestamp() != null) {
            expression = new Expression(node.timestamp(), node.key(), node.operator(), node.value().toArray(new BaseValueSymbol[0]));
        }
        else {
            expression = new Expression(node.key(), node.operator(), node.value().toArray(new BaseValueSymbol[0]));
        }

        ((Queue<PostfixNotationSymbol>) data).add(expression);

        return data;
    }
}