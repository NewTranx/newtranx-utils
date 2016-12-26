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

package com.newtranx.util;

import java.nio.ByteBuffer;
import java.util.UUID;

public class UUIDUtils {

    public static byte[] toBytes(UUID uuid) {
        ByteBuffer bb = ByteBuffer.allocate(128 / 8);
        writeToByteBuffer(uuid, bb);
        return bb.array();
    }

    public static ByteBuffer toByteBuffer(UUID uuid) {
        ByteBuffer bb = ByteBuffer.allocate(128 / 8);
        writeToByteBuffer(uuid, bb);
        bb.flip();
        return bb;
    }

    public static void writeToByteBuffer(UUID uuid, ByteBuffer bb) {
        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());
    }

    public static UUID fromBytes(byte[] bytes) {
        ByteBuffer bb = ByteBuffer.wrap(bytes);
        long most = bb.getLong();
        long least = bb.getLong();
        return new UUID(most, least);
    }

    public static UUID fromByteBuffer(ByteBuffer bb) {
        long most = bb.getLong();
        long least = bb.getLong();
        return new UUID(most, least);
    }

    public static UUID fromByteBufferWithReset(ByteBuffer bb) {
        bb.mark();
        try {
            long most = bb.getLong();
            long least = bb.getLong();
            return new UUID(most, least);
        } finally {
            bb.reset();
        }
    }

    public static byte[] fromStringToBytes(String uuidInStr) {
        UUID uuid = UUID.fromString(uuidInStr);
        return toBytes(uuid);
    }

    public static ByteBuffer fromStringToByteBuffer(String uuidInStr) {
        UUID uuid = UUID.fromString(uuidInStr);
        return toByteBuffer(uuid);
    }

}
