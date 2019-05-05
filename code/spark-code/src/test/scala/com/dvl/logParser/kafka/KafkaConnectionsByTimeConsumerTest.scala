package com.dvl.logParser.kafka

import java.time.Duration

import com.dvl.logParser.avro.HostnameByTime
import com.typesafe.config.{Config, ConfigFactory}
import io.confluent.kafka.serializers.KafkaAvroDeserializer
import org.apache.kafka.clients.consumer.{ConsumerConfig, KafkaConsumer}
import org.apache.kafka.common.serialization.Serdes
import org.mockito.Mockito
import org.scalatest.{FunSuite, Matchers}

class KafkaConnectionsByTimeConsumerTest extends FunSuite with Matchers{

  val bootstrapServers = "bootstrapServers"
  val offset = "earliest"

  val consumerMock = {
    val globalConfig = ConfigFactory.parseString(s"{bootstrapServers: $bootstrapServers}")
    val consumerConfig = ConfigFactory.parseString(s"{offset_config: $offset}")
    new KafkaConnectionsByTimeConsumer(globalConfig, consumerConfig) {
      override lazy val consumer: KafkaConsumer[Long, HostnameByTime] = Mockito.mock(classOf[KafkaConsumer[Long, HostnameByTime]])

      def getProperties = buildConsumerProperties()
    }
  }


  test("asset call poll of consumer with default duration") {
    consumerMock.poll()
    Mockito.verify(consumerMock.consumer).poll(Duration.ofSeconds(60))
  }

  test("asset call poll of consumer with duration") {
    consumerMock.poll(Duration.ofSeconds(30))
    Mockito.verify(consumerMock.consumer).poll(Duration.ofSeconds(30))
  }

  test("build correctly properties") {

    val consumerProperties = consumerMock.getProperties

    consumerProperties.getProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG) shouldBe bootstrapServers
    consumerProperties.getProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG) shouldBe offset
    consumerProperties.getProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG) shouldBe Serdes.Long.deserializer.getClass.getName
    consumerProperties.getProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG) shouldBe classOf[KafkaAvroDeserializer].getName
  }

}
