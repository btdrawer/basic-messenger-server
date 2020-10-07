package routes

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.zaxxer.hikari.HikariDataSource

import database.handlers.MessageActionHandler
import model._

import scala.concurrent.{ExecutionContext, Future}

case class MessageRouteHandler()(implicit connectionPool: HikariDataSource, executionContext: ExecutionContext)
  extends RouteHandler {
  def routes: Route =
    pathPrefix("messages") {
      concat(
        pathPrefix("server" / Segment) { server =>
          concat(
            post {
              decodeRequest {
                entity(as[CreatableMessage]) { message =>
                  authenticateMember(server) { id =>
                    val result: Future[Result[ServerMessage]] = Future(MessageActionHandler.createServerMessage(
                      message, server.toInt, id
                    ))
                    onComplete(result)(complete(_))
                  }
                }
              }
            },
            get {
              parameters("limit".optional, "offset".optional) { (limit, offset) =>
                authenticateMember(server) { _ =>
                  val result: Future[List[ChildMessage]] = Future(MessageActionHandler.getServerMessages(
                    server.toInt,
                    limit.getOrElse("100").toInt,
                    offset.getOrElse("0").toInt
                  ))
                  onComplete(result)(complete(_))
                }
              }
            }
          )
        },
        pathPrefix("direct" / Segment) { recipient =>
          concat(
            post {
              decodeRequest {
                entity(as[CreatableMessage]) { message =>
                  authenticateUser { id =>
                    val result: Future[Result[DirectMessage]] = Future(MessageActionHandler.createDirectMessage(
                      message, recipient.toInt, id
                    ))
                    onComplete(result)(complete(_))
                  }
                }
              }
            },
            get {
              parameters("limit".optional, "offset".optional) { (limit, offset) =>
                authenticateUser { id =>
                  val result: Future[List[ChildMessage]] = Future(MessageActionHandler.getDirectMessages(
                    id,
                    recipient.toInt,
                    limit.getOrElse("100").toInt,
                    offset.getOrElse("0").toInt
                  ))
                  onComplete(result)(complete(_))
                }
              }
            }
          )
        }
      )
    }
}
