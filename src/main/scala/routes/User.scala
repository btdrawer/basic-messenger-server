package routes

import java.sql.Connection

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route

import model._
import database.actions.{User => UserActions}
import converters.JsonConverters

import scala.concurrent.{ExecutionContext, Future}

object User extends JsonConverters {
   def apply()(implicit connection: Connection, executionContext: ExecutionContext): Route = pathPrefix("users") {
    concat(
      post {
        decodeRequest {
          entity(as[CreatableUser]) { user =>
            val result: Future[Result[RootUser]] = Future(UserActions.createUser(user))
            onComplete(result)(complete(_))
          }
        }
      },
      get {
        path(Segment) { id =>
          val result: Future[Result[RootUser]] = Future(UserActions.getUser(id.toInt))
          onComplete(result)(complete(_))
        }
      },
      put {
        path(Segment) { id =>
          ???
        }
      },
      delete {
        path(Segment) { id =>
          ???
        }
      }
    )
  }
}
