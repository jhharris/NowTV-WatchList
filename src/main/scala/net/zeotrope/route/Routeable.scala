package net.zeotrope.route

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.{ Directives, ExceptionHandler, Route }
import com.typesafe.scalalogging.LazyLogging

import scala.util.control.NonFatal

trait Routeable extends Directives with LazyLogging {
  def route: Route

  val expHander = ExceptionHandler {
    case NonFatal(e) =>
      extractRequest { req =>
        val loggingString = s"${req.method.value}:${req.uri}"
        logger.error(loggingString, e)
        complete(StatusCodes.InternalServerError, "Something is broken.")
      }
  }
}

