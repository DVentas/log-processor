//package com.dvl.logParser.cleaninput;
//
//import org.apache.kafka.common.serialization.*;
//import org.apache.kafka.streams.StreamsBuilder;
//import org.apache.kafka.streams.StreamsConfig;
//import org.apache.kafka.streams.Topology;
//import org.apache.kafka.streams.TopologyTestDriver;
//import org.apache.kafka.streams.state.KeyValueStore;
//import org.apache.kafka.streams.state.Stores;
//import org.apache.kafka.streams.test.ConsumerRecordFactory;
//import org.apache.kafka.streams.test.OutputVerifier;
//import org.junit.After;
//import org.junit.Assert;
//import org.junit.Before;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.junit.runners.JUnit4;
//
//import java.util.Properties;
//
//@RunWith(JUnit4.class)
//public class CleanInputToAvroTopicTest {
//
//    private TopologyTestDriver testDriver;
//    private KeyValueStore<String, Long> store;
//
//    private StringDeserializer stringDeserializer = new StringDeserializer();
//    private LongDeserializer longDeserializer = new LongDeserializer();
//    private ConsumerRecordFactory<String, Long> recordFactory = new ConsumerRecordFactory<>(new StringSerializer(), new LongSerializer());
//
//    @Before
//    public void setup() {
//        Topology topology = new Topology();
//        topology.addSource("sourceProcessor", "input-topic");
//        topology.addProcessor("aggregator", CleanInputToAvroTopic.buildCSVToAvroStream("dummy:1234", "dummy:1234"), "sourceProcessor");
//        topology.addStateStore(
//                Stores.keyValueStoreBuilder(
//                        Stores.inMemoryKeyValueStore("aggStore"),
//                        Serdes.String(),
//                        Serdes.Long()).withLoggingDisabled(), // need to disable logging to allow store pre-populating
//                "aggregator");
//        topology.addSink("sinkProcessor", "result-topic", "aggregator");
//
//        // setup test driver
//        Properties config = new Properties();
//        config.setProperty(StreamsConfig.APPLICATION_ID_CONFIG, "maxAggregation");
//        config.setProperty(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "dummy:1234");
//        config.setProperty(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
//        config.setProperty(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.Long().getClass().getName());
//        testDriver = new TopologyTestDriver(topology, config);
//
//        // pre-populate store
//        store = testDriver.getKeyValueStore("aggStore");
//        store.put("a", 21L);
//    }
//
//    @After
//    public void tearDown() {
//        testDriver.close();
//    }
//
//    @Test
//    public void testBadRecordEmptyTime() {
//        String badRecord = "host1 host2";
//        Assert.assertTrue(CleanInputToAvroTopic.isBadRecord(badRecord));
//    }
//
//    @Test
//    public void testTopology() {
//        testDriver.pipeInput(recordFactory.create("input-topic", "a", 1L, 9999L));
//        OutputVerifier.compareKeyValue(testDriver.readOutput("result-topic", stringDeserializer, longDeserializer), "a", 21L);
//        Assert.assertNull(testDriver.readOutput("result-topic", stringDeserializer, longDeserializer));
//
//    }
//}