object SpooniumVersioning {
  def version(seleniumVersion: String) = {
    val simpleDateFormatter = new java.text.SimpleDateFormat("yyyy.M.d")
    val datePart = simpleDateFormatter.format(new java.util.Date())
    val buildNo = System.getProperty("spoon.version","-SNAPSHOT") match{
      case "-SNAPSHOT" => "-SNAPSHOT"
      case v => "." + v
    }

    seleniumVersion + "-" + datePart + "" + buildNo
  }
}