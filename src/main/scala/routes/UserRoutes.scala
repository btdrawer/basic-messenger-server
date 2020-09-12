package routes

import java.sql.Connection

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route

import scala.concurrent.{ExecutionContext, Future}
import model.{JsonConverters, _}
import database.actions.UserActions

import scala.util.{Failure, Success}

object UserRoutes extends JsonConverters {
   def apply()(implicit connection: Connection, executionContext: ExecutionContext): Route = pathPrefix("users") {
    concat(
      post {
        decodeRequest {
          entity(as[CreatableUser]) { user =>
            val result: Future[Result[User]] = Future(UserActions.createUser(user))
            onComplete(result)(complete(_))
          }
        }
      },
      get {
        path(Segment) { id =>
          val result: Future[Result[User]] = Future(UserActions.getUser(id.toInt))
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
          val result: Future[Result[NoRootElement]] = Future(UserActions.deleteUser(id.toInt))
          onComplete(result)(complete(_))
        }
      }
    )
  }
}
