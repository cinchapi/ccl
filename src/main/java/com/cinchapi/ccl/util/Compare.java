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
package com.cinchapi.ccl.util;

import java.util.concurrent.TimeUnit;

/**
 * A class to aid with the comparison of timestamps
 */
public final class Compare {

    /**
     * Helper function to compare timestamps
     *
     * @param long1
     * @param long2
     * @param precision
     * @return
     */
    public static boolean compareTimestampMicros(long long1, long long2, TimeUnit precision) {
        return precision.convert(long1, TimeUnit.MICROSECONDS)
                == precision.convert(long2, TimeUnit.MICROSECONDS);
    }
}
