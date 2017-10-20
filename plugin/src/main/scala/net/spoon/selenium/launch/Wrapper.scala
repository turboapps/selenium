package net.spoon.selenium.launch

import java.net.ServerSocket

import scala.collection.JavaConversions._
import scala.util.Try
import com.spoonium.grid.util.{PluginProperties, ProcessUtils}

/**
 *
 * Launches selenium grid or nodes. Used as a startupfile for the selenium images
 *
 * Usage: node <browserName=firefox|chrome|ie> <browserVersion> <parentPid>
 * Usage: hub
 * => optional parameters:
 * -DcustomBrowserOverrides="ie9=edi/custom-ie9;firefox10=edi/custom-ff10"
 * -Dturbo.plugin.enabled=true
 * -Dturbo.plugin.screenshots.enabled=true
 * -Dturbo.plugin.screenshots.path=C:\Selenium
 *
 * Example: node firefox 30
 * Example: hub true
 * Example: node firefox 30 123456
 * Example: hub 
 *
 */
object Wrapper extends WrapperTrait

trait WrapperTrait {

  def main(args: Array[String]) = {
    //println("Arguments: " + args.mkString(","))

    def arg(i: Int) = args.lift(i).getOrElse("")

    // normalize ie
    def normalized(browser: String) = browser match {
      case "ie" => "internet explorer"
      case "internet-explorer" => "internet explorer"
      case other => other
    }

    val nodeMode = args.lift(0) == Some("node")
    val hubMode = !nodeMode

    val (browserName, browserVersion) = {
      if (nodeMode) {
        (normalized(arg(1)), arg(2))
      } else {
        ("", "")
      }
    }

    val parentPid = if(nodeMode) args.lift(3).map(_.toInt) else None

    def chromeDriverPath = {
      val chromeVersion = Try(browserVersion.toInt).getOrElse(1000)
      val driverVersion = {
			 if (chromeVersion >= 60) { "2.33"}
		else if (chromeVersion >= 59) { "2.32"}
		else if (chromeVersion >= 58) { "2.31"}
		else if (chromeVersion >= 56) { "2.29"}
		else if (chromeVersion >= 55) { "2.28"}
		else if (chromeVersion >= 54) { "2.27"}
		else if (chromeVersion >= 53) { "2.26"}
		else if (chromeVersion >= 52) { "2.24"}
		else if (chromeVersion >= 51) { "2.23"}
		else if (chromeVersion >= 49) { "2.22"}
		else if (chromeVersion >= 46) { "2.21"}
		else if (chromeVersion >= 43) { "2.20"}
		else if (chromeVersion >= 36) { "2.11"}
		else if (chromeVersion >= 33) { "2.10"}
		else if (chromeVersion >= 31) { "2.9"}
		else {"2.8"}
      }
      s"C:\\Selenium\\chromedriver\\$driverVersion\\chromedriver.exe"
    }

	def firefoxDriverPath = {
      val ffVersion = Try(browserVersion.toInt).getOrElse(1000)
      val ffDriverVersion = {
			 if (ffVersion >= 55) { "0.19.0"}
		else if (ffVersion >= 53) { "0.18.0"}
		else { "0.17.0"}
      }
      s"C:\\Selenium\\geckodriver\\$ffDriverVersion\\geckodriver.exe"
    }

    def firefoxProfile = {
      val ffVersion = Try(browserVersion.toInt).getOrElse(1000)

      if (ffVersion >= 33) {
        ""
      } else {
        "-Dwebdriver.firefox.profile=default"
      }
    }

    val nodePort = Ports.freePort()

    def launchNodeCmd = {
      Seq(
        "java",
        s"-Dwebdriver.chrome.driver=$chromeDriverPath",
		s"-Dwebdriver.gecko.driver=$firefoxDriverPath",
		"-Dwebdriver.ie.driver=C:\\Selenium\\iedriverserver.exe",
        firefoxProfile,
		"-jar", "C:\\Selenium\\selenium-server-standalone.jar",
        "-role", "node",
        "-proxy", "com.spoonium.grid.SpooniumPlugin",
        "-hubHost", "localhost",
        "-maxSession", "5",
        "-port", nodePort.toString,
        "-cleanUpCycle", "5000",
        "-browser", "\"browserName=" + browserName + ",version=" + browserVersion + ",maxInstances=5\""
      )
    }

    def launchGridCmd = {
      // pass through all system props coming in as args
      val systemProps = args.filter(_.startsWith("-D")).toSeq

      Seq("java") ++ systemProps ++
      Seq(
        "-Dwebdriver.firefox.profile=default",
        "-javaagent:C:\\Selenium\\spoonium-grid-plugin-assembly.jar",
        "-cp", "C:\\Selenium\\selenium-server-standalone.jar",
		"org.openqa.grid.selenium.GridLauncherV3",
        "-role", "hub",
        "-port", "4444",
        "-maxSession", "50",
        "-timeout", PluginProperties.Selenium.timeout(args),
        "-browserTimeout", PluginProperties.Selenium.browserTimeout(args)
      ) ++
        PluginProperties.Selenium.hubHost(args)
          .map(host => Seq("-host", host))
          .getOrElse(Seq.empty)
    }

    if (hubMode) {
      if (Ports.isTaken(4444)) {
        System.err.println("Selenium grid already running...")

        Thread.sleep(1000)
        System.exit(0)
      }
    }

    val cmd = if (nodeMode) launchNodeCmd else launchGridCmd
    println(cmd.mkString(" "))
    println("\n")

    val p = startProcess(cmd)

    // shutdown node when parent grid shuts down
    if(nodeMode && parentPid.isDefined) {
      import scala.concurrent._
      implicit val ec = ExecutionContext.fromExecutorService(java.util.concurrent.Executors.newCachedThreadPool())

      future {
        while(ProcessUtils.isProcessRunning(parentPid.get)) {
          Thread.sleep(1000)
        }

        println(s"Parent grid process (${parentPid.get}}) exited, will now shutdown this node.")

        p.destroy()
      }
    }

    p.waitFor()
    doExit()
  }

  def startProcess(cmd: Seq[String]) =  {
    new ProcessBuilder(cmd)
      .redirectErrorStream(true)
      .inheritIO()
      .start()
  }

  def doExit() {
    System.exit(0)
  }
}

object Ports {

  def freePort() = {
    val s = new ServerSocket(0)
    s.setReuseAddress(true)
    val port = s.getLocalPort
    Try(s.close())
    port
  }

  def isTaken(port: Int) = {
    try {
      val s = new ServerSocket(port)
      s.close()
      false
    } catch {
      case e: Exception => true
    }
  }
}

