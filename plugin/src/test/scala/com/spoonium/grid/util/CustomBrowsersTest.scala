package com.spoonium.grid.util

import org.scalatest.FunSuite
import java.net.URLEncoder
import org.scalatest.Matchers._
import com.spoonium.grid.TestUtils._
import scala.Some

class CustomBrowsersTest extends FunSuite {

  test("parse params, urlecoded and not") {
    val input = "ie9=" + enc("edi/custom-ie-9") + ";firefox10=" + "edi/firefox-10"
    val cb = CustomBrowsers(input)
    cb.get("ie", "9") should be(Some(s"edi/custom-ie-9"))
    cb.get("ie", "10") should be(None)
  }

  test("overrides supplied via system props") {
    withSystemProperty("customBrowserOverrides", "ie13=edi/custom-ie-13") {
      CustomBrowsers.fromSystemProps().get("ie", "13") should be(Some("edi/custom-ie-13"))
    }
  }

  test("emptiness") {
    CustomBrowsers.fromSystemProps().map should be('empty)
  }

  test("handles duplicates with 'first key wins' strategy") {
    CustomBrowsers("firefox30=edi/another-firefox-30;firefox30=edi/custom-firefox").get("firefox", "30") should be(Some("edi/another-firefox-30"))
  }

  def enc(s: String) = URLEncoder.encode(s, "UTF-8")
}
