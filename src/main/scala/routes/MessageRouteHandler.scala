package routes

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.zaxxer.hikari.HikariDataSource

import database.handlers.MessageActionHandler
import model._

import scala.concurrent.{ExecutionContext, Future}

case class MessageRouteHandler()(implicit connectionPool: HikariDataSource, executionContext: ExecutionContext)
  extends RouteHandler {
  override val routes: Route =
    pathPrefix("messages") {
      pathPrefix("server" / IntNumber) { server =>
        post {
          decodeRequest {
            entity(as[CreatableMessage]) { message =>
              authenticateMember(server) { id =>
                val result: Future[Result[ServerMessage]] =
                  MessageActionHandler.createServerMessage(message, server, id)
                onComplete(result)(complete(_))
              }
            }
          }
        } ~ get {
          parameters(
            Symbol("limit").as[Int].optional,
            Symbol("offset").as[Int].optional
          ) { (limit, offset) =>
            authenticateMember(server) { _ =>
              val result: Future[List[ChildMessage]] = MessageActionHandler.getServerMessages(
                server,
                limit.getOrElse(100),
                offset.getOrElse(0)
              )
              onComplete(result)(complete(_))
            }
          }
        }
      } ~ pathPrefix("direct" / IntNumber) { recipient =>
        post {
          decodeRequest {
            entity(as[CreatableMessage]) { message =>
              authenticateUser { id =>
                val result: Future[Result[DirectMessage]] =
                  MessageActionHandler.createDirectMessage(message, recipient, id)
                onComplete(result)(complete(_))
              }
            }
          }
        } ~ get {
          parameters(
            Symbol("limit").as[Int].optional,
            Symbol("offset").as[Int].optional
          ) { (limit, offset) =>
            authenticateUser { id =>
              val result: Future[List[ChildMessage]] = MessageActionHandler.getDirectMessages(
                id,
                recipient,
                limit.getOrElse(100),
                offset.getOrElse(0)
              )
              onComplete(result)(complete(_))
            }
          }
        }
      }
    }
}
