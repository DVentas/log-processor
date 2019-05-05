package com.dvl.testat

import cucumber.api.CucumberOptions
import cucumber.api.junit.Cucumber
import org.junit.runner.RunWith

@RunWith(classOf[Cucumber])
@CucumberOptions(
  features = Array("classpath:acceptanceTests/"),
  plugin = Array("pretty", "json:target/cucumber.json")
)
object  RunCukesTest