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
package com.cinchapi.ccl;

import java.util.Set;

import javax.annotation.Nullable;

/**
 * The {@link CommandAnalysis} provides metadata about a CCL command,
 * aggregating both the selection-side and the {@code WHERE}-side of the
 * statement under the same {@link StatementAnalysis} accessors. Each
 * key/temporal/navigation accessor it inherits unions selection keys,
 * inner condition keys, and any scope pivots so a single call answers
 * "what does this whole command touch".
 *
 * @author Jeff Nelson
 */
public interface CommandAnalysis extends StatementAnalysis {

    /**
     * Return the type of the parsed command (e.g. {@code "SELECT"},
     * {@code "FIND"}, {@code "ADD"}), as exposed by the underlying
     * {@link com.cinchapi.ccl.grammar.command.CommandSymbol}.
     *
     * @return the command type
     */
    public String commandType();

    /**
     * Return the command-level timestamp (the operation's
     * trailing-{@code at} / {@code as of} / equivalent), in
     * microseconds since the Unix epoch, or {@code null} when the
     * command is unpinned. Range-history commands ({@code audit},
     * {@code chronicle}, {@code diff}) have a {@code from … to …}
     * window rather than a single point and report {@code null} here;
     * use {@link #rangeStart()} and {@link #rangeEnd()} for those.
     *
     * @return the command-level microsecond timestamp or {@code null}
     */
    @Nullable
    public Long commandTimestamp();

    /**
     * Return the inclusive lower bound of a range-history command's
     * window, in microseconds since the Unix epoch, or {@code null}
     * when the command has no range.
     *
     * @return the range start microseconds or {@code null}
     */
    @Nullable
    public Long rangeStart();

    /**
     * Return the inclusive upper bound of a range-history command's
     * window, in microseconds since the Unix epoch, or {@code null}
     * when the command has no range or no end was supplied.
     *
     * @return the range end microseconds or {@code null}
     */
    @Nullable
    public Long rangeEnd();

    /**
     * Return the record identifiers the command directly touches. A
     * single-record command contributes one element; a multi-record
     * command contributes its full collection; commands that operate
     * over a {@code WHERE} criterion (no fixed record list) return an
     * empty {@link Set}.
     *
     * @return the referenced record identifiers
     */
    public Set<Long> referencedRecords();

}
