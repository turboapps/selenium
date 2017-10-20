package com.spoonium.grid.util

case class SeleniumJsonWireCommand(uri: String) {

  val prefixLength = 52 /* that's "/wd/hub/session/<UUID>".length */
  // the path in the selenium command, excluding the session specific prefix
  // Eg: for a command like "/session/:sessionId/element/:id/name" the path is "/element/:id/name"
  val path = if(uri.length >= prefixLength) uri.substring(prefixLength) else ""

  def isSessionManipulation = uri.length <= prefixLength

  def isScreenshot = uri.contains("screenshot")

  def isPageLoad = path.startsWith("/url")

  def isTimeout = path.startsWith("/timeouts")
  def isElementFind = Set("/element", "/elements").contains(path)
}
