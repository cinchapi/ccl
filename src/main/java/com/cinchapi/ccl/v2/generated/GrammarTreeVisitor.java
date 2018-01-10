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
import com.cinchapi.ccl.grammar.Expression;
import com.cinchapi.ccl.syntax.AbstractSyntaxTree;
import com.cinchapi.ccl.syntax.AndTree;
import com.cinchapi.ccl.syntax.ExpressionTree;
import com.cinchapi.ccl.syntax.OrTree;

/**
 * A visitor pattern implementation of {@link GrammarVisitor} that
 * generates an abstract syntax tree of the accepted string.
 */
public class GrammarTreeVisitor implements GrammarVisitor
{
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
     * Visitor for a {@link ASTStart}
     *
     * @param node the node
     * @param data the data
     * @return the data
     */
    public Object visit(ASTStart node, Object data) {
        data = node.jjtGetChild(0).jjtAccept(this, data);
        return data;
    }

    /**
     * Visitor for a {@link ASTAnd}
     *
     * @param node the node
     * @param data a reference to the tree
     * @return the tree
     */
    public Object visit(ASTAnd node, Object data) {
        AbstractSyntaxTree left = (AbstractSyntaxTree) node.jjtGetChild(0).jjtAccept(this, data);
        AbstractSyntaxTree right =(AbstractSyntaxTree) node.jjtGetChild(1).jjtAccept(this, data);
        return new AndTree(left, right);
    }

    /**
     * Visitor for a {@link ASTOr}
     *
     * @param node the node
     * @param data a reference to the tree
     * @return the tree
     */
    public Object visit(ASTOr node, Object data) {
        AbstractSyntaxTree left = (AbstractSyntaxTree) node.jjtGetChild(0).jjtAccept(this, data);
        AbstractSyntaxTree right =(AbstractSyntaxTree) node.jjtGetChild(1).jjtAccept(this, data);
        return new OrTree(left, right);
    }

    /**
     * Visitor for a {@link ASTExpression}
     *
     * @param node the node
     * @param data a reference to the tree
     * @return the expression tree
     */
    public Object visit(ASTExpression node, Object data) {
        Object key = node.jjtGetChild(0).jjtAccept(this, data);

        /*
        Expression expression;
        if (node.timestamp() != null) {
            expression = new Expression(node.timestamp(), node.key(), node.operator(), node.values().toArray(new BaseValueSymbol[0]));
        }
        else {
            expression = new Expression(node.key(), node.operator(), node.values().toArray(new BaseValueSymbol[0]));
        }
        */

        return new ExpressionTree(expression);
    }
}