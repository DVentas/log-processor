package com.dvl.logParser.ktables;

import org.apache.kafka.streams.kstream.TimeWindows;

import java.time.Duration;

abstract class KTableCommons {

    protected static TimeWindows timeOfWindow() {
        return TimeWindows.of(Duration.ofHours(1))
                .grace(Duration.ofMinutes(5));
    }
}
