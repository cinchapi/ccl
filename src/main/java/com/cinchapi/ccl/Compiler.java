/*
 * Copyright (c) 2013-2020 Cinchapi Inc.
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

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.function.Function;

import com.cinchapi.ccl.grammar.QuerySymbol;
import com.cinchapi.ccl.grammar.ConjunctionSymbol;
import com.cinchapi.ccl.grammar.condition.ExpressionSymbol;
import com.cinchapi.ccl.grammar.KeySymbol;
import com.cinchapi.ccl.grammar.condition.OperatorSymbol;
import com.cinchapi.ccl.grammar.ParenthesisSymbol;
import com.cinchapi.ccl.grammar.PostfixNotationSymbol;
import com.cinchapi.ccl.grammar.Symbol;
import com.cinchapi.ccl.grammar.TimestampSymbol;
import com.cinchapi.ccl.grammar.ValueTokenSymbol;
import com.cinchapi.ccl.syntax.*;
import com.cinchapi.ccl.syntax.condition.*;
import com.cinchapi.ccl.type.Operator;
import com.cinchapi.common.base.Verify;
import com.cinchapi.common.function.TriFunction;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

/**
 * A {@link Compiler} transforms a CCL statement into an
 * {@link AbstractSyntaxTree} that can be logically evaluated.
 *
 * @author Jeff Nelson
 */
public abstract class Compiler {

    /**
     * Create a {@link Compiler} that can parse CCL statements into intermediate
     * formats for logical evaluation.
     * 
     * @param valueParser a {@link Function} that parses values appropriately
     * @param operatorParser a {@link Function} that parses {@link Operator
     *            operators} appropriately
     * @return the {@link Compiler}
     */
    public static Compiler create(Function<String, Object> valueParser,
            Function<String, Operator> operatorParser) {
        return new CompilerJavaCC(valueParser, operatorParser);
    }

    /**
     * A function that transforms the string representation of an operator into
     * the appropriate {@link Operator} object.
     */
    protected Function<String, Operator> operatorParser;

    /**
     * A function that transforms the string representation of a value into the
     * appropriate {@link Object}.
     */
    protected Function<String, Object> valueParser;

    /**
     * Construct a new instance.
     * 
     * @param valueParser
     * @param operatorParser
     */
    protected Compiler(Function<String, Object> valueParser,
            Function<String, Operator> operatorParser) {
        this.valueParser = valueParser;
        this.operatorParser = operatorParser;
    }

    /**
     * Evaluate the {@code ccl} statement. If it is well-formed, return a
     * {@link AbstractSyntaxTree} that can be used to logically evaluate the
     * statement.
     * 
     * @param ccl the CCL statement to parse
     * @return an {@link AbstractSyntaxTree} that represents the CCL statement
     */
    public final AbstractSyntaxTree parse(String ccl) {
        return parse(ccl, ImmutableMultimap.of());
    }

    /**
     * Evaluate the {@code ccl} statement. If it is well-formed, return a
     * {@link AbstractSyntaxTree} that can be used to logically evaluate the
     * statement.
     * <p>
     * The provided {@code data} will be used to perform local resolution of any
     * variable values in the CCL statement. The variable values, will be
     * replaced with values from the local {@code data} if possible.
     * </p>
     * 
     * @param ccl the CCL statement to parse
     * @param data data that can be used to perform local resolution of any
     *            value variables (e.g. ssn = $ssn) in the CCL statement
     * @return an {@link AbstractSyntaxTree} that represents the CCL statement
     */
    public abstract AbstractSyntaxTree parse(String ccl,
            Multimap<String, Object> data);

