package com.spoonium.grid.util

import javax.imageio.ImageIO
import java.io.{File, ByteArrayOutputStream, ByteArrayInputStream}
import com.google.common.io.BaseEncoding
import java.awt.image.BufferedImage

object ScreenshotUtils {

  def fromBase64(src: String): BufferedImage = {
    val buf = BaseEncoding.base64().decode(src)
    val in = new ByteArrayInputStream(buf)

    try {
      ImageIO.read(in)
    } finally {
      in.close()
    }
  }

  def fromBase64toFile(src: String, dest: File) {
    ImageIO.write(fromBase64(src), "png", dest)
  }

  def toBase64(src: BufferedImage): String = {
    val out = new ByteArrayOutputStream()
    try {
      ImageIO.write(src, "png", out)
      out.flush()
      BaseEncoding.base64().encode(out.toByteArray)
    } finally {
      out.close()
    }
  }
}
