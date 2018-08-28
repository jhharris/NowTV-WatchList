package net.zeotrope

import net.zeotrope.WatchListActor.ActionCompleted

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.DefaultJsonProtocol

trait JsonSupport extends SprayJsonSupport {
  import DefaultJsonProtocol._

  implicit val contentIdsJsonFormat = jsonFormat1(ContentIds)
  implicit val contentItemJsonFormat = jsonFormat1(ContentItem)
  implicit val actionJsonFormat = jsonFormat1(ActionCompleted)
}
