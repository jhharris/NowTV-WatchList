package net.zeotrope.route

import akka.actor.ActorRef
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import akka.pattern.ask
import akka.util.Timeout
import net.zeotrope.WatchListActor.{ ActionCompleted, AddWatchItem, DeleteWatchItem, GetWatchItems }
import net.zeotrope._

import scala.concurrent.Future
import scala.concurrent.duration._

class WatchListRoutes(watchListActor: ActorRef) extends Routeable with JsonSupport {

  private val contentIdFormat: String = "[a-zA-Z0-9]{5}"
  private val customerIdFormat: String = "[0-9]{5}"
  implicit lazy val timeout = Timeout(5.seconds)

  override def route: Route =
    pathPrefix("watchlist") {
      path(Segment) { id =>
        get {
          validate(validateDataFormat(id, customerIdFormat), "CustomerID has to be 5 alphanumeric characters.") {
            logger.debug(s"Customer $id watchlist get items endpoint")
            val items: Future[ContentIds] =
              (watchListActor ? GetWatchItems(id)).mapTo[ContentIds]
            complete(items)
          }
        } ~
          put {
            entity(as[ContentItem]) { item =>
              validate(validateDataFormat(item.contentId, contentIdFormat), "ContentID has to be 5 alphanumeric characters.") {
                logger.debug(s"Customer $id add item ${item.contentId} to watchlist")
                val itemAdded: Future[ActionCompleted] =
                  (watchListActor ? AddWatchItem(id, item.contentId)).mapTo[ActionCompleted]
                onComplete(itemAdded) { action =>
                  complete(StatusCodes.Created)
                }
              }
            }
          } ~
          delete {
            entity(as[ContentItem]) { item =>
              validate(validateDataFormat(item.contentId, contentIdFormat), "ContentID has to be 5 alphanumeric characters.") {
                logger.debug(s"Customer $id delete item ${item.contentId} from watchlist")
                val itemAdded: Future[ActionCompleted] =
                  (watchListActor ? DeleteWatchItem(id, item.contentId)).mapTo[ActionCompleted]
                onComplete(itemAdded) { action =>
                  complete(StatusCodes.OK)
                }
              }
            }
          }
      }
    }

  private def validateDataFormat(s: String, format: String): Boolean = {
    s.matches(format)
  }
}