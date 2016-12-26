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

import java.util.Map;
import java.util.function.Function;

import org.apache.kafka.common.serialization.Deserializer;

/**
 * Use https://github.com/confluentinc/schema-registry/blob/master/avro-serializer/src/main/java/io/confluent/kafka/serializers/KafkaAvroDeserializer.java
 */
@Deprecated
public class KafkaAvroDeserializerAdapter<T> implements Deserializer<T> {

    private final Function<byte[], T> decoder;

    public KafkaAvroDeserializerAdapter(Function<byte[], T> decoder) {
        this.decoder = decoder;
    }

    @Override
    public void close() {
        // nothing to close
    }

    @Override
    public void configure(Map<String, ?> arg0, boolean arg1) {
        // nothing to configure
    }

    @Override
    public T deserialize(String arg0, byte[] arg1) {
        return (T) this.decoder.apply(arg1);
    }

}
