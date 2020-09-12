package routes

import java.sql.Connection

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route

import database.actions.MessageActions
import model._

import scala.concurrent.{ExecutionContext, Future}

object MessageRoutes extends JsonConverters {
  def apply()(implicit connection: Connection, executionContext: ExecutionContext): Route =
    pathPrefix("messages") {
      concat(
        post {
          decodeRequest {
            entity(as[CreatableMessage]) { message =>
              val result: Future[Result[Message]] = Future(MessageActions.createMessage(message))
              onComplete(result)(complete(_))
            }
          }
        }
      )
    }
}
