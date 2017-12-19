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
package com.cinchapi.ccl;

import java.util.function.Function;

import com.cinchapi.ccl.type.Operator;
import com.cinchapi.concourse.util.Convert;
import com.google.common.collect.Multimap;

/**
 * @author jnelson
 *
 */
public abstract class AbstractParserTest extends ParserTest {

    @Override
    protected final Parser createParser(String ccl) {
        return createParser(ccl, (value) -> Convert.stringToJava(value),
                (operator) -> Convert.stringToOperator(operator));
    }

    @Override
    protected final Parser createParser(String ccl,
            Multimap<String, Object> data) {
        return createParser(ccl, data, (value) -> Convert.stringToJava(value),
                (operator) -> Convert.stringToOperator(operator));
    }

    protected abstract Parser createParser(String ccl,
            Function<String, Object> valueTransformFunction,
            Function<String, Operator> operatorTransformFunction);

    protected abstract Parser createParser(String ccl,
            Multimap<String, Object> data,
            Function<String, Object> valueTransformFunction,
            Function<String, Operator> operatorTransformFunction);

}
