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

import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.function.Function;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.ThreadSafe;

import com.cinchapi.ccl.grammar.ConjunctionSymbol;
import com.cinchapi.ccl.grammar.ExpressionSymbol;
import com.cinchapi.ccl.grammar.OperatorSymbol;
import com.cinchapi.ccl.grammar.PostfixNotationSymbol;
import com.cinchapi.ccl.grammar.KeySymbol;
import com.cinchapi.ccl.grammar.Symbol;
import com.cinchapi.ccl.syntax.AbstractSyntaxTree;
import com.cinchapi.ccl.syntax.ConditionTree;
import com.cinchapi.ccl.syntax.ConjunctionTree;
import com.cinchapi.ccl.syntax.ExpressionTree;
import com.cinchapi.ccl.syntax.PageTree;
import com.cinchapi.ccl.syntax.StatementTree;
import com.cinchapi.ccl.syntax.Visitor;
import com.cinchapi.ccl.type.Operator;
import com.cinchapi.common.base.Verify;
import com.cinchapi.common.function.TriFunction;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

/**
 * A {@link Parser} is a stateful object that transforms raw CCL strings into
 * organized structures that can be logically evaluated.
 *
 * @author Jeff Nelson
 */
@ThreadSafe
@Immutable
public abstract class Parser {

    /**
     * Return a new {@link Parser} for the {@code ccl} statement that uses the
     * {@code valueTransformFunction} and {@code operatorTransformFunction}.
     *
     * @param ccl the ccl query to parse
     * @param valueTransformFunction value function
     * @param operatorTransformFunction operator function
     * @return the {@link Parser}
     */
    public static Parser create(String ccl,
            Function<String, Object> valueTransformFunction,
            Function<String, Operator> operatorTransformFunction) {
        return create(ccl, valueTransformFunction, operatorTransformFunction,
                null);
    }

    /**
     * Return a new {@link Parser} for the {@code ccl} statement that uses the
     * {@code valueTransformFunction} and {@code operatorTransformFunction}.
     *
     * @param ccl the ccl query to parse
     * @param valueTransformFunction value function
     * @param operatorTransformFunction operator function
     * @param localEvaluationFunction a {@link TriFunction} that takes a value
     *            and determine whether it satisfies an {@link Operator} in
     *            relation to a list of other values
     * @return the {@link Parser}
     */
    public static Parser create(String ccl,
            Function<String, Object> valueTransformFunction,
            Function<String, Operator> operatorTransformFunction,
            TriFunction<Object, Operator, List<Object>, Boolean> localEvaluationFunction) {
        return new JavaCCParser(ccl, valueTransformFunction,
                operatorTransformFunction, localEvaluationFunction);
    }

    /**
     * Return a new {@link Parser} for the {@code ccl} statement that uses the
     * {@code data} for location resolution and the
     * {@code valueTransformFunction} and {@code operatorTransformFunction}.
     *
     * @param ccl the ccl query to parse
     * @param data the local data
     * @param valueTransformFunction value function
     * @param operatorTransformFunction operator function
     * @return the {@link Parser}
     */
    public static Parser create(String ccl, Multimap<String, Object> data,
            Function<String, Object> valueTransformFunction,
            Function<String, Operator> operatorTransformFunction) {
        return create(ccl, data, valueTransformFunction,
                operatorTransformFunction, null);
    }

    /**
     * Return a new {@link Parser} for the {@code ccl} statement that uses the
     * {@code data} for location resolution and the
     * {@code valueTransformFunction} and {@code operatorTransformFunction}.
     * 
     * @param ccl
     * @param data
     * @param valueTransformFunction
     * @param operatorTransformFunction
     * @param localEvaluationFunction a {@link TriFunction} that takes a value
     *            and determine whether it satisfies an {@link Operator} in
     *            relation to a list of other values
     * @return the {@link Parser}
     */
    public static Parser create(String ccl, Multimap<String, Object> data,
            Function<String, Object> valueTransformFunction,
            Function<String, Operator> operatorTransformFunction,
            TriFunction<Object, Operator, List<Object>, Boolean> localEvaluationFunction) {
        return new JavaCCParser(ccl, data, valueTransformFunction,
                operatorTransformFunction, localEvaluationFunction);
    }

    /**
     * The ccl statement being parsed.
     */
    protected final String ccl;

    /**
     * The dataset used for location resolution.
     */
    protected final Multimap<String, Object> data;

    /**
     * An (optional) {@link TriFunction} that takes a value and {@link Operator}
     * as input and returns a boolean that indicates whether
     */
    @Nullable
    private final LocalEvaluator evaluator;

    /**
     * A boolean that indicates whether this {@link Parser} supports local
     * evaluation.
     */
    private final boolean supportsLocalEvaluation;

    /**
     * Construct a new instance.
     * 
     * @param ccl
     * @param data
     */
    public Parser(String ccl, Multimap<String, Object> data,
            @Nullable TriFunction<Object, Operator, List<Object>, Boolean> localEvaluationFunction) {
        this.ccl = ccl;
        this.data = data;
        this.evaluator = localEvaluationFunction != null
                ? new LocalEvaluator(localEvaluationFunction)
                : null;
        this.supportsLocalEvaluation = localEvaluationFunction != null;
    }

