package com.spoonium.grid.model

import org.scalatest.FunSuite
import org.scalatest.Matchers._
import java.util.{UUID, Date}

class SeleniumTestSessionTest extends FunSuite {

  test("json serialization") {
    val obj = SeleniumTestSession("testId", Map("browser" -> "firefox"), Map("browser" -> "firefox", "version" -> "29"), "93b382dc-3e04-455c-8261-1c7ad550e21f", ("firefox", "29"), new Date(101))
    obj.toJsonString should be("""{"type":"SeleniumTestSession","id":"testId","capabilities":{"browser":"firefox"},"requestedCapabilities":{"browser":"firefox"},"nodeCapabilities":{"browser":"firefox","version":"29"},"startTime":101,"sessionId":"93b382dc-3e04-455c-8261-1c7ad550e21f","browser":"firefox","version":"29"}""")
  }
}
