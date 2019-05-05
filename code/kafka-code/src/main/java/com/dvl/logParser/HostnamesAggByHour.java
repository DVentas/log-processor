package com.dvl.logParser;

import com.dvl.logParser.kstreams.HostnameAggByHourStream;
import com.dvl.logParser.kstreams.InputLogStream;
import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig;
import io.confluent.kafka.streams.serdes.avro.GenericAvroSerde;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Properties;

public class HostnamesAggByHour {

    private static final Logger logger = LoggerFactory.getLogger(HostnamesAggByHour.class);

    private final static String AVRO_INPUT_LOGS = "input-log-avro";
    private final static String AVRO_HOSTNAME_AGG_BY_TIME = "hostname-aggregations-by-hour";
    private final static String AVRO_HOSTNAME_AGG_BY_TIME_OUT = "hostname-aggregations-by-hour-result";

    public static void main(final String[] args) {
        final String bootstrapServers = args.length > 0 ? args[0] : "broker:9092";
        final String schemaRegistryUrl = args.length > 1 ? args[1] : "http://schema-registry:8081";

        logger.info("Build topology");
        final KafkaStreams streams = topologyAggByHour(
                bootstrapServers,
                schemaRegistryUrl
        );

        logger.info("start streams");
        streams.start();

        // Add shutdown hook to respond to SIGTERM and gracefully close Kafka Streams
        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
    }

    private static KafkaStreams topologyAggByHour(final String bootstrapServers,
                                                  final String schemaRegistryUrl) {


        final StreamsBuilder builder = new StreamsBuilder();

        InputLogStream inputLogStream = new InputLogStream(schemaRegistryUrl);

        HostnameAggByHourStream hostnameAggByHourStream = new HostnameAggByHourStream(schemaRegistryUrl);

        Topology topology = new Topology(schemaRegistryUrl,
                inputLogStream.initStream(builder, AVRO_INPUT_LOGS),
                hostnameAggByHourStream.initStream(builder, AVRO_HOSTNAME_AGG_BY_TIME));

        logger.info("init topology");
        topology.initTopology(AVRO_HOSTNAME_AGG_BY_TIME_OUT);

        Properties streamProperties = buildStreamProperties(bootstrapServers, schemaRegistryUrl);

        return new
                KafkaStreams(builder.build(), streamProperties);
    }

    public static Properties buildStreamProperties(String bootstrapServers, String schemaRegistryUrl) {
        final Properties streamsConfiguration = new Properties();

        streamsConfiguration.put(StreamsConfig.APPLICATION_ID_CONFIG, "hostname-agg-by-hour");
        streamsConfiguration.put(StreamsConfig.CLIENT_ID_CONFIG, "hostname-agg-by-hour-client");
        streamsConfiguration.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        // Where to find the Confluent schema registry instance(s)
        streamsConfiguration.put(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl);

        streamsConfiguration.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, GenericAvroSerde.class);
        streamsConfiguration.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        streamsConfiguration.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        streamsConfiguration.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 100 * 1000);

        return streamsConfiguration;
    }

}
