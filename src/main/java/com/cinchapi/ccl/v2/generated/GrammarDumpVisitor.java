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

/**
 * A visitor pattern implementation of {@link GrammarVisitor} that
 * prints the node information to std out
 */
public class GrammarDumpVisitor implements GrammarVisitor
{
    /**
     * The indent level
     */
    private int indent = 0;

    /**
     * Visitor for a {@link SimpleNode}
     *
     * @param node the node
     * @param data the data
     * @return the data
     */
    public Object visit(SimpleNode node, Object data) {
        System.out.println(indentString() + node +
                ": acceptor not unimplemented in subclass?");
        ++indent;
        data = node.childrenAccept(this, data);
        --indent;
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
        System.out.println(indentString() + node);
        ++indent;
        data = node.childrenAccept(this, data);
        --indent;
        return data;
    }

    /**
     * Visitor for a {@link ASTAnd}
     *
     * @param node the node
     * @param data the data
     * @return the data
     */
    public Object visit(ASTAnd node, Object data) {
        System.out.println(indentString() + node);
        ++indent;
        data = node.childrenAccept(this, data);
        --indent;
        return data;
    }

    @Override
    public Object visit(ASTExpression node, Object data) {
        System.out.println(indentString() + node);
        ++indent;
        data = node.childrenAccept(this, data);
        --indent;
        return data;
    }

    @Override
    public Object visit(ASTFunctionKey node, Object data) {
        System.out.println(indentString() + node);
        ++indent;
        data = node.childrenAccept(this, data);
        --indent;
        return data;
    }

    @Override
    public Object visit(ASTKey node, Object data) {
        System.out.println(indentString() + node);
        ++indent;
        data = node.childrenAccept(this, data);
        --indent;
        return data;
    }

    @Override
    public Object visit(ASTFunctionValue node, Object data) {
        System.out.println(indentString() + node);
        ++indent;
        data = node.childrenAccept(this, data);
        --indent;
        return data;
    }

    @Override
    public Object visit(ASTValue node, Object data) {
        System.out.println(indentString() + node);
        ++indent;
        data = node.childrenAccept(this, data);
        --indent;
        return data;
    }

    @Override
    public Object visit(ASTOperator node, Object data) {
        System.out.println(indentString() + node);
        ++indent;
        data = node.childrenAccept(this, data);
        --indent;
        return data;
    }

    @Override
    public Object visit(ASTTimestamp node, Object data) {
        System.out.println(indentString() + node);
        ++indent;
        data = node.childrenAccept(this, data);
        --indent;
        return data;
    }

    /**
     * Visitor for a {@link ASTOr}
     *
     * @param node the node
     * @param data the data
     * @return the data
     */
    public Object visit(ASTOr node, Object data) {
        System.out.println(indentString() + node);
        ++indent;
        data = node.childrenAccept(this, data);
        --indent;
        return data;
    }

    /**
     * Creates an empty string with the current indent level.
     *
     * @return the indented string
     */
    private String indentString() {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < indent; ++i) {
            sb.append(' ');
        }
        return sb.toString();
    }
}