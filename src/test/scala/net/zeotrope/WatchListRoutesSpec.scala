package net.zeotrope

import akka.actor.ActorRef
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.ValidationRejection
import akka.http.scaladsl.testkit.ScalatestRouteTest
import net.zeotrope.route.WatchListRoutes
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{ Matchers, WordSpec }

class WatchListRoutesSpec extends WordSpec with Matchers with ScalaFutures with ScalatestRouteTest with SprayJsonSupport {

  val watchListActor: ActorRef = system.actorOf(WatchListActor.props, "watchListActor")

  lazy val watchListRoutes = new WatchListRoutes(watchListActor)

  "Service WatchList routes" should {
    "return no watchlist items for a new customer (GET /watchlist/00012)" in {
      val userId = "00012"
      val request = HttpRequest(uri = s"/watchlist/$userId")

      request ~> watchListRoutes.route ~> check {
        status should ===(StatusCodes.OK)
        contentType should ===(ContentTypes.`application/json`)
        entityAs[String] should ===("""{"ids":[]}""")
      }
    }

    "return a validation rejection when attempting to use an invalid format customer ID (not consisting of 5 numeric characters" in {
      val request = HttpRequest(uri = "/watchlist/$$001")

      request ~> watchListRoutes.route ~> check {
        rejection should ===(ValidationRejection("CustomerID has to be 5 alphanumeric characters.", None))
      }
    }

    "return a validation rejection when attempting to insert an invalid format content ID (not consisting of 5 alphanumeric characters for a valid customer ID" in {
      val userId = "67890"
      val invalidContent = "{\"contentId\" : \"333-11\"}"
      val request = Put(uri = s"/watchlist/$userId").withEntity(ContentTypes.`application/json`, invalidContent)

      request ~> watchListRoutes.route ~> check {
        rejection should ===(ValidationRejection("ContentID has to be 5 alphanumeric characters.", None))
      }
    }

    "should insert a valid content ID for the valid customer ID and when queried for same customer ID, a list of 1 content ID is returned" in {
      val userId = "44555"
      val validContent = "{\"contentId\" : \"188aZ\"}"
      val p1 = Put(uri = s"/watchlist/$userId").withEntity(ContentTypes.`application/json`, validContent)
      p1 ~> watchListRoutes.route ~> check {
        status should ===(StatusCodes.Created)
      }
      val r1 = HttpRequest(uri = s"/watchlist/$userId")
      r1 ~> watchListRoutes.route ~> check {
        status should ===(StatusCodes.OK)
        contentType should ===(ContentTypes.`application/json`)
        entityAs[String] should ===("""{"ids":["188aZ"]}""")
      }

    }

    "should ignore a duplicate content ID for the valid customer ID" in {
      val userId = "77866"
      val validContent = "{\"contentId\" : \"188aZ\"}"
      val p1 = Put(uri = s"/watchlist/$userId").withEntity(ContentTypes.`application/json`, validContent)
      p1 ~> watchListRoutes.route ~> check {
        status should ===(StatusCodes.Created)
      }
      val p2 = Put(uri = s"/watchlist/$userId").withEntity(ContentTypes.`application/json`, validContent)
      p2 ~> watchListRoutes.route ~> check {
        status should ===(StatusCodes.Created)
      }
      val r1 = HttpRequest(uri = s"/watchlist/$userId")
      r1 ~> watchListRoutes.route ~> check {
        status should ===(StatusCodes.OK)
        contentType should ===(ContentTypes.`application/json`)
        entityAs[String] should ===("""{"ids":["188aZ"]}""")
      }
    }

    "when 4 content IDs are inserted (3 unique) into a second customer, it should return a list of 3 unique content IDs for the customer ID" in {
      val userId = "40001"
      val validContent1 = "{\"contentId\" : \"Df50z\"}"
      val validContent2 = "{\"contentId\" : \"xy4Mj\"}"
      val validContent3 = "{\"contentId\" : \"Ju9fp\"}"
      val p1 = Put(uri = s"/watchlist/$userId").withEntity(ContentTypes.`application/json`, validContent1)
      val p2 = Put(uri = s"/watchlist/$userId").withEntity(ContentTypes.`application/json`, validContent2)
      val p3 = Put(uri = s"/watchlist/$userId").withEntity(ContentTypes.`application/json`, validContent1)
      val p4 = Put(uri = s"/watchlist/$userId").withEntity(ContentTypes.`application/json`, validContent3)
      val request = HttpRequest(uri = s"/watchlist/$userId")

      p1 ~> watchListRoutes.route ~> check {
        status should ===(StatusCodes.Created)
      }

      p2 ~> watchListRoutes.route ~> check {
        status should ===(StatusCodes.Created)
      }

      p3 ~> watchListRoutes.route ~> check {
        status should ===(StatusCodes.Created)
      }

      p4 ~> watchListRoutes.route ~> check {
        status should ===(StatusCodes.Created)
      }

      request ~> watchListRoutes.route ~> check {
        status should ===(StatusCodes.OK)
        contentType should ===(ContentTypes.`application/json`)
        entityAs[String] should ===("""{"ids":["Df50z","Ju9fp","xy4Mj"]}""")
      }
    }

    "when 2 content IDs are inserted into a third customer and one deleted, it should return a list of 1 remaining content ID for that customer" in {
      val userId = "55883"
      val validContent1 = "{\"contentId\" : \"Fx123\"}"
      val validContent2 = "{\"contentId\" : \"BB666\"}"
      val p1 = Put(uri = s"/watchlist/$userId").withEntity(ContentTypes.`application/json`, validContent1)
      val p2 = Put(uri = s"/watchlist/$userId").withEntity(ContentTypes.`application/json`, validContent2)
      val d1 = Delete(uri = s"/watchlist/$userId").withEntity(ContentTypes.`application/json`, validContent2)
      val request = HttpRequest(uri = s"/watchlist/$userId")

      p1 ~> watchListRoutes.route ~> check {
        status should ===(StatusCodes.Created)
      }

      p2 ~> watchListRoutes.route ~> check {
        status should ===(StatusCodes.Created)
      }

      d1 ~> watchListRoutes.route ~> check {
        status should ===(StatusCodes.OK)
      }

      request ~> watchListRoutes.route ~> check {
        status should ===(StatusCodes.OK)
        contentType should ===(ContentTypes.`application/json`)
        entityAs[String] should ===("""{"ids":["Fx123"]}""")
      }
    }

    "given a customer with userID 12345 and an empty watchlist, when customer adds contentIDs 'crid1', 'crid2', 'crid3', 'crid4', 'crid5' to their watchlist should only contain these contentIDs" in {
      val userId = 12345
      val validContent1 = "{\"contentId\" : \"crid1\"}"
      val validContent2 = "{\"contentId\" : \"crid2\"}"
      val validContent3 = "{\"contentId\" : \"crid3\"}"
      val validContent4 = "{\"contentId\" : \"crid4\"}"
      val validContent5 = "{\"contentId\" : \"crid5\"}"
      val p1 = Put(uri = s"/watchlist/$userId").withEntity(ContentTypes.`application/json`, validContent1)
      val p2 = Put(uri = s"/watchlist/$userId").withEntity(ContentTypes.`application/json`, validContent2)
      val p3 = Put(uri = s"/watchlist/$userId").withEntity(ContentTypes.`application/json`, validContent3)
      val p4 = Put(uri = s"/watchlist/$userId").withEntity(ContentTypes.`application/json`, validContent4)
      val p5 = Put(uri = s"/watchlist/$userId").withEntity(ContentTypes.`application/json`, validContent5)
      val request = HttpRequest(uri = s"/watchlist/$userId")

      p1 ~> watchListRoutes.route ~> check {
        status should ===(StatusCodes.Created)
      }
      p2 ~> watchListRoutes.route ~> check {
        status should ===(StatusCodes.Created)
      }
      p3 ~> watchListRoutes.route ~> check {
        status should ===(StatusCodes.Created)
      }
      p4 ~> watchListRoutes.route ~> check {
        status should ===(StatusCodes.Created)
      }
      p5 ~> watchListRoutes.route ~> check {
        status should ===(StatusCodes.Created)
      }

      request ~> watchListRoutes.route ~> check {
        status should ===(StatusCodes.OK)
        contentType should ===(ContentTypes.`application/json`)
        entityAs[String] should ===("""{"ids":["crid1","crid2","crid3","crid4","crid5"]}""")
      }
    }

    "given a customer with userID 22345 with contentIDs 'crid1', 'crid2', 'crid3', 'crid4', 'crid5' in their watchlist. deleting 'crid1' then the watchlist will only contain 'crid2', 'crid3', 'crid4', 'crid5'" in {
      val userId = 22345
      val validContent1 = "{\"contentId\" : \"crid1\"}"
      val validContent2 = "{\"contentId\" : \"crid2\"}"
      val validContent3 = "{\"contentId\" : \"crid3\"}"
      val validContent4 = "{\"contentId\" : \"crid4\"}"
      val validContent5 = "{\"contentId\" : \"crid5\"}"
      val p1 = Put(uri = s"/watchlist/$userId").withEntity(ContentTypes.`application/json`, validContent1)
      val p2 = Put(uri = s"/watchlist/$userId").withEntity(ContentTypes.`application/json`, validContent2)
      val p3 = Put(uri = s"/watchlist/$userId").withEntity(ContentTypes.`application/json`, validContent3)
      val p4 = Put(uri = s"/watchlist/$userId").withEntity(ContentTypes.`application/json`, validContent4)
      val p5 = Put(uri = s"/watchlist/$userId").withEntity(ContentTypes.`application/json`, validContent5)
      val d1 = Delete(uri = s"/watchlist/$userId").withEntity(ContentTypes.`application/json`, validContent1)
      val request = HttpRequest(uri = s"/watchlist/$userId")

      p1 ~> watchListRoutes.route ~> check {
        status should ===(StatusCodes.Created)
      }
      p2 ~> watchListRoutes.route ~> check {
        status should ===(StatusCodes.Created)
      }
      p3 ~> watchListRoutes.route ~> check {
        status should ===(StatusCodes.Created)
      }
      p4 ~> watchListRoutes.route ~> check {
        status should ===(StatusCodes.Created)
      }
      p5 ~> watchListRoutes.route ~> check {
        status should ===(StatusCodes.Created)
      }
      d1 ~> watchListRoutes.route ~> check {
        status should ===(StatusCodes.OK)
      }

      request ~> watchListRoutes.route ~> check {
        status should ===(StatusCodes.OK)
        contentType should ===(ContentTypes.`application/json`)
        entityAs[String] should ===("""{"ids":["crid2","crid3","crid4","crid5"]}""")
      }
    }

    "given two customers with userID 33333 and 44444. userID 33333 has a watchlist of 'crid1', 'crid2', 'crid3'. userID 44444 has a watchlist of 'crid1'. Customer 33333 requests their watchlist and should get list 'crid1', 'crid2', 'crid3' " in {
      val userId1 = 33333
      val userId2 = 44444
      val validContent1 = "{\"contentId\" : \"crid1\"}"
      val validContent2 = "{\"contentId\" : \"crid2\"}"
      val validContent3 = "{\"contentId\" : \"crid3\"}"
      val p1 = Put(uri = s"/watchlist/$userId1").withEntity(ContentTypes.`application/json`, validContent1)
      val p2 = Put(uri = s"/watchlist/$userId1").withEntity(ContentTypes.`application/json`, validContent2)
      val p3 = Put(uri = s"/watchlist/$userId1").withEntity(ContentTypes.`application/json`, validContent3)
      val p4 = Put(uri = s"/watchlist/$userId2").withEntity(ContentTypes.`application/json`, validContent1)
      val request = HttpRequest(uri = s"/watchlist/$userId1")

      p1 ~> watchListRoutes.route ~> check {
        status should ===(StatusCodes.Created)
      }
      p2 ~> watchListRoutes.route ~> check {
        status should ===(StatusCodes.Created)
      }
      p3 ~> watchListRoutes.route ~> check {
        status should ===(StatusCodes.Created)
      }
      p4 ~> watchListRoutes.route ~> check {
        status should ===(StatusCodes.Created)
      }

      request ~> watchListRoutes.route ~> check {
        status should ===(StatusCodes.OK)
        contentType should ===(ContentTypes.`application/json`)
        entityAs[String] should ===("""{"ids":["crid1","crid2","crid3"]}""")
      }
    }

  }

}

