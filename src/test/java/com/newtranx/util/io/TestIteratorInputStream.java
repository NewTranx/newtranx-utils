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

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.assertEquals;

public class TestIteratorInputStream {

    Charset cs = Charset.forName("utf8");

    @Test
    public void testString() throws IOException {
        List<String> list = Collections.singletonList("abc");
        IteratorInputStream<String> is = IteratorInputStream.newBuilder(list.iterator())
                .bufferSize(10240)
                .encoder((s, buf) -> buf.put(s.getBytes(cs)))
                .build();

        String abc = IOUtils.toString(is, cs);
        assertEquals("abc", abc);
    }

    @Test
    public void testRndString() throws IOException {
        Random rnd = new Random();
        int size = rnd.nextInt(100);
        List<String> list = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            list.add(RandomStringUtils.randomAlphabetic(rnd.nextInt(1000) + 1));
        }
        IteratorInputStream<String> is = IteratorInputStream.newBuilder(list.iterator())
                .bufferSize(10240)
                .encoder((s, buf) -> buf.put(s.getBytes(cs)))
                .separator("\n".getBytes(cs))
                .build();
        BufferedReader br = new BufferedReader(new InputStreamReader(is, cs));

        int i = 0;
        String line;
        while ((line = br.readLine()) != null) {
            assertEquals(list.get(i), line);
            i++;
        }
    }

}