    /**
     * Return {@link StatementAnalysis analysis} about the {@link ConditionTree
     * tree}.
     * 
     * @param tree
     * @return the {@link StatementAnalysis}
     */
    public final StatementAnalysis analyze(ConditionTree tree) {
        return new StatementAnalysis() {
            
            List<Symbol> tokens = null;

            @Override
            public Set<String> keys() {              
                Set<String> keys = Sets
                        .newLinkedHashSetWithExpectedSize($tokens().size());
                $tokens().forEach((symbol) -> {
                    if(symbol instanceof ExpressionSymbol) {
                        keys.add(((ExpressionSymbol) symbol).raw().key());
                    }
                    else if(symbol instanceof KeySymbol) {
                        keys.add(((KeySymbol) symbol).key());
                    }
                });
                return Collections.unmodifiableSet(keys);
            }

            @Override
            public Set<String> keys(Operator operator) {
                List<Symbol> tokens = $tokens();
                tokens = Parsing.groupExpressions(tokens);
                Set<String> keys = Sets
                        .newLinkedHashSetWithExpectedSize(tokens.size());
                tokens.forEach((symbol) -> {
                    ExpressionSymbol expression;
                    if(symbol instanceof ExpressionSymbol
                            && (expression = (ExpressionSymbol) symbol).raw()
                                    .operator().equals(operator)) {
                        keys.add(expression.raw().key());
                    }
                });
                return Collections.unmodifiableSet(keys);
            }

            @Override
            public Set<Operator> operators() {
                Set<Operator> operators = Sets
                        .newLinkedHashSetWithExpectedSize($tokens().size());
                $tokens().forEach((symbol) -> {
                    if(symbol instanceof ExpressionSymbol) {
                        operators.add(
                                ((ExpressionSymbol) symbol).raw().operator());
                    }
                    else if(symbol instanceof OperatorSymbol) {
                        operators.add(((OperatorSymbol) symbol).operator());
                    }
                });
                return operators;
            }
            
            private List<Symbol> $tokens() {
                if(tokens == null) {
                    tokens = tokenize(tree);
                }
                return tokens;
            }

        };
    }

    /**
     * Return {@code true} if the {@code data} is described by the condition
     * encapsulated in the {@code tree}.
     * 
     * @param tree the {@link ConditionTree} that represents the condition
     * @param data the data to test for adherences to the condition
     * @param evaluator a {@link TriFunction} that takes a consideration value,
     *            {@link Operator}, and list of reference values as input and
     *            returns a boolean that indicates whether the consideration
     *            value satisfies the {@link Operator} in relation to the
     *            reference values
     * @return {@code true} if the data is described by the criteria that has
     *         been parsed
     */
    public final boolean evaluate(ConditionTree tree,
            Multimap<String, Object> data,
            TriFunction<Object, Operator, List<Object>, Boolean> evaluator) {
        Visitor<Boolean> visitor = new ConditionTreeVisitor<Boolean>() {

            @Override
            public Boolean visit(ConjunctionTree tree, Object... data) {
                if(tree.root() == ConjunctionSymbol.AND) {
                    boolean a = false;
                    AbstractSyntaxTree bTree;
                    if(!tree.left().isLeaf() && tree.right().isLeaf()) {
                        a = tree.right().accept(this, data);
                        bTree = tree.left();
                    }
                    else {
                        a = tree.left().accept(this, data);
                        bTree = tree.right();
                    }
                    return !a ? false : bTree.accept(this, data) && a;
                }
                else {
                    return tree.left().accept(this, data)
                            || tree.right().accept(this, data);
                }
            }

            @SuppressWarnings("unchecked")
            @Override
            public Boolean visit(ExpressionTree tree, Object... data) {
                Verify.thatArgument(data.length > 0);
                Verify.thatArgument(data[0] instanceof Multimap);
                Multimap<String, Object> dataset = (Multimap<String, Object>) data[0];
                ExpressionSymbol expression = ((ExpressionSymbol) tree.root());
                String key = expression.raw().key();
                Operator operator = expression.raw().operator();
                List<Object> values = expression.raw().values();
                boolean matches = false;
                for (Object stored : dataset.get(key)) {
                    if(evaluator.apply(stored, operator, values)) {
                        matches = true;
                        break;
                    }
                    else {
                        continue;
                    }
                }
                return matches;
            }

        };
        return tree.accept(visitor, data);
    }

