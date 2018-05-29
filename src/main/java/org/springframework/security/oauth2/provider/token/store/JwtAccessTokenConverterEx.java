/*
 * Copyright 2018 NewTranx Co. Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.security.oauth2.provider.token.store;

import org.springframework.security.jwt.JwtHelper;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class JwtAccessTokenConverterEx extends JwtAccessTokenConverter {

    private final Map<String, JwtAccessTokenConverter> internal;

    private JwtAccessTokenConverter defaultInstance;

    public JwtAccessTokenConverterEx(Map<String, String> pubKeys) throws Exception {
        Map<String, JwtAccessTokenConverter> temp = new HashMap<>();
        for (Map.Entry<String, String> e : pubKeys.entrySet()) {
            String kid = e.getKey();
            String key64 = e.getValue();
            JwtAccessTokenConverter i = new JwtAccessTokenConverter();
            i.setVerifierKey(key64);
            i.afterPropertiesSet();
            temp.put(kid, i);
        }
        internal = Collections.unmodifiableMap(temp);
        this.defaultInstance = null;
    }

    public JwtAccessTokenConverterEx(Map<String, String> pubKeys, JwtAccessTokenConverter defaultInstance) throws Exception {
        this(pubKeys);
        this.defaultInstance = defaultInstance;
    }

    @Override
    protected Map<String, Object> decode(String token) {
        String kid = JwtHelper.headers(token).get("kid");
        final JwtAccessTokenConverter i;
        if (kid == null && defaultInstance != null) {
            i = defaultInstance;
        } else if (kid == null && internal.size() == 1) {
            i = internal.values().iterator().next();
        } else if (kid != null) {
            i = internal.get(kid);
        } else {
            throw new IllegalArgumentException("kid is required");
        }
        return i.decode(token);
    }

}
