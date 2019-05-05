package com.dvl.logParser.process

import java.sql.Date
import java.text.SimpleDateFormat

import com.dvl.logParser.avro.HostnameByTime
import com.dvl.logParser.kafka.{KafkaConnectionsByTimeConsumer, KafkaConnectionsByTimeResultProducer}
import com.typesafe.config.Config
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.apache.spark.sql._
import org.apache.spark.sql.functions._

import scala.collection.JavaConverters._

class ProcessRequest (config: Config, spark: SparkSession) {

  val format = new SimpleDateFormat("yyyy-MM-dd-HH")

  lazy val consumer = new KafkaConnectionsByTimeConsumer(
    config.getConfig("globalProperties"),
    config.getConfig("connectionsByTimeConsumer")
  )

  lazy val producer = new KafkaConnectionsByTimeResultProducer(
    config.getConfig("globalProperties"),
    config.getConfig("connectionsByTimeProducer")
  )

  lazy val inputLogData: DataFrame = spark.read.parquet("hdfs://hadoop:9000/data/input-log/topics/input-log-avro/")

  def process(): Unit = {
    while(true) {

      val requestHostnames: ConsumerRecords[Long, HostnameByTime] = consumer.poll()

      requestHostnames.iterator().asScala.foreach(

        requestHostnames => {

          val requestHostnameValue: HostnameByTime = requestHostnames.value()

          val hostnamesConnectedFiltered: Array[Row] =
            getRowsOfHostnamesConnected(inputLogData, requestHostnameValue)

          val hostnamesConnected: Set[CharSequence] = extractListFromRows(hostnamesConnectedFiltered)

          producer.sendMessage(requestHostnameValue, hostnamesConnected)
        }
      )
    }
  }

  private def extractListFromRows(rows: Array[Row]) : Set[CharSequence] = {
    rows.map(_.getSeq[CharSequence](0).toSet)
      .headOption
      .getOrElse(Set[CharSequence]())
  }

  private def getRowsOfHostnamesConnected(inputLogData : DataFrame, requestHostnameValue: HostnameByTime): Array[Row]= {
    inputLogData.where(buildFilters(requestHostnameValue))
      .groupBy("dest")
      .agg(
        collect_set("source").as("hostnamesConnected")
      ).select("hostnamesConnected")
      .collect()
  }

  private def buildFilters(requestHostnameValue: HostnameByTime) : Column = {
    filterDate(requestHostnameValue)
      .and(
        filterTime(requestHostnameValue)
      ).and(
      filterDest(requestHostnameValue)
    )
  }

  private def filterDate(requestHostnameValue: HostnameByTime) : Column = {
    col("date").between(
      format.format(new Date(requestHostnameValue.getInitDatetime)),
      format.format(new Date(requestHostnameValue.getEndDatetime)))
  }

  private def filterTime(requestHostnameValue: HostnameByTime) : Column = {
    col("time") between (requestHostnameValue.getInitDatetime, requestHostnameValue.getEndDatetime)
  }

  private def filterDest(requestHostnameValue: HostnameByTime) : Column = {
    col("dest") === requestHostnameValue.getHostname.toString
  }
}
