package database.actions

import java.sql.Connection

import api.model.{ReadableMessage, ReadableServer, ReadableUser, ReadableUser, ServerResult}
import database.queries.{Server => ServerQueries}
import model.{Message, Role, Status}

object Server {
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

  def getServer(id: String)(implicit connection: Connection): ServerResult = {
    val statement = connection.prepareStatement(ServerQueries.getServer)
    statement.setString(1, id)
    val resultSet = statement.executeQuery()
    resultSet.last()
    if (resultSet.getRow < 1) ServerResult.createFailedServerResult("Server not found.")
    else {
      val users = getServerUsers(id)
      ServerResult(
        success = true,
        server = Some(
          ReadableServer(
            id = id,
            name = resultSet.getString(1),
            address = resultSet.getString(2),
            users = Some(users),
            messages = None
          )
        ),
        message = None
      )
    }
  }

  def getServerMessages(id: String, limit: Int, offset: Int)(implicit connection: Connection): List[Message] = {
    val statement = connection.prepareStatement(ServerQueries.getServer)
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
