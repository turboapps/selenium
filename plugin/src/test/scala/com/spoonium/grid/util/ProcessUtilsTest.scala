package com.spoonium.grid.util

import org.scalatest.{Matchers, FunSuite}

class ProcessUtilsTest extends FunSuite with Matchers {

  test("Is process running") {
    if(ProcessUtils.isWindows){
      ProcessUtils.isProcessRunning(ProcessUtils.currentPid()) should be(true)
    }
  }
}
