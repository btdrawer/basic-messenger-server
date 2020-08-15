package database.actions

import java.sql.Connection

import api.model.{CreatableServer, ReadableServer, ReadableUser, ServerResult}
import database.queries.{Server => ServerQueries}
import model.{Message, Role, Status}

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
    List[ReadableServer] = {
    val statement = connection.prepareStatement(query)
    statement.setString(1, template)
    val resultSet = statement.executeQuery()
    val servers = List[ReadableServer]()
    while (resultSet.next()) {
      servers.+:(ReadableServer(
        id = resultSet.getString(1),
        name = resultSet.getString(2),
        address = resultSet.getString(3),
        users = None,
        messages = None
      ))
    }
    servers
  }

  def findServersByName(name: String)(implicit connection: Connection): List[ReadableServer] =
    findServers(name, ServerQueries.findServersByName)

  def findServersByAddress(address: String)(implicit connection: Connection): List[ReadableServer] =
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
          ReadableServer(
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

  private def getServerUsers(id: String)(implicit connection: Connection): Map[ReadableUser, Role.Value] = {
    val statement = connection.prepareStatement(ServerQueries.getServerUsers)
    statement.setString(1, id)
    val resultSet = statement.executeQuery()
    resultSet.last()

    val userMap = Map[ReadableUser, Role.Value]()
    while(resultSet.next()) {
      userMap + ReadableUser(
        id = resultSet.getString(1),
        username = resultSet.getString(2),
        servers = None,
        status = Status(
          resultSet.getString(3),
          resultSet.getString(4)
        )
      )
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
          sender = ReadableUser(
            id = resultSet.getString(3),
            username = resultSet.getString(4),
            servers = None,
            status = Status(
              id = resultSet.getString(5),
              content = resultSet.getString(6)
            )
          ),
          createdAt = resultSet.getDate(7).toInstant
        )
      )
    }
    messages
  }
}
