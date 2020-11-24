package database.handlers

import com.zaxxer.hikari.HikariDataSource
import model._
import database.queries.UserQueries
import authentication.{AuthData, HashPassword}

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration._

object UserActionHandler extends ActionHandler {
  def getAuthData(username: String)
      (implicit connectionPool: HikariDataSource, executionContext: ExecutionContext): Future[Option[AuthData]] =
    runAndGetFirst(UserQueries.getAuthData, List(username)) {
      case Some(rs) => Some(
        AuthData(
          id = rs.getInt(1),
          password = rs.getString(2),
          salt = rs.getString(3)
        )
      )
      case None => throw ApiException(FailureMessages.LOGIN_INCORRECT)
    }

  def usernameIsNotTaken(username: String)
      (implicit connectionPool: HikariDataSource, executionContext: ExecutionContext): Future[Unit] =
    runAndGetFirst(UserQueries.checkUsernameExists, List(username)) { user =>
      if (user.nonEmpty) throw ApiException(FailureMessages.USERNAME_EXISTS)
      else ()
    }

  def userIdExists(id: Int)
      (implicit connectionPool: HikariDataSource, executionContext: ExecutionContext): Future[Unit] =
    runAndGetFirst(UserQueries.getUser, List(id)) { user =>
      if (user.isEmpty) throw ApiException(FailureMessages.USER_NOT_FOUND)
      else ()
    }

  def checkPasswordIsValid(password: String): Boolean =
    password.length > 7 &&
      password.matches(".*[0-9].*") &&
      password.matches(".*[a-z].*") &&
      password.matches(".*[A-Z].*")

  def createUser(user: CreatableUser)
      (implicit connectionPool: HikariDataSource, executionContext: ExecutionContext): Future[Result[User]] =
    if (!checkPasswordIsValid(user.password))
      throw ApiException(FailureMessages.PASSWORD_INVALID)
    else for {
      _ <- usernameIsNotTaken(user.username)
      hashedPassword = HashPassword(user.password)
      u <- runAndGetFirst(
        UserQueries.createUser,
        List(
          user.username,
          hashedPassword.password,
          hashedPassword.salt,
          user.passwordReset.question,
          user.passwordReset.answer
        )
      ) {
        case Some(rs) => Success(
          result = Some(
            User(
              id = rs.getInt(1),
              username = rs.getString(2),
              servers = List[UserServerRole](),
              status = Status.withName(rs.getString(3))
            )
          ),
          message = Some("User successfully created.")
        )
        case None => throw ApiException(FailureMessages.GENERIC)
      }
    } yield u

  private def getUserServers(id: Int)
      (implicit connectionPool: HikariDataSource, executionContext: ExecutionContext): Future[List[UserServerRole]] =
    runAndIterate(
      UserQueries.getUserServers,
      List(id),
      resultSet => UserServerRole(
        server = ChildServer(
          id = resultSet.getInt(1),
          name = resultSet.getString(2),
          address = resultSet.getString(3)
        ),
        role = Role.withName(resultSet.getString(4))
      )
    )

  def getUser(id: Int)
      (implicit connectionPool: HikariDataSource, executionContext: ExecutionContext): Future[Result[User]] =
    for {
      servers <- getUserServers(id)
      result <- runAndGetFirst(UserQueries.getUser, List(id)) {
        case Some(rs) =>
          Success(
            result = Some(
              User(
                id,
                username = rs.getString(1),
                servers,
                status = Status.withName(rs.getString(2))
              )
            ),
            message = None
          )
        case None => throw ApiException(FailureMessages.USER_NOT_FOUND)
      }
    } yield result

  def getUserAsChildElement(id: Int)
      (implicit connectionPool: HikariDataSource, executionContext: ExecutionContext): Future[ChildUser] =
    runAndGetFirst(UserQueries.getUser, List(id)) {
      case Some(rs) => ChildUser(
        id,
        username = rs.getString(1),
        status = Status.withName(rs.getString(2))
      )
      case None => throw ApiException(FailureMessages.USER_NOT_FOUND)
    }

  def updateUser(id: Int, user: UpdatableUser)
      (implicit connectionPool: HikariDataSource, executionContext: ExecutionContext): Future[Result[User]] =
    for {
      _ <- runUpdate(UserQueries.updateUser, user.toParameterList :+ id)
      user <- getUser(id)
    } yield Success(
      result = user.result,
      message = Some("User updated successfully.")
    )

  def deleteUser(id: Int)
      (implicit connectionPool: HikariDataSource, executionContext: ExecutionContext): Future[Result[NoRootElement]] =
    for {
      _ <- userIdExists(id)
      _ <- runUpdate(UserQueries.deleteUser, List(id, id, id, id, id))
    } yield Success(
      result = None,
      message = Some("User deleted.")
    )
}
