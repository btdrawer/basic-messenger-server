package routes

import java.sql.Connection

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route

import database.handlers.MessageActionHandler
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
        }
      )
    }
}
