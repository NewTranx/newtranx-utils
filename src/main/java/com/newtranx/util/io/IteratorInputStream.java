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

package com.newtranx.util.io;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.Optional;
import java.util.function.BiConsumer;

public class IteratorInputStream<E> extends InputStream {

    private final Iterator<E> iter;

    private final BiConsumer<E, ByteBuffer> encoder;

    private final ByteBuffer buffer;

    private final Optional<byte[]> sep;

    public IteratorInputStream(Iterator<E> iter, int bufSize, Optional<byte[]> sep, BiConsumer<E, ByteBuffer> encoder) {
        this.iter = iter;
        this.encoder = encoder;
        this.sep = sep;
        buffer = ByteBuffer.allocate(bufSize);
        buffer.flip();
    }

    @Override
    public int read() throws IOException {
        if (buffer.hasRemaining()) {
            return buffer.get() & 0xff;
        } else {
            if (iter.hasNext()) {
                buffer.clear();
                encoder.accept(iter.next(), buffer);
                if (iter.hasNext())
                    sep.ifPresent(buffer::put);
                buffer.flip();
                return buffer.get() & 0xff;
            } else {
                return -1;
            }
        }
    }

    public static class Builder<E> {
        private Iterator<E> iter;

        private BiConsumer<E, ByteBuffer> encoder;

        private int bufSize;

        private Optional<byte[]> sep = Optional.empty();

        public Builder<E> iterator(Iterator<E> v) {
            this.iter = v;
            return this;
        }

        public Builder<E> bufferSize(int v) {
            this.bufSize = v;
            return this;
        }

        public Builder<E> encoder(BiConsumer<E, ByteBuffer> v) {
            this.encoder = v;
            return this;
        }

        public Builder<E> separator(byte[] v) {
            this.sep = Optional.ofNullable(v);
            return this;
        }

        public IteratorInputStream<E> build() {
            return new IteratorInputStream(iter, bufSize, sep, encoder);
        }
    }

    public static <E> Builder<E> newBuilder(Iterator<E> iter) {
        return new Builder<E>().iterator(iter);
    }

}
