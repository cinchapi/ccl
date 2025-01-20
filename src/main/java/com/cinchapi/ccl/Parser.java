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

import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.function.Function;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.ThreadSafe;

import com.cinchapi.ccl.grammar.condition.ExpressionSymbol;
import com.cinchapi.ccl.grammar.PostfixNotationSymbol;
import com.cinchapi.ccl.grammar.Symbol;
import com.cinchapi.ccl.syntax.AbstractSyntaxTree;
import com.cinchapi.ccl.syntax.condition.ConditionTree;
import com.cinchapi.ccl.type.Operator;
import com.cinchapi.common.base.Verify;
import com.cinchapi.common.function.TriFunction;
import com.google.common.collect.Multimap;

/**
 * A {@link Parser} is a stateful object that transforms raw CCL strings into
 * organized structures that can be logically evaluated.
 * 
 * @deprecated use {@link Compiler} instead
 *
 * @author Jeff Nelson
 */
@ThreadSafe
@Immutable
@Deprecated
public abstract class Parser {

    /**
     * Return a new {@link Parser} for the {@code ccl} statement that uses the
     * {@code valueTransformFunction} and {@code operatorTransformFunction}.
     *
     * @param ccl the ccl query to parse
     * @param valueTransformFunction value function
     * @param operatorTransformFunction operator function
     * @return the {@link Parser}
     * @deprecated use {@link Compiler#create(Function, Function)} instead
     */
    @Deprecated
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
     * @deprecated use {@link Compiler#create(Function, Function)} instead
     */
    @Deprecated
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
     * @deprecated use {@link Compiler#create(Function, Function)} instead
     */
    @Deprecated
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
     * @deprecated use {@link Compiler#create(Function, Function)} instead
     */
    @Deprecated
    public static Parser create(String ccl, Multimap<String, Object> data,
            Function<String, Object> valueTransformFunction,
            Function<String, Operator> operatorTransformFunction,
            TriFunction<Object, Operator, List<Object>, Boolean> localEvaluationFunction) {
        return new JavaCCParser(ccl, data, valueTransformFunction,
                operatorTransformFunction, localEvaluationFunction);
    }

    /**
     * An (optional) {@link TriFunction} that takes a value and {@link Operator}
     * as input and returns a boolean that indicates whether
     */
    @Nullable
    private final TriFunction<Object, Operator, List<Object>, Boolean> evaluator;

    /**
     * A boolean that indicates whether this {@link Parser} supports local
     * evaluation.
     */
    private final boolean supportsLocalEvaluation;

    protected final Compiler compiler;

    protected final String ccl;
    
    protected final Multimap<String, Object> data;

    /**
     * Construct a new instance.
     * 
     * @param ccl
     * @param data
     * @deprecated use {@link Compiler}
     */
    @Deprecated
    public Parser(String ccl, Multimap<String, Object> data,
            @Nullable TriFunction<Object, Operator, List<Object>, Boolean> localEvaluationFunction) {
        this.ccl = ccl;
        this.data = data;
        this.evaluator = localEvaluationFunction;
        this.supportsLocalEvaluation = localEvaluationFunction != null;
        this.compiler = compiler(this::transformValue, this::transformOperator);
    }

    /**
     * Return {@link Analysis} about the {@link #tokenize(String) tokenized} CCL
     * statement.
     * 
     * @param tokens
     * @return the {@link Analysis}
     * @deprecated use {@link Compiler#analyze(ConditionTree)}
     */
    @Deprecated
    public Analysis analyze() {
        AbstractSyntaxTree ast = compiler.parse(ccl, data);
        if(ast instanceof ConditionTree) {
            StatementAnalysis analysis = compiler.analyze((ConditionTree) ast);
            return new Analysis() {

                @Override
                public Set<String> keys() {
                    return analysis.keys();
                }

                @Override
                public Set<String> keys(Operator operator) {
                    return analysis.keys(operator);
                }

                @Override
                public Set<Operator> operators() {
                    return analysis.operators();
                }

            };
        }
        else {
            throw new UnsupportedOperationException();
        }
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
     * @deprecated Use
     *             {@link ConditionTree#evaluate(Multimap, TriFunction)}
     *             instead
     */
    @Deprecated
    public boolean evaluate(Multimap<String, Object> data) {
        Verify.that(supportsLocalEvaluation,
                "This Parser does not support local evaluation");
        AbstractSyntaxTree ast = compiler.parse(ccl, data);
        if(ast instanceof ConditionTree) {
            return compiler.evaluate((ConditionTree) ast, data, evaluator);
        }
        else {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * Transform a sequential list of {@link Symbol} tokens to an {@link Queue}
     * of symbols in {@link PostfixNotationSymbol postfix notation} that are
     * sorted by the proper order of operations.
     * 
     * @return a {@link Queue} of {@link PostfixNotationSymbol
     *         PostfixNotationSymbols}
     * @deprecated use {@link Compiler#arrange(ConditionTree)} instead
     */
    @Deprecated
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
     * @deprecated Use
     *             {@link Compiler#parse(String, Multimap)} instead
     */
    @Deprecated
    public abstract AbstractSyntaxTree parse();

    /**
     * Convert a CCL statement to a list of {@link Symbol} tokens and bind any
     * local variables using the provided {@code data}.
     * 
     * @param ccl the CCL statement
     * @param data the data to use for binding local variables
     * @return a list of {@link Symbol} tokens
     * @deprecated use {@link Compiler#tokenize(AbstractSyntaxTree)} instead
     */
    @Deprecated
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
     * Return a {@link Compiler} for delegation.
     * 
     * @return the {@link Compiler}
     */
    protected abstract Compiler compiler(Function<String, Object> valueParser,
            Function<String, Operator> operatorParser);

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

}
