package net.zeotrope

import com.typesafe.config.ConfigFactory

trait Configuration {
  val config = ConfigFactory.load()

  val serverHost = config.getString("server.host")
  val serverPort = config.getInt("akka.http.server.default-http-port")
}
