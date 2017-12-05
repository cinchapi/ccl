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
package com.cinchapi.ccl.generator;

import com.cinchapi.ccl.Parser;
import com.cinchapi.ccl.grammar.Expression;
import com.cinchapi.ccl.grammar.KeySymbol;
import com.cinchapi.ccl.grammar.OperatorSymbol;
import com.cinchapi.ccl.grammar.TimestampSymbol;
import com.cinchapi.ccl.grammar.ValueSymbol;
import com.cinchapi.ccl.syntax.AbstractSyntaxTree;
import com.cinchapi.ccl.syntax.AndTree;
import com.cinchapi.ccl.syntax.ExpressionTree;
import com.cinchapi.ccl.syntax.OrTree;
import com.cinchapi.ccl.util.NaturalLanguage;

import java.util.List;
import java.util.stream.Collectors;

/**
 *
 */
public class GrammarBuildTreeVisitor implements GrammarVisitor
{
    /**
     *
     */
    private final Parser parser;

    /**
     *
     * @param parser
     */
    GrammarBuildTreeVisitor(Parser parser) {
        super();
        this.parser = parser;
    }

    public Object visit(SimpleNode node, Object data) {
        System.out.println(node +
               ": acceptor not unimplemented in subclass?");
        data = node.childrenAccept(this, data);
        return data;
    }

    public Object visit(ASTStart node, Object data) {
        data = node.childrenAccept(this, data);
        return data;
    }

    public Object visit(ASTAnd node, Object data) {
        AbstractSyntaxTree left = (AbstractSyntaxTree) node.jjtGetChild(0).jjtAccept(this, data);
        AbstractSyntaxTree right =(AbstractSyntaxTree) node.jjtGetChild(1).jjtAccept(this, data);

        return new AndTree(left, right);
    }

    public Object visit(ASTOr node, Object data) {
        AbstractSyntaxTree left = (AbstractSyntaxTree) node.jjtGetChild(0).jjtAccept(this, data);
        AbstractSyntaxTree right =(AbstractSyntaxTree) node.jjtGetChild(1).jjtAccept(this, data);

        return new OrTree(left, right);
    }

    public Object visit(ASTRelationalExpression node, Object data) {
        KeySymbol key = new KeySymbol(node.key());
        OperatorSymbol operator = new OperatorSymbol(parser.transformOperator(node.operator()));
        List<ValueSymbol> values = node.value().stream().map(
            value -> new ValueSymbol(value)).collect(Collectors.toList());
        long ts = NaturalLanguage.parseMicros(node.timestamp());
        TimestampSymbol timestamp = new TimestampSymbol(ts);

        Expression expression;
        if (ts > 0) {
            expression = new Expression(timestamp, key, operator, values.toArray(new ValueSymbol[0]));
        }
        else {
            expression = new Expression(key, operator, values.toArray(new ValueSymbol[0]));
        }
        ExpressionTree tree = new ExpressionTree(expression);

        return tree;
    }
}
