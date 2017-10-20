package com.spoonium.grid

object TestUtils {

  def withSystemProperty(name: String, value: String)(f: => Unit) = {
    val oldValue = System.getProperty(name)
    try {
      System.setProperty(name, value)
      f
    } finally {
      System.clearProperty(name)
      Option(oldValue).foreach { v => System.setProperty(name, v)}
    }
  }
}
