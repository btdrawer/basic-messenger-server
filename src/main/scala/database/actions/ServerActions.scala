package database.actions

import java.sql.Connection

import model._
import database.Query
import database.queries.ServerQueries

object ServerActions {
  def createServer(server: CreatableServer)(implicit connection: Connection): Result[Server] = {
    val isNameTaken = getServerByName(server.name)
    if (isNameTaken.success) throw ApiException(FailureMessages.SERVER_NAME_TAKEN)
    val isAddressTaken = getServerByAddress(server.address)
    if (isAddressTaken.success) throw ApiException(FailureMessages.SERVER_ADDRESS_TAKEN)
    else {
      val resultSet = Query.runQuery(
        ServerQueries.createServer,
        List(server.name, server.address, server.creator)
      )
      resultSet.first()
      Success(
        result = Some(
          Server(
            id = resultSet.getInt(1),
            name = server.name,
            address = server.address,
            users = List[ServerUserRole](),
            messages = List[ChildMessage]()
          )
        ),
        message = None
      )
    }
  }

  private def findServers(query: String, template: String)(implicit connection: Connection): List[ChildServer] =
    Query.runAndIterate(
      query,
      List(template),
      resultSet => ChildServer(
        id = resultSet.getInt(1),
        name = resultSet.getString(2),
        address = resultSet.getString(3)
      )
    )

  def findServersByName(name: String)(implicit connection: Connection): List[ChildServer] =
    findServers(name, ServerQueries.findServersByName)

  def findServersByAddress(address: String)(implicit connection: Connection): List[ChildServer] =
    findServers(address, ServerQueries.findServersByAddress)

  private def getServer(query: String, template: String)(implicit connection: Connection): Result[Server] = {
    val resultSet = Query.runQuery(query, List(template))
    resultSet.first()
    if (resultSet.getRow < 1) throw ApiException(FailureMessages.SERVER_NOT_FOUND)
    else {
      val id = resultSet.getInt(1)
      val users = getServerUsers(id)
      val messages = getServerMessages(id, 100, 0)
      Success(
        result = Some(
          Server(
            id = id,
            name = resultSet.getString(2),
            address = resultSet.getString(3),
            users,
            messages
          )
        ),
        message = None
      )
    }
  }

  def getServerById(id: Int)(implicit connection: Connection): Result[Server] =
    getServer(id.toString, ServerQueries.getServerById)

  def getServerByName(name: String)(implicit connection: Connection): Result[Server] =
    getServer(name, ServerQueries.getServerByName)

  def getServerByAddress(address: String)(implicit connection: Connection): Result[Server] =
    getServer(address, ServerQueries.getServerByAddress)

  private def getServerUsers(id: Int)(implicit connection: Connection): List[ServerUserRole] =
    Query.runAndIterate(
      ServerQueries.getServerUsers,
      List(id),
      resultSet => ServerUserRole(
        user = ChildUser(
          id = resultSet.getInt(1),
          username = resultSet.getString(2),
          status = Status.withName(resultSet.getString(3))
        ),
        role = Role.withName(resultSet.getString(5))
      )
    )

  def getServerMessages(id: Int, limit: Int, offset: Int)(implicit connection: Connection): List[ChildMessage] =
    Query.runAndIterate(
      ServerQueries.getServerById,
      List(id, limit, offset),
      resultSet => ChildMessage(
        id = resultSet.getInt(1),
        content = resultSet.getString(2),
        sender = ChildUser(
          id = resultSet.getInt(3),
          username = resultSet.getString(4),
          status = Status.withName(resultSet.getString(5))
        ),
        createdAt = resultSet.getDate(7).toInstant
      )
    )
}
