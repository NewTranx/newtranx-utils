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

package com.newtranx.util.kafka.async_resp;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.util.Collections;
import java.util.Comparator;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.*;

/**
 * Created by luyi on 30/10/2016.
 */
class BasicKafkaAsyncResponseBroker<V> implements KafkaAsyncResponseBroker<V> {

    private final KafkaConsumer<String, V> consumer;

    private final TopicPartition channel;

    private final ConcurrentMap<String, KafkaAsyncResponseImpl> suspended = new ConcurrentHashMap<>();

    private final ConsumerThread consumerThread = new ConsumerThread();

    public BasicKafkaAsyncResponseBroker(Properties consumerProps,
                                         Deserializer<V> valueDeser,
                                         String channelTopic, int channelPartition) {
        this(new KafkaConsumer<>(processConfig(consumerProps), new StringDeserializer(), valueDeser),
                channelTopic, channelPartition);
    }

    public BasicKafkaAsyncResponseBroker(Properties consumerProps,
                                         Class<Deserializer<V>> valueDeserClass,
                                         String channelTopic, int channelPartition) {
        this(new KafkaConsumer<>(processConfig(consumerProps, valueDeserClass)),
                channelTopic, channelPartition);
    }

    public BasicKafkaAsyncResponseBroker(Properties consumerProps,
                                         String channelTopic, int channelPartition) {
        this(new KafkaConsumer<>(processConfig(consumerProps)), channelTopic, channelPartition);
    }

    BasicKafkaAsyncResponseBroker(KafkaConsumer<String, V> consumer,
                                  String channelTopic, int channelPartition) {
        this.consumer = consumer;
        int totalPartitions = consumer.partitionsFor(channelTopic)
                .stream().max(Comparator.comparingInt(PartitionInfo::partition))
                .get().partition() + 1;
        this.channel = new TopicPartition(channelTopic, channelPartition % totalPartitions);
    }

    @Override
    public KafkaAsyncResponse<V> get() {
        KafkaAsyncResponseImpl obj = new KafkaAsyncResponseImpl();
        suspended.put(obj.key(), obj);
        return obj;
    }

    @Override
    public void start() {
        consumer.assign(Collections.singleton(this.channel));
        consumerThread.start();
    }

    @Override
    public void shutdown() throws InterruptedException {
        if (consumerThread.isAlive()) {
            consumerThread.shutdown();
            consumerThread.join();
        }
    }

    private static Properties processConfig(Properties in) {
        Properties props = (Properties) in.clone();
        props.setProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        props.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        props.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        return props;
    }

    private static <V> Properties processConfig(Properties in, Class<Deserializer<V>> valueDeserClass) {
        Properties props = processConfig(in);
        props.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, valueDeserClass.getName());
        return props;
    }

    private class KafkaAsyncResponseImpl implements KafkaAsyncResponse<V> {

        private final String key = UUID.randomUUID().toString();

        private final CompletableFuture<V> promise = new CompletableFuture<>();

        private final CompletionStage<V> future = promise.whenComplete((v, t) -> suspended.remove(key));

        @Override
        public TopicPartition channel() {
            return channel;
        }

        @Override
        public String key() {
            return key;
        }

        @Override
        public CompletionStage<V> future() {
            return future;
        }

        @Override
        public boolean cancel() {
            return promise.cancel(false);
        }

        @Override
        public boolean cancel(Throwable t) {
            return this.promise.completeExceptionally(t);
        }

        void onResponseReceived(V value) {
            promise.complete(value);
        }

    }

    private class ConsumerThread extends Thread {

        private volatile boolean shutdown;

        {
            this.setName("BasicKafkaAsyncResponseBroker");
        }

        void shutdown() {
            this.shutdown = true;
            consumer.wakeup();
        }

        @Override
        public void run() {
            try {
                loop();
            } finally {
                consumer.close();
            }
        }

        private void loop() {
            while (!shutdown) {
                try {
                    ConsumerRecords<String, V> records = consumer.poll(Long.MAX_VALUE);
                    for (ConsumerRecord<String, V> record : records) {
                        KafkaAsyncResponseImpl suspendedResp = suspended.get(record.key());
                        if (suspendedResp != null)
                            suspendedResp.onResponseReceived(record.value());
                    }
                    // We do not commit offset intentionally,
                    // as kafka consumer will reset offset to latest by default
                    // when startup
                } catch (WakeupException wakeUp) {
                    //do nothing, begin next loop or shutdown
                }
            }
        }

    }

}
