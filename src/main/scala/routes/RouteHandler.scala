package routes

import java.sql.Connection

import akka.http.scaladsl.server.Route
import authentication.Directives
import model.JsonConverters

import scala.concurrent.ExecutionContext

abstract class RouteHandler(implicit connection: Connection, executionContext: ExecutionContext)
  extends Directives with JsonConverters {
  def routes: Route
}
