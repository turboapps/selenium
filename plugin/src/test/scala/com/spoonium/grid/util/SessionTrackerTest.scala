package com.spoonium.grid.util

import org.scalatest.FunSuite
import org.scalatest.Matchers._
import org.scalatest.concurrent.Timeouts
import org.scalatest.time.{Seconds, Span}

class SessionTrackerTest extends FunSuite with Timeouts {

  SessionTracker.timeout = 2000
  val sessionId = "testSessionId"
  val onTimeoutNoop = () => {}
  var lessThanTimeout = SessionTracker.timeout - 500
  var moreThanTimeout = SessionTracker.timeout + SessionTracker.checkInterval + 1000

  test("session timing out because no command received is detected") {
    
    SessionTracker.start(sessionId, onTimeoutNoop)
    Thread.sleep(moreThanTimeout)
    SessionTracker.stop(sessionId) should be(Some(true))
  }
  
  test("session onTimeout() handler is invoked when session times out") {
    var sessionTimedOut = false
    SessionTracker.start(sessionId, onTimeout = () => { sessionTimedOut = true })
    Thread.sleep(moreThanTimeout)
    
    sessionTimedOut should be(true)
  }

  test("session onTimeout() handler is not invoked if session doesn't time out") {
    var sessionTimedOut = false
    SessionTracker.start(sessionId, onTimeout = () => { sessionTimedOut = true })
    Thread.sleep(lessThanTimeout)
    SessionTracker.stop(sessionId)
    Thread.sleep(moreThanTimeout)

    sessionTimedOut should be(false)
  }

  test("session does not timeout if commands are received") {
    SessionTracker.start(sessionId, onTimeoutNoop)
    Thread.sleep(lessThanTimeout)
    SessionTracker.commandReceived(sessionId)
    Thread.sleep(lessThanTimeout)

    SessionTracker.stop(sessionId) should be(Some(false))
    SessionTracker.stop(sessionId) should be(None)
  }

  test("commands received after session is stopped are softly ignored (no hard failure)") {
    SessionTracker.start(sessionId, onTimeoutNoop)
    SessionTracker.stop(sessionId)
    SessionTracker.commandReceived(sessionId)
  }

  test("can determine session running state") {
    SessionTracker.start(sessionId, onTimeoutNoop)
    SessionTracker.isRunning(sessionId) should be(true)
    Thread.sleep(moreThanTimeout)
    SessionTracker.isRunning(sessionId) should be(false)
    SessionTracker.stop(sessionId)
    SessionTracker.isRunning(sessionId) should be(false)
  }

  test("can track if session loaded an url") {
    failAfter(Span(10, Seconds)) {
      SessionTracker.start(sessionId, onTimeoutNoop)
      SessionTracker.wasUrlLoaded(sessionId) should be(false)
      SessionTracker.urlLoaded(sessionId)
      SessionTracker.wasUrlLoaded(sessionId) should be(true)
      SessionTracker.stop(sessionId)
      SessionTracker.wasUrlLoaded(sessionId) should be(false)
    }
  }
}
