package com.dvl.logParser;


import com.dvl.logParser.avro.ArrayHostnames;
import com.dvl.logParser.avro.HostnameAggByHour;
import com.dvl.logParser.avro.HostnameAggByHourResult;
import com.dvl.logParser.avro.InputLog;
import com.dvl.logParser.ktables.HostnameAggByHourAgg;
import com.dvl.logParser.ktables.HostnamesDestAgg;
import com.dvl.logParser.ktables.HostnamesSourceAgg;
import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.streams.kstream.*;

import java.util.Collections;

public class Topology {


    private final KTable<Windowed<String>, ArrayHostnames> aggDest;
    private final KTable<Windowed<String>, ArrayHostnames> aggSource;
    private final KTable<Windowed<String>, HostnameAggByHourResult> aggHostnames;

    private final Serde<Windowed<String>> windowedStringSerde;
    private final SpecificAvroSerde<HostnameAggByHourResult> hostnamesAggByHourResultSerde;

    public Topology(String schemaRegistryUrl,
                    KStream<Long, InputLog> inputLogsStream,
                    KStream<Long, HostnameAggByHour> hostnameAggByHourStream) {

        HostnamesDestAgg hostnamesDestAgg = new HostnamesDestAgg(schemaRegistryUrl);
        this.aggDest = hostnamesDestAgg.initKTable(inputLogsStream);

        HostnamesSourceAgg hostnamesSourceAgg = new HostnamesSourceAgg(schemaRegistryUrl);
        this.aggSource = hostnamesSourceAgg.initKTable(inputLogsStream);

        HostnameAggByHourAgg hostnameAggByHourAgg = new HostnameAggByHourAgg();
        this.aggHostnames = hostnameAggByHourAgg.initKTable(hostnameAggByHourStream);

        this.windowedStringSerde = WindowedSerdes.timeWindowedSerdeFrom(String.class);
        this.windowedStringSerde.configure(Collections.singletonMap(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG,
                schemaRegistryUrl), true);

        this.hostnamesAggByHourResultSerde = new SpecificAvroSerde<>();
        this.hostnamesAggByHourResultSerde.configure(Collections.singletonMap(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG,
                schemaRegistryUrl), false);
    }

    public void initTopology(String hostnameAggByTimeResultTopic) {
        aggHostnames
                .leftJoin(aggDest,
                        (partialResult, hostConnected) -> {

                            partialResult.setHostConnected(
                                    // would be null for leftJoin
                                    hostConnected == null ? Collections.emptyList() : hostConnected.getHostReceived()
                            );
                            return partialResult;
                        }
                ).leftJoin(aggSource,
                (partialResult, connectToHosts) -> {
                    partialResult.setHostReceived(
                            // would be null for leftJoin
                            connectToHosts == null ? Collections.emptyList() : connectToHosts.getHostReceived()
                    );
                    return partialResult;
                })
                .toStream()
                .to(hostnameAggByTimeResultTopic, Produced.with(windowedStringSerde, hostnamesAggByHourResultSerde));
    }

}
