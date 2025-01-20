package com.cinchapi.ccl.syntax.command;

import com.cinchapi.ccl.grammar.KeySymbol;
import com.cinchapi.ccl.syntax.AbstractSyntaxTree;
import com.cinchapi.ccl.syntax.BaseAbstractSyntaxTree;

import java.util.Collection;
import java.util.Collections;

public abstract class CommandTree extends BaseAbstractSyntaxTree {
    protected final KeySymbol key;
    protected final long record;

    public CommandTree(KeySymbol key, long record) {
        this.key = key;
        this.record = record;
    }

    public KeySymbol getKey() {
        return key;
    }

    public long getRecord() {
        return record;
    }

    @Override
    public Collection<AbstractSyntaxTree> children() {
        return Collections.emptyList();
    }
}
