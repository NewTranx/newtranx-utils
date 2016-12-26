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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.Metric;
import org.apache.kafka.common.MetricName;
import org.apache.kafka.common.PartitionInfo;

class RichProducerImpl<K, V> implements RichProducer<K, V> {

	public final Producer<K, V> producer;

	public RichProducerImpl(Producer<K, V> producer) {
		this.producer = producer;
	}

	public CompletableFuture<RecordMetadata> send(ProducerRecord<K, V> record, Optional<Executor> optExecutor) {
		CompletableFuture<RecordMetadata> f = new CompletableFuture<RecordMetadata>();
		this.producer.send(record, new Callback() {
			@Override
			public void onCompletion(RecordMetadata metadata, Exception exception) {
				if (optExecutor.isPresent())
					optExecutor.get().execute(() -> onSendCompletion(metadata, exception, f));
				else
					onSendCompletion(metadata, exception, f);
			}
		});
		return f;
	}

	@Override
	public CompletableFuture<List<RecordMetadata>> batchSend(List<ProducerRecord<K, V>> records,
			Optional<Executor> optExecutor) {
		@SuppressWarnings("unchecked")
		CompletableFuture<RecordMetadata>[] buff = new CompletableFuture[records.size()];
		int buffPos = 0;
		for (ProducerRecord<K, V> record : records) {
			buff[buffPos++] = this.send(record, optExecutor);
		}
		return CompletableFuture.allOf(buff).thenApply(v -> {
			List<RecordMetadata> result = new ArrayList<>(buff.length);
			for (CompletableFuture<RecordMetadata> f : buff) {
				try {
					// f.get() will success
					result.add(f.get());
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
			return result;
		});
	}

	public Future<RecordMetadata> send(ProducerRecord<K, V> record, Callback callback) {
		return producer.send(record, callback);
	}

	public List<PartitionInfo> partitionsFor(String topic) {
		return producer.partitionsFor(topic);
	}

	public Map<MetricName, ? extends Metric> metrics() {
		return producer.metrics();
	}

	@Override
	public void close(long arg0, TimeUnit arg1) {
		producer.close(arg0, arg1);
	}

	@Override
	public void flush() {
		producer.flush();
	}

	public void close() {
		producer.close();
	}

	@Override
	public Future<RecordMetadata> sendOldApi(ProducerRecord<K, V> record) {
		return producer.send(record);
	}

	@Override
	public void sendSync(ProducerRecord<K, V> record) throws InterruptedException, ExecutionException {
		producer.send(record).get();
	}

	private static void onSendCompletion(RecordMetadata metadata, Exception exception,
			CompletableFuture<RecordMetadata> promise) {
		if (metadata != null && exception == null) {
			promise.complete(metadata);// success
		} else if (exception != null && metadata == null) {
			promise.completeExceptionally(exception);// fail
		} else {
			promise.completeExceptionally(new AssertionError()); // impossible
		}
	}

}
