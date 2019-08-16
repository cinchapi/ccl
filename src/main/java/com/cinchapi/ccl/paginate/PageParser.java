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
package com.cinchapi.ccl.paginate;

import com.cinchapi.ccl.paginate.generated.PageGrammar;
import com.cinchapi.ccl.paginate.generated.PageGrammarBasicVisitor;
import com.cinchapi.ccl.paginate.generated.PropagatedSyntaxException;
import com.cinchapi.ccl.paginate.generated.SimpleNode;
import com.cinchapi.concourse.lang.paginate.Page;

import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.ThreadSafe;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * A {@link PageParser} is a stateful object that transforms raw input strings into
 * organized structures that can be logically evaluated.
 */
@ThreadSafe
@Immutable
public class PageParser {

    /**
     * The string statement being parsed.
     */
    protected final String input;

    /**
     * Construct a new instance.
     *
     * @param input
     */
    public PageParser(String input) {
        this.input = input;
    }

    /**
     * Return the input statement that was parsed.
     *
     * @return the input string
     */
    public String input() {
        return input;
    }

    /**
     * Transform a input string into an {@link Page}.
     *
     * @return a {@link Page}
     */
    public Page page() {
        try {
            InputStream stream = new ByteArrayInputStream(
                    input.getBytes(StandardCharsets.UTF_8.name()));
            PageGrammar grammar = new PageGrammar(stream);

            SimpleNode start = grammar.Start();

            PageGrammarBasicVisitor visitor = new PageGrammarBasicVisitor();
            return (Page) start.jjtAccept(visitor, null);
        }
        catch (Exception exception) {
            throw new PropagatedSyntaxException(exception, this);
        }
    }

    @Override
    public String toString() {
        return input;
    }

}
