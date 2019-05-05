package com.dvl.testat.steps

import java.time.Duration
import java.util
import java.util.{Collections, Properties}

import com.dvl.logParser.avro.{HostnameByTime, HostnameByTimeResult}
import cucumber.api.scala.{EN, ScalaDsl}
import io.confluent.kafka.serializers.{AbstractKafkaAvroSerDeConfig, KafkaAvroDeserializer}
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde
import org.apache.kafka.clients.consumer.{ConsumerConfig, KafkaConsumer}
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerConfig, ProducerRecord}
import org.apache.kafka.common.serialization.{Serdes, Serializer}
import org.scalatest.Matchers

import scala.collection.JavaConverters._

class HostnameByTimeSteps extends Matchers with ScalaDsl with EN {

  val bootstrapServers: String = "broker:29092"

  val serdeConfig: util.Map[String, String] =
    Collections.singletonMap(
      AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, "http://schema-registry:8081")

  val offsetMessage = 12345678L

  val consumer : KafkaConsumer[HostnameByTime, HostnameByTimeResult] = buildConsumer()

  val request = HostnameByTime
    .newBuilder()
    .setHostname("Davionne")
    .setInitDatetime(1565647260788L)
    .setEndDatetime(1565733598341L)
    .build()

  var response : Option[HostnameByTimeResult] = None

  Given("""^a consumer listenning (.*) topic$""") {
    (topic : String) =>

      consumer.subscribe(Collections.singleton(topic))
  }

  When("""^send message to (.*) with request$"""){
    (topic : String) =>

      val hostnameByTimeResultSerde: SpecificAvroSerde[HostnameByTime] = new SpecificAvroSerde[HostnameByTime]
      hostnameByTimeResultSerde.configure(serdeConfig, false)

      val hostnameByTimeSerde: SpecificAvroSerde[HostnameByTime] = new SpecificAvroSerde[HostnameByTime]
      hostnameByTimeSerde.configure(serdeConfig, false)

      val producer = new KafkaProducer[Long, HostnameByTime](
        buildProducerProperties(),
        Serdes.Long.serializer.asInstanceOf[Serializer[Long]],
        hostnameByTimeSerde.serializer)

      producer.send(
        new ProducerRecord[Long, HostnameByTime](topic, offsetMessage, request)
      )
  }

  Then("""^should receive message in 10 seconds$"""){
    (seconds:Int) =>
      val messages = consumer.poll(Duration.ofSeconds(10))

      response = messages
        .iterator()
        .asScala
        .filter(_.key().equals(request))
        .map(_.value())
        .toSeq
        .lastOption
  }

  And("""^response should have correct data""") {
    () =>

      response match {
        case Some(hostnameResult) => {
          hostnameResult.getHostconnected.size() shouldBe 10

          val expectedResults = Seq("Dianny", "Torie", "Arquan", "Yhadira", "Masaya", "Vinh", "Dylanthomas", "Edras", "Rufus", "Jazzminn")
          val result = hostnameResult.getHostconnected.asScala.map(_.toString)

          expectedResults.forall(result.contains(_)) shouldBe true
        }
        case None => fail()
      }
  }

  protected def buildConsumer () : KafkaConsumer[HostnameByTime, HostnameByTimeResult] = {
    val hostnameByTimeSerde: SpecificAvroSerde[HostnameByTime] = new SpecificAvroSerde[HostnameByTime]
    hostnameByTimeSerde.configure(serdeConfig, false)

    val hostnameByTimeResultSerde: SpecificAvroSerde[HostnameByTimeResult] = new SpecificAvroSerde[HostnameByTimeResult]
    hostnameByTimeResultSerde.configure(serdeConfig, false)

    new KafkaConsumer[HostnameByTime, HostnameByTimeResult](
      buildConsumerProperties(),
      hostnameByTimeSerde.deserializer,
      hostnameByTimeResultSerde.deserializer
    )
  }

  protected def buildConsumerProperties(): Properties = {
    val consumerProps: Properties = new Properties
    consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers)
    consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, "consumer-hostnames-by-time-test-at")
    consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
    consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, classOf[KafkaAvroDeserializer].getName)
    consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, classOf[KafkaAvroDeserializer].getName)
    consumerProps
  }

  protected def buildProducerProperties() : Properties = {
    val producerProperties: Properties = new Properties
    producerProperties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers)
    producerProperties
  }

}
