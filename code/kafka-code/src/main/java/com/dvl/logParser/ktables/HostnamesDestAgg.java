package com.dvl.logParser.ktables;

import com.dvl.logParser.avro.ArrayHostnames;
import com.dvl.logParser.avro.InputLog;
import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.Grouped;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Windowed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class HostnamesDestAgg extends KTableCommons {

    private static final Logger logger = LoggerFactory.getLogger(HostnamesDestAgg.class);

    private final SpecificAvroSerde<ArrayHostnames> arrayHostnamesSerde;
    private final ArrayHostnames.Builder arrayHostnamesBuilder;

    public HostnamesDestAgg(String schemaRegistryUrl) {

        this.arrayHostnamesSerde = new SpecificAvroSerde<>();
        this.arrayHostnamesSerde.configure(Collections.singletonMap(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG,
                schemaRegistryUrl), false);

        this.arrayHostnamesBuilder = ArrayHostnames.newBuilder();
    }

    public KTable<Windowed<String>, ArrayHostnames> initKTable(KStream<Long, InputLog> inputLogsStream) {
        logger.info("init ktable HostnamesDestAgg");
        return inputLogsStream
                .map((k, v) -> {
                    ArrayHostnames a = arrayHostnamesBuilder.setHostReceived(Collections.singletonList(v.getSource())).build();
                    return new KeyValue<>(v.getDest().toString(), a);
                })
                .groupByKey(Grouped.with(Serdes.String(), arrayHostnamesSerde))
                .windowedBy(timeOfWindow())
                .reduce(
                        (previous, next) -> {
                            Set<CharSequence> hosts = new HashSet<>(previous.getHostReceived());
                            hosts.addAll(next.getHostReceived());
                            previous.setHostReceived(new ArrayList<>(hosts));
                            return previous;
                        });
    }
}
