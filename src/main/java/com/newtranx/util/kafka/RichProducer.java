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

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;

import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

public interface RichProducer<K, V> extends Producer<K, V> {

    /**
     * Complete the future asynchronously in the given executor
     */
    CompletableFuture<RecordMetadata> send(ProducerRecord<K, V> record, Optional<Executor> optExecutor);

    default CompletableFuture<RecordMetadata> send(ProducerRecord<K, V> record) {
        return send(record, Optional.empty());
    }

    default CompletableFuture<RecordMetadata> send(ProducerRecord<K, V> record, Executor executor) {
        if (executor == null)
            throw new NullPointerException("executor must not be null");
        return send(record, Optional.of(executor));
    }

    Future<RecordMetadata> sendOldApi(ProducerRecord<K, V> record);

    void sendSync(ProducerRecord<K, V> record) throws InterruptedException, ExecutionException;

    CompletableFuture<List<RecordMetadata>> batchSend(List<ProducerRecord<K, V>> records,
                                                      Optional<Executor> optExecutor);

    default CompletableFuture<List<RecordMetadata>> batchSend(List<ProducerRecord<K, V>> records) {
        return batchSend(records, Optional.empty());
    }

    default CompletableFuture<List<RecordMetadata>> batchSend(List<ProducerRecord<K, V>> records, Executor executor) {
        if (executor == null)
            throw new NullPointerException("executor must not be null");
        return batchSend(records, Optional.of(executor));
    }

    static <K, V> RichProducer<K, V> wrap(Producer<K, V> producer) {
        return new RichProducerImpl<K, V>(producer);
    }

}
