package com.spoonium.grid

import javax.servlet.http.{HttpServletRequest, HttpServletResponse}

import com.google.common.io.ByteStreams
import com.spoonium.grid.model.{SeleniumCommand, SeleniumTestSession, SeleniumTestSessionEnd, _}
import com.spoonium.grid.reporting.Reporter
import com.spoonium.grid.util.{CapabilitiesWrapper, SeleniumJsonWireCommand, _}
import com.spoonium.grid.web.HackedHttpServletResponse
import org.openqa.grid.internal.{Registry, TestSession}
import org.openqa.grid.web.servlet.handler.{SeleniumBasedResponse, WebDriverRequest}
import org.springframework.mock.web.MockHttpServletRequest

import scala.collection.JavaConverters._
import scala.util.control.NonFatal

case class TestInterceptor(session: TestSession, registry: Registry, reporters: Seq[Reporter]) extends Loggable {

  val capabilities = CapabilitiesWrapper(session.getSlot.getCapabilities.asScala)
  val requestedCapabilities = CapabilitiesWrapper(session.getRequestedCapabilities.asScala)

  def seleniumSessionId = session.getInternalKey // selenium session id (guid)
  def name = Option(session.getExternalKey).map(_.getKey)

  lazy val id = StringHelpers.randomString(11) // shareId / reportId
  val screenshotsEnabled = PluginProperties.screenshotsEnabled || PluginProperties.screenshotsStorePath.isDefined
  if(screenshotsEnabled) {
    logger.info(s"Screenshots enabled, Turbo selenium plugin will take screenshots and store them in ${PluginProperties.screenshotsStorePath}")
  }

  def beforeSession() {
    DurationTimer.start(seleniumSessionId)
    SessionTracker.start(seleniumSessionId, onTimeout = beforeRelease)

    val nodeCapabilities = CapabilitiesWrapper(session.getSlot.getCapabilities.asScala).toStringsMap

    logCommand(
      new SeleniumTestSession(id, requestedCapabilities.toStringsMap, nodeCapabilities, seleniumSessionId, nodeBrowserVersion)
    )
  }
  
  def afterSession() = _sessionEnded(externalTimedOut = false)

  // can be called multiple times for the same session, ensures end event is sent only once
  private def _sessionEnded(externalTimedOut: Boolean) {
    DurationTimer.stop(seleniumSessionId)
    SessionTracker.stop(seleniumSessionId) match {
      case Some(hasTimedOut) => {
        logCommand(
          new SeleniumTestSessionEnd(id, externalTimedOut || hasTimedOut, seleniumSessionId, nodeBrowserVersion)
        )
      }
      case None => // session was not tracked anymore, already ended
    }
  }

  // called when a session times out
  def beforeRelease() {
    _sessionEnded(externalTimedOut = true)
  }

  def beforeCommand(req: HttpServletRequest) {
    val uri = req.getRequestURI
    DurationTimer.start(uri)
    SessionTracker.commandReceived(seleniumSessionId)

    appendSeleniumCommand(req, None)
  }

  def afterCommand(req: HttpServletRequest, res: HttpServletResponse) {
    val uri = req.getRequestURI
    val seleniumCmd = SeleniumJsonWireCommand(uri)
    if(seleniumCmd.isPageLoad) {
      SessionTracker.urlLoaded(seleniumSessionId)
    }

    appendSeleniumCommand(req, Some(res))
    DurationTimer.stop(uri)
  }

  def reportKnownSessionInitFailureReasons(res: HttpServletResponse) = {
    try {
      val body = res.asInstanceOf[SeleniumBasedResponse].getForwardedContent
      val localizedMessage = JsonUtil.toStringOpt(body, "value", "localizedMessage")

      val customBrowserImage = requestedCapabilities.spoonImage

      localizedMessage match {
        case Some(msg) if msg.contains("unknown error: Chrome version must be >=") && customBrowserImage.isDefined =>
          val image = customBrowserImage.getOrElse("")
          val version = capabilities.version
          logger.severe(s"The custom image ($image) does not match the browser version specified in the capabilities ($version). Please correct the 'version' capability and launch the test again.")
        case _ => // noop
      }

    } catch {
      case NonFatal(e) => e.printStackTrace()
    }
  }

  def appendSeleniumCommand(req: HttpServletRequest, res: Option[HttpServletResponse]) {
    val method = req.getMethod
    val uri = req.getRequestURI
    val id = req.getRequestURI
    val seleniumCmd = SeleniumJsonWireCommand(uri)
    val commandTookPlace = res.isDefined

    val commandDuration = DurationTimer.time(uri)
    val sessionRelativeDuration = DurationTimer.time(session.getInternalKey)

    if(seleniumCmd.isSessionManipulation && commandTookPlace) {
      reportKnownSessionInitFailureReasons(res.get)
    }

    val shouldSkipCommand = seleniumCmd.isSessionManipulation || seleniumCmd.isScreenshot
    if(shouldSkipCommand) return

    val path = seleniumCmd.path
    val requestBody = new String(ByteStreams.toByteArray(req.getInputStream), "utf-8")

    val originalResponseBody = res.map(_.asInstanceOf[SeleniumBasedResponse].getForwardedContent)
    val sanitizedResponseBody = originalResponseBody.map(s => new SeleniumResponseBodyModifier(s).transformed())
    if(commandTookPlace) {
      //println(s"Response body: $responseBody")
    }

    val commandSessionId = originalResponseBody.flatMap(body => JsonUtil.toStringOpt(body, "sessionId"))
    if(commandTookPlace) {
      //println(s"Command session id: $commandSessionId")
    }

    val screenshot = {
      val sessionStillRunning = SessionTracker.isRunning(seleniumSessionId)
      val wasUrlEverLoaded = SessionTracker.wasUrlLoaded(seleniumSessionId)
      def screenshotRelevantInCommandContext() = {
        sessionStillRunning && wasUrlEverLoaded && commandTookPlace &&
          method == "POST" && !(seleniumCmd.isTimeout || seleniumCmd.isElementFind)
      }

      if(screenshotsEnabled && screenshotRelevantInCommandContext())
        takeScreenshot(session, commandSessionId.get)
      else None
    }

    val command = SeleniumCommand(id, seleniumSessionId, method, path, requestBody, sanitizedResponseBody, commandDuration, sessionRelativeDuration, screenshot, nodeBrowserVersion)
    //println("Command is: " + command.toString)
    logCommand(command)
  }

  def nodeBrowserVersion = (capabilities.browser, capabilities.version)

  def logCommand(data: SeleniumEvent) = {
    reporters.foreach { r =>
      r.reportMessage(data)
    }
  }

  def takeScreenshot(session: TestSession, sessionId: String): Option[String] = {
    try {
      //println("About to take screenshot...")
      val req = new MockHttpServletRequest("GET", s"/session/$sessionId/screenshot")
      req.setContent(Array.empty)
      val response = new HackedHttpServletResponse

      session.forward(new WebDriverRequest(req, registry), response, false)

      if(response.getStatus / 100 == 2) {
        val result = JsonUtil.toStringOpt(response.getContentAsString, "value")
        //println("... done")
        result
      } else {
        logger.severe("Failed to take screenshot. Response status: " + response.getStatus)
        logger.severe("Response body: " + response.getContentAsString)
        None
      }
    } catch {
      case e: Exception => {
        loggerSevere("Failed to take screenshot", e)
        None
      }
      case e: Error => {
        loggerSevere("A severe error occurred", e)
        throw e
      }
    }
  }
}


