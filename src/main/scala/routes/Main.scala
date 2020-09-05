package routes

import java.sql.Connection

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route

import scala.concurrent.ExecutionContext

object Main {
  def apply()(implicit connection: Connection, executionContext: ExecutionContext): Route = concat(
    User()
  )
}
