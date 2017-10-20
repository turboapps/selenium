package com.spoonium.grid.util

import net.liftweb.json._

object JsonUtil {

  def toStringOpt(v: JValue): Option[String] = {
    v match {
      case JString(s) => Some(s)
      case _ => None
    }
  }

  def toStringOpt(json: String, variable: String): Option[String] = {
    toStringOpt(JsonParser.parse(json) \ variable)
  }

  def toStringOpt(json: String, path1: String, path2: String): Option[String] = {
    toStringOpt(JsonParser.parse(json) \ path1 \ path2)
  }
}
