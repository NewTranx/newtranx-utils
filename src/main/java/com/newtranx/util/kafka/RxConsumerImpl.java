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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Pattern;

import org.apache.kafka.clients.consumer.ConsumerRebalanceListener;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.Metric;
import org.apache.kafka.common.MetricName;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;

import rx.Observable;
import rx.observables.ConnectableObservable;
import rx.subjects.PublishSubject;

class RxConsumerImpl<K, V> implements RxConsumer<K, V> {

	private final KafkaConsumer<K, V> consumer;

	private final ConnectableObservable<ConsumerRecords<K, V>> observable;

	private final Lock lock = new ReentrantLock();

	private final PublishSubject<ConsumerRebalanceEvent<K, V>> consumerRebalanceSubject;

	private final ConsumerRebalanceListener consumerRebalanceListener;

	private final AtomicBoolean closed = new AtomicBoolean(false);

	private long pollTimeout = 100;

	public RxConsumerImpl(KafkaConsumer<K, V> consumer) {
		this.consumer = consumer;
		Observable<ConsumerRecords<K, V>> coldObs = Observable.create(s -> {
			lock.lock();
			try {
				while (!closed.get() && !s.isUnsubscribed()) {
					try {
						ConsumerRecords<K, V> records = consumer.poll(pollTimeout);
						if (records.isEmpty())
							continue;
						s.onNext(records);
						consumer.commitSync();
					} catch (WakeupException e) {
						/* Ignore exception if closing */
						if (!closed.get())
							throw e;
					}
				}
			} catch (Exception e) {
				s.onError(e);
			} finally {
				lock.unlock();
				if (!s.isUnsubscribed())
					s.onCompleted();
			}
		});
		this.observable = coldObs.publish();
		this.consumerRebalanceSubject = PublishSubject.create();
		this.consumerRebalanceListener = new ConsumerRebalanceListener() {

			@Override
			public void onPartitionsRevoked(Collection<TopicPartition> partitions) {
				ConsumerRebalanceEvent<K, V> event = new PartitionsRevokedEvent<K, V>(partitions, RxConsumerImpl.this);
				consumerRebalanceSubject.onNext(event);
			}

			@Override
			public void onPartitionsAssigned(Collection<TopicPartition> partitions) {
				ConsumerRebalanceEvent<K, V> event = new PartitionsAssignedEvent<K, V>(partitions, RxConsumerImpl.this);
				consumerRebalanceSubject.onNext(event);
			}

		};
	}

	@Override
	public Observable<ConsumerRebalanceEvent<K, V>> consumerRebalanceObservable() {
		return this.consumerRebalanceSubject;
	}

	@Override
	public void start() {
		this.observable.connect();
	}

	@Override
	public void shutdown() {
		if (closed.get())
			throw new IllegalStateException("already closed!");
		closed.set(true);
		consumer.wakeup();
		lock.lock();
		try {
			consumer.close();
			consumerRebalanceSubject.onCompleted();
		} finally {
			lock.unlock();
		}
	}

	@Override
	public Observable<ConsumerRecords<K, V>> observe() {
		return observable;
	}

	@Override
	public Observable<Iterable<ConsumerRecord<K, V>>> observeTopic(String topic) {
		return this.observe().map(x -> x.records(topic));
	}

	@Override
	public long getPollTimeout() {
		return pollTimeout;
	}

	@Override
	public void setPollTimeout(long pollTimeout) {
		this.pollTimeout = pollTimeout;
	}

	@Override
	public Set<TopicPartition> assignment() {
		return consumer.assignment();
	}

	@Override
	public Set<String> subscription() {
		return consumer.subscription();
	}

	@Override
	public void subscribe(List<String> topics) {
		consumer.subscribe(topics, this.consumerRebalanceListener);
	}

	@Override
	public void subscribe(Pattern pattern) {
		consumer.subscribe(pattern, this.consumerRebalanceListener);
	}

	@Override
	public void unsubscribe() {
		consumer.unsubscribe();
	}

	@Override
	public void assign(List<TopicPartition> partitions) {
		consumer.assign(partitions);
	}

	@Override
	public void seek(TopicPartition partition, long offset) {
		consumer.seek(partition, offset);
	}

	@Override
	public long position(TopicPartition partition) {
		return consumer.position(partition);
	}

	@Override
	public Map<MetricName, ? extends Metric> metrics() {
		return consumer.metrics();
	}

	@Override
	public List<PartitionInfo> partitionsFor(String topic) {
		return consumer.partitionsFor(topic);
	}

	@Override
	public Map<String, List<PartitionInfo>> listTopics() {
		return consumer.listTopics();
	}

	@Override
	public void resume(Collection<TopicPartition> arg0) {
		consumer.resume(arg0);
	}

	@Override
	public void seekToBeginning(Collection<TopicPartition> arg0) {
		consumer.seekToBeginning(arg0);
	}

	@Override
	public void seekToEnd(Collection<TopicPartition> arg0) {
		consumer.seekToEnd(arg0);
	}

	@Override
	public void pause(Collection<TopicPartition> arg0) {
		consumer.pause(arg0);
	}

}
