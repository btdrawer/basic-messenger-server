package database.actions

import java.sql.Connection

import api.model.{ApiServer, BasicApiServer, BasicApiUser, UserResult}
import database.queries.{User => UserQueries}
import model.{Role, Status, User}

object User {
  private def createFailedUserResult(message: String): UserResult = UserResult(
    success = false,
    user = None,
    message = Some(message)
  )

  private def checkUsernameExists(username: String)(implicit connection: Connection): Boolean = {
    val statement = connection.prepareStatement(UserQueries.checkUsernameExists)
    statement.setString(1, username)
    val resultSet = statement.executeQuery()
    resultSet.last()
    resultSet.getRow > 0
  }

  private def checkPasswordIsValid(password: String): Boolean =
    password.length > 7 &&
      password.matches(".*[0-9].*") &&
      password.matches(".*[a-z].*") &&
      password.matches(".*[A-Z].*")

  def createUser(user: User)(implicit connection: Connection): UserResult = {
    val usernameExists = checkUsernameExists(user.username)
    val passwordIsValid = checkPasswordIsValid(user.password)

    if (usernameExists) createFailedUserResult(
      "A user with that username already exists."
    )
    else if (!passwordIsValid) createFailedUserResult(
      "Your password must be at least 8 characters and contain " +
        "at least one lowercase letter, uppercase letter, and number."
    )
    else {
      val createStatement = connection.prepareStatement(UserQueries.createUser)
      createStatement.setString(1, user.username)
      createStatement.setString(2, user.password)
      createStatement.setString(3, user.status.id)
      createStatement.executeUpdate()

      val passwordResetStatement = connection.prepareStatement(UserQueries.createPasswordResetData)
      passwordResetStatement.setString(1, user.passwordReset.question.id)
      passwordResetStatement.setString(2, user.passwordReset.answer)
      passwordResetStatement.executeUpdate()

      UserResult(
        success = true,
        user = Some(
          BasicApiUser(
            id = user.id,
            username = user.username,
            servers = Map[BasicApiServer, Role.Value](),
            status = user.status
          )
        ),
        message = Some("User successfully created.")
      )
    }
  }

  private def getUserServers(id: String)(implicit connection: Connection): Map[ApiServer, Role.Value] = {
    val statement = connection.prepareStatement(UserQueries.getUserServers)
    statement.setString(1, id)
    val resultSet = statement.executeQuery()
    resultSet.last()

    val userMap = Map[ApiServer, Role.Value]()
    while(resultSet.next()) {
      userMap + (
        BasicApiServer(
          resultSet.getString(1),
          resultSet.getString(2),
          resultSet.getString(3)
        ) -> resultSet.getString(4)
      )
    }
    userMap
  }

  def getUserProfile(id: String)(implicit connection: Connection): UserResult = {
    val statement = connection.prepareStatement(UserQueries.getUser)
    statement.setString(1, id)
    val resultSet = statement.executeQuery()
    resultSet.last()

    if (resultSet.getRow < 1) createFailedUserResult("User not found.")
    else {
      val servers = getUserServers(id)
      val user = BasicApiUser(
        id = id,
        username = resultSet.getString(1),
        servers = servers,
        status = Status(
          resultSet.getString(2),
          resultSet.getString(3)
        )
      )
      UserResult(
        success = true,
        user = Some(user),
        message = None
      )
    }
  }
}
