package com.dvl.logParser.kafka

import com.dvl.logParser.avro.{HostnameByTime, HostnameByTimeResult}
import com.typesafe.config.{Config, ConfigFactory}
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerConfig, ProducerRecord}
import org.mockito.Mockito
import org.scalatest.{FunSuite, Matchers}

class KafkaConnectionsByTimeProducerTest extends FunSuite with Matchers{

  val bootstrapServers = "bootstrapServers"
  val producerTopic = "producerTopic"

  val producerMock: KafkaProducer[HostnameByTime, HostnameByTimeResult] =
    Mockito.mock(classOf[KafkaProducer[HostnameByTime, HostnameByTimeResult]])

  val producerRecord : ProducerRecord[HostnameByTime, HostnameByTimeResult] =
    Mockito.mock(classOf[ProducerRecord[HostnameByTime, HostnameByTimeResult]])

  val connectionsByTimeProducerProducerMock = {
    val globalConfig = ConfigFactory.parseString(s"{bootstrapServers: $bootstrapServers}")
    val consumerConfig = ConfigFactory.parseString(s"{producerTopic: $producerTopic}")

    new KafkaConnectionsByTimeResultProducer(globalConfig, consumerConfig) {
      override lazy val producer: KafkaProducer[HostnameByTime, HostnameByTimeResult] = producerMock

      override def buildProducerRecord(key: HostnameByTime, message: Set[CharSequence]) = producerRecord
      def getProperties = buildProducerProperties()
    }
  }

  test("should send builded record") {
    connectionsByTimeProducerProducerMock.sendMessage(None.orNull, Set())

    Mockito.verify(producerMock).send(producerRecord)
  }

  test("build correctly properties") {

    val producerProperties = connectionsByTimeProducerProducerMock.getProperties

    producerProperties.getProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG) shouldBe bootstrapServers
  }

}


