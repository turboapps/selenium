package com.spoonium.grid.util

import java.lang.management.ManagementFactory
import com.sun.jna.Native
import com.sun.jna.platform.win32._
import com.sun.jna.win32.W32APIOptions

object ProcessUtils extends Loggable {

  def currentPid() = {
    val name = ManagementFactory.getRuntimeMXBean.getName
    name.substring(0, name.indexOf('@')).toInt
  }

  def isProcessRunning(pid: Int): Boolean = {
    listProcesses().contains(pid)
  }

  def isWindows = System.getProperty("os.name").toLowerCase.contains("windows")

  def listProcesses(): Map[Int, String] = {
    if(isWindows){
      listProcessesWindows()
    } else {
      logger.warning("Listing processes on non-windows platforms not implemented")
      Map.empty
    }
  }

  private def listProcessesWindows(): Map[Int, String] = {
    val winNT = Native.loadLibrary(classOf[WinNT], W32APIOptions.UNICODE_OPTIONS).asInstanceOf[WinNT]
    val snapshot = winNT.CreateToolhelp32Snapshot(Tlhelp32.TH32CS_SNAPPROCESS, new WinDef.DWORD(0))
    val processEntry = new Tlhelp32.PROCESSENTRY32.ByReference()

    try {
      var result = Map[Int, String]()

      while (winNT.Process32Next(snapshot, processEntry)) {
        result = result.updated(processEntry.th32ProcessID.toString.toInt, Native.toString(processEntry.szExeFile))
      }

      result
    } finally {
      winNT.CloseHandle(snapshot)
    }
  }
}
