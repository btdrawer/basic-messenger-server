package database.actions

import java.sql.Connection

import database.model.UserResult
import database.queries.UserQueries
import model.{DatabaseUser, Role, Server, SimplifiedUser}

object UserActions {
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

  def createUser(user: DatabaseUser)(implicit connection: Connection): UserResult = {
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
      createStatement.executeUpdate()

      val passwordResetStatement = connection.prepareStatement(UserQueries.createPasswordResetData)
      passwordResetStatement.setString(1, user.passwordReset.question)
      passwordResetStatement.setString(2, user.passwordReset.answer)
      passwordResetStatement.executeUpdate()

      UserResult(
        success = true,
        user = Some(user),
        message = Some("User successfully created.")
      )
    }
  }

  private def getUserServers(id: String)(implicit connection: Connection): Map[Server, Role.Value] = {
    val statement = connection.prepareStatement(UserQueries.getUserServers)
    statement.setString(1, id)
    val resultSet = statement.executeQuery()
    resultSet.last()

    val userMap = Map[Server, Role.Value]()
    while(resultSet.next()) {
      userMap + (
        Server(
          resultSet.getString(1),
          resultSet.getString(2),
          resultSet.getString(3),
          ???,
          ???
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
      val user = SimplifiedUser(
        id = id,
        username = resultSet.getString(1),
        servers = servers,
        status = resultSet.getString(2)
      )
      UserResult(
        success = true,
        user = Some(user),
        message = None
      )
    }
  }
}
