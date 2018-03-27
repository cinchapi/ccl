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

import com.cinchapi.ccl.grammar.KeySymbol;
import com.cinchapi.ccl.grammar.ValueSymbol;
import org.junit.Assert;
import org.junit.Test;

import com.cinchapi.ccl.grammar.Expression;
import com.cinchapi.ccl.grammar.OperatorSymbol;
import com.cinchapi.ccl.type.Operator;
import com.google.common.collect.Multimap;

/**
 * {@link ParserTest} that uses the {@link JavaCCParser}
 * 
 * @author Jeff Nelson
 */
public class JavaCCParserTest extends AbstractParserTest {

    @Override
    protected Parser createParser(String ccl,
            Function<String, Object> valueTransformFunction,
            Function<String, Operator> operatorTransformFunction) {
        return Parser.create(ccl, valueTransformFunction,
                operatorTransformFunction);
    }

    @Override
    protected Parser createParser(String ccl, Multimap<String, Object> data,
            Function<String, Object> valueTransformFunction,
            Function<String, Operator> operatorTransformFunction) {
        return Parser.create(ccl, data, valueTransformFunction,
                operatorTransformFunction);
    }

    @Test
    public void testParseCclNoSpaces() {
        String ccl = "name=jeff";
        Parser parser = createParser(ccl);
        parser.order();
        Assert.assertEquals(new Expression(new KeySymbol("name"),
                new OperatorSymbol(
                        com.cinchapi.concourse.thrift.Operator.EQUALS),
                new ValueSymbol("jeff")), parser.order().peek());
    }

}
