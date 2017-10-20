package com.spoonium.grid.reporting

import org.scalatest.{Matchers, FeatureSpec}
import com.spoonium.grid.model.SeleniumCommand
import com.spoonium.grid.test.ScreenshotTestHelpers
import java.io.File
import net.spoon.common.io.TempDirectory
import com.spoonium.grid.util.StringHelpers
import scala.util.Try

class DiskFileReporterTest extends FeatureSpec with ScreenshotTestHelpers with Matchers {

  feature("Report selenium events to a disk file") {
    scenario("Event contains embedded base64 screenshot") {

      val command = new SeleniumCommand("/foo/bar/bla", sessionId = StringHelpers.randomString(11),
        method = "GET", uri = "/foo/bar/bla", requestBody = "dummy", responseBody = None, duration = None, sessionRelativeDuration = None,
        screenshot = Some(testBase64Image),
        ("firefox", "101")
      )

      usingTempFolder { tmp =>
        val reporter = new DiskFileReporter(tmp.getAbsolutePath)
        reporter.reportMessage(command)

        val screenshotsDir = new File(tmp, "screenshots")
        val screenshotFilenames = Option(screenshotsDir.list()).toSeq.flatten // because of null when empty

        screenshotFilenames.size should be(1)
        screenshotFilenames.head should endWith(".png")

        val reportFile = new File(tmp, "SeleniumEventsReport.json")
        reportFile.exists() should be(true)
      }
    }
  }

  def usingTempFolder(block: File => Unit) {
    val tempFolder = TempDirectory()
    try {
      block(tempFolder.tempDir)
    } finally {
      Try(tempFolder.dispose())
    }
  }
}
