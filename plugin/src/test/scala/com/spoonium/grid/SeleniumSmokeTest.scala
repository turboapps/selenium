package com.spoonium.grid

import org.scalatest.FunSuite
import org.openqa.selenium.remote.{DesiredCapabilities, RemoteWebDriver}
import java.net.URL

class SeleniumSmokeTest extends FunSuite {

  ignore("Start new selenium session") {
    val caps = new DesiredCapabilities()
    caps.setBrowserName("internet explorer")
    val driver = new RemoteWebDriver(new URL("http://localhost:4444/wd/hub"), caps)
    driver.get("http://google.com")
    driver.close()
    driver.quit()
  }
}
