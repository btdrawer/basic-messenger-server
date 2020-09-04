package database.actions

import java.sql.Connection

import model.result.ServerResult
import database.queries.{Server => ServerQueries}
import model.Status
import model.resources.{Message, Role, Status}

object Server {
  def createServer(server: CreatableServer)(implicit connection: Connection): ServerResult = {
    val isNameTaken = getServerByName(server.name)
    if (isNameTaken.success) ServerResult.fail("That server name is already taken.")
    else {
      val isAddressTaken = getServerByAddress(server.address)
      if (isAddressTaken.success) ServerResult.fail("That server address is already taken.")
      else {
        val statement = connection.prepareStatement(ServerQueries.createServer)
        statement.setString(1, server.name)
        statement.setString(2, server.address)
        statement.setString(3, server.creator.id)
        val resultSet = statement.executeQuery()

        ServerResult(
          success = true,
          result = Some(
            ReadableServer(
              id = resultSet.getString(1),
              name = server.name,
              address = server.address,
              users = None,
              messages = None
            )
          ),
          message = None
        )
      }
    }
  }

  private def findServers(query: String, template: String)(implicit connection: Connection):
    List[BasicReadableServer] = {
    val statement = connection.prepareStatement(query)
    statement.setString(1, template)
    val resultSet = statement.executeQuery()
    val servers = List[BasicReadableServer]()

    while (resultSet.next()) {
      servers.+:(
        BasicReadableServer(
          id = resultSet.getString(1),
          name = resultSet.getString(2),
          address = resultSet.getString(3)
        )
      )
    }
    servers
  }

  def findServersByName(name: String)(implicit connection: Connection): List[BasicReadableServer] =
    findServers(name, ServerQueries.findServersByName)

  def findServersByAddress(address: String)(implicit connection: Connection): List[BasicReadableServer] =
    findServers(address, ServerQueries.findServersByAddress)

  private def getServer(query: String, template: String)(implicit connection: Connection): ServerResult = {
    val statement = connection.prepareStatement(template)
    statement.setString(1, query)
    val resultSet = statement.executeQuery()
    resultSet.last()
    if (resultSet.getRow < 1) ServerResult.fail("Server not found.")
    else {
      val id = resultSet.getString(1)
      val users = getServerUsers(id)
      ServerResult.success(
        result = Some(
          old_model.ReadableServer(
            id = id,
            name = resultSet.getString(2),
            address = resultSet.getString(3),
            users = Some(users),
            messages = None
          )
        ),
        message = None
      )
    }
  }

  def getServerById(id: String)(implicit connection: Connection): ServerResult =
    getServer(id, ServerQueries.getServerById)

  def getServerByName(name: String)(implicit connection: Connection): ServerResult =
    getServer(name, ServerQueries.getServerByName)

  def getServerByAddress(address: String)(implicit connection: Connection): ServerResult =
    getServer(address, ServerQueries.getServerByAddress)

  private def getServerUsers(id: String)(implicit connection: Connection): Map[BasicReadableUser, Role.Value] = {
    val statement = connection.prepareStatement(ServerQueries.getServerUsers)
    statement.setString(1, id)
    val resultSet = statement.executeQuery()
    resultSet.last()

    val userMap = Map[BasicReadableUser, Role.Value]()
    while(resultSet.next()) {
      userMap + (BasicReadableUser(
        id = resultSet.getString(1),
        username = resultSet.getString(2),
        status = Status.withName(resultSet.getString(3))
      ) -> resultSet.getString(5))
    }
    userMap
  }

  def getServerMessages(id: String, limit: Int, offset: Int)(implicit connection: Connection): List[Message] = {
    val statement = connection.prepareStatement(ServerQueries.getServerById)
    statement.setString(1, id)
    statement.setInt(2, limit)
    statement.setInt(3, offset)
    val resultSet = statement.executeQuery()
    resultSet.last()

    val messages = List[Message]()
    while(resultSet.next()) {
      messages.+:(
        ReadableMessage(
          id = resultSet.getString(1),
          content = resultSet.getString(2),
          server = None,
          sender = BasicReadableUser(
            id = resultSet.getString(3),
            username = resultSet.getString(4),
            status = Status.withName(resultSet.getString(5))
          ),
          createdAt = resultSet.getDate(7).toInstant
        )
      )
    }
    messages
  }
}
