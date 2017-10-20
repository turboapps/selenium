import AssemblyKeys._ // put this at the top of the file

assemblySettings

excludedJars in assembly <<= (fullClasspath in assembly) map { cp =>
  cp filter {_.data.getName == "scala-compiler.jar"}
}

jarName in assembly := "spoonium-grid-plugin-assembly.jar"

test in assembly := {}

assemblyOption in assembly ~= { _.copy(cacheOutput = false) }
