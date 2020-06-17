package com.cinchapi.ccl.syntax;

import com.cinchapi.ccl.grammar.FunctionTokenSymbol;
import com.cinchapi.ccl.grammar.Symbol;

import java.util.Collection;
import java.util.Collections;

/**
 * An abstraction for a function node in a {@link AbstractSyntaxTree}
 */
public class FunctionTree extends BaseAbstractSyntaxTree {
    /**
     * The root.
     */
    private final FunctionTokenSymbol function;

    /**
     * Construct a new instance.
     *
     * @param function
     */
    public FunctionTree(FunctionTokenSymbol function) {
        this.function = function;
    }

    @Override
    public Collection<AbstractSyntaxTree> children() {
        return Collections.emptyList();
    }

    @Override
    public Symbol root() {
        return function;
    }

    @Override
    public <T> T accept(Visitor<T> visitor, Object... data) {
        return visitor.visit(this, data);
    }
}
