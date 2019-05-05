package com.dvl.logParser.config

import java.io.{File, FileOutputStream}

import com.typesafe.config.ConfigException
import org.scalatest.{FunSuite, Matchers}

class ConfigurationParserTest extends FunSuite with Matchers{

  test("parse correct config file") {

    val mockedFile = mockConfigFile()
    val configParsed = ConfigurationParser(Some(mockedFile.getAbsolutePath))

    configParsed.getString("configMock") shouldBe "mock"
  }

  test("parse correct config file with fallback") {

    val mockedFile = mockConfigFile()
    val configParsed = ConfigurationParser(Some(mockedFile.getAbsolutePath))

    configParsed.getString("configMock") shouldBe "mock"
    configParsed.getString("hostnameConnected.ConfigurationParserTest") shouldBe "withFallback"
  }

  test("parse incorrect config file with fallback") {

    val configParsed = ConfigurationParser(None)

    intercept[ConfigException](configParsed.getString("configMock"))
    configParsed.getString("hostnameConnected.ConfigurationParserTest") shouldBe "withFallback"
  }

  def mockConfigFile() : File = {
    val configMockFile: File = File.createTempFile("jar:file:/tmp/configMocked", ".conf")
    configMockFile.deleteOnExit()

    val fileOutFile: FileOutputStream = new FileOutputStream(configMockFile)

    val configMocked =
      """
        | {
        |   configMock: mock
        | }
      """.stripMargin
    fileOutFile.write(configMocked.getBytes)
    fileOutFile.close()

    configMockFile
  }
}
