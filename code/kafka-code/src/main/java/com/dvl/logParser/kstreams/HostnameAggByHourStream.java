package com.dvl.logParser.kstreams;

import com.dvl.logParser.avro.HostnameAggByHour;
import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;

public class HostnameAggByHourStream {

    private static final Logger logger = LoggerFactory.getLogger(HostnameAggByHourStream.class);

    final SpecificAvroSerde<HostnameAggByHour> hostnamesAggByHourSerde;

    public HostnameAggByHourStream(String schemaRegistryUrl) {

        this.hostnamesAggByHourSerde = new SpecificAvroSerde<>();
        this.hostnamesAggByHourSerde.configure(Collections.singletonMap(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG,
                schemaRegistryUrl), false);
    }

    public KStream<Long, HostnameAggByHour> initStream(StreamsBuilder builder, String hostnameAggByHourTopic) {
        logger.info("init stream for topic: " + hostnameAggByHourTopic);
        return builder.stream(hostnameAggByHourTopic, Consumed.with(Serdes.Long(), hostnamesAggByHourSerde));
    }

}
