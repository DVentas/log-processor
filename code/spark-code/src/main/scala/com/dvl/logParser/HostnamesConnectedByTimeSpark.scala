package com.dvl.logParser

import com.dvl.logParser.config.ConfigurationParser
import com.dvl.logParser.process.ProcessRequest
import org.apache.spark.sql.SparkSession

object HostnamesConnectedByTimeSpark {

  def main(args: Array[String]) = {

    val config = ConfigurationParser(args.headOption)

    val spark = SparkSession
      .builder()
      .appName("HostnamesConnectedByTime")
      .getOrCreate()

    new ProcessRequest(config.getConfig("hostnameConnected"), spark)
        .process()
  }

}