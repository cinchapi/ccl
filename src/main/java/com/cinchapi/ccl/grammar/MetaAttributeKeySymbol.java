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

/**
 * A {@link Symbol} that contains a key and a meta attribute.
 *
 * @author Jeff Nelson
 */
public final class MetaAttributeKeySymbol extends BaseKeySymbol<String> {

    /**
     * The meta attribute of the key symbol
     */
    private String metaAttribute;

    /**
     * Construct a new instance.
     *
     * @param key the key
     */
    public MetaAttributeKeySymbol(String key, String metaAttribute) {
        super(key);
        this.metaAttribute = metaAttribute;
    }

    @Override
    public String toString() {
        return key + "#" + metaAttribute;
    }

    /**
     * Returns the meta attribute associated with the key symbol;
     *
     * @return
     */
    public String metaAttribute() {
        return metaAttribute;
    }
}
