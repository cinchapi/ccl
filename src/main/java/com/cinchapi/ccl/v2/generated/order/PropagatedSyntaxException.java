/*
 * Copyright (c) 2013-2018 Cinchapi Inc.
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
package com.cinchapi.ccl.v2.generated.order;

import com.cinchapi.ccl.SyntaxException;
import com.cinchapi.ccl.lang.order.OrderParser;
import com.cinchapi.common.base.AnyStrings;

/**
 * An unchecked exception that wraps a propagated exception and adds context
 * about the parser state.
 * 
 * @author Jeff Nelson
 */
@SuppressWarnings("serial")
public class PropagatedSyntaxException extends SyntaxException {

    /**
     * Construct a new instance.
     * 
     * @param e
     * @param orderParser
     */
    public PropagatedSyntaxException(Exception e, OrderParser orderParser) {
        super(AnyStrings.format("While processing '{}', {}", orderParser.input(),
                e.getMessage()));
        setStackTrace(e.getStackTrace());
    }

}