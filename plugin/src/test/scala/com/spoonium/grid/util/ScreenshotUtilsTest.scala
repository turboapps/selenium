package com.spoonium.grid.util

import org.scalatest.{Matchers, FunSuite}
import java.io.File
import javax.imageio.ImageIO
import com.spoonium.grid.test.ScreenshotTestHelpers

class ScreenshotUtilsTest extends FunSuite with Matchers with ScreenshotTestHelpers {

  test("base64 to file") {
    val tempFile = File.createTempFile("screenshot-utils-test", ".png")
    try {
      ScreenshotUtils.fromBase64toFile(testBase64Image, tempFile)
      ImageIO.read(tempFile).getWidth should be(testImageWidth)
    } finally {
      tempFile.delete()
    }
  }
  
}
