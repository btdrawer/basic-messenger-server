package database.actions

import java.sql.{Connection, ResultSet}

import model.{Success, _}
import database.Query
import database.queries.ServerQueries

object ServerActions {
  private def checkAddressTaken(address: String)(implicit connection: Connection): Boolean = {
    val resultSet = Query.runQuery(
      ServerQueries.getServerByAddress,
      List(address)
    )
    resultSet.first()
    resultSet.getRow > 0
  }

  def createServer(server: CreatableServer)(implicit connection: Connection): Result[Server] = {
    val isAddressTaken = checkAddressTaken(server.address)
    if (isAddressTaken) throw ApiException(FailureMessages.SERVER_ADDRESS_TAKEN)
    else {
      val resultSet = Query.runQuery(
        ServerQueries.createServer,
        List(server.name, server.address)
      )
      resultSet.first()
      val id = resultSet.getInt(1)
      val rowsUpdated = Query.runUpdate(
        ServerQueries.addUserToServer,
        List(server.creator, id, Role.ADMIN.toString)
      )
      if (rowsUpdated < 1) throw ApiException(FailureMessages.GENERIC)
      else {
        val serverUser = ServerActions.getServerUser(id, server.creator)
        Success(
          result = Some(
            Server(
              id,
              name = server.name,
              address = server.address,
              users = List[ServerUserRole](serverUser),
              messages = List[ChildMessage]()
            )
          ),
          message = Some("Server successfully created.")
        )
      }
    }
  }

  def findServers(name: String)(implicit connection: Connection): List[ChildServer] =
    Query.runAndIterate(
      ServerQueries.findServersByName,
      List(s"%$name%"),
      resultSet => ChildServer(
        id = resultSet.getInt(1),
        name = resultSet.getString(2),
        address = resultSet.getString(3)
      )
    )

  private def getServer(queryToRun: () => ResultSet)(implicit connection: Connection): Result[Server] = {
    val resultSet = queryToRun()
    resultSet.first()
    if (resultSet.getRow < 1) throw ApiException(FailureMessages.SERVER_NOT_FOUND)
    else {
      val id = resultSet.getInt(1)
      val users = getServerUsers(id)
      val messages = getServerMessages(id, 100, 0)
      Success(
        result = Some(
          Server(
            id,
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
    getServer(() => Query.runQuery(ServerQueries.getServerById, List(id)))

  def getServerByAddress(address: String)(implicit connection: Connection): Result[Server] =
    getServer(() => Query.runQuery(ServerQueries.getServerByAddress, List(address)))

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
        role = Role.withName(resultSet.getString(4))
      )
    )

  def getServerUser(serverId: Int, userId: Int)(implicit connection: Connection): ServerUserRole = {
    val resultSet = Query.runQuery(
      ServerQueries.getServerUser,
      List(serverId, userId)
    )
    resultSet.first()
    if (resultSet.getRow < 1) throw ApiException(FailureMessages.USER_NOT_FOUND)
    else ServerUserRole(
      user = ChildUser(
        id = resultSet.getInt(1),
        username = resultSet.getString(2),
        status = Status.withName(resultSet.getString(3))
      ),
      role = Role.withName(resultSet.getString(4))
    )
  }

  def getServerAsChildElement(id: Int)(implicit connection: Connection): ChildServer = {
    val resultSet = Query.runQuery(ServerQueries.getServerById, List(id))
    resultSet.first()
    if (resultSet.getRow < 1) throw ApiException(FailureMessages.SERVER_NOT_FOUND)
    else ChildServer(
      id,
      name = resultSet.getString(2),
      address = resultSet.getString(3)
    )
  }

  def getServerMessages(id: Int, limit: Int, offset: Int)(implicit connection: Connection): List[ChildMessage] =
    Query.runAndIterate(
      ServerQueries.getServerMessages,
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