    /**
     * Return {@link Analysis} about the {@link #tokenize(String) tokenized} CCL
     * statement.
     * 
     * @param tokens
     * @return the {@link Analysis}
     */
    public Analysis analyze() {
        return new Analysis() {

            @Override
            public Set<String> keys() {
                List<Symbol> tokens = tokenize();
                Set<String> keys = Sets
                        .newLinkedHashSetWithExpectedSize(tokens.size());
                tokens.forEach((symbol) -> {
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
                List<Symbol> tokens = tokenize();
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
                List<Symbol> tokens = tokenize();
                Set<Operator> operators = Sets
                        .newLinkedHashSetWithExpectedSize(tokens.size());
                tokens.forEach((symbol) -> {
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

        };
    }

    /**
     * Return the CCL statement that was parsed.
     * 
     * @return the CCL
     */
    public String ccl() {
        return ccl;
    }

    /**
     * Return {@code true} if the {@code data} is described by the criteria
     * which has been parsed by this {@link Parser}.
     * 
     * @param data the data to test for adherences to the criteria
     * @return {@code true} if the data is described by the criteria that has
     *         been parsed
     */
    public boolean evaluate(Multimap<String, Object> data) {
        Verify.that(supportsLocalEvaluation,
                "This Parser does not support local evaluation");
        return parse().accept(evaluator, data);
    }

    /**
     * Transform a sequential list of {@link Symbol} tokens to an {@link Queue}
     * of symbols in {@link PostfixNotationSymbol postfix notation} that are
     * sorted by the proper order of operations.
     * 
     * @return a {@link Queue} of {@link PostfixNotationSymbol
     *         PostfixNotationSymbols}
     */
    public abstract Queue<PostfixNotationSymbol> order();

    /**
     * Transform a sequential list of {@link Symbol} tokens to an
     * {@link AbstractSyntaxTree}.
     * 
     * <p>
     * NOTE: This method will group non-conjunctive symbols into
     * {@link ExpressionSymbol} objects.
     * </p>
     * 
     * @param symbols a sequential list of tokens
     * @return an {@link AbstractSyntaxTree} containing the parsed structure
     *         inherent in the symbols
     */
    public abstract AbstractSyntaxTree parse();

    /**
     * Convert a CCL statement to a list of {@link Symbol} tokens and bind any
     * local variables using the provided {@code data}.
     * 
     * @param ccl the CCL statement
     * @param data the data to use for binding local variables
     * @return a list of {@link Symbol} tokens
     */
    public abstract List<Symbol> tokenize();

    @Override
    public String toString() {
        return ccl;
    }

    /**
     * Implement a function that converts string operators to the appropriate
     * {@link Operator} object.
     * 
     * @return the transformed operator
     */
    protected abstract Operator transformOperator(String token);

    /**
     * Implement a function that converts string values to analogous java
     * objects.
     * 
     * @return the transformed value
     */
    protected abstract Object transformValue(String token);

    /**
     * A collection of insights about a CCL statement that is
     * {@link #analyze(String)} by this {@link Parser}.
     * 
     * @author Jeff Nelson
     */
    public interface Analysis {

        /**
         * Return an ordered collection of keys that are included in the CCL
         * statement.
         * 
         * @return the included keys
         */
        public Set<String> keys();

        /**
         * Return an ordered collection of keys that are included in the CCL
         * statement in an expression that contains the specified
         * {@code operator}.
         * 
         * @return the included keys that are evaluated against the
         *         {@code operator}
         */
        public Set<String> keys(Operator operator);

        /**
         * Return all the operators used in the CCL statement.
         * 
         * @return the included operators
         */
        public Set<Operator> operators();
    }

    /**
     * A {@link Visitor} that evaluates whether the criteria that has been
     * parsed matches a dataset.
     *
     * @author Jeff Nelson
     */
    private class LocalEvaluator implements Visitor<Boolean> {

        /**
         * The evaluation function.
         */
        private final TriFunction<Object, Operator, List<Object>, Boolean> function;

        /**
         * Construct a new instance.
         * 
         * @param function
         */
        public LocalEvaluator(
                TriFunction<Object, Operator, List<Object>, Boolean> function) {
            this.function = function;
        }

        @Override
        public Boolean visit(StatementTree tree, Object... data) {
            for(AbstractSyntaxTree child : tree.children()) {
                if(!child.accept(this, data)) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public Boolean visit(ConditionTree tree, Object... data) {
            for(AbstractSyntaxTree child : tree.children()) {
                if(!child.accept(this, data)) {
                    return false;
                }
            }
            return true;
        }

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
                if(function.apply(stored, operator, values)) {
                    matches = true;
                    break;
                }
                else {
                    continue;
                }
            }
            return matches;
        }

        @Override
        public Boolean visit(PageTree tree, Object... data) {
            // TODO: implement and return true if data[0] fits within the page.
            // May need more context.
            return true;
        }
    }

}
