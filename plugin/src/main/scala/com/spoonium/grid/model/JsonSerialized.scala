package com.spoonium.grid.model

import net.liftweb.json._

trait JsonSerialized {

  def toJson: JValue
  
  def toJsonString = {
    import net.liftweb.json._

    compactRender(toJson)
  }
}
