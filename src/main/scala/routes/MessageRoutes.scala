package routes

import java.sql.Connection

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route

import database.actions.MessageActions
import model._

import scala.concurrent.{ExecutionContext, Future}

case class MessageRoutes()(implicit connection: Connection, executionContext: ExecutionContext) extends Routes {
  def routes: Route =
    pathPrefix("messages") {
      concat(
        post {
          authenticateUser { id =>
            decodeRequest {
              entity(as[CreatableMessage]) { message =>
                val result: Future[Result[Message]] = Future(MessageActions.createMessage(message, id))
                onComplete(result)(complete(_))
              }
            }
          }
        }
      )
    }
}
