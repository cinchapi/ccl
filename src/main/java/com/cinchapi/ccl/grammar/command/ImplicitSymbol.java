package com.cinchapi.ccl.grammar.command;

/**
 * A {@link CommandSymbol} that represents an IMPLICIT command
 */
public class ImplicitSymbol implements CommandSymbol{
    /**
     * Singleton instance.
     */
    public static final ImplicitSymbol INSTANCE = new ImplicitSymbol();

    private ImplicitSymbol() {
        // Use INSTANCE
    }

    @Override
    public String type() {
        return "IMPLICIT";
    }
}
