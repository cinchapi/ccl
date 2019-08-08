package com.cinchapi.ccl.grammar;

/**
 * A {@link Symbol} that contains a {@link ValueFunction}
 */
public class FunctionValueSymbol extends BaseValueSymbol<ValueFunction> {

    /**
     * Construct a new instance.
     *
     * @param value
     */
    public FunctionValueSymbol(ValueFunction value) {
        super(value);
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
