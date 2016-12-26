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

package com.newtranx.util.kafka;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.Metric;
import org.apache.kafka.common.MetricName;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;

import rx.Observable;

public interface RxConsumer<K, V> {

    public void start();

    /**
     * Block until shutdown is completed.
     */
    public void shutdown();

    public Observable<ConsumerRecords<K, V>> observe();

    public long getPollTimeout();

    public void setPollTimeout(long pollTimeout);

    public Observable<Iterable<ConsumerRecord<K, V>>> observeTopic(String topic);

    public Observable<ConsumerRebalanceEvent<K, V>> consumerRebalanceObservable();

    public Set<TopicPartition> assignment();

    public Set<String> subscription();

    public void subscribe(List<String> topics);

    public void subscribe(Pattern pattern);

    public void unsubscribe();

    public void assign(List<TopicPartition> partitions);

    public void seek(TopicPartition partition, long offset);

    public long position(TopicPartition partition);

    public Map<MetricName, ? extends Metric> metrics();

    public List<PartitionInfo> partitionsFor(String topic);

    public Map<String, List<PartitionInfo>> listTopics();

    public void resume(Collection<TopicPartition> arg0);

    public void seekToBeginning(Collection<TopicPartition> arg0);

    public void seekToEnd(Collection<TopicPartition> arg0);

    public void pause(Collection<TopicPartition> arg0);

    public abstract class ConsumerRebalanceEvent<K, V> {

        private final Collection<TopicPartition> topicPartitions;

        private final RxConsumer<K, V> consumer;

        public ConsumerRebalanceEvent(Collection<TopicPartition> topicPartitions, RxConsumer<K, V> consumer) {
            super();
            this.topicPartitions = topicPartitions;
            this.consumer = consumer;
        }

        public Collection<TopicPartition> getTopicPartitions() {
            return topicPartitions;
        }

        public RxConsumer<K, V> getConsumer() {
            return consumer;
        }

    }

    public class PartitionsRevokedEvent<K, V> extends ConsumerRebalanceEvent<K, V> {

        public PartitionsRevokedEvent(Collection<TopicPartition> topicPartitions, RxConsumer<K, V> consumer) {
            super(topicPartitions, consumer);
        }

    }

    public class PartitionsAssignedEvent<K, V> extends ConsumerRebalanceEvent<K, V> {

        public PartitionsAssignedEvent(Collection<TopicPartition> topicPartitions, RxConsumer<K, V> consumer) {
            super(topicPartitions, consumer);
        }

    }

    static <K, V> RxConsumer<K, V> wrap(KafkaConsumer<K, V> consumer) {
        return new RxConsumerImpl<K, V>(consumer);
    }

}