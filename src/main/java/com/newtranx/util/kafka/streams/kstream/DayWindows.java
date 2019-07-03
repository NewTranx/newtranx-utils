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

import org.apache.kafka.streams.kstream.Windows;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Created by luyi on 22/03/2017.
 */
public class DayWindows extends Windows<OffsetTimeWindow> {

    private static final long SIZE_MS = TimeUnit.DAYS.toMillis(1);

    private final Set<ZoneOffset> zones;

    private final long graceMs;

    public DayWindows(Set<ZoneOffset> zones, long graceMs) {
        this.zones = zones;
        this.graceMs = graceMs;
    }

    public static DayWindows forZones(ZoneOffset... zones) {
        return new DayWindows(new HashSet<>(Arrays.asList(zones)), 24 * 60 * 60 * 1000L /* one day */);
    }

    @Override
    public Map<Long, OffsetTimeWindow> windowsFor(long timestamp) {
        Instant instant = Instant.ofEpochMilli(timestamp);
        final Map<Long, OffsetTimeWindow> windows = new HashMap<>();
        for (ZoneOffset zone : this.zones) {
            OffsetDateTime day = instant.atOffset(zone).truncatedTo(ChronoUnit.DAYS);
            long windowStart = day.toInstant().toEpochMilli();
            OffsetTimeWindow window = new OffsetTimeWindow(windowStart, windowStart + SIZE_MS, zone);
            windows.put(windowStart, window);
        }
        return windows;
    }

    @Override
    public long size() {
        return SIZE_MS;
    }

    @Override
    public long gracePeriodMs() {
        return graceMs;
    }

    /**
     * {@inheritDoc}
     * <p>
     * For {@code TimeWindows} the maintain duration is at least as small as the window size.
     *
     * @return the window maintain duration
     */
    @Override
    public long maintainMs() {
        return Math.max(super.maintainMs(), SIZE_MS);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DayWindows that = (DayWindows) o;

        return zones.equals(that.zones);
    }

    @Override
    public int hashCode() {
        return zones.hashCode();
    }

}
