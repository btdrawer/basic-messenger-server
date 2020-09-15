package authentication

import java.sql.Connection

import akka.http.scaladsl.server.directives.Credentials
import akka.http.scaladsl.server.directives.Credentials._

import database.actions.{ServerActions, UserActions}
import model.{ApiException, FailureMessages, Role}

object BasicAuthenticator {
  private def verify(p: Provided, username: String)(implicit connection: Connection): Option[Int] = {
    val authData = UserActions.getAuthData(username)
    authData match {
      case Some(d) =>
        if (p.verify(d.password, HashPassword.verify(d.salt))) Some(d.id)
        else throw ApiException(FailureMessages.LOGIN_INCORRECT)
      case _ => throw ApiException(FailureMessages.LOGIN_INCORRECT)
    }
  }

  def apply(credentials: Credentials)(implicit connection: Connection): Option[Int] = credentials match {
    case p @ Provided(username) => verify(p, username)
    case _ => throw ApiException(FailureMessages.LOGIN_INCORRECT)
  }
}

object RoleAuthenticator {
  private def hasCorrectRole(
    roles: List[Role.Value],
    server: Int,
    user: Int
  )(implicit connection: Connection): Option[Int] = {
    val serverIdExists = ServerActions.serverIdExists(server)
    if (!serverIdExists) throw ApiException(FailureMessages.SERVER_NOT_FOUND)
    else {
      val userRole = ServerActions.getServerUser(server, user)
      if (roles.contains(userRole.role)) Some(user)
      else throw ApiException(FailureMessages.INSUFFICIENT_PERMISSIONS)
    }
  }

  def apply(roles: List[Role.Value], server: Int)
           (credentials: Credentials)
           (implicit connection: Connection): Option[Int] = BasicAuthenticator(credentials) match {
    case Some(user) => hasCorrectRole(roles, server, user)
    case _ => throw ApiException(FailureMessages.LOGIN_INCORRECT)
  }
}
