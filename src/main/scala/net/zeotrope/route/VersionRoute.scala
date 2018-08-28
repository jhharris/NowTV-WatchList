package net.zeotrope.route

import akka.http.scaladsl.model.{ ContentType, HttpEntity, HttpResponse, MediaTypes }
import akka.http.scaladsl.server.Route
import net.zeotrope.{ JsonSupport, WatchListBuildInfo }

class VersionRoute extends Routeable with JsonSupport {

  override def route: Route =
    pathPrefix("watchlist" / "version") {
      pathEndOrSingleSlash {
        get {
          logger.debug("watchlist version endpoint")
          complete(HttpResponse(entity = HttpEntity(ContentType(MediaTypes.`application/json`), WatchListBuildInfo.toJson)))
        }
      }
    }
}
