package routes

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.zaxxer.hikari.HikariDataSource

import model._
import database.handlers.ServerActionHandler

import scala.concurrent.{ExecutionContext, Future}

case class ServerRouteHandler()(implicit connectionPool: HikariDataSource, executionContext: ExecutionContext)
  extends RouteHandler {
  override val routes: Route =
    pathPrefix("servers") {
      post {
        authenticateUser { id =>
          decodeRequest {
            entity(as[CreatableServer]) { server =>
              val result: Future[Result[Server]] = ServerActionHandler.createServer(server, id)
              onComplete(result)(complete(_))
            }
          }
        }
      } ~ get {
        path("search" / Segment) { name =>
          val result: Future[List[ChildServer]] = ServerActionHandler.findServers(name)
          onComplete(result)(complete(_))
        }
      } ~ get {
        path("id" / IntNumber) { id =>
          val result: Future[Result[Server]] = ServerActionHandler.getServerById(id)
          onComplete(result)(complete(_))
        }
      } ~ get {
        path("address" / Segment) { address =>
          val result: Future[Result[Server]] = ServerActionHandler.getServerByAddress(address)
          onComplete(result)(complete(_))
        }
      } ~ put {
        path(IntNumber) { id =>
          authenticateAdmin(id) { _ =>
            decodeRequest {
              entity(as[UpdatableServer]) { server =>
                val result: Future[Result[NoRootElement]] = ServerActionHandler.updateServer(id, server)
                onComplete(result)(complete(_))
              }
            }
          }
        }
      } ~ put {
        path(IntNumber / "users" / IntNumber) { (server, user) =>
          authenticateModerator(server) { _ =>
            val result: Future[Result[NoRootElement]] = ServerActionHandler.addServerUser(server, user)
            onComplete(result)(complete(_))
          }
        }
      } ~ put {
        path(IntNumber / "users" / IntNumber / "roles" / Segment) { (server, member, role) =>
          authenticateAdmin(server) { _ =>
            val result: Future[Result[NoRootElement]] =
              ServerActionHandler.updateUserRole(server, member, Role.withName(role))
            onComplete(result)(complete(_))
          }
        }
      } ~ delete {
        path(IntNumber) { id =>
          authenticateAdmin(id) { _ =>
            val result: Future[Result[NoRootElement]] = ServerActionHandler.deleteServer(id)
            onComplete(result)(complete(_))
          }
        }
      } ~ delete {
        path(IntNumber / "users" / IntNumber) { (server, user) =>
          authenticateModerator(server) { _ =>
            val result: Future[Result[NoRootElement]] = ServerActionHandler.removeServerUser(server, user)
            onComplete(result)(complete(_))
          }
        }
      }
    }
  }

