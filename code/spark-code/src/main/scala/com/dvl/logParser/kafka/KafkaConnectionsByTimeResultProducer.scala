package com.dvl.logParser.kafka

import java.util
import java.util.{Collections, Properties}

import com.dvl.logParser.avro.{HostnameByTime, HostnameByTimeResult}
import com.typesafe.config.Config
import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerConfig, ProducerRecord}

import scala.collection.JavaConverters._

class KafkaConnectionsByTimeResultProducer(val globalConfig: Config, val producerConfig: Config) {

  lazy val producer: KafkaProducer[HostnameByTime, HostnameByTimeResult] = buildProducer()

  lazy val topic = producerConfig.getString("producerTopic")

  val resultBuilder = HostnameByTimeResult.newBuilder

  def sendMessage(key: HostnameByTime, message: Set[CharSequence]): Unit = {
    producer.send(
      buildProducerRecord(key, message)
    )
  }

  protected def buildProducerRecord(key: HostnameByTime, message: Set[CharSequence])
      : ProducerRecord[HostnameByTime, HostnameByTimeResult] = {

    val newResult = resultBuilder.setHostconnected(message.toList.asJava).build()

    new ProducerRecord[HostnameByTime, HostnameByTimeResult](topic, key, newResult)
  }

  protected def buildProducer() : KafkaProducer[HostnameByTime, HostnameByTimeResult]= {
    val serdeConfig: util.Map[String, String] =
      Collections.singletonMap(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, globalConfig.getString("schemaRegistryUrl"))

    val hostnameByTimeResultSerde: SpecificAvroSerde[HostnameByTimeResult] = new SpecificAvroSerde[HostnameByTimeResult]
    hostnameByTimeResultSerde.configure(serdeConfig, false)

    val hostnameByTimeSerde: SpecificAvroSerde[HostnameByTime] = new SpecificAvroSerde[HostnameByTime]
    hostnameByTimeSerde.configure(serdeConfig, false)

    new KafkaProducer(buildProducerProperties(), hostnameByTimeSerde.serializer, hostnameByTimeResultSerde.serializer)
  }

  protected def buildProducerProperties() : Properties = {
    val producerProperties: Properties = new Properties
    producerProperties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, globalConfig.getString("bootstrapServers"))
    producerProperties
  }
}
