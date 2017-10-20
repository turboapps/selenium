package com.spoonium.grid.util

import java.util.{Date, TimerTask, Timer}
import scala.collection.JavaConverters._
import java.util.concurrent.ConcurrentHashMap

object SessionTracker extends Loggable {

  val trackedSessions = new ConcurrentHashMap[String, TrackedSession]()
  // should be at least enough time for every browser to start (virtual ie can take a long time)
  var timeout =  10 * 60 * 1000
  var checkInterval = 2 * 1000

  case class TrackedSession(lastCommandTime: Long, onTimeout: () => Unit, urlLoaded: Boolean = false) {
    def hasTimedOut: Boolean = now() - lastCommandTime > timeout
    def withUpdatedLastCommandTime() = this.copy(lastCommandTime = now())
  }
  
  def start(key: String, onTimeout: () => Unit) {
    trackedSessions.put(key, TrackedSession(lastCommandTime = now(), onTimeout = onTimeout))
  }
  
  def commandReceived(key: String) = {
    Option(trackedSessions.get(key)) map { v =>
      trackedSessions.replace(key, v, v.withUpdatedLastCommandTime()) // could improve and loop while unsuccessful replacement
    }
  }

  def urlLoaded(key: String) = {
    Option(trackedSessions.get(key)) map { v =>
      trackedSessions.replace(key, v, v.copy(urlLoaded = true))
    }
  }

  def wasUrlLoaded(key: String): Boolean = {
    Option(trackedSessions.get(key)).exists(_.urlLoaded)
  }

  protected def now() = new Date().getTime

  /**
   * @return
   *         None - if session was not tracked (anymore)
   *         Some(hasTimedOut) - if session was tracked
   */
  def stop(key: String) = {
    Option(trackedSessions.remove(key)).map(_.hasTimedOut)
  }

  def isRunning(key: String) = Option(trackedSessions.get(key)) match {
    case Some(session) if !session.hasTimedOut => true
    case _ => false
  }

  new Timer(true).scheduleAtFixedRate(
    new TimerTask {
      def run() = {
        // check for timed out sessions
        trackedSessions.asScala.foreach({ case (key, session) =>
          if(session.hasTimedOut) {
            logger.info("Spoonium detected timed out session " + key)
            session.onTimeout()
          }
        })
      }
    }
    , 0, checkInterval)
}
