package com.dvl.logParser.kafka

import java.time.Duration
import java.util
import java.util.{Collections, Properties}

import com.dvl.logParser.avro.HostnameByTime
import com.typesafe.config.Config
import io.confluent.kafka.serializers.{AbstractKafkaAvroSerDeConfig, KafkaAvroDeserializer}
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde
import org.apache.kafka.clients.consumer.{ConsumerConfig, ConsumerRecords, KafkaConsumer}
import org.apache.kafka.common.serialization.{Deserializer, Serdes}

class KafkaConnectionsByTimeConsumer(val globalConfig: Config, val consumerConfig: Config) {


  lazy val consumer: KafkaConsumer[Long, HostnameByTime] = buildConsumer()

  def poll(duration: Duration = Duration.ofSeconds(60)): ConsumerRecords[Long, HostnameByTime] = {
    consumer.poll(duration)
  }

  protected def buildConsumer() : KafkaConsumer[Long, HostnameByTime] = {
    val serdeConfig: util.Map[String, String] =
      Collections.singletonMap(
        AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, globalConfig.getString("schemaRegistryUrl"))

    val hostnameByTimeSerde: SpecificAvroSerde[HostnameByTime] = new SpecificAvroSerde[HostnameByTime]
    hostnameByTimeSerde.configure(serdeConfig, false)

    val consumer = new KafkaConsumer[Long, HostnameByTime](
      buildConsumerProperties(),
      Serdes.Long().deserializer.asInstanceOf[Deserializer[Long]],
      hostnameByTimeSerde.deserializer
    )
    consumer.subscribe(Collections.singleton(consumerConfig.getString("consumerTopic")))
    
    consumer
  }

  protected def buildConsumerProperties(): Properties = {
    val consumerProps: Properties = new Properties
    consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, globalConfig.getString("bootstrapServers"))
    consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, "consumer-log-input-connected-by-time")
    consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, consumerConfig.getString("offset_config"))
    consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, Serdes.Long.deserializer.getClass.getName)
    consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, classOf[KafkaAvroDeserializer].getName)
    consumerProps
  }

}
