package com.cinchapi.ccl.v2.generated;

import com.cinchapi.ccl.grammar.ExplicitFunction;
import com.cinchapi.ccl.syntax.AbstractSyntaxTree;
import com.cinchapi.common.base.AnyStrings;

/**
 * Represents a {@link ExplicitFunction} where the value is a CCL represented
 * as a {@link AbstractSyntaxTree}
 */
public class ASTExplicitCclASTFunction extends ExplicitFunction<ASTStart> {
    /**
     * Constructs a new instance
     *
     * @param function the function
     * @param key      the key
     * @param value    the value
     */
    public ASTExplicitCclASTFunction(String function, String key, ASTStart value) {
        super(function, key, value);
    }

    @Override public String toString() {
        String string = AnyStrings.format("{} ({},", function, key);
        return string;
    }
}
