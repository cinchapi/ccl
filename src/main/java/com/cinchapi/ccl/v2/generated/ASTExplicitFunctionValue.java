package com.cinchapi.ccl.v2.generated;

import com.cinchapi.ccl.grammar.ExplicitFunction;
import com.cinchapi.ccl.grammar.ExplicitRecordsFunction;
import com.cinchapi.ccl.grammar.Symbol;
import com.cinchapi.ccl.syntax.AbstractSyntaxTree;
import com.cinchapi.ccl.syntax.Visitor;
import com.cinchapi.common.base.AnyStrings;

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

    public void build(String function, String key, List<String> records) {
        this.value = new ExplicitRecordsFunction(function, key, records);
    }

    public void build(String function, String key, ASTStart ccl) {
        //GrammarTreeVisitor visitor = new GrammarTreeVisitor(this);
        //return (AbstractSyntaxTree) ccl.jjtAccept(visitor, null);
        //this.value = new ExplicitRecordsFunction(function, key, ccl);
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
