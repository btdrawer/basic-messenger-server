package database.actions

import java.sql.Connection

import model._
import database.queries.{Server => ServerQueries}
import model.{Failure, Result, Success}

object Server {
  def createServer(server: CreatableServer)(implicit connection: Connection): Result[RootServer] = {
    val isNameTaken = getServerByName(server.name)
    if (isNameTaken.success) Failure("That server name is already taken.")
    else {
      val isAddressTaken = getServerByAddress(server.address)
      if (isAddressTaken.success) Failure("That server address is already taken.")
      else {
        val statement = connection.prepareStatement(ServerQueries.createServer)
        statement.setString(1, server.name)
        statement.setString(2, server.address)
        statement.setString(3, server.creator)
        val resultSet = statement.executeQuery()

        Success(
          result = Some(
            RootServer(
              id = resultSet.getString(1),
              name = server.name,
              address = server.address,
              users = Map[ChildUser, Role.Value](),
              messages = List[ChildMessage]()
            )
          ),
          message = None
        )
      }
    }
  }

  private def findServers(query: String, template: String)(implicit connection: Connection):
    List[ChildServer] = {
    val statement = connection.prepareStatement(query)
    statement.setString(1, template)
    val resultSet = statement.executeQuery()
    val servers = List[ChildServer]()

    while (resultSet.next()) {
      servers.+:(
        ChildServer(
          id = resultSet.getString(1),
          name = resultSet.getString(2),
          address = resultSet.getString(3)
        )
      )
    }
    servers
  }

  def findServersByName(name: String)(implicit connection: Connection): List[ChildServer] =
    findServers(name, ServerQueries.findServersByName)

  def findServersByAddress(address: String)(implicit connection: Connection): List[ChildServer] =
    findServers(address, ServerQueries.findServersByAddress)

  private def getServer(query: String, template: String)(implicit connection: Connection):
    Result[RootServer] = {
    val statement = connection.prepareStatement(template)
    statement.setString(1, query)

    val resultSet = statement.executeQuery()
    resultSet.last()

    if (resultSet.getRow < 1) Failure("Server not found.")
    else {
      val id = resultSet.getString(1)
      val users = getServerUsers(id)
      val messages = getServerMessages(id, 100, 0)

      Success(
        result = Some(
          RootServer(
            id = id,
            name = resultSet.getString(2),
            address = resultSet.getString(3),
            users = users,
            messages = messages
          )
        ),
        message = None
      )
    }
  }

  def getServerById(id: String)(implicit connection: Connection): Result[RootServer] =
    getServer(id, ServerQueries.getServerById)

  def getServerByName(name: String)(implicit connection: Connection): Result[RootServer] =
    getServer(name, ServerQueries.getServerByName)

  def getServerByAddress(address: String)(implicit connection: Connection): Result[RootServer] =
    getServer(address, ServerQueries.getServerByAddress)

  private def getServerUsers(id: String)(implicit connection: Connection): Map[ChildUser, Role.Value] = {
    val statement = connection.prepareStatement(ServerQueries.getServerUsers)
    statement.setString(1, id)
    val resultSet = statement.executeQuery()
    resultSet.last()

    val userMap = Map[ChildUser, Role.Value]()
    while(resultSet.next()) {
      userMap + (
        ChildUser(
          id = resultSet.getString(1),
          username = resultSet.getString(2),
          status = Status.withName(resultSet.getString(3))
        ) -> resultSet.getString(5))
    }
    userMap
  }

  def getServerMessages(id: String, limit: Int, offset: Int)(implicit connection: Connection):
    List[ChildMessage] = {
    val statement = connection.prepareStatement(ServerQueries.getServerById)
    statement.setString(1, id)
    statement.setInt(2, limit)
    statement.setInt(3, offset)
    val resultSet = statement.executeQuery()
    resultSet.last()

    val messages = List[ChildMessage]()
    while(resultSet.next()) {
      messages.+:(
        ChildMessage(
          id = resultSet.getString(1),
          content = resultSet.getString(2),
          sender = ChildUser(
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
