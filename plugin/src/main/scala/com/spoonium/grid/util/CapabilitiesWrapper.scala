package com.spoonium.grid.util

import scala.collection.mutable

case class CapabilitiesWrapper(cap: mutable.Map[String, AnyRef]) {

  val browserNamesMapping = Map("internet explorer" -> "ie").withDefault(name => name)

  def browser = browserNamesMapping(cap("browserName").toString)
  def version = {
    cap.get("version") match {
      case Some(v) if !v.toString.trim.isEmpty => v.toString // specified version
      case _ => "" // any version
    }
  }

  def spoonImage = cap.get("spoon.image").map(_.toString)

  override def toString = cap.mkString(", ")
  // turn all values into their string representation
  def toStringsMap = cap.map({ case (k,v) => (k, v.toString)}).toMap
}
