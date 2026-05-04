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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import com.cinchapi.ccl.grammar.ConjunctionSymbol;
import com.cinchapi.ccl.grammar.ExpressionSymbol;
import com.cinchapi.ccl.grammar.KeySymbol;
import com.cinchapi.ccl.grammar.KeyTokenSymbol;
import com.cinchapi.ccl.grammar.NavigationKeyStop;
import com.cinchapi.ccl.grammar.NavigationKeySymbol;
import com.cinchapi.ccl.grammar.OperatorSymbol;
import com.cinchapi.ccl.grammar.ParenthesisSymbol;
import com.cinchapi.ccl.grammar.PostfixNotationSymbol;
import com.cinchapi.ccl.grammar.ScopeEndSymbol;
import com.cinchapi.ccl.grammar.ScopeSymbol;
import com.cinchapi.ccl.grammar.Symbol;
import com.cinchapi.ccl.grammar.TemporalKeySymbol;
import com.cinchapi.ccl.grammar.TimestampSymbol;
import com.cinchapi.ccl.grammar.ValueTokenSymbol;
import com.cinchapi.ccl.grammar.command.AddSymbol;
import com.cinchapi.ccl.grammar.command.AuditSymbol;
import com.cinchapi.ccl.grammar.command.BrowseSymbol;
import com.cinchapi.ccl.grammar.command.CalculateSymbol;
import com.cinchapi.ccl.grammar.command.ChronicleSymbol;
import com.cinchapi.ccl.grammar.command.ClearSymbol;
import com.cinchapi.ccl.grammar.command.CommandSymbol;
import com.cinchapi.ccl.grammar.command.DescribeSymbol;
import com.cinchapi.ccl.grammar.command.DiffSymbol;
import com.cinchapi.ccl.grammar.command.FindOrAddSymbol;
import com.cinchapi.ccl.grammar.command.FindOrInsertSymbol;
import com.cinchapi.ccl.grammar.command.FindSymbol;
import com.cinchapi.ccl.grammar.command.GetSymbol;
import com.cinchapi.ccl.grammar.command.HoldsSymbol;
import com.cinchapi.ccl.grammar.command.ImplicitSymbol;
import com.cinchapi.ccl.grammar.command.InsertSymbol;
import com.cinchapi.ccl.grammar.command.JsonifySymbol;
import com.cinchapi.ccl.grammar.command.LinkSymbol;
import com.cinchapi.ccl.grammar.command.NavigateSymbol;
import com.cinchapi.ccl.grammar.command.ReconcileSymbol;
import com.cinchapi.ccl.grammar.command.RemoveSymbol;
import com.cinchapi.ccl.grammar.command.RevertSymbol;
import com.cinchapi.ccl.grammar.command.SearchSymbol;
import com.cinchapi.ccl.grammar.command.SelectSymbol;
import com.cinchapi.ccl.grammar.command.SetSymbol;
import com.cinchapi.ccl.grammar.command.TraceSymbol;
import com.cinchapi.ccl.grammar.command.UnlinkSymbol;
import com.cinchapi.ccl.grammar.command.VerifyAndSwapSymbol;
import com.cinchapi.ccl.grammar.command.VerifyOrSetSymbol;
import com.cinchapi.ccl.grammar.command.VerifySymbol;
import com.cinchapi.ccl.syntax.*;
import com.cinchapi.ccl.type.Operator;
import com.cinchapi.common.base.Verify;
import com.cinchapi.common.collect.Association;
import com.cinchapi.common.collect.Multimaps;
import com.cinchapi.common.collect.Sequences;
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
                        keys.add(((ExpressionSymbol) symbol).key().bareKey());
                    }
                    else if(symbol instanceof KeyTokenSymbol) {
                        keys.add(((KeyTokenSymbol<?>) symbol).bareKey());
                    }
                    else if(symbol instanceof ScopeSymbol) {
                        keys.add(((ScopeSymbol) symbol).prefix().bareKey());
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
                        keys.add(expression.key().bareKey());
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

            @Override
            public Set<String> storageKeys() {
                Set<String> keys = new LinkedHashSet<>();
                forEachKey($tokens(),
                        (key) -> addStorageKeys(key, keys));
                return Collections.unmodifiableSet(keys);
            }

            @Override
            public Set<String> storageKeys(Operator operator) {
                Set<String> keys = new LinkedHashSet<>();
                forEachKeyWithOperator(operator,
                        (key) -> addStorageKeys(key, keys));
                return Collections.unmodifiableSet(keys);
            }

            @Override
            public Map<String, Set<Long>> temporalKeys() {
                Map<String, Set<Long>> result = new LinkedHashMap<>();
                forEachKey($tokens(),
                        (key) -> addBracketTemporals(key, result));
                return unmodifiableTemporalMap(result);
            }

            @Override
            public Map<String, Set<Long>> temporalKeys(Operator operator) {
                Map<String, Set<Long>> result = new LinkedHashMap<>();
                forEachKeyWithOperator(operator,
                        (key) -> addBracketTemporals(key, result));
                return unmodifiableTemporalMap(result);
            }

            @Override
            public Set<String> transitiveNavigationKeys() {
                Set<String> keys = new LinkedHashSet<>();
                forEachKey($tokens(),
                        (key) -> addTransitiveStops(key, keys));
                return Collections.unmodifiableSet(keys);
            }

            @Override
            public Set<String> transitiveNavigationKeys(Operator operator) {
                Set<String> keys = new LinkedHashSet<>();
                forEachKeyWithOperator(operator,
                        (key) -> addTransitiveStops(key, keys));
                return Collections.unmodifiableSet(keys);
            }

            @Override
            public Set<String> navigationKeys() {
                Set<String> paths = new LinkedHashSet<>();
                forEachKey($tokens(), (key) -> {
                    String path = navPathOf(key);
                    if(path != null) {
                        paths.add(path);
                    }
                });
                return Collections.unmodifiableSet(paths);
            }

            @Override
            public Set<String> navigationKeys(Operator operator) {
                Set<String> paths = new LinkedHashSet<>();
                forEachKeyWithOperator(operator, (key) -> {
                    String path = navPathOf(key);
                    if(path != null) {
                        paths.add(path);
                    }
                });
                return Collections.unmodifiableSet(paths);
            }

            @Override
            public Set<String> navigationKeyStops() {
                Set<String> stops = new LinkedHashSet<>();
                forEachKey($tokens(),
                        (key) -> addNavigationStops(key, stops));
                return Collections.unmodifiableSet(stops);
            }

            @Override
            public Set<String> navigationKeyStops(Operator operator) {
                Set<String> stops = new LinkedHashSet<>();
                forEachKeyWithOperator(operator,
                        (key) -> addNavigationStops(key, stops));
                return Collections.unmodifiableSet(stops);
            }

            @Override
            public Map<String, List<String>> scopedKeys() {
                return collectScopedKeys(null);
            }

            @Override
            public Map<String, List<String>> scopedKeys(Operator operator) {
                return collectScopedKeys(operator);
            }

            /**
             * Walk the scoped {@link ScopeSymbol} / {@link ScopeEndSymbol}
             * structure in {@link #$tokens()} and build the pivot-to-children
             * {@link Map}. When {@code filterOperator} is non-{@code null},
             * only scopes that directly contain an expression evaluated
             * against that {@link Operator} are kept.
             *
             * @param filterOperator the {@link Operator} to filter on, or
             *            {@code null} for no filtering
             * @return the scoped-keys {@link Map}
             */
            private Map<String, List<String>> collectScopedKeys(
                    Operator filterOperator) {
                Map<String, List<String>> result = new LinkedHashMap<>();
                Deque<ScopeFrame> stack = new ArrayDeque<>();
                List<Symbol> grouped = Parsing
                        .groupExpressions($tokens());
                for (Symbol symbol : grouped) {
                    if(symbol instanceof ScopeSymbol) {
                        String pivot = ((ScopeSymbol) symbol).prefix()
                                .bareKey();
                        if(!stack.isEmpty()) {
                            stack.peek().children.add(pivot);
                        }
                        ScopeFrame frame = new ScopeFrame(pivot);
                        stack.push(frame);
                    }
                    else if(symbol instanceof ScopeEndSymbol) {
                        ScopeFrame frame = stack.pop();
                        if(filterOperator == null || frame.containsOperator) {
                            result.merge(frame.pivot, frame.children,
                                    (existing, fresh) -> {
                                        existing.addAll(fresh);
                                        return existing;
                                    });
                        }
                    }
                    else if(!stack.isEmpty()
                            && symbol instanceof ExpressionSymbol) {
                        ExpressionSymbol expr = (ExpressionSymbol) symbol;
                        ScopeFrame frame = stack.peek();
                        addStorageKeys(expr.key(), frame.children);
                        if(expr.operator().operator().equals(filterOperator)) {
                            frame.containsOperator = true;
                        }
                    }
                }
                return Collections.unmodifiableMap(result);
            }

            private List<Symbol> $tokens() {
                if(tokens == null) {
                    tokens = tokenize(tree);
                }
                return tokens;
            }

            /**
             * Invoke {@code action} for every {@link KeyTokenSymbol} and
             * scope-prefix {@link KeyTokenSymbol} in the token stream.
             *
             * @param symbols the token stream
             * @param action the action to apply
             */
            private void forEachKey(List<Symbol> symbols,
                    java.util.function.Consumer<KeyTokenSymbol<?>> action) {
                for (Symbol symbol : symbols) {
                    if(symbol instanceof KeyTokenSymbol) {
                        action.accept((KeyTokenSymbol<?>) symbol);
                    }
                    else if(symbol instanceof ScopeSymbol) {
                        action.accept(((ScopeSymbol) symbol).prefix());
                    }
                }
            }

            /**
             * Invoke {@code action} for the {@link KeyTokenSymbol} of every
             * {@link ExpressionSymbol} whose operator equals
             * {@code operator}, after grouping the token stream into
             * expressions.
             *
             * @param operator the {@link Operator} to filter on
             * @param action the action to apply
             */
            private void forEachKeyWithOperator(Operator operator,
                    java.util.function.Consumer<KeyTokenSymbol<?>> action) {
                List<Symbol> grouped = Parsing
                        .groupExpressions($tokens());
                for (Symbol symbol : grouped) {
                    if(symbol instanceof ExpressionSymbol
                            && ((ExpressionSymbol) symbol).operator()
                                    .operator().equals(operator)) {
                        action.accept(((ExpressionSymbol) symbol).key());
                    }
                }
            }

        };
    }

    /**
     * Return {@link CommandAnalysis analysis} about the {@link CommandTree
     * tree}, aggregating selection-side keys, the inner {@code WHERE}
     * condition (if any), the command-level timestamp, and the records
     * the command directly touches.
     *
     * @param tree the {@link CommandTree} to analyze
     * @return the {@link CommandAnalysis}
     */
    public final CommandAnalysis analyze(CommandTree tree) {
        CommandSymbol command = (CommandSymbol) tree.root();
        StatementAnalysis condition = tree.conditionTree() != null
                ? analyze(tree.conditionTree())
                : null;
        List<KeyTokenSymbol<?>> selection = commandSelectionKeys(command);
        TimestampSymbol cmdTs = commandTimestamp(command);
        TimestampSymbol cmdStart = commandRangeStart(command);
        TimestampSymbol cmdEnd = commandRangeEnd(command);
        Set<Long> records = commandReferencedRecords(command);
        return new CommandAnalysis() {

            @Override
            public String commandType() {
                return command.type();
            }

            @Override
            public Long commandTimestamp() {
                return cmdTs == null ? null : cmdTs.timestamp();
            }

            @Override
            public Long rangeStart() {
                return cmdStart == null ? null : cmdStart.timestamp();
            }

            @Override
            public Long rangeEnd() {
                return cmdEnd == null ? null : cmdEnd.timestamp();
            }

            @Override
            public Set<Long> referencedRecords() {
                return records;
            }

            @Override
            public Set<String> keys() {
                Set<String> result = new LinkedHashSet<>();
                for (KeyTokenSymbol<?> key : selection) {
                    result.add(key.bareKey());
                }
                if(condition != null) {
                    result.addAll(condition.keys());
                }
                return Collections.unmodifiableSet(result);
            }

            @Override
            public Set<String> keys(Operator operator) {
                return condition == null ? Collections.emptySet()
                        : condition.keys(operator);
            }

            @Override
            public Set<Operator> operators() {
                return condition == null ? Collections.emptySet()
                        : condition.operators();
            }

            @Override
            public Set<String> storageKeys() {
                Set<String> result = new LinkedHashSet<>();
                for (KeyTokenSymbol<?> key : selection) {
                    addStorageKeys(key, result);
                }
                if(condition != null) {
                    result.addAll(condition.storageKeys());
                }
                return Collections.unmodifiableSet(result);
            }

            @Override
            public Set<String> storageKeys(Operator operator) {
                return condition == null ? Collections.emptySet()
                        : condition.storageKeys(operator);
            }

            @Override
            public Map<String, Set<Long>> temporalKeys() {
                Map<String, Set<Long>> result = new LinkedHashMap<>();
                for (KeyTokenSymbol<?> key : selection) {
                    addBracketTemporals(key, result);
                }
                if(condition != null) {
                    for (Map.Entry<String, Set<Long>> entry : condition
                            .temporalKeys().entrySet()) {
                        result.computeIfAbsent(entry.getKey(),
                                k -> new LinkedHashSet<>())
                                .addAll(entry.getValue());
                    }
                }
                return unmodifiableTemporalMap(result);
            }

            @Override
            public Map<String, Set<Long>> temporalKeys(Operator operator) {
                return condition == null ? Collections.emptyMap()
                        : condition.temporalKeys(operator);
            }

            @Override
            public Set<String> transitiveNavigationKeys() {
                Set<String> result = new LinkedHashSet<>();
                for (KeyTokenSymbol<?> key : selection) {
                    addTransitiveStops(key, result);
                }
                if(condition != null) {
                    result.addAll(condition.transitiveNavigationKeys());
                }
                return Collections.unmodifiableSet(result);
            }

            @Override
            public Set<String> transitiveNavigationKeys(Operator operator) {
                return condition == null ? Collections.emptySet()
                        : condition.transitiveNavigationKeys(operator);
            }

            @Override
            public Set<String> navigationKeys() {
                Set<String> result = new LinkedHashSet<>();
                for (KeyTokenSymbol<?> key : selection) {
                    String path = navPathOf(key);
                    if(path != null) {
                        result.add(path);
                    }
                }
                if(condition != null) {
                    result.addAll(condition.navigationKeys());
                }
                return Collections.unmodifiableSet(result);
            }

            @Override
            public Set<String> navigationKeys(Operator operator) {
                return condition == null ? Collections.emptySet()
                        : condition.navigationKeys(operator);
            }

            @Override
            public Set<String> navigationKeyStops() {
                Set<String> result = new LinkedHashSet<>();
                for (KeyTokenSymbol<?> key : selection) {
                    addNavigationStops(key, result);
                }
                if(condition != null) {
                    result.addAll(condition.navigationKeyStops());
                }
                return Collections.unmodifiableSet(result);
            }

            @Override
            public Set<String> navigationKeyStops(Operator operator) {
                return condition == null ? Collections.emptySet()
                        : condition.navigationKeyStops(operator);
            }

            @Override
            public Map<String, List<String>> scopedKeys() {
                return condition == null ? Collections.emptyMap()
                        : condition.scopedKeys();
            }

            @Override
            public Map<String, List<String>> scopedKeys(Operator operator) {
                return condition == null ? Collections.emptyMap()
                        : condition.scopedKeys(operator);
            }

        };
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

            @SuppressWarnings("unchecked")
            @Override
            public Queue<PostfixNotationSymbol> visit(ScopedConditionTree tree,
                    Object... data) {
                Queue<PostfixNotationSymbol> queue = (Queue<PostfixNotationSymbol>) data[0];
                queue.add((ScopeSymbol) tree.root());
                tree.condition().accept(this, data);
                queue.add(ScopeEndSymbol.INSTANCE);
                return queue;
            }

        };
        return tree.accept(visitor, new LinkedList<>());
    }

    /**
     * Evaluate a CCL string that may contain multiple statements separated by
     * semicolons. Each statement is parsed independently and an
     * {@link AbstractSyntaxTree} is returned for each.
     *
     * @param ccl the CCL string, potentially containing semicolons
     * @return a {@link List} of {@link AbstractSyntaxTree} instances, one per
     *         statement
     */
    public final List<AbstractSyntaxTree> compile(String ccl) {
        return compile(ccl, ImmutableMultimap.of());
    }

    /**
     * Evaluate a CCL string that may contain multiple statements separated by
     * semicolons, using the provided {@code data} for local variable
     * resolution. Each statement is parsed independently.
     *
     * @param ccl the CCL string, potentially containing semicolons
     * @param data data for local resolution of value variables
     * @return a {@link List} of {@link AbstractSyntaxTree} instances
     */
    public abstract List<AbstractSyntaxTree> compile(String ccl,
            Multimap<String, Object> data);

    /**
     * Return {@code true} if the {@code data} satisfies the condition
     * encapsulated in the {@code tree}.
     * <p>
     * Prefer this overload over
     * {@link #evaluate(ConditionTree, Multimap, TriFunction)} when possible
     * &mdash; it avoids the intermediate conversion from {@link Multimap} to
     * {@link Association} and is therefore more efficient.
     * </p>
     *
     * @param tree the {@link ConditionTree} that represents
     *            the condition to evaluate
     * @param data the {@link Association} containing the data to test for
     *            adherence to the condition
     * @param evaluator a {@link TriFunction} that takes a stored value,
     *            {@link Operator}, and list of reference values as input and
     *            returns a boolean indicating whether the stored value
     *            satisfies the {@link Operator} in relation to the reference
     *            values
     * @return {@code true} if the {@code data} is described by the condition in
     *         the {@code tree}
     */
    public final boolean evaluate(ConditionTree tree, Association data,
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

            @Override
            public Boolean visit(ExpressionTree tree, Object... data) {
                Verify.thatArgument(data.length > 0);
                Verify.thatArgument(data[0] instanceof Association);
                Association dataset = (Association) data[0];
                ExpressionSymbol expression = ((ExpressionSymbol) tree.root());
                String key = expression.raw().key();
                Operator operator = expression.raw().operator();
                List<Object> values = expression.raw().values();
                Object stored = dataset.fetch(key);
                Stream<Object> stream = Sequences.isSequence(stored)
                        ? Sequences.stream(stored)
                        : Stream.of(stored);
                return stream
                        .map(item -> evaluator.apply(item, operator, values))
                        .filter(value -> Boolean.TRUE.equals(value)).findFirst()
                        .orElse(false);

            }

            @Override
            public Boolean visit(ScopedConditionTree tree, Object... data) {
                throw new UnsupportedOperationException(
                        "Local evaluation cannot honor scoped "
                                + "same-destination semantics for a "
                                + "prefix-scoped condition; scoped "
                                + "navigation evaluation is the "
                                + "engine's responsibility.");
            }

        };
        return tree.accept(visitor, data);
    }

    /**
     * Return {@code true} if the {@code data} is described by the condition
     * encapsulated in the {@code tree}.
     * <p>
     * This overload converts the {@link Multimap} to an {@link Association}
     * before evaluating. When an {@link Association} is already available,
     * prefer {@link #evaluate(ConditionTree, Association, TriFunction)} to
     * avoid the conversion overhead.
     * </p>
     *
     * @param tree the {@link ConditionTree} that represents
     *            the condition
     * @param data the data to test for adherence to the condition
     * @param evaluator a {@link TriFunction} that takes a consideration value,
     *            {@link Operator}, and list of reference values as input and
     *            returns a boolean indicating whether the consideration value
     *            satisfies the {@link Operator} in relation to the reference
     *            values
     * @return {@code true} if the {@code data} is described by the condition in
     *         the {@code tree}
     */
    public final boolean evaluate(ConditionTree tree,
            Multimap<String, Object> data,
            TriFunction<Object, Operator, List<Object>, Boolean> evaluator) {
        return evaluate(tree,
                Association.ensure(
                        Multimaps.asMapWithSingleValueWherePossible(data)),
                evaluator);
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
    public AbstractSyntaxTree parse(String ccl, Multimap<String, Object> data) {
        List<AbstractSyntaxTree> results = compile(ccl, data);
        if(results.isEmpty()) {
            throw new IllegalArgumentException(
                    "No statements found in: " + ccl);
        }
        else {
            return results.get(0);
        }
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
            public List<Symbol> visit(CommandTree tree, Object... data) {
                List<Symbol> symbols = (List<Symbol>) data[0];
                if(!(tree.root() instanceof ImplicitSymbol)) {
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
            public List<Symbol> visit(FunctionTree tree, Object... data) {
                List<Symbol> symbols = (List<Symbol>) data[0];
                symbols.add(tree.root());
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
            public List<Symbol> visit(ScopedConditionTree tree,
                    Object... data) {
                List<Symbol> symbols = (List<Symbol>) data[0];
                symbols.add(tree.root());
                tree.condition().accept(this, data);
                symbols.add(ScopeEndSymbol.INSTANCE);
                return symbols;
            }

        };
        return ast.accept(visitor, Lists.newArrayList());
    }

    /**
     * Append every storage-level key reachable from {@code key} to
     * {@code result}. A {@link KeySymbol} contributes its bare key; a
     * {@link NavigationKeySymbol} contributes each stop's bare key; a
     * {@link TemporalKeySymbol} unwraps to its inner
     * {@link KeyTokenSymbol} and contributes its keys.
     *
     * @param key the {@link KeyTokenSymbol} to walk
     * @param result the {@link java.util.Collection} to append to
     */
    private static void addStorageKeys(KeyTokenSymbol<?> key,
            java.util.Collection<String> result) {
        KeyTokenSymbol<?> unwrapped = unwrapTemporal(key);
        if(unwrapped instanceof NavigationKeySymbol) {
            for (NavigationKeyStop stop : ((NavigationKeySymbol) unwrapped)
                    .stops()) {
                result.add(stop.key());
            }
        }
        else {
            result.add(unwrapped.bareKey());
        }
    }

    /**
     * Append every storage-level key that participates in a navigation
     * path inside {@code key} to {@code result}. Flat keys contribute
     * nothing.
     *
     * @param key the {@link KeyTokenSymbol} to walk
     * @param result the {@link Set} to append to
     */
    private static void addNavigationStops(KeyTokenSymbol<?> key,
            Set<String> result) {
        KeyTokenSymbol<?> unwrapped = unwrapTemporal(key);
        if(unwrapped instanceof NavigationKeySymbol) {
            for (NavigationKeyStop stop : ((NavigationKeySymbol) unwrapped)
                    .stops()) {
                result.add(stop.key());
            }
        }
    }

    /**
     * Append every storage-level key marked with the transitive suffix
     * inside {@code key} to {@code result}.
     *
     * @param key the {@link KeyTokenSymbol} to walk
     * @param result the {@link Set} to append to
     */
    private static void addTransitiveStops(KeyTokenSymbol<?> key,
            Set<String> result) {
        KeyTokenSymbol<?> unwrapped = unwrapTemporal(key);
        if(unwrapped instanceof NavigationKeySymbol) {
            for (NavigationKeyStop stop : ((NavigationKeySymbol) unwrapped)
                    .stops()) {
                if(stop.isTransitive()) {
                    result.add(stop.key());
                }
            }
        }
    }

    /**
     * Append every (storage key, microsecond) pair carried by a bracket
     * annotation inside {@code key} to {@code result}. A
     * {@link TemporalKeySymbol} is guaranteed to wrap a non-navigation
     * inner key (see the {@link TemporalKeySymbol} constructor
     * contract); a {@link NavigationKeySymbol} carries its temporal pins
     * directly on its {@link NavigationKeyStop stops}.
     *
     * @param key the {@link KeyTokenSymbol} to walk
     * @param result the {@link Map} to append to
     */
    private static void addBracketTemporals(KeyTokenSymbol<?> key,
            Map<String, Set<Long>> result) {
        if(key instanceof TemporalKeySymbol) {
            TemporalKeySymbol temporal = (TemporalKeySymbol) key;
            addTemporalEntry(temporal.key().bareKey(),
                    temporal.timestamp().timestamp(), result);
        }
        else if(key instanceof NavigationKeySymbol) {
            for (NavigationKeyStop stop : ((NavigationKeySymbol) key)
                    .stops()) {
                if(stop.timestamp() != null) {
                    addTemporalEntry(stop.key(),
                            stop.timestamp().timestamp(), result);
                }
            }
        }
    }

    /**
     * Append a single {@code (storageKey, ts)} pair to a temporal-keys
     * {@link Map}, allocating the value {@link Set} on first insert.
     *
     * @param storageKey the storage key
     * @param ts the microsecond timestamp
     * @param result the {@link Map} to append to
     */
    private static void addTemporalEntry(String storageKey, long ts,
            Map<String, Set<Long>> result) {
        result.computeIfAbsent(storageKey, k -> new LinkedHashSet<>())
                .add(ts);
    }

    /**
     * Wrap a temporal-keys {@link Map} so the outer {@link Map} and
     * every inner {@link Set} are immutable to callers.
     *
     * @param map the mutable temporal-keys {@link Map}
     * @return the unmodifiable view
     */
    private static Map<String, Set<Long>> unmodifiableTemporalMap(
            Map<String, Set<Long>> map) {
        Map<String, Set<Long>> wrapped = new LinkedHashMap<>(map.size());
        for (Map.Entry<String, Set<Long>> entry : map.entrySet()) {
            wrapped.put(entry.getKey(),
                    Collections.unmodifiableSet(entry.getValue()));
        }
        return Collections.unmodifiableMap(wrapped);
    }

    /**
     * Return the canonical bare path string of {@code key} when it (or
     * the {@link TemporalKeySymbol}-wrapped key) is a
     * {@link NavigationKeySymbol}; {@code null} otherwise.
     *
     * @param key the {@link KeyTokenSymbol} to inspect
     * @return the canonical bare path or {@code null}
     */
    private static String navPathOf(KeyTokenSymbol<?> key) {
        KeyTokenSymbol<?> unwrapped = unwrapTemporal(key);
        return unwrapped instanceof NavigationKeySymbol
                ? ((NavigationKeySymbol) unwrapped).bareKey() : null;
    }

    /**
     * Return the {@link KeyTokenSymbol} wrapped inside a
     * {@link TemporalKeySymbol}, or {@code key} unchanged when it is
     * not temporal.
     *
     * @param key the {@link KeyTokenSymbol} to unwrap
     * @return the inner {@link KeyTokenSymbol}
     */
    private static KeyTokenSymbol<?> unwrapTemporal(KeyTokenSymbol<?> key) {
        return key instanceof TemporalKeySymbol
                ? ((TemporalKeySymbol) key).key() : key;
    }

    /**
     * Return the selection-side keys {@code command} operates on, or
     * an empty {@link List} when the command is selectionless. The
     * selection keys are the keys named in the command itself, distinct
     * from any keys appearing in a nested {@code WHERE} condition.
     *
     * @param command the {@link CommandSymbol}
     * @return the selection keys
     */
    private static List<KeyTokenSymbol<?>> commandSelectionKeys(
            CommandSymbol command) {
        List<KeyTokenSymbol<?>> result = new ArrayList<>();
        if(command instanceof SelectSymbol) {
            addAll(result, ((SelectSymbol) command).keys());
        }
        else if(command instanceof GetSymbol) {
            addAll(result, ((GetSymbol) command).keys());
        }
        else if(command instanceof BrowseSymbol) {
            addAll(result, ((BrowseSymbol) command).keys());
        }
        else if(command instanceof NavigateSymbol) {
            addAll(result, ((NavigateSymbol) command).keys());
        }
        else if(command instanceof ClearSymbol) {
            ClearSymbol clear = (ClearSymbol) command;
            if(clear.keys() != null) {
                addAll(result, clear.keys());
            }
            else if(clear.key() != null) {
                result.add(clear.key());
            }
        }
        else if(command instanceof RevertSymbol) {
            RevertSymbol revert = (RevertSymbol) command;
            if(revert.keys() != null) {
                addAll(result, revert.keys());
            }
            else if(revert.key() != null) {
                result.add(revert.key());
            }
        }
        else if(command instanceof CalculateSymbol) {
            result.add(((CalculateSymbol) command).key());
        }
        else if(command instanceof SearchSymbol) {
            result.add(((SearchSymbol) command).key());
        }
        else if(command instanceof VerifySymbol) {
            result.add(((VerifySymbol) command).key());
        }
        else if(command instanceof AuditSymbol) {
            KeyTokenSymbol<?> key = ((AuditSymbol) command).key();
            if(key != null) {
                result.add(key);
            }
        }
        else if(command instanceof ChronicleSymbol) {
            result.add(((ChronicleSymbol) command).key());
        }
        else if(command instanceof DiffSymbol) {
            KeyTokenSymbol<?> key = ((DiffSymbol) command).key();
            if(key != null) {
                result.add(key);
            }
        }
        else if(command instanceof ReconcileSymbol) {
            result.add(((ReconcileSymbol) command).key());
        }
        else if(command instanceof AddSymbol) {
            result.add(((AddSymbol) command).key());
        }
        else if(command instanceof SetSymbol) {
            result.add(((SetSymbol) command).key());
        }
        else if(command instanceof RemoveSymbol) {
            result.add(((RemoveSymbol) command).key());
        }
        else if(command instanceof LinkSymbol) {
            result.add(((LinkSymbol) command).key());
        }
        else if(command instanceof UnlinkSymbol) {
            result.add(((UnlinkSymbol) command).key());
        }
        else if(command instanceof FindOrAddSymbol) {
            result.add(((FindOrAddSymbol) command).key());
        }
        else if(command instanceof VerifyAndSwapSymbol) {
            result.add(((VerifyAndSwapSymbol) command).key());
        }
        else if(command instanceof VerifyOrSetSymbol) {
            result.add(((VerifyOrSetSymbol) command).key());
        }
        return result;
    }

    /**
     * Return the command-level point-in-time {@link TimestampSymbol} of
     * {@code command}, or {@code null} when the command has no
     * timestamp or its timestamp is a range (handled by
     * {@link #commandRangeStart} / {@link #commandRangeEnd}).
     *
     * @param command the {@link CommandSymbol}
     * @return the {@link TimestampSymbol} or {@code null}
     */
    private static TimestampSymbol commandTimestamp(CommandSymbol command) {
        if(command instanceof SelectSymbol) {
            return ((SelectSymbol) command).timestamp();
        }
        if(command instanceof GetSymbol) {
            return ((GetSymbol) command).timestamp();
        }
        if(command instanceof BrowseSymbol) {
            return ((BrowseSymbol) command).timestamp();
        }
        if(command instanceof NavigateSymbol) {
            return ((NavigateSymbol) command).timestamp();
        }
        if(command instanceof CalculateSymbol) {
            return ((CalculateSymbol) command).timestamp();
        }
        if(command instanceof VerifySymbol) {
            return ((VerifySymbol) command).timestamp();
        }
        if(command instanceof FindSymbol) {
            return ((FindSymbol) command).timestamp();
        }
        if(command instanceof FindOrInsertSymbol) {
            return ((FindOrInsertSymbol) command).timestamp();
        }
        if(command instanceof DescribeSymbol) {
            return ((DescribeSymbol) command).timestamp();
        }
        if(command instanceof JsonifySymbol) {
            return ((JsonifySymbol) command).timestamp();
        }
        if(command instanceof RevertSymbol) {
            return ((RevertSymbol) command).timestamp();
        }
        if(command instanceof TraceSymbol) {
            return ((TraceSymbol) command).timestamp();
        }
        return null;
    }

    /**
     * Return the inclusive-start {@link TimestampSymbol} of a
     * range-history command ({@link AuditSymbol}, {@link ChronicleSymbol},
     * {@link DiffSymbol}), or {@code null} otherwise.
     *
     * @param command the {@link CommandSymbol}
     * @return the start {@link TimestampSymbol} or {@code null}
     */
    private static TimestampSymbol commandRangeStart(CommandSymbol command) {
        if(command instanceof AuditSymbol) {
            return ((AuditSymbol) command).start();
        }
        if(command instanceof ChronicleSymbol) {
            return ((ChronicleSymbol) command).start();
        }
        if(command instanceof DiffSymbol) {
            return ((DiffSymbol) command).start();
        }
        return null;
    }

    /**
     * Return the inclusive-end {@link TimestampSymbol} of a
     * range-history command, or {@code null} when the command has no
     * range or only supplied a start.
     *
     * @param command the {@link CommandSymbol}
     * @return the end {@link TimestampSymbol} or {@code null}
     */
    private static TimestampSymbol commandRangeEnd(CommandSymbol command) {
        if(command instanceof AuditSymbol) {
            return ((AuditSymbol) command).end();
        }
        if(command instanceof ChronicleSymbol) {
            return ((ChronicleSymbol) command).end();
        }
        if(command instanceof DiffSymbol) {
            return ((DiffSymbol) command).end();
        }
        return null;
    }

    /**
     * Return the record identifiers {@code command} directly touches,
     * or an empty {@link Set} when the command operates on a condition
     * or has no record argument.
     *
     * @param command the {@link CommandSymbol}
     * @return the referenced records
     */
    private static Set<Long> commandReferencedRecords(CommandSymbol command) {
        Set<Long> result = new LinkedHashSet<>();
        if(command instanceof SelectSymbol) {
            SelectSymbol s = (SelectSymbol) command;
            addRecord(result, s.record());
            addAll(result, s.records());
        }
        else if(command instanceof GetSymbol) {
            GetSymbol g = (GetSymbol) command;
            addRecord(result, g.record());
            addAll(result, g.records());
        }
        else if(command instanceof NavigateSymbol) {
            NavigateSymbol n = (NavigateSymbol) command;
            addRecord(result, n.record());
            addAll(result, n.records());
        }
        else if(command instanceof CalculateSymbol) {
            addAll(result, ((CalculateSymbol) command).records());
        }
        else if(command instanceof DescribeSymbol) {
            DescribeSymbol d = (DescribeSymbol) command;
            addRecord(result, d.record());
            addAll(result, d.records());
        }
        else if(command instanceof JsonifySymbol) {
            JsonifySymbol j = (JsonifySymbol) command;
            addRecord(result, j.record());
            addAll(result, j.records());
        }
        else if(command instanceof TraceSymbol) {
            TraceSymbol t = (TraceSymbol) command;
            addRecord(result, t.record());
            addAll(result, t.records());
        }
        else if(command instanceof HoldsSymbol) {
            HoldsSymbol h = (HoldsSymbol) command;
            addRecord(result, h.record());
            addAll(result, h.records());
        }
        else if(command instanceof InsertSymbol) {
            InsertSymbol i = (InsertSymbol) command;
            addRecord(result, i.record());
            addAll(result, i.records());
        }
        else if(command instanceof AddSymbol) {
            AddSymbol a = (AddSymbol) command;
            addRecord(result, a.record());
            addAll(result, a.records());
        }
        else if(command instanceof SetSymbol) {
            SetSymbol s = (SetSymbol) command;
            addRecord(result, s.record());
            addAll(result, s.records());
        }
        else if(command instanceof RemoveSymbol) {
            RemoveSymbol r = (RemoveSymbol) command;
            addRecord(result, r.record());
            addAll(result, r.records());
        }
        else if(command instanceof ClearSymbol) {
            ClearSymbol c = (ClearSymbol) command;
            addRecord(result, c.record());
            addAll(result, c.records());
        }
        else if(command instanceof RevertSymbol) {
            RevertSymbol r = (RevertSymbol) command;
            addRecord(result, r.record());
            addAll(result, r.records());
        }
        else if(command instanceof AuditSymbol) {
            result.add(((AuditSymbol) command).record());
        }
        else if(command instanceof ChronicleSymbol) {
            result.add(((ChronicleSymbol) command).record());
        }
        else if(command instanceof DiffSymbol) {
            Long record = ((DiffSymbol) command).record();
            addRecord(result, record);
        }
        else if(command instanceof ReconcileSymbol) {
            result.add(((ReconcileSymbol) command).record());
        }
        else if(command instanceof VerifySymbol) {
            result.add(((VerifySymbol) command).record());
        }
        else if(command instanceof VerifyAndSwapSymbol) {
            result.add(((VerifyAndSwapSymbol) command).record());
        }
        else if(command instanceof VerifyOrSetSymbol) {
            result.add(((VerifyOrSetSymbol) command).record());
        }
        return Collections.unmodifiableSet(result);
    }

    private static <T> void addAll(java.util.Collection<T> target,
            java.util.Collection<? extends T> source) {
        if(source != null) {
            target.addAll(source);
        }
    }

    private static void addRecord(Set<Long> target, Long record) {
        if(record != null) {
            target.add(record);
        }
    }

    /**
     * Mutable accumulator for one {@link ScopeSymbol} frame, used while
     * walking the token stream to build the {@code scopedKeys} map.
     */
    private static final class ScopeFrame {

        /**
         * The bare-form scope-prefix key.
         */
        final String pivot;

        /**
         * The direct child storage keys (and nested pivot keys)
         * collected for this scope.
         */
        final List<String> children = new ArrayList<>();

        /**
         * Whether this scope directly contains an
         * {@link ExpressionSymbol} matching the operator filter (only
         * meaningful when one was supplied).
         */
        boolean containsOperator = false;

        ScopeFrame(String pivot) {
            this.pivot = pivot;
        }
    }

}
