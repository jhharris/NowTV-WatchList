package net.zeotrope

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import net.zeotrope.route.VersionRoute
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{ Matchers, WordSpec }
import spray.json.DefaultJsonProtocol

class VersionRouteSpec extends WordSpec with Matchers with ScalaFutures with ScalatestRouteTest with SprayJsonSupport {
  import DefaultJsonProtocol._

  implicit val buildInfoFormat = jsonFormat6(BuildInfo)
  lazy val versionRoute = new VersionRoute

  "Service version routes" should {
    "return version information (GET /watchlist/version)" in {
      val request = HttpRequest(uri = "/watchlist/version")

      request ~> versionRoute.route ~> check {
        status should ===(StatusCodes.OK)
        contentType should ===(ContentTypes.`application/json`)
        val buildInfo = entityAs[BuildInfo]
        assertResult("nowtv-watchlist")(buildInfo.name)
      }
    }
  }
}

case class BuildInfo(builtAtMillis: String, name: String, scalaVersion: String, version: String, sbtVersion: String, builtAtString: String)
