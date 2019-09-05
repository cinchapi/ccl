/*
 * Copyright (c) 2013-2019 Cinchapi Inc.
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
package com.cinchapi.ccl.v3;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Queue;
import java.util.function.Function;

import com.cinchapi.ccl.JavaCCParser;
import com.cinchapi.ccl.Parser;
import com.cinchapi.ccl.Parsing;
import com.cinchapi.ccl.PropagatedSyntaxException;
import com.cinchapi.ccl.SyntaxException;
import com.cinchapi.ccl.grammar.PostfixNotationSymbol;
import com.cinchapi.ccl.grammar.Symbol;
import com.cinchapi.ccl.type.Operator;
import com.cinchapi.ccl.v2.generated.CriteriaGrammar;
import com.cinchapi.ccl.v2.generated.CriteriaGrammarPostfixVisitor;
import com.cinchapi.ccl.v2.generated.ParseException;
import com.cinchapi.ccl.v2.generated.SimpleNode;
import com.cinchapi.common.base.CheckedExceptions;
import com.cinchapi.common.function.TriFunction;
import com.google.common.collect.Lists;

import javax.annotation.Nullable;

/**
 * A {@link Condition} is an object that is used to encapsulate the semantics of
 * a complex query. Any any given time, objects of this class can exist in one
 * of two modes: {@code building} or {@code built}. When a Condition is
 * {@code built}, it is guaranteed to represent a fully and well formed query
 * that can be processed. On the other hand, when a Condition is {@code building}
 * it is in an incomplete state.
 * <p>
 * This class is the public interface to Condition construction. It is meant to
 * be used in a chained manner, where the caller initially calls
 * {@link Condition#where()} and continues to construct the Condition using the
 * options available from each subsequently returned state.
 * </p>
 *
 * @author Jeff Nelson
 */
public interface Condition extends Symbol {

    /**
     * Return a {@link Condition} object that expresses the same as the
     * {@code ccl} statement.
     *
     * @param ccl the CCL statement to parse
     * @return an equivalanet {@link Condition} object
     */
    public static Queue<PostfixNotationSymbol> parse(String ccl,
            Function<String, Object> valueTransformFunction,
            Function<String, Operator> operatorTransformFunction) {
        Parser parser = Parser.create(ccl, valueTransformFunction,
                operatorTransformFunction);
        BuiltCondition condition = new BuiltCondition();
        try {
            condition.symbols = Lists.newArrayList(parser.tokenize());
            return Parsing.toPostfixNotation(condition.symbols);
        }
        catch (Exception e) {
            if(e instanceof SyntaxException
                    || e instanceof IllegalStateException
                    || e.getCause() != null && e
                    .getCause() instanceof ParseException) {
                throw e;
            }
            else {
                throw CheckedExceptions.throwAsRuntimeException(e);
            }
        }
    }


    /**
     * Start building a new {@link Condition}.
     *
     * @return the Condition builder
     */
    public static StartState where() {
        return new StartState(new BuiltCondition());
    }

    /**
     * Return this {@link Condition} with each expression (e.g. {key} {operator}
     * {values}) pinned to the specified {@code timestamp}.
     *
     * <strong>NOTE:</strong> Any timestamps that are pinned to any expressions
     * within this Condition will be replaced by the specified {@code timestamp}.
     *
     * @param timestamp the {@link Timestamp} to which the returned
     *            {@link Condition} is pinned
     *
     * @return this {@link Condition} pinned to {@code timestamp}
     */
    public Condition at(Timestamp timestamp);

    /**
     * Return a CCL string that is equivalent to this object.
     *
     * @return an equivalent CCL string
     */
    public String ccl();

    /**
     * Return a CCL string that is equivalent to this object.
     *
     * @return an equivalent CCL string
     * @deprecated in favor of {@link #ccl()}
     */
    @Deprecated
    public default String getCclString() {
        return ccl();
    }

    /**
     * Return the order list of symbols that make up this {@link Condition}.
     *
     * @return symbols
     */
    public List<Symbol> symbols();

}

