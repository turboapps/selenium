package com.spoonium.grid.model

import java.util.Date
import net.liftweb.json._
import net.liftweb.json.JsonDSL._

case class SeleniumTestSession(id: String,
                               reqCapabilities: Map[String, String], nodeCapabilities: Map[String, String],
                               sessionId: String,
                               nodeBrowserVersion: (String, String),
                               startDate: Date = new Date())
  extends SeleniumEvent {

  override def toJson = {

    implicit val formats = DefaultFormats

    ("type" -> "SeleniumTestSession") ~
      ("id" -> id) ~
      ("capabilities" -> reqCapabilities) ~ // deprecated
      ("requestedCapabilities" -> reqCapabilities) ~
      ("nodeCapabilities" -> nodeCapabilities) ~
      ("startTime" -> startDate.getTime) ~
      ("sessionId" -> sessionId) ~ // selenium session id
      ("browser" -> nodeBrowserVersion._1) ~
      ("version" -> nodeBrowserVersion._2)
  }
}


