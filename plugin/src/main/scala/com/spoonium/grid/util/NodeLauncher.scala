package com.spoonium.grid.util

import scala.collection.JavaConversions._
import java.io.File
import scala.concurrent.Future
import java.util.UUID

class NodeLauncher(caps: CapabilitiesWrapper, customBrowsers: CustomBrowsers) extends Loggable {

  def launch(): Future[Int] = {
    val browser = caps.browser.toLowerCase.trim
    val version = caps.version

    val launchWrapperArgs = Seq("node", browser, version, ProcessUtils.currentPid().toString)
    // prevents the nodes from inheriting the CONTAINERSESSIONID from the parent container (the grid)
    // this would cause their output streams to get mixed up
    val envVars = Map[String, String]("SPOONCONTAINERSESSIONID" -> UUID.randomUUID().toString.replaceAll("-", ""))

    // default browsers
    lazy val defaultImageName = browser match {
      case "ie" => "selenium/ie-selenium"
      case "firefox" => "mozilla/firefox-base"
      case "chrome" => "google/chrome-base"
      case _ => "unknown"
    }

    val customImage = caps.spoonImage.orElse(customBrowsers.get(browser, version))

    val imageName = customImage match {
      // default browsers
      case None if version.isEmpty => defaultImageName // latest version
      case None => s"$defaultImageName:$version" // specific version
      // custom browsers
      case Some(customBrowserImage) => {
        if(browser == "ie") {
          s"$customBrowserImage,ie-selenium-shims:11"
        } else {
          customBrowserImage
        }
      }
    }

    val seleniumDrivers = browser match {
      case "chrome" => Seq("selenium/selenium-chrome-driver")
      case "firefox" => Seq("selenium/selenium-gecko-driver")
      case "ie" => Seq("selenium/selenium-ie-driver")
      case _ => Seq.empty
    }

    val windowTitle = s"Selenium Node - ${browser.capitalize} $version ${customImage.getOrElse("")}".trim
    val seleniumContainerVer = Option(System.getenv("SELENIUM_CONTAINER_VER")).getOrElse("head")

    val imagesToLaunch = Seq("base", imageName, "oracle/jre-core:8.25") ++ seleniumDrivers ++ Seq(s"selenium/selenium-grid-node:$seleniumContainerVer")

    val command = Seq("cmd.exe", "/C", "start", "/I", "/wait", quoted(windowTitle), "turbo.exe", "try", "--attach", imagesToLaunch.mkString(",")) ++ launchWrapperArgs

    executeCommand(command, envVars)
  }

  def executeCommand(command: Seq[String], envVars: Map[String, String]): Future[Int] = {
    logger.info(command.mkString(" "))

    val pb = new ProcessBuilder(command)
      .directory(new File("C:\\"))

    pb.environment().putAll(envVars)

    val p = pb.start()

    import scala.concurrent._
    implicit val ec = ExecutionContext.fromExecutorService(java.util.concurrent.Executors.newCachedThreadPool())

    future {
      blocking {
        p.waitFor()
      }
    }
  }

  private def quoted(s: String) = "\""+s+"\""
}
