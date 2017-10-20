package com.spoonium.grid

import org.openqa.grid.internal.listeners.{TimeoutListener, TestSessionListener, CommandListener}
import org.openqa.grid.internal.{Registry, TestSession}
import org.openqa.grid.selenium.proxy.DefaultRemoteProxy
import org.openqa.grid.common.RegistrationRequest
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import com.google.common.cache.{CacheLoader, CacheBuilder}
import java.util.concurrent.TimeUnit
import com.spoonium.grid.util.{PluginProperties, Loggable}
import com.spoonium.grid.reporting.DiskFileReporter

class SpooniumPlugin(request: RegistrationRequest, registry: Registry)
  extends DefaultRemoteProxy(request, registry)
  with CommandListener with TestSessionListener with TimeoutListener
  with Loggable {

  val diskFileReporter = new DiskFileReporter(path = PluginProperties.screenshotsStorePath.getOrElse("C:\\Selenium"))
  val reporters = Seq(diskFileReporter)

  val enabled = PluginProperties.pluginEnabled || PluginProperties.screenshotsEnabled || PluginProperties.screenshotsStorePath.isDefined
  if(!enabled) {
    logger.info(s"Spoonium Plugin is disabled")
  }

  val testLogCache = CacheBuilder.newBuilder()
    .maximumSize(1000)
    .expireAfterAccess(10, TimeUnit.MINUTES)
    .build[TestSession, TestInterceptor](
      new CacheLoader[TestSession, TestInterceptor]() {
        def load(session: TestSession) = {
          new TestInterceptor(session, registry, reporters)
        }
      })

  override def beforeCommand(session: TestSession, request: HttpServletRequest, response: HttpServletResponse) = {
    super.beforeCommand(session, request, response)

    try {
      usingTestLog(session) {
        _.beforeCommand(request)
      }
    } catch {
      case e: Throwable => loggerSevere("Failed to execute beforeCommand", e)
    }
  }

  override def afterCommand(session: TestSession, request: HttpServletRequest, response: HttpServletResponse) = {
    super.afterCommand(session, request, response)

    try {
      usingTestLog(session) {
        _.afterCommand(request, response)
      }
    } catch {
      case e: Throwable => loggerSevere("Failed to execute afterCommand", e)
    }
  }

  override def beforeSession(session: TestSession) {
    super.beforeSession(session)

    try {
      usingTestLog(session) {
        _.beforeSession()
      }
    } catch {
      case e: Throwable => loggerSevere("Failed to execute beforeSession", e)
    }
  }

  override def afterSession(session: TestSession) {
    super.afterSession(session)

    try {
      usingTestLog(session) {
        _.afterSession()
      }
    } catch {
      case e: Throwable => loggerSevere("Failed to execute afterSession", e)
    }
  }

  override def beforeRelease(session: TestSession) {
    try {
      usingTestLog(session) {
        _.beforeRelease()
      }
    } catch {
      case e: Throwable => loggerSevere("Failed to execute beforeRelease", e)
    } finally {
      super.beforeRelease(session)
    }
  }

  private def usingTestLog(session: TestSession)(block: TestInterceptor => Unit) = {
    if(enabled){
      block(testLogCache.get(session))
    } else {
      // noop
    }
  }

}
