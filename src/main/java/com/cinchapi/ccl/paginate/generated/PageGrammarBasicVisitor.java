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
package com.cinchapi.ccl.paginate.generated;


/**
 * A visitor pattern implementation of {@link PageGrammarVisitor} that
 * generates an {@link Page} of the accepted string.
 */
public class PageGrammarBasicVisitor implements PageGrammarVisitor
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
     * Visitor for a {@link ASTPage}
     *
     * @param node the node
     * @param data a reference to the tree
     * @return the expression tree
     */
    public Object visit(ASTPage node, Object data) {

    }
}
