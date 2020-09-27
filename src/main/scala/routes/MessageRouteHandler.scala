package routes

import java.sql.Connection

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import database.handlers.{MessageActionHandler, ServerActionHandler}
import model._

import scala.concurrent.{ExecutionContext, Future}

case class MessageRouteHandler()(implicit connection: Connection, executionContext: ExecutionContext)
  extends RouteHandler {
  def routes: Route =
    pathPrefix("messages") {
      concat(
        post {
          decodeRequest {
            entity(as[CreatableMessage]) { message =>
              authenticateMember(message.server.toString) { id =>
                val result: Future[Result[Message]] = Future(MessageActionHandler.createMessage(message, id))
                onComplete(result)(complete(_))
              }
            }
          }
        },
        get {
          parameters("server", "limit".optional, "offset".optional) { (server, limit, offset) =>
            authenticateMember(server) { _ =>
              val result: Future[List[ChildMessage]] = Future(ServerActionHandler.getServerMessages(
                server.toInt,
                limit.getOrElse("100").toInt,
                offset.getOrElse("0").toInt
              ))
              onComplete(result)(complete(_))
            }
          }
        }
      )
    }
}
