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
import com.cinchapi.ccl.grammar.ExplicitCclASTFunction;
import com.cinchapi.ccl.grammar.ExplicitCclInfixFunction;
import com.cinchapi.ccl.grammar.Expression;
import com.cinchapi.ccl.grammar.FunctionValueSymbol;
import com.cinchapi.ccl.grammar.Symbol;
import com.cinchapi.ccl.syntax.ConjunctionTree;
import com.cinchapi.ccl.syntax.ExpressionTree;
import com.cinchapi.ccl.syntax.Visitor;
import com.google.common.collect.Lists;

import java.util.List;

/**
 * A visitor pattern implementation of {@link GrammarVisitor} that
 * generates a postfix notation queue of the accepted string.
 */
public class GrammarInfixVisitor implements GrammarVisitor
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
        List<Symbol> symbols = Lists.newArrayList();
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
        List<Symbol> symbols = (List<Symbol>) node.jjtGetChild(0).jjtAccept(this, data);
        symbols.add(ConjunctionSymbol.AND);
        // Return value isn't needed
        node.jjtGetChild(1).jjtAccept(this, data);
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
        List<Symbol> symbols = (List<Symbol>) node.jjtGetChild(0).jjtAccept(this, data);
        symbols.add(ConjunctionSymbol.OR);
        // Return value isn't needed
        node.jjtGetChild(1).jjtAccept(this, data);
        return symbols;
    }

    /**
     * Visitor for a {@link ASTExpression}
     *
     * @param node the node
     * @param data the data
     * @return the queue of postfix symbols
     */
    @SuppressWarnings("unchecked")
    public Object visit(ASTExpression node, Object data) {
        Expression expression;

        // Convert our AST to postfix queue
        if (node.values().get(0).value() instanceof ExplicitCclASTFunction) {
            ExplicitCclASTFunction value = (ExplicitCclASTFunction) node.values().get(0).value();

            Visitor<Object> visitor = new Visitor<Object>() {
                List<Symbol> symbols = Lists.newArrayList();

                @Override
                public Object visit(ConjunctionTree tree, Object... data) {
                    tree.left().accept(this, data);
                    tree.right().accept(this, data);
                    symbols.add(tree.root());
                    return symbols;
                }

                @Override
                public Object visit(ExpressionTree tree, Object... data) {
                    symbols.add(tree.root());
                    return symbols;
                }

            };
            List<Symbol> symbols = (List<Symbol>) value.value().accept(visitor);

            node.values().remove(0);
            node.values().add(0, new FunctionValueSymbol(
                    new ExplicitCclInfixFunction(value.function(), value.key(), symbols)));
        }

        if (node.timestamp() != null) {
            expression = new Expression(node.timestamp(), node.key(), node.operator(), node.values().toArray(new BaseValueSymbol[0]));
        }
        else {
            expression = new Expression(node.key(), node.operator(), node.values().toArray(new BaseValueSymbol[0]));
        }

        ((List<Symbol>) data).add(expression);

        return data;
    }
}