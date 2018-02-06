package com.cinchapi.ccl.v2.generated;

import com.cinchapi.ccl.grammar.ExplicitFunction;
import com.cinchapi.ccl.grammar.ExplicitRecordsFunction;
import com.cinchapi.ccl.grammar.Symbol;
import com.cinchapi.ccl.syntax.AbstractSyntaxTree;
import com.cinchapi.ccl.syntax.Visitor;

import java.util.List;

/**
 * Represents a {@link ExplicitFunction} where the value is a CCL represented
 * as a {@link AbstractSyntaxTree}
 */
public class ASTExplicitFunctionValue extends ASTBaseValue<ExplicitFunction> {

    /**
     * Constructs a new instance.
     *
     * @param id the id
     */
    public ASTExplicitFunctionValue(int id) {
        super(id);
    }

    /**
     *
     * @param function
     * @param key
     * @param records
     */
    public void build(String function, String key, List<String> records) {
        this.value = new ExplicitRecordsFunction(function, key, records);
    }

    /**
     *
     * @param function
     * @param key
     * @param ccl
     */
    public void build(String function, String key, ASTStart ccl) {
        this.value = new ExplicitCclJavaCCASTFunction(function, key, ccl);
    }

    /**
     * Accept a visitor
     *
     * @param visitor the visitor
     * @param data the data
     * @return the result of the visit
     */
    public Object jjtAccept(GrammarVisitor visitor, Object data) {
        return visitor.visit(this, data);
    }

    @Override
    public ExplicitFunction value() {
        return value;
    }

    @Override
    public Symbol root() {
        return null;
    }

    @Override
    public <T> T accept(Visitor<T> visitor, Object... data) {
        return null;
    }
}
