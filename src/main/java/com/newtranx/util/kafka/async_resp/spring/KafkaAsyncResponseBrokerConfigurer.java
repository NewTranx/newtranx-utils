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

package com.newtranx.util.kafka.async_resp.spring;

import com.newtranx.util.kafka.async_resp.KafkaAsyncResponseBroker;
import com.newtranx.util.kafka.spring.KafkaCommonConfigUtil;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

/**
 * Created by luyi on 2016/10/31.
 */
@Configuration
public class KafkaAsyncResponseBrokerConfigurer {

    @Value("${spring.application.name}")
    private String appName;

    @Value("${server.instance.id}")
    private String instanceId;

    @Value("#{EnvPropertyMapper.subProperties('kafka.async.response.broker.consumer.')}")
    private Properties consumerProps;

    @Value("${kafka.async.response.broker.topic.prefix:async_response_}")
    private String topicPrefix;

    @Autowired
    private KafkaCommonConfigUtil commonConfigUtil;

    @Bean(initMethod = "start", destroyMethod = "shutdown")
    public KafkaAsyncResponseBroker<?> kafkaAsyncResponseBroker() {
        Properties consumerProps = (Properties) this.consumerProps.clone();
        commonConfigUtil.config(consumerProps);
        if (!consumerProps.containsKey(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG)) {
            consumerProps.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        }
        if (!consumerProps.containsKey(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG)) {
            consumerProps.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class.getName());
        }
        String topic = topicPrefix + appName;
        int partition = Integer.parseInt(instanceId);
        return KafkaAsyncResponseBroker.create(consumerProps, topic, partition);
    }

}
