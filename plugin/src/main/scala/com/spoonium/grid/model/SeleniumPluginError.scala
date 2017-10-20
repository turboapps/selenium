package com.spoonium.grid.model

case class SeleniumPluginError(code: Int, message: String) extends SeleniumEvent {
  def toJson = {
    import net.liftweb.json.JsonDSL._

    ("type" -> "SeleniumPluginError") ~
    ("contents" -> message) ~
    ("code" -> code)
  }
}
