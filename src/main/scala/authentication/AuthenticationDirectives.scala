package authentication

import akka.http.scaladsl.server.Directives.authenticateBasic
import akka.http.scaladsl.server.directives.AuthenticationDirective
import com.zaxxer.hikari.HikariDataSource
import model.Role

import scala.concurrent.ExecutionContext

class AuthenticationDirectives(implicit connectionPool: HikariDataSource, executionContext: ExecutionContext) {
  final val REALM: String = "basic-messenger"

  private def authenticateRoles(roles: List[Role.Value], server: Int): AuthenticationDirective[Int] =
    authenticateBasic(realm = REALM, RoleAuthenticator(roles, server))

  def authenticateAdmin(server: Int): AuthenticationDirective[Int] =
    authenticateRoles(roles = List(Role.ADMIN), server)

  def authenticateModerator(server: Int): AuthenticationDirective[Int] =
    authenticateRoles(roles = List(Role.ADMIN, Role.MODERATOR), server)

  def authenticateMember(server: Int): AuthenticationDirective[Int] =
    authenticateRoles(roles = List(Role.ADMIN, Role.MODERATOR, Role.MEMBER), server)

  def authenticateUser: AuthenticationDirective[Int] =
    authenticateBasic(realm = REALM, BasicAuthenticator.apply)
}
