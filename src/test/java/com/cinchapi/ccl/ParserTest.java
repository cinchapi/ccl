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

import org.junit.Assert;
import org.junit.Test;

import com.cinchapi.ccl.type.Operator;
import com.google.common.collect.Sets;

/**
 * Unit tests for {@link Parser} functionality.
 */
public class ParserTest {

    enum TestOperator implements Operator {
        EQUALS("=");

        String symbol;

        TestOperator(String symbol) {
            this.symbol = symbol;
        }

        @Override
        public int operands() {
            return 1;
        }

        @Override
        public String symbol() {
            return symbol;
        }

    }

    @Test
    public void testParserAnalysisIncludesAllCriteriaKeys() {
        String ccl = "name = jeff and age = 100 and company = cinchapi or company = blavity";
        Parser parser = Parser.newParser(ccl, (value) -> value,
                (operator) -> TestOperator.EQUALS);
        Assert.assertEquals(Sets.newHashSet("name", "age", "company"),
                parser.analyze().keys());;
    }

}
