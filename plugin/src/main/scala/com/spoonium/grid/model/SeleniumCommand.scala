package com.spoonium.grid.model

import java.io.File
import java.text.SimpleDateFormat
import java.util.Date

import com.spoonium.grid.util.ScreenshotUtils

case class SeleniumCommand(id: String,
                           sessionId: String,
                           method: String, uri: String,
                           requestBody: String, responseBody: Option[String],
                           duration: Option[Long],
                           sessionRelativeDuration: Option[Long],
                           screenshot: Option[String],
                           nodeBrowserVersion: (String, String))
 extends SeleniumEvent {
  def toJson = {
    import net.liftweb.json.JsonDSL._

    ("type" -> "SeleniumCommand") ~
    ("id" -> id) ~
    ("sessionId" -> sessionId) ~
    ("method" -> method) ~
    ("requestBody" -> requestBody) ~
    ("responseBody" -> responseBody) ~
    ("uri" -> uri) ~
    ("duration" -> duration) ~
    ("sessionRelativeDuration" -> sessionRelativeDuration) ~
    ("screenshot" -> screenshot) ~
    ("browser" -> nodeBrowserVersion._1) ~
    ("version" -> nodeBrowserVersion._2)
  }

  override def toString = s"id:$id, method:$method, uri:$uri, responseBody:${responseBody.isDefined}, duration:$duration, sessionRelativeDuration:$sessionRelativeDuration, screenshot:${screenshot.isDefined}"

  def withDiskSerializedScreenshot(screenshotsFolder: File): SeleniumCommand = {
    screenshot match {
      case None => this
      case Some(base64) => {
        val filename = new StringBuilder(new SimpleDateFormat("yyyyMMdd-HHmmss-S").format(new Date())).append(".png").toString()
        val dest = new File(screenshotsFolder, filename)
        ScreenshotUtils.fromBase64toFile(base64, dest)
        this.copy(screenshot = Some(filename))
      }
    }
  }
}
