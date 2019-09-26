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

import javax.annotation.concurrent.Immutable;

import com.cinchapi.ccl.grammar.v3.Token;

/**
 * A {@link Symbol} is a terminal or non-terminal component in a grammar.
 *
 * @author Jeff Nelson
 */
@Immutable
public interface Symbol extends Token {/* marker */}
