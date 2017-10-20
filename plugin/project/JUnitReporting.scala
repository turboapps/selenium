package ca.seibelnet

import sbt._
import sbt.testing._
import Keys._
import java.net.InetAddress
import scala.collection.mutable.ListBuffer
import java.text.SimpleDateFormat
import java.util.Date
import scala.xml.XML

/**
 * User: bseibel
 * Date: 12-04-25
 * Time: 12:02 PM
 */

object JUnitTestReporting extends Plugin {
  override def settings = Seq(
    testListeners += new JUnitTestListener(new TestGroupXmlWriterFactory, new TestReportDirectory("./target/test-reports"))
  )
  def newListener(location:String) = {
    new JUnitTestListener(new TestGroupXmlWriterFactory, new TestReportDirectory(location+"/target/test-reports"))
  }
}

class JUnitTestListener(val writerFactory: TestGroupWriterFactory, val reportDirectory: TestReportDirectory) extends TestReportListener {

  var currentOutput: Map[String, TestGroupWriter] = Map()
  reportDirectory.setupTestDirectory

  def testEvent(event: TestEvent): Unit = {
    if (event.detail.size > 0) {
      currentOutput.get(event.detail.head.fullyQualifiedName) match {
        case Some(v) => v.addEvent(event)
        case None    => Unit
      }
    }
  }

  def endGroup(name: String, result: TestResult.Value): Unit = {
    flushOutput(name)
  }

  def endGroup(name: String, t: Throwable): Unit = {
    flushOutput(name)
  }

  def startGroup(name: String): Unit = this.synchronized {
    currentOutput = currentOutput + (name -> writerFactory.createTestGroupWriter(name))
  }

  private def flushOutput(name: String): Unit = {
    currentOutput.get(name) match {
      case Some(v) =>  v.write(reportDirectory)
      case None    =>  Unit
    }
  }

}

trait TestGroupWriter {
  def addEvent(testEvent: TestEvent)
  def write(reportDirectory: TestReportDirectory)
}
trait TestGroupWriterFactory {
  def createTestGroupWriter(groupClassName: String): TestGroupWriter
}

object TestGroupXmlWriter {

  def apply(name: String) = {
    new TestGroupXmlWriter(name)
  }
}

class TestGroupXmlWriter(val name: String) extends TestGroupWriter {

  var errors: Int = 0
  var failures: Int = 0
  var skipped: Int = 0
  var tests: Int = 0

  lazy val hostName = InetAddress.getLocalHost.getHostName
  lazy val testEvents: ListBuffer[TestEvent] = new ListBuffer[TestEvent]

  def addEvent(testEvent: TestEvent) {
    testEvents += testEvent
    for(e: Event <- testEvent.detail) {
      tests += 1
      e.status match {
        case Status.Failure => failures += 1
        case Status.Error => errors += 1
        case Status.Skipped => skipped += 1
        case _ =>
      }
    }
  }

  def write(reportDirectory: TestReportDirectory) {
    val resultXml =
      <testsuite errors={ errors.toString } failures={ failures.toString } name={ name } tests={ tests.toString } time={ "0" } timestamp={ new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").format(new Date()) }>
        <properties/>
        {
        for (e <- testEvents; t <- e.detail) yield {
          <testcase classname={ t.fullyQualifiedName } name={ t.selector.asInstanceOf[TestSelector].testName } time={ "0" }>
            {
            t.status match {
              case Status.Failure =>
                if(t.throwable.isDefined) {
                  <failure message={ t.throwable.get.getMessage } type={ t.throwable.getClass.getName }>{ t.throwable.get.getStackTrace.map { e => e.toString }.mkString("\n") }</failure>
                }
              case Status.Error =>
                if(t.throwable.isDefined) {
                  <error message={ t.throwable.get.getMessage } type={ t.throwable.getClass.getName }>{ t.throwable.get.getStackTrace.map { e => e.toString }.mkString("\n") }</error>
                }
              case _ => {}

            }
            }
          </testcase>
        }
        }
        <system-out></system-out>
        <system-err></system-err>
      </testsuite>

    XML.save(reportDirectory.getAbsolutePath +"/TEST-"+name+".xml",resultXml,xmlDecl = true)
  }

}
class TestGroupXmlWriterFactory extends TestGroupWriterFactory {
  def createTestGroupWriter(groupClassName: String): TestGroupWriter = {
    new TestGroupXmlWriter(groupClassName)
  }
}


import java.io.{IOException, File}

class TestReportDirectory(val targetPath: String) {

  val file = new File(targetPath)

  def getAbsolutePath: String = {
    file.getAbsolutePath
  }

  def setupTestDirectory: Unit = {
    if (file.exists()) deleteDirectory(file)
    file.mkdirs()
  }

  private def deleteDirectory(dir: File): Unit = {
    val dirContents = dir.listFiles()

    dirContents foreach { file: File =>
      if (file.isDirectory) deleteDirectory(file)
      else file.delete()

      if (file.exists()) throw new IOException("Failed to delete test report file or directory: " + file.getAbsolutePath)
    }
  }
}