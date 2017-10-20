package com.spoonium.grid.util

import org.scalatest.{Matchers, FunSuite}

class PluginPropertiesTest extends FunSuite with Matchers {

  test("defaults stay the same as out-of-the-box selenium timeouts") {
    val args = Seq()
    PluginProperties.Selenium.timeout(args) should be("300")
    PluginProperties.Selenium.browserTimeout(args) should be("0")
  }

  test("selenium timeouts can be configured via cli arguments") {
    val args = Seq("-Dselenium.timeout=40", "-Dselenium.browserTimeout=90")
    PluginProperties.Selenium.timeout(args) should be("40")
    PluginProperties.Selenium.browserTimeout(args) should be("90")
  }
}
