package routes

import akka.http.scaladsl.server.Route
import authentication.Directives
import com.zaxxer.hikari.HikariDataSource

import model.JsonConverters

import scala.concurrent.ExecutionContext

abstract class RouteHandler(implicit connectionPool: HikariDataSource, executionContext: ExecutionContext)
  extends Directives with JsonConverters {
  val routes: Route
}
