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
package com.cinchapi.ccl.type.function;

import java.util.List;

import com.google.common.collect.Lists;

/**
 * A function applied to a key across multiple records.
 */
public class KeyRecordsFunction extends ExplicitBinaryFunction<List<Long>> {

    /**
     * Constructs a new instance
     *
     * @param function the function
     * @param key the key
     * @param records the records
     */
    public KeyRecordsFunction(String function, String key,
            List<String> records) {
        this(function, key, records.stream()
                .map(record -> Long.parseLong(record)).toArray(Long[]::new));
    }

    /**
     * Constructs a new instance
     *
     * @param function the function
     * @param key the key
     * @param records the records
     * @param timestamp the timestamp
     */
    public KeyRecordsFunction(String function, String key, List<String> records,
            long timestamp) {
        this(timestamp, function, key, records.stream()
                .map(record -> Long.parseLong(record)).toArray(Long[]::new));
    }

    /**
     * Construct a new instance.
     * 
     * @param function
     * @param key
     * @param records
     */
    public KeyRecordsFunction(String function, String key, Long... records) {
        super(function, key, Lists.newArrayList(records));
    }

    /**
     * Construct a new instance.
     *
     * @param function
     * @param key
     * @param records
     */
    public KeyRecordsFunction(long timestamp, String function, String key,
            Long... records) {
        super(function, key, Lists.newArrayList(records), timestamp);
    }

    @Override
    protected String _sourceToString() {
        return args[1].toString();
    }
}
