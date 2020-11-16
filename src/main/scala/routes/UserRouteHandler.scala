package routes

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.zaxxer.hikari.HikariDataSource

import model._
import database.handlers.UserActionHandler

import scala.concurrent.{ExecutionContext, Future}

case class UserRouteHandler()(implicit connectionPool: HikariDataSource, executionContext: ExecutionContext)
  extends RouteHandler {

  override val routes: Route =
    pathPrefix("users") {
      post {
        decodeRequest {
          entity(as[CreatableUser]) { user =>
            val result: Future[Result[User]] = Future(UserActionHandler.createUser(user))
            onComplete(result)(complete(_))
          }
        }
      } ~ get {
        path(IntNumber) { id =>
          val result: Future[Result[User]] = Future(UserActionHandler.getUser(id))
          onComplete(result)(complete(_))
        }
      } ~ put {
        authenticateUser { id =>
          decodeRequest {
            entity(as[UpdatableUser]) { user =>
              val result: Future[Result[User]] = Future(UserActionHandler.updateUser(id, user))
              onComplete(result)(complete(_))
            }
          }
        }
      } ~ delete {
        authenticateUser { id =>
          val result: Future[Result[NoRootElement]] = Future(UserActionHandler.deleteUser(id))
          onComplete(result)(complete(_))
        }
      }
  }

}
