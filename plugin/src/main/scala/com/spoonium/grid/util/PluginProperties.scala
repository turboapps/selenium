package com.spoonium.grid.util

object PluginProperties {

  def pluginEnabled = System.getProperty("turbo.plugin.enabled", "false").toBoolean
  def screenshotsEnabled = System.getProperty("turbo.plugin.screenshots.enabled", "false").toBoolean
  def screenshotsStorePath: Option[String] = Option(System.getProperty("turbo.plugin.screenshots.path"))
  def customBrowserOverrides = Option(System.getProperty("customBrowserOverrides"))

  object Selenium {
    def timeout(args: Seq[String]) = find("-Dselenium.timeout", args).getOrElse("300")
    def browserTimeout(args: Seq[String]) = find("-Dselenium.browserTimeout", args).getOrElse("0")
    def hubHost(args: Seq[String]) = find("-Dselenium.hubHost", args)
  }

  private def find(prop: String, args: Seq[String]): Option[String] = {
    val arg = args.find(_.startsWith(prop))

    // Ex: -Dsome.prop=value
    arg.flatMap(s => {
      s.split("=").lift(1)
    })
  }
}
