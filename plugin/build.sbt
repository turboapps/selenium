import com.atlassian.labs.gitstamp.GitStampPlugin._

name := "spoonium-grid-plugin"

scalaVersion := "2.10.2"

version := SpooniumVersioning.version(seleniumVersion = "2.41")

libraryDependencies ++= Seq(
   "org.seleniumhq.selenium"    % "selenium-server"         % "2.41.0"                  % "provided",
   "com.google.guava"           % "guava"                   % "17.0",
   "com.google.code.findbugs"   % "jsr305"                  % "2.0.1",
   "net.liftweb"                %% "lift-json"              % "2.6-M3",
   "org.springframework"        % "spring-test"             % "4.0.5.RELEASE",
   "javax.servlet"              % "javax.servlet-api"       % "3.0.1",
   "org.scalatest"              % "scalatest_2.10"          % "2.2.0-M1"                % "test",
   "javassist"                  % "javassist"               % "3.12.1.GA",
   "net.java.dev.jna"           % "jna"                     % "3.4.0",
   "net.java.dev.jna"           % "platform"                % "3.4.0"
)

// Bamboo support for tests passed/failed
testListeners <<= target.map(t => Seq(ca.seibelnet.JUnitTestReporting.newListener(t.getAbsolutePath)))

packageOptions in (Compile, packageBin) +=
    Package.ManifestAttributes( "Premain-Class" -> "com.spoonium.grid.InstrumentationAgent" )

Seq( gitStampSettings: _* )

 
