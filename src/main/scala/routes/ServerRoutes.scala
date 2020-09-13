package routes

import java.sql.Connection

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route

import database.actions.{ServerActions, UserActions}
import model._

import scala.concurrent.{ExecutionContext, Future}

object ServerRoutes extends JsonConverters {
  def apply()(implicit connection: Connection, executionContext: ExecutionContext): Route =
    pathPrefix("servers") {
      concat(
        post {
          decodeRequest {
            entity(as[CreatableServer]) { server =>
              val result: Future[Result[Server]] = Future(ServerActions.createServer(server))
              onComplete(result)(complete(_))
            }
          }
        },
        get {
          path("search" / Segment) { name =>
            val result: Future[List[ChildServer]] = Future(ServerActions.findServers(name))
            onComplete(result)(complete(_))
          }
        },
        get {
          path("id" / Segment) { id =>
            val result: Future[Result[Server]] = Future(ServerActions.getServerById(id.toInt))
            onComplete(result)(complete(_))
          }
        },
        get {
          path("address" / Segment) { address =>
            val result: Future[Result[Server]] = Future(ServerActions.getServerByAddress(address))
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