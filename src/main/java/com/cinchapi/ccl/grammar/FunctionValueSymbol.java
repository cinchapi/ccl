package com.cinchapi.ccl.grammar;

/**
 * A {@link Symbol} that contains a {@link Function}
 */
public class FunctionValueSymbol extends BaseValueSymbol<Function> {

    /**
     * Construct a new instance.
     *
     * @param value
     */
    public FunctionValueSymbol(Function value) {
        super(value);
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
