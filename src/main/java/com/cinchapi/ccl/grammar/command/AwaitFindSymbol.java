/*
 * Copyright (c) 2013-2026 Cinchapi Inc.
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
package com.cinchapi.ccl.grammar.command;

import java.time.Duration;

import javax.annotation.concurrent.Immutable;

/**
 * A {@link CommandSymbol} for an AWAIT FIND command. The command's read finds
 * the records that match its condition and applies its optional order and page.
 * The command waits up to its timeout for the read to have a non-empty result,
 * and then runs the read.
 * <p>
 * The condition, order and page are children of the
 * {@link com.cinchapi.ccl.syntax.CommandTree CommandTree} whose root is this
 * symbol, the same as for a {@link FindSymbol}.
 * </p>
 *
 * @author Jeff Nelson
 */
@Immutable
public final class AwaitFindSymbol implements CommandSymbol {

    /**
     * The longest time that the command may wait.
     */
    private final Duration timeout;

    /**
     * Construct a new instance.
     *
     * @param timeout the longest time that the command may wait
     */
    public AwaitFindSymbol(Duration timeout) {
        this.timeout = timeout;
    }

    @Override
    public String type() {
        return "AWAIT_FIND";
    }

    /**
     * Return the longest time that the command may wait for its read to have a
     * non-empty result.
     *
     * @return the timeout
     */
    public Duration timeout() {
        return timeout;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof AwaitFindSymbol) {
            return timeout.equals(((AwaitFindSymbol) obj).timeout);
        }
        else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return timeout.hashCode();
    }

}
