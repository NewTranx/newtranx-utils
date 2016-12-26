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

import com.newtranx.util.kafka.RichProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

import java.util.concurrent.CompletableFuture;

/**
 * Created by luyi on 18/11/2016.
 */
public class KafkaAsyncResponseUtils {

    private KafkaAsyncResponseUtils() {

    }

    public static <T> CompletableFuture<RecordMetadata> sendReply(String dst, T msg, RichProducer<String, T> producer) {
        String[] dstArr = dst.trim().split("/");
        String topic = dstArr[0];
        int partition = Integer.parseInt(dstArr[1]);
        String key = dstArr[2];
        ProducerRecord<String, T> record = new ProducerRecord<>(topic, partition, key, msg);
        return producer.send(record);
    }

}
