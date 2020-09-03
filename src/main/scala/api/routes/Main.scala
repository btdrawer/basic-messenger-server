package api.routes

import java.sql.Connection

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route

object Main {
  def apply()(implicit connection: Connection): Route = concat(
    User()
  )
}
