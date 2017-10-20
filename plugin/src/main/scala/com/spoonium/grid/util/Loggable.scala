package com.spoonium.grid.util

import java.util.logging.{Level, Logger}


trait Loggable {

  val logger = Logger.getLogger("")

  def loggerSevere(msg: String, ex: Throwable) = {
    logger.log(Level.SEVERE, msg, ex)
  }
}
