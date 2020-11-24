package authentication

import akka.http.scaladsl.server.directives.Credentials
import akka.http.scaladsl.server.directives.Credentials._
import com.zaxxer.hikari.HikariDataSource
import database.handlers.{ServerActionHandler, UserActionHandler}
import model.{ApiException, FailureMessages, Role}

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration._

case class AuthData(id: Int, password: String, salt: String)

object BasicAuthenticator {
  private def verify(p: Provided, username: String)
      (implicit connectionPool: HikariDataSource, executionContext: ExecutionContext): Future[Option[Int]] =
    for {
      authData <- UserActionHandler.getAuthData(username)
    } yield authData match {
      case Some(d) =>
        if (p.verify(d.password, HashPassword.verify(d.salt))) Some(d.id)
        else throw ApiException(FailureMessages.LOGIN_INCORRECT)
      case _ => throw ApiException(FailureMessages.LOGIN_INCORRECT)
    }

  def apply(credentials: Credentials)
      (implicit connectionPool: HikariDataSource, executionContext: ExecutionContext): Option[Int] =
    credentials match {
      case p @ Provided(username) => Await.result(verify(p, username), 5 seconds)
      case _ => throw ApiException(FailureMessages.LOGIN_INCORRECT)
    }
}

object RoleAuthenticator {
  private def hasCorrectRole(roles: List[Role.Value], server: Int, user: Int)
      (implicit connectionPool: HikariDataSource, executionContext: ExecutionContext): Future[Option[Int]] =
    for {
      _ <- ServerActionHandler.serverIdExists(server)
      userRole <- ServerActionHandler.getServerUser(server, user)
    } yield {
      if (roles.contains(userRole.role)) Some(user)
      else throw ApiException(FailureMessages.INSUFFICIENT_PERMISSIONS)
    }

  def apply(roles: List[Role.Value], server: Int)
      (credentials: Credentials)
      (implicit connectionPool: HikariDataSource, executionContext: ExecutionContext): Option[Int] =
    BasicAuthenticator(credentials) match {
      case Some(user) => Await.result(hasCorrectRole(roles, server, user), 5 seconds)
      case _ => throw ApiException(FailureMessages.LOGIN_INCORRECT)
    }
}
