package routes

import java.sql.Connection

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route

import model._
import database.actions.{User => UserActions}
import converters.JsonConverters

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure => UtilFailure, Success => UtilSuccess}

object User extends JsonConverters {
  def apply()(implicit connection: Connection, executionContext: ExecutionContext): Route = path("users") {
    concat(
      post {
        decodeRequest {
          entity(as[CreatableUser]) { user =>
            val result: Future[Result[RootUser]] = Future(UserActions.createUser(user))
            onComplete(result) {
              case UtilSuccess(res) => complete(res)
              case UtilFailure(err) => complete(err)
            }
          }
        }
      },
      get {
        pathPrefix(LongNumber) { id =>
          val result: Future[Result[RootUser]] = Future(UserActions.getUser(id.toInt))
          onComplete(result) {
            case UtilSuccess(res) => complete(res)
            case UtilFailure(err) => complete(err)
          }
        }
      },
      put {
        pathPrefix(LongNumber) { id =>
          ???
        }
      },
      delete {
        pathPrefix(LongNumber) { id =>
          ???
        }
      }
    )
  }
}
