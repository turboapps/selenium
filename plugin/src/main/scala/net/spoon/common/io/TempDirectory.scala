package net.spoon.common.io

import java.io.{File => JFile, IOException}
import java.util.UUID
import scala.collection.mutable.HashSet

/// Borrowed from net.spoon.common.io
/// A lame temp directory implementation. Should replace with the built in implementation based on
/// nio temp directory when we move to Java 7.       clean
class TempDirectory private(val tempDir: JFile) {

  val tempDirPath = tempDir.getCanonicalPath()

  TempDirectory.addTempDirToDeleteOnExit(this)

  def dispose() = _dispose(true)

  private def _dispose(removeFromMap: Boolean) = {

    var deleteSuccess = true
    def _deleteDeep(f: JFile): Unit = {
      // we have to manually remove files to delete a directory pre-J7 :(
      val files = f.listFiles()
      // can return null
      if (files != null) {
        for (child <- files) {
          if (child.isDirectory()) _deleteDeep(child)
          child.delete()
          //val fdel = child.delete()
          //if (!fdel) println("*** Oops! Could not delete temp: " + child.getCanonicalPath)
        }
      }
      val res = f.delete()

      if (!res) {
        if (f == tempDir) {
          println("Could not delete temp dir: " + f.getCanonicalPath)
          deleteSuccess = false
        }
      }
    }

    _deleteDeep(tempDir)

    // remove it from global map only if the dir got deleted
    // this way it can get another chance at deleting when java shuts down
    if (removeFromMap && deleteSuccess) {
      TempDirectory.removeTempDirToDeleteOnExit(this)
    }
  }
}

object TempDirectory {

  // dirty but effective way to better purge temp dirs
  private val tempDirs = HashSet[TempDirectory]()
  Runtime.getRuntime().addShutdownHook(new Thread() {
    override def run() = {
      try {
        tempDirs.synchronized{
          for (d <- tempDirs) {
            println("Temp directory wasn't disposed during runtime, purging: " + d.tempDirPath)
            //d._dispose(false)
          }
        }
      } catch {
        case e: Exception => println("TempDirectory shutdown hook exception: " + e.getMessage)
      }
    }
  })

  def addTempDirToDeleteOnExit(d:TempDirectory) = tempDirs.synchronized{
    tempDirs.add(d);
  }
  def removeTempDirToDeleteOnExit(d:TempDirectory) = tempDirs.synchronized{
    tempDirs.remove(d);
  }

  /// Creates a temp dir with the given path.
  def apply(pathCreator: ()=> String) = {
    val maxTries = 200
    var theFile = new JFile(pathCreator())
    var i =0
    while(!theFile.mkdirs() && i<maxTries){
      i = i+1
      theFile = new JFile(pathCreator())
    }
    if(i==maxTries){
      throw new IOException(s"Failed to create unique temp folder. Gave up after $maxTries tries. Last tried name was $maxTries")
    }
    new TempDirectory(theFile)
  }

  /// Creates temp directory with name ramdomUUIID rooted at java.io.tmpdir.
  def apply() = {
    val tempRoot = new JFile(System.getProperty("java.io.tmpdir"))
    val tempDirName = "SPNTMP_" + UUID.randomUUID().toString()
    val tempDir = new JFile(tempRoot, tempDirName)
    //tempDir.deleteOnExit()
    if(!tempDir.mkdirs()){
      throw new IOException(s"Failed to create unique temp folder: $tempDir")
    }
    new TempDirectory(tempDir)
  }
}
