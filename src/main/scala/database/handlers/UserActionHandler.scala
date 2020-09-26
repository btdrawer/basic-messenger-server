package database.handlers

import java.sql.Connection

import model._
import database.queries.UserQueries
import authentication.{AuthData, HashPassword}

object UserActionHandler extends ActionHandler {
  def getAuthData(username: String)(implicit connection: Connection): Option[AuthData] = {
    val resultSet = runAndGetFirst(UserQueries.getAuthData, List(username))
    resultSet match {
      case Some(rs) => Some(
        AuthData(
          id = rs.getInt(1),
          password = rs.getString(2),
          salt = rs.getString(3)
        )
      )
      case None => throw ApiException(FailureMessages.LOGIN_INCORRECT)
    }
  }

  def usernameExists(username: String)(implicit connection: Connection): Boolean =
    runAndGetFirst(UserQueries.checkUsernameExists, List(username)).nonEmpty

  def userIdExists(id: Int)(implicit connection: Connection): Boolean =
    runAndGetFirst(UserQueries.getUser, List(id)).nonEmpty

  def checkPasswordIsValid(password: String): Boolean =
    password.length > 7 &&
      password.matches(".*[0-9].*") &&
      password.matches(".*[a-z].*") &&
      password.matches(".*[A-Z].*")

  def createUser(user: CreatableUser)(implicit connection: Connection): Result[User] =
    if (!checkPasswordIsValid(user.password)) throw ApiException(FailureMessages.PASSWORD_INVALID)
    else if (usernameExists(user.username)) throw ApiException(FailureMessages.USERNAME_EXISTS)
    else {
      val hashedPassword = HashPassword(user.password)
      runAndGetFirst(
        UserQueries.createUser,
        List(
          user.username,
          hashedPassword.password,
          hashedPassword.salt,
          user.passwordReset.question,
          user.passwordReset.answer
        )
      ) match {
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
    }

  private def getUserServers(id: Int)(implicit connection: Connection): List[UserServerRole] =
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

  def getUser(id: Int)(implicit connection: Connection): Result[User] = {
    val resultSet = runAndGetFirst(UserQueries.getUser, List(id))
    resultSet match {
      case Some(rs) =>
        val servers = getUserServers(id)
        Success(
          result = Some(
            User(
              id = id,
              username = rs.getString(1),
              servers,
              status = Status.withName(rs.getString(2))
            )
          ),
          message = None
        )
      case None => throw ApiException(FailureMessages.USER_NOT_FOUND)
    }
  }

  def updateUser(id: Int, user: UpdatableUser)(implicit connection: Connection): Result[User] = {
    runUpdate(UserQueries.updateUser, user.toParameterList :+ id)
    val userResult = getUser(id)
    Success(
      result = userResult.result,
      message = Some("User updated successfully.")
    )
  }

  def deleteUser(id: Int)(implicit connection: Connection): Result[NoRootElement] =
    if (!userIdExists(id)) throw ApiException(FailureMessages.USER_NOT_FOUND)
    else {
      runUpdate(UserQueries.deleteUser, List(id, id, id))
      Success(
        result = None,
        message = Some("User deleted.")
      )
    }
}
