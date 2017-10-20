package com.spoonium.grid

import org.openqa.grid.web.servlet.handler.RequestHandler
import com.spoonium.grid.util._
import scala.collection.JavaConverters._
import org.openqa.grid.internal.Registry
import com.spoonium.grid.util.CapabilitiesWrapper

/**
 * Selenium Registry API hooks
 * Invoked through instrumentation of existing Selenium classes. Not supported by Selenium.
 *
 * @see InstrumentationAgent
 */
object SpooniumRegistry extends Loggable {

  val version = getClass.getPackage.getImplementationVersion
  logger.info(s"Spoonium Plugin $version initialized")

  /**
   * Called before org.openqa.internal.Registry.addNewSessionRequest() gets executed
   */
  def addNewSessionRequest(registry: Registry, handler: RequestHandler) {

    // does the hub have a node that accepts these capabilities?
    def isTestRunnable() = registry.getAllProxies.hasCapability(handler.getRequest.getDesiredCapabilities)
    if(isTestRunnable()) return

    val caps = CapabilitiesWrapper(handler.getRequest.getDesiredCapabilities.asScala)

    // request launch of new browser node
    val launchFuture = new NodeLauncher(caps, CustomBrowsers.fromSystemProps()).launch()
    logger.info(s"Launching node: ${caps.browser}${caps.version}, please wait...")

    def nodeNotOnline() = !isTestRunnable()
    def nodeLaunchDidNotFail() = !launchFuture.isCompleted
    while(nodeNotOnline() && nodeLaunchDidNotFail()) {
      Thread.sleep(500)
    }

    if(isTestRunnable()) {
      logger.info(s"Node ${caps.browser}${caps.version} is ready, continuing with the tests")
    } else {
      logger.info("*******************************************************")
      logger.info(s"Node ${caps.browser}${caps.version} failed to launch !")
      logger.info("*******************************************************")
    }

    // proceeds with the test or tells test that the node is not available
  }
}
