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

package com.newtranx.util.kafka.streams.kstream;

import org.apache.kafka.streams.kstream.internals.TimeWindow;

import java.time.ZoneOffset;

/**
 * TimeWindow which stores time zone information.
 * <p>
 * Created by luyi on 22/03/2017.
 */
public class OffsetTimeWindow extends TimeWindow {

    private final ZoneOffset zoneOffset;

    /**
     * Create a new window for the given start time (inclusive) and end time (exclusive).
     *
     * @param startMs the start timestamp of the window (inclusive)
     * @param endMs   the end timestamp of the window (exclusive)
     * @throws IllegalArgumentException if {@code startMs} is negative or if {@code endMs} is smaller than or equal to
     *                                  {@code startMs}
     */
    public OffsetTimeWindow(long startMs, long endMs, ZoneOffset zoneOffset) throws IllegalArgumentException {
        super(startMs, endMs);
        this.zoneOffset = zoneOffset;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        OffsetTimeWindow that = (OffsetTimeWindow) o;

        return zoneOffset.equals(that.zoneOffset);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + zoneOffset.hashCode();
        return result;
    }
    
}
