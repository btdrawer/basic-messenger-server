package database.actions

import java.sql.Connection

import model._
import database.Query
import database.queries.UserQueries

object UserActions extends UpdatableConverters {
  def usernameExists(username: String)(implicit connection: Connection): Boolean = {
    val resultSet = Query.runAndGetFirst(UserQueries.checkUsernameExists, List(username))
    resultSet.getRow > 0
  }

  def userIdExists(id: Int)(implicit connection: Connection): Boolean = {
    val resultSet = Query.runAndGetFirst(UserQueries.getUser, List(id))
    resultSet.getRow > 0
  }

  def checkPasswordIsValid(password: String): Boolean =
    password.length > 7 &&
      password.matches(".*[0-9].*") &&
      password.matches(".*[a-z].*") &&
      password.matches(".*[A-Z].*")

  def createUser(user: CreatableUser)(implicit connection: Connection): Result[User] =
    if (!checkPasswordIsValid(user.password))
      throw ApiException(FailureMessages.PASSWORD_INVALID)
    else if (usernameExists(user.username))
      throw ApiException(FailureMessages.USERNAME_EXISTS)
    else {
      val resultSet = Query.runAndGetFirst(
        UserQueries.createUser,
        List(
          user.username,
          user.password,
          user.passwordReset.question,
          user.passwordReset.answer
        )
      )
      Success(
        result = Some(
          User(
            id = resultSet.getInt(1),
            username = resultSet.getString(2),
            servers = List[UserServerRole](),
            status = Status.withName(resultSet.getString(3))
          )
        ),
        message = Some("User successfully created.")
      )
    }

  private def getUserServers(id: Int)(implicit connection: Connection): List[UserServerRole] =
    Query.runAndIterate(
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
    val resultSet = Query.runAndGetFirst(UserQueries.getUser, List(id))
    if (resultSet.getRow < 1) throw ApiException(FailureMessages.USER_NOT_FOUND)
    else {
      val servers = getUserServers(id)
      Success(
        result = Some(
          User(
            id = id,
            username = resultSet.getString(1),
            servers,
            status = Status.withName(resultSet.getString(2))
          )
        ),
        message = None
      )
    }
  }

  def updateUser(id: Int, user: UpdatableUser)(implicit connection: Connection): Result[User] = {
    Query.runUpdate(UserQueries.updateUser, user.toParameterList :+ id)
    val userResult = getUser(id)
    Success(
      result = userResult.result,
      message = Some("User updated successfully.")
    )
  }

  def updateUsername(id: Int, username: String)(implicit connection: Connection): Result[User] = {
    if (usernameExists(username))
      throw ApiException(FailureMessages.USERNAME_EXISTS)
    else {
      Query.runUpdate(UserQueries.updateUsername, List(username, id))
      Success(
        result = None,
        message = Some("Your username has been updated.")
      )
    }
  }

  def updateStatus(id: Int, status: Status.Value)(implicit connection: Connection): Result[User] = {
    Query.runUpdate(UserQueries.updateUsername, List(status.toString, id))
    Success(
      result = None,
      message = Some("Your status has been updated.")
    )
  }

  def deleteUser(id: Int)(implicit connection: Connection): Result[NoRootElement] =
    if (!userIdExists(id)) throw ApiException(FailureMessages.USER_NOT_FOUND)
    else {
      Query.runUpdate(UserQueries.deleteUser, List(id, id, id))
      Success(
        result = None,
        message = Some("User deleted.")
      )
    }
}
