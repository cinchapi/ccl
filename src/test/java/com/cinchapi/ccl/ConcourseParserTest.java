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

import com.google.common.collect.Multimap;
import com.cinchapi.ccl.type.Operator;

/**
 * {@link ParserTest} that uses the {@link ConcourseParser}
 * 
 * @author Jeff Nelson
 */
@SuppressWarnings("deprecation")
public class ConcourseParserTest extends AbstractParserTest {

    @Override
    protected Parser createParser(String ccl,
            Function<String, Object> valueTransformFunction,
            Function<String, Operator> operatorTransformFunction) {
        return Parser.newParser(ccl, valueTransformFunction,
                operatorTransformFunction);
    }

    @Override
    protected Parser createParser(String ccl, Multimap<String, Object> data,
            Function<String, Object> valueTransformFunction,
            Function<String, Operator> operatorTransformFunction) {
        return Parser.newParser(ccl, data, valueTransformFunction,
                operatorTransformFunction);
    }

}