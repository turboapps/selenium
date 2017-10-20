package com.spoonium.grid.model

import net.liftweb.json.JsonDSL._
import net.liftweb.json._

case class SeleniumTestSessionEnd(id: String,
                                  timedOut: Boolean,
                                  sessionId: String,
                                  nodeBrowserVersion: (String, String))
  extends SeleniumEvent {

  override def toJson = {

    implicit val formats = DefaultFormats

    ("type" -> "SeleniumTestSessionEnd") ~
    ("id" -> id) ~
    ("timedOut" -> timedOut) ~
    ("sessionId" -> sessionId) ~ // selenium session id
    ("browser" -> nodeBrowserVersion._1) ~
    ("version" -> nodeBrowserVersion._2)
  }
}
