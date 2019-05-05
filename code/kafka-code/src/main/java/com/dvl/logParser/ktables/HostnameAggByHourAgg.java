package com.dvl.logParser.ktables;

import com.dvl.logParser.avro.HostnameAggByHour;
import com.dvl.logParser.avro.HostnameAggByHourResult;
import com.dvl.logParser.avro.HostnameCount;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Windowed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;

public class HostnameAggByHourAgg extends KTableCommons {

    private static final Logger logger = LoggerFactory.getLogger(HostnameAggByHourAgg.class);

    private final HostnameAggByHourResult.Builder hostnameAggByHourResultBuilder;
    private final HostnameCount.Builder hostnameCountBuilder;

    public HostnameAggByHourAgg() {
        this.hostnameAggByHourResultBuilder = HostnameAggByHourResult.newBuilder();
        this.hostnameCountBuilder = HostnameCount.newBuilder();
    }

    public KTable<Windowed<String>, HostnameAggByHourResult> initKTable(KStream<Long, HostnameAggByHour> hostnameAggByHourStream) {
        logger.info("init ktable HostnameAggByHourAgg");
        return hostnameAggByHourStream
                .selectKey((k, v) -> v.getHostname().toString())
                .groupByKey()
                .windowedBy(timeOfWindow())
                // groupby only for windowed key, so, reduce is not necessary
                .reduce((a, b) -> a)
                .mapValues((k, v) -> hostnameAggByHourResultBuilder
                        .setHostnameRequest(v.getHostname())
                        .setHostConnected(Collections.emptyList())
                        .setHostReceived(Collections.emptyList())
                        .setHostWithMostConnections(hostnameCountBuilder
                                .setHostname("noHostnameCount")
                                .setCount(0L)
                                .build())
                        .build());
    }
}
