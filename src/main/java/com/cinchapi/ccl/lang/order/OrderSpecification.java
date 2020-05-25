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
package com.cinchapi.ccl.lang.order;

import com.cinchapi.concourse.Timestamp;
import com.cinchapi.concourse.lang.sort.Order;

public class OrderSpecification {
    private String key;
    private Timestamp timestamp;
    private Direction direction;

    public OrderSpecification(String key, Timestamp timestamp, Direction direction) {
        this.key = key;
        this.timestamp= timestamp;
        this.direction = direction;
    }

    public OrderSpecification(String key, Direction direction) {
        this.key = key;
        this.timestamp = Timestamp.now();
        this.direction = direction;
    }

    public String key() {
        return key;
    }

    public Timestamp timestamp() {
        return timestamp;
    }

    public Direction direction() {
        return direction;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof OrderSpecification) {
            // Remove milliseconds from Timestamp
            // This is needed to avoid millisecond errors when comparing timestamps in
            // @JavaCCParserLogicTest
            int pmIndex = timestamp.toString().indexOf("PM");
            int objPmIndex = ((OrderSpecification)obj).timestamp.toString().indexOf("PM");
            String tempTimestamp = timestamp.toString();
            String objTempTimestamp = ((OrderSpecification)obj).timestamp.toString();
            if (pmIndex > 3 && objPmIndex > 3) {
                tempTimestamp =
                        timestamp.toString().substring(0, pmIndex - 3) + timestamp.toString().substring(pmIndex
                                - 1);
                objTempTimestamp =
                        ((OrderSpecification)obj).timestamp.toString().
                                substring(0, pmIndex - 3)
                                + ((OrderSpecification)obj).timestamp.toString()
                                .substring(pmIndex - 1);
            }

            if (!key.equals(((OrderSpecification)obj).key)) {
                return false;
            }
            else if (!tempTimestamp.equals(objTempTimestamp)) {
                return false;
            }
            else if (!direction.equals(((OrderSpecification)obj).direction)) {
                return false;
            }
            else {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return key + " " + timestamp.toString() + " " + direction.toString();
    }
}
