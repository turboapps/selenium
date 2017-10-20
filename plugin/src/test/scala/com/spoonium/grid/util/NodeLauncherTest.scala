package com.spoonium.grid.util

import org.scalatest.FeatureSpec
import org.scalatest.Matchers._
import scala.collection.mutable
import scala.util.Properties

class NodeLauncherTest extends FeatureSpec {

  val seleniumGridVersion = Properties.envOrElse("SELENIUM_CONTAINER_VER", "head")
  val currentPid = ProcessUtils.currentPid().toString

  feature("Regular browsers") {
    scenario("Launching ie") {
      val actual = launchWith(mutable.Map[String, AnyRef](
        "browserName" -> "ie",
        "version" -> "11"
      ))

      val expected = Seq("cmd.exe", "/C", "start", "/I", "/wait", "\"Selenium Node - Ie 11\"", "spoon.exe", "try", "--attach",
        s"base,selenium/ie-selenium:11,oracle/jre-core:8.25,selenium/selenium-ie-driver,selenium/selenium-grid-node:$seleniumGridVersion",
        "node", "ie", "11", currentPid)

      actual should be(expected)
    }

    scenario("Launching chrome") {
      val actual = launchWith(mutable.Map[String, AnyRef](
        "browserName" -> "chrome",
        "version" -> "40"
      ))

      val expected = Seq("cmd.exe", "/C", "start", "/I", "/wait", "\"Selenium Node - Chrome 40\"", "spoon.exe", "try", "--attach",
        s"base,google/chrome-base:40,oracle/jre-core:8.25,selenium/selenium-chrome-driver,selenium/selenium-grid-node:$seleniumGridVersion",
        "node", "chrome", "40", currentPid)

      actual should be(expected)
    }

    scenario("Launching latest chrome") {
      val actual = launchWith(mutable.Map[String, AnyRef](
        "browserName" -> "chrome"
      ))

      val expected = Seq("cmd.exe", "/C", "start", "/I", "/wait", "\"Selenium Node - Chrome\"", "spoon.exe", "try", "--attach",
        s"base,google/chrome-base,oracle/jre-core:8.25,selenium/selenium-chrome-driver,selenium/selenium-grid-node:$seleniumGridVersion",
        "node", "chrome", "", currentPid)

      actual should be(expected)
    }

    scenario("Launching firefox") {
      val actual = launchWith(mutable.Map[String, AnyRef](
        "browserName" -> "firefox",
        "version" -> "28"
      ))

      val expected = Seq("cmd.exe", "/C", "start", "/I", "/wait", "\"Selenium Node - Firefox 28\"", "spoon.exe", "try", "--attach",
        s"base,mozilla/firefox-base:28,oracle/jre-core:8.25,selenium/selenium-gecko-driver,selenium/selenium-grid-node:$seleniumGridVersion",
        "node", "firefox", "28", currentPid)

      actual should be(expected)
    }
  }

  feature("Custom browsers") {
    scenario("Launching custom ie with override specified in test script capabilities") {
      val actual = launchWith(mutable.Map[String, AnyRef](
        "browserName" -> "ie",
        "version" -> "10",
        "spoon.image" -> "edi/custom-ie-10"
      ))

      val expected = Seq("cmd.exe", "/C", "start", "/I", "/wait", "\"Selenium Node - Ie 10 edi/custom-ie-10\"", "spoon.exe", "try", "--attach",
        s"base,edi/custom-ie-10,ie-selenium-shims:11,oracle/jre-core:8.25,selenium/selenium-ie-driver,selenium/selenium-grid-node:$seleniumGridVersion",
        "node", "ie", "10", currentPid)

      actual should be(expected)
    }

    scenario("Launching custom chrome or firefox with override specified in test script capabilities") {
      val actual = launchWith(mutable.Map[String, AnyRef](
        "browserName" -> "firefox",
        "version" -> "30",
        "spoon.image" -> "edi/custom-ff-30"
      ))

      val expected = Seq("cmd.exe", "/C", "start", "/I", "/wait", "\"Selenium Node - Firefox 30 edi/custom-ff-30\"", "spoon.exe", "try", "--attach",
        s"base,edi/custom-ff-30,oracle/jre-core:8.25,selenium/selenium-gecko-driver,selenium/selenium-grid-node:$seleniumGridVersion",
        "node", "firefox", "30", currentPid)

      actual should be(expected)
    }

    scenario("Launching custom firefox which overrides default one, as defined in the /selenium page overrides, not test script") {
      val actual = launchWith(mutable.Map[String, AnyRef](
        "browserName" -> "firefox",
        "version" -> "30"
      ), CustomBrowsers.apply("firefox30=edi/custom-ff-30"))

      val expected = Seq("cmd.exe", "/C", "start", "/I", "/wait", "\"Selenium Node - Firefox 30 edi/custom-ff-30\"", "spoon.exe", "try", "--attach",
        s"base,edi/custom-ff-30,oracle/jre-core:8.25,selenium/selenium-gecko-driver,selenium/selenium-grid-node:$seleniumGridVersion",
        "node", "firefox", "30", currentPid)

      actual should be(expected)
    }
  }
  
  def launchWith(caps: mutable.Map[String, AnyRef], cb: CustomBrowsers = CustomBrowsers.Empty) = {
    val launcher = new TestableNodeLauncher(new CapabilitiesWrapper(caps), cb)
    launcher.launch()
    launcher.command.getOrElse(throw new RuntimeException("No launch command found, node launch failed"))
  }
}

class TestableNodeLauncher(caps: CapabilitiesWrapper, customBrowsers: CustomBrowsers) extends NodeLauncher(caps, customBrowsers) {
  import scala.concurrent._
  import scala.concurrent.ExecutionContext.Implicits.global
  
  var command: Option[Seq[String]] = None
  var env: Option[Map[String, String]] = None
  
  override def executeCommand(cmd: Seq[String], envVars: Map[String, String]) = {
    command = Some(cmd)
    env = Some(envVars)
    
    future {
      1
    }
  }
}
