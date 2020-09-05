package database.actions

import java.sql.Connection

import model.results.{Result, ServerFailure, ServerSuccess}
import database.queries.{Server => ServerQueries}
import model.resources._

object Server {
  def createServer(server: CreatableServer)(implicit connection: Connection): Result[RootReadableServer] = {
    val isNameTaken = getServerByName(server.name)
    if (isNameTaken.success) ServerFailure("That server name is already taken.")
    else {
      val isAddressTaken = getServerByAddress(server.address)
      if (isAddressTaken.success) ServerFailure("That server address is already taken.")
      else {
        val statement = connection.prepareStatement(ServerQueries.createServer)
        statement.setString(1, server.name)
        statement.setString(2, server.address)
        statement.setString(3, server.creator)
        val resultSet = statement.executeQuery()

        ServerSuccess(
          result = RootReadableServer(
            id = resultSet.getString(1),
            name = server.name,
            address = server.address,
            users = Map[ChildReadableUser, Role.Value](),
            messages = List[ChildReadableMessage]()
          ),
          message = None
        )
      }
    }
  }

  private def findServers(query: String, template: String)(implicit connection: Connection):
    List[ChildReadableServer] = {
    val statement = connection.prepareStatement(query)
    statement.setString(1, template)
    val resultSet = statement.executeQuery()
    val servers = List[ChildReadableServer]()

    while (resultSet.next()) {
      servers.+:(
        ChildReadableServer(
          id = resultSet.getString(1),
          name = resultSet.getString(2),
          address = resultSet.getString(3)
        )
      )
    }
    servers
  }

  def findServersByName(name: String)(implicit connection: Connection): List[ChildReadableServer] =
    findServers(name, ServerQueries.findServersByName)

  def findServersByAddress(address: String)(implicit connection: Connection): List[ChildReadableServer] =
    findServers(address, ServerQueries.findServersByAddress)

  private def getServer(query: String, template: String)(implicit connection: Connection):
    Result[RootReadableServer] = {
    val statement = connection.prepareStatement(template)
    statement.setString(1, query)

    val resultSet = statement.executeQuery()
    resultSet.last()

    if (resultSet.getRow < 1) ServerFailure("Server not found.")
    else {
      val id = resultSet.getString(1)
      val users = getServerUsers(id)
      val messages = getServerMessages(id, 100, 0)

      ServerSuccess(
        result = RootReadableServer(
          id = id,
          name = resultSet.getString(2),
          address = resultSet.getString(3),
          users = users,
          messages = messages
        ),
        message = None
      )
    }
  }

  def getServerById(id: String)(implicit connection: Connection): Result[RootReadableServer] =
    getServer(id, ServerQueries.getServerById)

  def getServerByName(name: String)(implicit connection: Connection): Result[RootReadableServer] =
    getServer(name, ServerQueries.getServerByName)

  def getServerByAddress(address: String)(implicit connection: Connection): Result[RootReadableServer] =
    getServer(address, ServerQueries.getServerByAddress)

  private def getServerUsers(id: String)(implicit connection: Connection): Map[ChildReadableUser, Role.Value] = {
    val statement = connection.prepareStatement(ServerQueries.getServerUsers)
    statement.setString(1, id)
    val resultSet = statement.executeQuery()
    resultSet.last()

    val userMap = Map[ChildReadableUser, Role.Value]()
    while(resultSet.next()) {
      userMap + (
        ChildReadableUser(
          id = resultSet.getString(1),
          username = resultSet.getString(2),
          status = Status.withName(resultSet.getString(3))
        ) -> resultSet.getString(5))
    }
    userMap
  }

  def getServerMessages(id: String, limit: Int, offset: Int)(implicit connection: Connection):
    List[ChildReadableMessage] = {
    val statement = connection.prepareStatement(ServerQueries.getServerById)
    statement.setString(1, id)
    statement.setInt(2, limit)
    statement.setInt(3, offset)
    val resultSet = statement.executeQuery()
    resultSet.last()

    val messages = List[ChildReadableMessage]()
    while(resultSet.next()) {
      messages.+:(
        ChildReadableMessage(
          id = resultSet.getString(1),
          content = resultSet.getString(2),
          sender = ChildReadableUser(
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
