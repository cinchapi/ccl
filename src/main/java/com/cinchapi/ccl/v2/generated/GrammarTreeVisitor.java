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
import com.cinchapi.ccl.grammar.BaseKeySymbol;
import com.cinchapi.ccl.grammar.BaseValueSymbol;
import com.cinchapi.ccl.grammar.DynamicFunctionKeySymbol;
import com.cinchapi.ccl.grammar.ExplicitCclASTFunction;
import com.cinchapi.ccl.grammar.ExplicitFunctionValueSymbol;
import com.cinchapi.ccl.grammar.Expression;
import com.cinchapi.ccl.grammar.ImplicitFunctionValueSymbol;
import com.cinchapi.ccl.grammar.KeySymbol;
import com.cinchapi.ccl.grammar.OperatorSymbol;
import com.cinchapi.ccl.grammar.TimestampSymbol;
import com.cinchapi.ccl.grammar.ValueSymbol;
import com.cinchapi.ccl.syntax.AbstractSyntaxTree;
import com.cinchapi.ccl.syntax.AndTree;
import com.cinchapi.ccl.syntax.ExpressionTree;
import com.cinchapi.ccl.syntax.OrTree;
import com.cinchapi.common.base.AnyStrings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * A visitor pattern implementation of {@link GrammarVisitor} that
 * generates an abstract syntax tree of the accepted string.
 */
public class GrammarTreeVisitor implements GrammarVisitor {
    /**
     *
     */
    private final JavaCCParser parser;

    /**
     *
     */
    private final Multimap<String, Object> data;

    /**
     *
     * @param parser
     */
    public GrammarTreeVisitor(JavaCCParser parser) {
        this(parser, null);
    }

    /**
     *
     * @param parser
     */
    public GrammarTreeVisitor(JavaCCParser parser, Multimap<String, Object> data) {
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
        System.out.println(node.getClass().getName());
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

    @Override
    public Object visit(ASTExpression node, Object data) {
        BaseKeySymbol key = null;
        OperatorSymbol operator = null;
        List<BaseValueSymbol> values = Lists.newArrayList();
        TimestampSymbol timestamp = null;
        for(Node child : node.children) {
            Object result = child.jjtAccept(this, data);

            if(result instanceof BaseKeySymbol) {
                key = (BaseKeySymbol) result;
            }
            else if(result instanceof OperatorSymbol) {
                operator = (OperatorSymbol) result;
            }
            else if(result instanceof BaseValueSymbol) {
                values.add((BaseValueSymbol) result);
            }
            else if(result instanceof TimestampSymbol) {
                timestamp = (TimestampSymbol) result;
            }
        }

        if (key != null && operator != null && !values.isEmpty() && timestamp != null) {
            return new ExpressionTree(new Expression(timestamp, key, operator, values.toArray(new BaseValueSymbol[values.size()])));
        }
        else if (key != null && operator != null && !values.isEmpty()) {
            return new ExpressionTree(new Expression(key, operator, values.toArray(new BaseValueSymbol[values.size()])));
        }

        return null;
    }

    @Override
    public Object visit(ASTDynamicFunctionKey node, Object data) {
        return new DynamicFunctionKeySymbol(node.key());
    }

    @Override
    public Object visit(ASTKey node, Object data) {
        return new KeySymbol(node.key());
    }

    @Override
    public Object visit(ASTImplicitFunctionValue node, Object data) {
        return new ImplicitFunctionValueSymbol(node.value());
    }

    @Override
    public Object visit(ASTExplicitFunctionValue node, Object data) {

        if (node.value() instanceof ExplicitCclJavaCCASTFunction) {
            String function = node.value().function();
            String key = node.value().key();
            ASTStart ccl = (ASTStart) node.value().value();

            GrammarTreeVisitor visitor = new GrammarTreeVisitor(parser, this.data);
            AbstractSyntaxTree tree = (AbstractSyntaxTree) ccl
                    .jjtAccept(visitor, null);

            return new ExplicitFunctionValueSymbol(
                    new ExplicitCclASTFunction(function, key, tree));
        }
        else {
            return new ExplicitFunctionValueSymbol(node.value());
        }
    }

    @Override
    public Object visit(ASTValue node, Object data) {
        String value = node.value();
        if(value.charAt(0) == '$') {
            String var = value.substring(1);
            if (this.data == null) {
                System.out.println("null");
            }
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

        return new ValueSymbol(parser.transformValue(value));
    }

    @Override
    public Object visit(ASTOperator node, Object data) {
        return new OperatorSymbol(parser.transformOperator(node.operator()));
    }

    @Override
    public Object visit(ASTTimestamp node, Object data) {
        return new TimestampSymbol(node.timestamp());
    }
}