package routes

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.zaxxer.hikari.HikariDataSource

import model._
import database.handlers.ServerActionHandler

import scala.concurrent.{ExecutionContext, Future}

case class ServerRouteHandler()(implicit connectionPool: HikariDataSource, executionContext: ExecutionContext)
  extends RouteHandler {
  def routes: Route =
    pathPrefix("servers") {
      concat(
        post {
          authenticateUser { id =>
            decodeRequest {
              entity(as[CreatableServer]) { server =>
                val result: Future[Result[Server]] = Future(ServerActionHandler.createServer(server, id))
                onComplete(result)(complete(_))
              }
            }
          }
        },
        get {
          path("search" / Segment) { name =>
            val result: Future[List[ChildServer]] = Future(ServerActionHandler.findServers(name))
            onComplete(result)(complete(_))
          }
        },
        get {
          path("id" / Segment) { id =>
            val result: Future[Result[Server]] = Future(ServerActionHandler.getServerById(id.toInt))
            onComplete(result)(complete(_))
          }
        },
        get {
          path("address" / Segment) { address =>
            val result: Future[Result[Server]] = Future(ServerActionHandler.getServerByAddress(address))
            onComplete(result)(complete(_))
          }
        },
        put {
          path(Segment) { id =>
            authenticateAdmin(id) { _ =>
              decodeRequest {
                entity(as[UpdatableServer]) { server =>
                  val result: Future[Result[NoRootElement]] =
                    Future(ServerActionHandler.updateServer(id.toInt, server))
                  onComplete(result)(complete(_))
                }
              }
            }
          }
        },
        put {
          path(Segment / "users" / Segment) { (server, user) =>
            authenticateModerator(server) { _ =>
              val result: Future[Result[NoRootElement]] =
                Future(ServerActionHandler.addServerUser(server.toInt, user.toInt))
              onComplete(result)(complete(_))
            }
          }
        },
        put {
          path(Segment / "users" / Segment / "roles" / Segment) { (server, member, role) =>
            authenticateAdmin(server) { _ =>
              val result: Future[Result[NoRootElement]] =
                Future(ServerActionHandler.updateUserRole(server.toInt, member.toInt, Role.withName(role)))
              onComplete(result)(complete(_))
            }
          }
        },
        delete {
          path(Segment) { id =>
            authenticateAdmin(id) { _ =>
              val result: Future[Result[NoRootElement]] = Future(ServerActionHandler.deleteServer(id.toInt))
              onComplete(result)(complete(_))
            }
          }
        },
        delete {
          path(Segment / "users" / Segment) { (server, user) =>
            authenticateModerator(server) { _ =>
              val result: Future[Result[NoRootElement]] =
                Future(ServerActionHandler.removeServerUser(server.toInt, user.toInt))
              onComplete(result)(complete(_))
            }
          }
        },
      )
    }
  }