    /**
     * Traverse the {@code ast} in breadth-first order and break up its nodes
     * into distinct {@link Symbol symbols} (i.e. separate an
     * {@link ExpressionSymbol} into its distinct parts}.
     * 
     * @param ast
     * @return the list of {@link Symbol symbols} in the {@code ast}
     */
    public final List<Symbol> tokenize(AbstractSyntaxTree ast) {
        Visitor<List<Symbol>> visitor = new Visitor<List<Symbol>>() {

            @SuppressWarnings("unchecked")
            @Override
            public List<Symbol> visit(QueryTree tree, Object... data) {
                List<Symbol> symbols = (List<Symbol>) data[0];
                if(tree.root() != QuerySymbol.IMPLICIT) {
                    symbols.add(tree.root());
                }
                for (AbstractSyntaxTree child : tree.children()) {
                    symbols = child.accept(this, data);
                }
                return symbols;
            }

            @SuppressWarnings("unchecked")
            @Override
            public List<Symbol> visit(ConjunctionTree tree, Object... data) {
                List<Symbol> symbols = (List<Symbol>) data[0];
                if(tree.root() == ConjunctionSymbol.OR) {
                    symbols = tree.left().accept(this, data);
                    symbols.add(tree.root());
                    symbols = tree.right().accept(this, data);
                }
                else {
                    for (AbstractSyntaxTree child : tree.children()) {
                        boolean parenthesis = false;
                        if(child instanceof OrTree) {
                            symbols.add(ParenthesisSymbol.LEFT);
                            parenthesis = true;
                        }
                        symbols = child.accept(this, data);
                        if(parenthesis) {
                            symbols.add(ParenthesisSymbol.RIGHT);
                            parenthesis = false;
                        }
                        symbols.add(tree.root());
                    }
                    symbols.remove(symbols.size() - 1); // remove dangling root
                                                        // symbol
                }
                return symbols;
            }

            @SuppressWarnings("unchecked")
            @Override
            public List<Symbol> visit(ExpressionTree tree, Object... data) {
                List<Symbol> symbols = (List<Symbol>) data[0];
                ExpressionSymbol root = (ExpressionSymbol) tree.root();
                symbols.add(root.key());
                symbols.add(root.operator());
                for (ValueTokenSymbol<?> symbol : root.values()) {
                    symbols.add(symbol);
                }
                if(root.timestamp() != null
                        && root.timestamp() != TimestampSymbol.PRESENT) {
                    symbols.add(root.timestamp());
                }
                return symbols;
            }

            @SuppressWarnings("unchecked")
            @Override
            public List<Symbol> visit(OrderTree tree, Object... data) {
                List<Symbol> symbols = (List<Symbol>) data[0];
                symbols.add(tree.root());
                return symbols;
            }

            @SuppressWarnings("unchecked")
            @Override
            public List<Symbol> visit(PageTree tree, Object... data) {
                List<Symbol> symbols = (List<Symbol>) data[0];
                symbols.add(tree.root());
                return symbols;
            }

            @SuppressWarnings("unchecked")
            @Override
            public List<Symbol> visit(FunctionTree tree,
                                      Object... data) {
                List<Symbol> symbols = (List<Symbol>) data[0];
                symbols.add(tree.root());
                return symbols;
            }

        };
        return ast.accept(visitor, Lists.newArrayList());
    }

    /**
     * Arrange the {@link Symbol symbols} in the {@code tree} as a {@link Queue}
     * of {@link PostfixNotationSymbol}s (i.e. expressions are grouped into
     * {@link ExpressionSymbol}s that are sorted by the proper order of
     * operations.
     * 
     * @param tree
     * @return a {@link Queue} of {@link PostfixNotationSymbol
     *         PostfixNotationSymbols}
     */
    public final Queue<PostfixNotationSymbol> arrange(ConditionTree tree) {
        Visitor<Queue<PostfixNotationSymbol>> visitor = new ConditionTreeVisitor<Queue<PostfixNotationSymbol>>() {

            @SuppressWarnings("unchecked")
            @Override
            public Queue<PostfixNotationSymbol> visit(ConjunctionTree tree,
                    Object... data) {
                Queue<PostfixNotationSymbol> queue = (Queue<PostfixNotationSymbol>) data[0];
                for (AbstractSyntaxTree child : tree.children()) {
                    queue = child.accept(this, data);
                }
                queue.add((ConjunctionSymbol) tree.root());
                return queue;
            }

            @SuppressWarnings("unchecked")
            @Override
            public Queue<PostfixNotationSymbol> visit(ExpressionTree tree,
                    Object... data) {
                Queue<PostfixNotationSymbol> queue = (Queue<PostfixNotationSymbol>) data[0];
                queue.add((ExpressionSymbol) tree.root());
                return queue;
            }

        };
        return tree.accept(visitor, new LinkedList<>());
    }

}
