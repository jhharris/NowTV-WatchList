package net.zeotrope

import akka.Done
import akka.actor.{ ActorRef, ActorSystem }
import akka.http.scaladsl.model.{ HttpRequest, HttpResponse, StatusCodes }
import akka.http.scaladsl.server.{ HttpApp, Route }
import akka.http.scaladsl.settings.ServerSettings
import akka.stream.ActorMaterializer
import com.typesafe.scalalogging.LazyLogging
import net.zeotrope.route.{ Routeable, VersionRoute, WatchListRoutes }

import scala.concurrent.duration.Duration
import scala.concurrent.{ Await, ExecutionContext, Future }

object WatchListServer extends HttpApp with Configuration with LazyLogging {

  private val timeoutMessage = "Could not complete all operations in the configured timeout period."
  private val timeoutResponse = HttpResponse(StatusCodes.ServiceUnavailable, entity = timeoutMessage)

  implicit val system: ActorSystem = ActorSystem("watchListHttpServer")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  val watchListActor: ActorRef = system.actorOf(WatchListActor.props, "watchListActor")

  lazy val appRoutes: List[Routeable] = List(
    new VersionRoute,
    new WatchListRoutes(watchListActor))

  val timeoutResponseFun: HttpRequest => HttpResponse = { req =>
    val loggingString = s"${req.method.value}:${req.uri}"
    logger.error(timeoutMessage + loggingString)
    timeoutResponse
  }

  override protected def routes: Route =
    withRequestTimeoutResponse(timeoutResponseFun) {
      concat(appRoutes.map(_.route): _*)
    }

  override def waitForShutdownSignal(actorSystem: ActorSystem)(implicit executionContext: ExecutionContext): Future[Done] = {
    Future {
      Await.result(actorSystem.whenTerminated, Duration.Inf)
      Done
    }
  }

  def start(appName: String): Unit = {
    val host = serverHost
    val port = serverPort
    logger.info(s"$appName is running on $host:$port")
    startServer(host, port, ServerSettings(config), system)
  }

  def main(args: Array[String]): Unit = {
    start("WatchList")
  }
}