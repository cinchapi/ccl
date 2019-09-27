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
@Deprecated
public class Expression implements ExpressionSymbol {

    /**
     * The {@link ExpressionSymbol} that serves as a delegate.
     */
    private final ExpressionSymbol delegate;

    /**
     * Construct a new instance.
     * 
     * @param key
     * @param operator
     * @param values
     * @deprecated use
     *             {@link ExpressionSymbol#create(KeyTokenSymbol, OperatorSymbol, ValueTokenSymbol...)}
     *             instead.
     */
    @Deprecated
    public Expression(KeySymbol key, OperatorSymbol operator,
            ValueSymbol... values) {
        this.delegate = ExpressionSymbol.create(null, key, operator, values);
    }

    /**
     * Construct a new instance.
     * 
     * @param timestamp
     * @param key
     * @param operator
     * @param values
     * @deprecated use
     *             {@link ExpressionSymbol#create(TimestampSymbol, KeyTokenSymbol, OperatorSymbol, ValueTokenSymbol...)}
     *             instead.
     */
    public Expression(TimestampSymbol timestamp, KeySymbol key,
            OperatorSymbol operator, ValueSymbol... values) {
        this.delegate = ExpressionSymbol.create(key, operator, values);
    }

    @Override
    public <T extends KeyTokenSymbol<?>> T key() {
        return delegate.key();
    }

    @Override
    public OperatorSymbol operator() {
        return delegate.operator();
    }

    @Override
    public TimestampSymbol timestamp() {
        return delegate.timestamp();
    }

    @Override
    public List<ValueTokenSymbol<?>> values() {
        return delegate.values();
    }
    
    @Override
    public int hashCode() {
        return delegate.hashCode();
    }
    
    @Override
    public String toString() {
        return delegate.toString();
    }
    
    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Expression) {
            return delegate.equals(((Expression) obj).delegate);
        }
        else {
            return delegate.equals(obj);
        }
    }

}