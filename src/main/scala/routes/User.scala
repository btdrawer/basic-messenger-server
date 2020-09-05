package routes

import java.sql.Connection

import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route

import model._

import scala.concurrent.Future

object User {
  def apply()(implicit connection: Connection): Route = path("users") {
    concat(
      post {
        entity(as[CreatableUser]) { user =>
          val result: Future[Result[RootUser]] = Future(UserActions.createUser(user))
          onSuccess(result) { r =>
            complete(r)
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
