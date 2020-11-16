package routes

import akka.http.scaladsl.server.Route
import authentication.AuthenticationDirectives
import com.zaxxer.hikari.HikariDataSource

import model.JsonConverters

import scala.concurrent.ExecutionContext

abstract class RouteHandler(implicit connectionPool: HikariDataSource, executionContext: ExecutionContext)
  extends AuthenticationDirectives
    with JsonConverters {
  val routes: Route
}
