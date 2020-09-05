package routes

import java.sql.Connection

import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route

import model._
import database.actions.{User => UserActions}
import converters.JsonConverters

import scala.concurrent.{ExecutionContext, Future}

object User extends JsonConverters {
  def apply()(implicit connection: Connection, executionContext: ExecutionContext): Route = path("users") {
    concat(
      post {
        decodeRequest {
          entity(as[CreatableUser]) { user =>
            println(user)
            complete {
              val result: Future[Result[RootUser]] = Future(UserActions.createUser(user))
              result
            }
          }
        }
      },
      get {
        complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, "<h1>Say hello to akka-http</h1>"))
      },
      get {
        pathPrefix(LongNumber) { id =>
          ???
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
