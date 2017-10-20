package com.spoonium.grid.util

import scala.collection.mutable
import java.net.URLDecoder

case class CustomBrowsers(map: Map[String, String]) {
  def get(browser: String, version: String): Option[String] = {
    val key = s"$browser$version"
    map.get(key)
  }

  override def toString = map.mkString
}

object CustomBrowsers {
  val Empty = new CustomBrowsers(Map.empty)
  // Format: ie8=edi/custom-ie-8;firefox10=edi/firefox-10
  def apply(s: String) = {
    val mappings = s.split(";")
      .flatMap({ case fragment =>
        fragment.trim.split("=").toList match {
          case key :: value :: Nil => Some(key.trim -> URLDecoder.decode(value.trim, "UTF-8"))
          case _ => None
        }
      })

    new CustomBrowsers(toMapFirstKeyWins(mappings))
  }

  private def toMapFirstKeyWins(in: Seq[(String, String)]): Map[String, String] = {
    var result = Map[String, String]()
    in.foreach({ case (key, value) =>
      if(!result.contains(key)){
        result = result.updated(key, value)
      }
    })
    result
  }

  def applyOption(s: String) = try {
    Some(apply(s))
  } catch {
    case e: Exception => {
      println("Failed to parse custom browser overrides")
      e.printStackTrace()
      None
    }
  }
  
  def fromSystemProps() = {
    PluginProperties.customBrowserOverrides.flatMap(applyOption) getOrElse Empty
  }
}