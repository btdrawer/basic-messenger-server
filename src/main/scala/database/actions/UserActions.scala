package database.actions

import java.sql.Connection

import model._
import database.Query
import database.queries.{ServerQueries, UserQueries}

object UserActions {
  private def checkUsernameExists(username: String)(implicit connection: Connection): Boolean = {
    val resultSet = Query.runQuery(
      UserQueries.checkUsernameExists,
      List(username)
    )
    resultSet.first()
    resultSet.getRow > 0
  }

  private def checkPasswordIsValid(password: String): Boolean =
    password.length > 7 &&
      password.matches(".*[0-9].*") &&
      password.matches(".*[a-z].*") &&
      password.matches(".*[A-Z].*")

  def createUser(user: CreatableUser)(implicit connection: Connection): Result[User] = {
    val usernameExists = checkUsernameExists(user.username)
    val passwordIsValid = checkPasswordIsValid(user.password)
    if (usernameExists) throw ApiException(FailureMessages.USERNAME_EXISTS)
    else if (!passwordIsValid) throw ApiException(FailureMessages.PASSWORD_INVALID)
    else {
      val resultSet = Query.runQuery(
        UserQueries.createUser,
        List(
          user.username,
          user.password,
          user.passwordReset.question,
          user.passwordReset.answer
        )
      )
      resultSet.first()
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
    val resultSet = Query.runQuery(UserQueries.getUser, List(id))
    resultSet.first()
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

  // def updateUser(user: UpdatableUser)(implicit connection: Connection): model.results.UserResult = ???

  def updateUsername(id: Int, username: String)(implicit connection: Connection): Result[User] = {
    val usernameExists = checkUsernameExists(username)
    if (usernameExists) throw ApiException(FailureMessages.USERNAME_EXISTS)
    else {
      val resultSet = Query.runQuery(
        UserQueries.updateUsername,
        List(username, id)
      )
      resultSet.first()
      Success(
        result = None,
        message = Some("Your username has been updated.")
      )
    }
  }

  def updateStatus(id: Int, status: Status.Value)(implicit connection: Connection): Result[User] = {
    val resultSet = Query.runQuery(
      UserQueries.updateUsername,
      List(status.toString, id)
    )
    resultSet.first()
    Success(
      result = None,
      message = Some("Your status has been updated.")
    )
  }

  def deleteUser(id: Int)(implicit connection: Connection): Result[NoRootElement] = {
    Query.runUpdate(UserQueries.deleteUser, List(id))
    Success(
      result = None,
      message = Some("User deleted.")
    )
  }
}
