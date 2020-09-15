package routes

import java.sql.Connection

import akka.http.scaladsl.server.Directives.authenticateBasic
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.AuthenticationDirective

import authentication.{BasicAuthenticator, RoleAuthenticator}
import model.Role
import model.converters.JsonConverters

import scala.concurrent.ExecutionContext

abstract class Routes(implicit connection: Connection, executionContext: ExecutionContext) extends JsonConverters {
  def routes: Route

  private def authenticateRoles(roles: List[Role.Value], server: String): AuthenticationDirective[Int] =
    authenticateBasic(realm = "secure site", RoleAuthenticator(roles, server.toInt))

  def authenticateAdmin(server: String): AuthenticationDirective[Int] =
    authenticateRoles(roles = List(Role.ADMIN), server)

  def authenticateModerator(server: String): AuthenticationDirective[Int] =
    authenticateRoles(roles = List(Role.ADMIN, Role.MODERATOR), server)

  def authenticateUser: AuthenticationDirective[Int] =
    authenticateBasic(realm = "secure site", BasicAuthenticator.apply)
}
