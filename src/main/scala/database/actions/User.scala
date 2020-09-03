package database.actions

import java.sql.Connection

import api.model.{ReadableServer, ReadableUser, CreatableUser, UpdatableUser, UserResult}
import database.queries.{User => UserQueries}
import model.{Role, Status}

object User {
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

  def createUser(user: CreatableUser)(implicit connection: Connection): UserResult = {
    val usernameExists = checkUsernameExists(user.username)
    val passwordIsValid = checkPasswordIsValid(user.password)

    if (usernameExists) UserResult.fail("A user with that username already exists.")
    else if (!passwordIsValid) UserResult.fail(
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

      val userIdStatement = connection.prepareStatement(UserQueries.getUserId)
      userIdStatement.setString(1, user.username)
      val resultSet = userIdStatement.executeQuery()
      resultSet.last()

      UserResult.success(
        result = Some(
          ReadableUser(
            id = resultSet.getString(1),
            username = user.username,
            servers = Some(
              Map[ReadableServer, Role.Value]()
            ),
            status = user.status
          )
        ),
        message = Some("User successfully created.")
      )
    }
  }

  private def getUserServers(id: String)(implicit connection: Connection): Map[ReadableServer, Role.Value] = {
    val statement = connection.prepareStatement(UserQueries.getUserServers)
    statement.setString(1, id)
    val resultSet = statement.executeQuery()
    resultSet.last()

    val serverMap = Map[ReadableServer, Role.Value]()
    while(resultSet.next()) {
      serverMap + (
        ReadableServer(
          id = resultSet.getString(1),
          name = resultSet.getString(2),
          address = resultSet.getString(3),
          users = None,
          messages = None
        ) -> resultSet.getString(4)
      )
    }
    serverMap
  }

  def getUser(id: String)(implicit connection: Connection): UserResult = {
    val statement = connection.prepareStatement(UserQueries.getUser)
    statement.setString(1, id)
    val resultSet = statement.executeQuery()
    resultSet.last()

    if (resultSet.getRow < 1) UserResult.fail("User not found.")
    else {
      val servers = getUserServers(id)
      val user = ReadableUser(
        id = id,
        username = resultSet.getString(1),
        servers = Some(servers),
        status = Status(
          resultSet.getString(2),
          resultSet.getString(3)
        )
      )
      UserResult.success(
        result = Some(user),
        message = None
      )
    }
  }

  def updateUser(user: UpdatableUser)(implicit connection: Connection): UserResult = ???

  def updateUsername(id: String, username: String)(implicit connection: Connection): UserResult = {
    val usernameExists = checkUsernameExists(username)
    if (usernameExists) UserResult.fail("A user with that username already exists.")
    else {
      val statement = connection.prepareStatement(UserQueries.updateUsername)
      statement.setString(1, username)
      statement.setString(2, id)
      statement.executeUpdate()
      UserResult.success(
        result = None,
        message = Some("Your username has been updated.")
      )
    }
  }

  def updateStatus(id: String, status: Status)(implicit connection: Connection): UserResult = {
    val statement = connection.prepareStatement(UserQueries.updateUsername)
    statement.setString(1, status.id)
    statement.setString(2, id)
    statement.executeUpdate()
    UserResult.success(
      result = None,
      message = Some("Your status has been updated.")
    )
  }
}
