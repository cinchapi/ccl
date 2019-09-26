/*
 * Copyright (c) 2013-2017 Cinchapi Inc.
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
package com.cinchapi.ccl.grammar;

import java.util.List;
import com.cinchapi.ccl.grammar.v3.ExpressionToken;
import com.cinchapi.ccl.grammar.v3.KeyToken;
import com.cinchapi.ccl.grammar.v3.OperatorToken;
import com.cinchapi.ccl.grammar.v3.TimestampToken;
import com.cinchapi.ccl.grammar.v3.Token;
import com.cinchapi.ccl.grammar.v3.ValueToken;

/**
 * An {@link Expression} is a {@link Symbol} that describes a query operation
 * on a key with respect to one or more values (e.g. key = value, key >=
 * value, etc) at a certain timestamp.
 * <p>
 * This class is designed to make it easier to process the results of the
 * Shunting-Yard algorithm.
 * </p>
 * 
 * @author Jeff Nelson
 */
public class Expression extends BaseSymbol implements PostfixNotationSymbol, ExpressionToken {

    private final ExpressionToken delegate;

    /**
     * Construct a new instance.
     * 
     * @param key
     * @param operator
     * @param values
     */
    public Expression(KeySymbol key, OperatorSymbol operator,
            ValueSymbol... values) {
        this.delegate = ExpressionToken.create(key, operator, values);
    }

    /**
     * Construct a new instance.
     * 
     * @param timestamp
     * @param key
     * @param operator
     * @param values
     */
    public Expression(TimestampSymbol timestamp, KeySymbol key,
            OperatorSymbol operator, ValueSymbol... values) {
        this.delegate = ExpressionToken.create(timestamp, key, operator, values);
    }

    @Override
    public <T extends KeyToken<?>> T key() {
        return delegate.key();
    }

    @Override
    public OperatorToken operator() {
        return delegate.operator();
    }

    @Override
    public TimestampToken timestamp() {
        return delegate.timestamp();
    }

    @Override
    public List<ValueToken<?>> values() {
        return delegate.values();
    }
    
    @Override
    protected Token delegate() {
        return delegate;
    }


}
