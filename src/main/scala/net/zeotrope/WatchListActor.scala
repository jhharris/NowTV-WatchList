package net.zeotrope

import akka.actor.{ Actor, ActorLogging, Props }

import scala.collection.mutable
import scala.collection.mutable.Set

final case class CustomerWatchData(watchlistIds: Set[String])
final case class ContentItem(contentId: String)
final case class ContentIds(ids: Seq[String])

class WatchListActor extends Actor with ActorLogging {

  import WatchListActor._

  var inMemoryDb: mutable.HashMap[String, CustomerWatchData] = new mutable.HashMap[String, CustomerWatchData]()

  private def getCustomerWatchListItems(userId: String): CustomerWatchData = {
    inMemoryDb.getOrElse(userId, CustomerWatchData(Set.empty[String]))
  }

  private def addItemtoWatchList(watchList: CustomerWatchData, contentId: String): CustomerWatchData = {
    watchList.watchlistIds += contentId
    watchList
  }

  private def removeItemFromWatchList(watchList: CustomerWatchData, contentId: String): CustomerWatchData = {
    watchList.watchlistIds -= contentId
    watchList
  }

  private def updateDataStore(id: String, watchList: CustomerWatchData) = {
    inMemoryDb.put(id, watchList)
  }

  override def receive: Receive = {
    case AddWatchItem(userId, contentId) =>
      updateDataStore(userId, addItemtoWatchList(getCustomerWatchListItems(userId), contentId))
      sender() ! ActionCompleted(s"Customer $userId, contentId $contentId added to watch list")

    case GetWatchItems(id) =>
      sender() ! ContentIds(getCustomerWatchListItems(id).watchlistIds.toSeq.sorted)

    case DeleteWatchItem(userId, contentId) =>
      updateDataStore(userId, removeItemFromWatchList(getCustomerWatchListItems(userId), contentId))
      sender() ! ActionCompleted(s"Customer $userId, contentId $contentId removed from watch list")
  }
}

object WatchListActor {

  final case class AddWatchItem(userId: String, contentId: String)
  final case class GetWatchItems(userId: String)
  final case class DeleteWatchItem(userId: String, contentId: String)
  final case class ActionCompleted(msg: String)

  def props: Props = Props[WatchListActor]
}