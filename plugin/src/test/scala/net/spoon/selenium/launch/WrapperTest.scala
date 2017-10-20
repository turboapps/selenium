package net.spoon.selenium.launch

import org.scalatest.{Matchers, FunSuite}
import scala.collection.JavaConversions._
import scala.util.Try

class WrapperTest extends FunSuite with Matchers {

  // not thread safe
  object TestWrapper extends WrapperTrait {
    var launchedProcessCmd: Seq[String] = Seq.empty

    override def startProcess(cmd: Seq[String]): Process = {
      launchedProcessCmd = cmd
      new ProcessBuilder(Seq("cmd.exe", "/c", "echo")).start()
    }

    override def doExit() {
      // noop
    }
  }

  test("Java system properties are passed on to the java process being launched by the wrapper") {
    val tested = TestWrapper
    Try(tested.main(Array("hub", "-Dmy.test.prop=true")))
    val launched = tested.launchedProcessCmd

    launched(0) should be("java")
    launched(1) should be("-Dmy.test.prop=true")
  }

}
