package com.dvl.logParser.kstreams;

import com.dvl.logParser.avro.InputLog;
import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;

public class InputLogStream {

    private static final Logger logger = LoggerFactory.getLogger(InputLogStream.class);

    private final SpecificAvroSerde<InputLog> inputSerde;

    public InputLogStream(String schemaRegistryUrl) {

        this.inputSerde = new SpecificAvroSerde<>();
        this.inputSerde.configure(
                Collections.singletonMap(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG,
                        schemaRegistryUrl), false);
    }

    public KStream<Long, InputLog> initStream(StreamsBuilder builder, String inputLogTopic) {
        logger.info("init stream for topic: " + inputLogTopic);
        return builder.stream(inputLogTopic, Consumed.with(Serdes.Long(), inputSerde));
    }

}
