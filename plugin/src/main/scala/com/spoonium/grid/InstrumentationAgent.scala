package com.spoonium.grid

import java.lang.instrument.{ClassFileTransformer, Instrumentation}
import java.security.ProtectionDomain
import javassist.ClassPool
import java.io.ByteArrayInputStream

object InstrumentationAgent {
  def premain(agentArgs: String, instumentation: Instrumentation) {
    instumentation.addTransformer(new SpooniumByteCodeTransformer())
  }
}

class SpooniumByteCodeTransformer extends ClassFileTransformer {
  def transform(loader: ClassLoader, className: String, classBeingRedefined: Class[_], protectionDomain: ProtectionDomain, classfileBuffer: Array[Byte]): Array[Byte] = {
    try {
      className match {
        case x if x == "org/openqa/grid/internal/Registry" => {
          //println(s"Instrumenting $className ...")
          val clazz = ClassPool.getDefault.makeClass(new ByteArrayInputStream(classfileBuffer))
          val method = clazz.getDeclaredMethod("addNewSessionRequest")
          //method.insertBefore("System.out.println(\"!!!!Instrumented code being executed!!!!!\");")
          method.insertBefore("try { com.spoonium.grid.SpooniumRegistry.addNewSessionRequest($0, $1); } catch(Exception e) { e.printStackTrace(); }")

          val instrumentedByteCode = clazz.toBytecode
          clazz.detach()

          instrumentedByteCode
        }
        case _ => classfileBuffer
      }
    } catch {
      case e: Exception => {
        println("Failed to instrument, hooks will not be called")
        e.printStackTrace()
        classfileBuffer
      }
    }
  }
}
