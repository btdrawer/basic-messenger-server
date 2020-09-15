package routes

import java.sql.Connection

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route

import model._
import database.actions.UserActions

import scala.concurrent.{ExecutionContext, Future}

case class UserRoutes()(implicit connection: Connection, executionContext: ExecutionContext) extends Routes {
  def routes: Route = pathPrefix("users") {
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
        authenticateUser { id =>
          decodeRequest {
            entity(as[UpdatableUser]) { user =>
              val result: Future[Result[User]] = Future(UserActions.updateUser(id, user))
              onComplete(result)(complete(_))
            }
          }
        }
      },
      delete {
        authenticateUser { id =>
          val result: Future[Result[NoRootElement]] = Future(UserActions.deleteUser(id))
          onComplete(result)(complete(_))
        }
      }
    )
  }
}
