/*
 * Copyright 2017 NewTranx Co. Ltd.
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

package com.newtranx.util.kafka.streams.serde.avro;

import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.Serializer;

import java.util.Comparator;
import java.util.Map;
import java.util.PriorityQueue;

/**
 * Note: This serde is not functional yet because `PriorityQueueSerializer` and
 * `PriorityQueueDeserializer` are not functional in turn.
 */
public class PriorityQueueSerde<T> implements Serde<PriorityQueue<T>> {

  private final Serde<PriorityQueue<T>> inner;

  /**
   * Constructor used by Kafka Streams.
   * @param comparator
   * @param avroSerde
   */
  public PriorityQueueSerde(final Comparator<T> comparator, final Serde<T> valueSerde) {
    inner = Serdes.serdeFrom(new PriorityQueueSerializer<>(comparator, valueSerde.serializer()),
                             new PriorityQueueDeserializer<>(comparator, valueSerde.deserializer()));
  }

  @Override
  public Serializer<PriorityQueue<T>> serializer() {
    return inner.serializer();
  }

  @Override
  public Deserializer<PriorityQueue<T>> deserializer() {
    return inner.deserializer();
  }

  @Override
  public void configure(Map<String, ?> configs, boolean isKey) {
    inner.serializer().configure(configs, isKey);
    inner.deserializer().configure(configs, isKey);
  }

  @Override
  public void close() {
    inner.serializer().close();
    inner.deserializer().close();
  }

}