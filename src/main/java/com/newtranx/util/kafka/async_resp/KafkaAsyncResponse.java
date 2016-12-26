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

import org.apache.kafka.common.TopicPartition;

import java.util.concurrent.CompletionStage;

/**
 * Created by luyi on 30/10/2016.
 */
public interface KafkaAsyncResponse<V> {

    TopicPartition channel();

    String key();

    /**
     * @return "{topic}/{partition}/{key}"
     */
    default String destination() {
        return String.format("%s/%d/%s", channel().topic(), channel().partition(), key());
    }

    CompletionStage<V> future();

    boolean cancel();

    boolean cancel(Throwable t);

}
