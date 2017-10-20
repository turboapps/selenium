package com.spoonium.grid.reporting

import com.spoonium.grid.model.{SeleniumCommand, SeleniumEvent}
import java.io.File
import com.google.common.io.Files
import net.liftweb.json._
import com.google.common.base.Charsets
import scala.util.control.NonFatal

trait Reporter {
  def reportMessage(msg: SeleniumEvent)
}

class DiskFileReporter(path: String) extends Reporter {
  // make sure base directory exists
  new File(path).mkdirs()

  val file = new File(path, "SeleniumEventsReport.json")
  file.getParentFile.mkdirs()

  val screenshotsDir = new File(path, "screenshots")
  screenshotsDir.mkdirs()

  override def reportMessage(msg: SeleniumEvent) = {

    val event = msg match {
      case command: SeleniumCommand => command.withDiskSerializedScreenshot(screenshotsDir)
      case other => other
    }

    try {
      val string = compactRender(event.toJson) + "\r\n"
      Files.append(string, file, Charsets.UTF_8)
    } catch {
      case NonFatal(e) => println(s"Failed to append to report file $file")
    }
  }
}
