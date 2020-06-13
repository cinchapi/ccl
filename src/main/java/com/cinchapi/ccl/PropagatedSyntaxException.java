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
package com.cinchapi.ccl;

import com.cinchapi.common.base.AnyStrings;

/**
 * An unchecked exception that wraps a propagated exception and adds context
 * about the compiler state.
 * 
 * @author Jeff Nelson
 */
@SuppressWarnings("serial")
public class PropagatedSyntaxException extends SyntaxException {

    /**
     * Construct a new instance.
     * 
     * @param e
     * @param info supplementary information about the syntax exception
     */
    public PropagatedSyntaxException(Exception e, String info) {
        super(AnyStrings.format("While processing '{}', {}", info,
                e.getMessage()));
        setStackTrace(e.getStackTrace());
    }

}
