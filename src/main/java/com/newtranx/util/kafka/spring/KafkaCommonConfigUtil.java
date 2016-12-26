/*
 * Copyright 2016 NewTranx Co. Ltd.
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

package com.newtranx.util.kafka.spring;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by luyi on 30/10/2016.
 */
@Component
public class KafkaCommonConfigUtil {

    private static final Set<String> CommonKeys = new HashSet<>();

    private static final Set<String> StreamsCommonKeys = new HashSet<>();

    static {
        CommonKeys.add("bootstrap.servers");
        CommonKeys.add("schema.registry.url");
        StreamsCommonKeys.addAll(CommonKeys);
        StreamsCommonKeys.add("zookeeper.connect");
    }

    @Value("#{EnvPropertyMapper.subProperties('kafka.common.config.')}")
    private Map<String, Object> commonKafkaProps;

    public void config(Map c) {
        this.config(c, CommonKeys);
    }

    public void configStreams(Map c) {
        this.config(c, StreamsCommonKeys);
    }

    public void config(Map c, Set<String> keys) {
        for (Object key : keys)
            if (!c.containsKey(key))
                c.put(key, this.commonKafkaProps.get(key));
    }

}
