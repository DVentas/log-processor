package com.dvl.logParser.config

import java.io.File

import com.typesafe.config.{Config, ConfigFactory}
import org.slf4j.LoggerFactory

import scala.util.{Failure, Success, Try}

object ConfigurationParser {

  val log = LoggerFactory.getLogger(getClass)

  def apply(configFilePath : Option[String]): Config = {

      val config : Config = Try(new File(configFilePath.get)) match {
        case Success(file) => ConfigFactory
          .parseFile(file)
        case Failure(_) => {
          log.warn("Could not parse log, apply default config from reference.conf")
          ConfigFactory.empty()
        }
      }

    config
      .withFallback(ConfigFactory.parseResources("reference.conf"))
      .resolve()
  }

}
