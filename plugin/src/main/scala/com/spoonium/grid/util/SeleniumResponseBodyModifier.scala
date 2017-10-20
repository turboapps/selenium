package com.spoonium.grid.util

import net.liftweb.json._

/**
 * Responsible for transforming the raw selenium command response into something Spoon-compatible
 * An example: selenium returns a screenshot in each command failure response. We don't need it, so this component is responsible for discarding it
 *
 * @param rawResponse the raw selenium http response body (https://code.google.com/p/selenium/wiki/JsonWireProtocol)
 */
class SeleniumResponseBodyModifier(rawResponse: String) {

  def transformed(): String = {
    val parsed = parse(rawResponse)

    val result = parsed transform {
      case JField("screen", any) => JNothing
    }

    compactRender(result)
  }
}
