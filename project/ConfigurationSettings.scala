object ConfigurationSettings {

  val run  = Seq("-Dconfig.resource=application.conf", "-Dlogger.resource=logger-config.xml")
  val test = Seq("-Dconfig.resource=test.conf",        "-Dlogger.resource=logger-config.xml")

}