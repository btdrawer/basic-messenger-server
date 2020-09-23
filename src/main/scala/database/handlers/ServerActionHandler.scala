package database.handlers

import java.sql.{Connection, ResultSet}

import model.{Success, _}
import database.queries.ServerQueries

object ServerActionHandler extends ActionHandler {
  def serverIdExists(id: Int)(implicit connection: Connection): Boolean = {
    val resultSet = runAndGetFirst(ServerQueries.getServerById, List(id))
    resultSet.getRow > 0
  }

  private def addressExists(address: String)(implicit connection: Connection): Boolean = {
    val resultSet = runAndGetFirst(ServerQueries.getServerByAddress, List(address))
    resultSet.getRow > 0
  }

  def createServer(server: CreatableServer, creator: Int)(implicit connection: Connection): Result[Server] =
    if (addressExists(server.address)) throw ApiException(FailureMessages.SERVER_ADDRESS_TAKEN)
    else {
      val resultSet = runAndGetFirst(ServerQueries.createServer, List(server.name, server.address))
      val id = resultSet.getInt(1)
      addServerUser(id, creator, Role.ADMIN)
        val serverUser = getServerUser(id, creator)
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

  def findServers(name: String)(implicit connection: Connection): List[ChildServer] =
    runAndIterate(
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
    getServer(() => runQuery(ServerQueries.getServerById, List(id)))

  def getServerByAddress(address: String)(implicit connection: Connection): Result[Server] =
    getServer(() => runQuery(ServerQueries.getServerByAddress, List(address)))

  private def getServerUsers(id: Int)(implicit connection: Connection): List[ServerUserRole] =
    runAndIterate(
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
    val resultSet = runAndGetFirst(
      ServerQueries.getServerUser,
      List(serverId, userId)
    )
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
    val resultSet = runQuery(ServerQueries.getServerById, List(id))
    resultSet.first()
    if (resultSet.getRow < 1) throw ApiException(FailureMessages.SERVER_NOT_FOUND)
    else ChildServer(
      id,
      name = resultSet.getString(2),
      address = resultSet.getString(3)
    )
  }

  def getServerMessages(id: Int, limit: Int, offset: Int)(implicit connection: Connection): List[ChildMessage] =
    runAndIterate(
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
        createdAt = resultSet.getTimestamp(7)
      )
    )

  def addServerUser(server: Int, member: Int, role: Role.Value = Role.MEMBER)
                   (implicit connection: Connection): Result[NoRootElement] = {
    if (!serverIdExists(server)) throw ApiException(FailureMessages.SERVER_NOT_FOUND)
    else if (!UserActionHandler.userIdExists(member)) throw ApiException(FailureMessages.USER_NOT_FOUND)
    else {
      runUpdate(ServerQueries.addServerUser, List(server, member, role.toString))
      Success(
        result = None,
        message = Some("User added to server.")
      )
    }
  }

  def updateServer(id: Int, server: UpdatableServer)
                    (implicit connection: Connection): Result[NoRootElement] = {
    if (!serverIdExists(id)) throw ApiException(FailureMessages.SERVER_NOT_FOUND)
    else {
      runUpdate(ServerQueries.updateServer, server.toParameterList :+ id)
      Success(
        result = None,
        message = Some("Server updated.")
      )
    }
  }

  def updateUserRole(server: Int, user: Int, role: Role.Value)
                    (implicit connection: Connection): Result[NoRootElement] =
    if (!serverIdExists(server)) throw ApiException(FailureMessages.SERVER_NOT_FOUND)
    else if (!UserActionHandler.userIdExists(user)) throw ApiException(FailureMessages.USER_NOT_FOUND)
    else {
      runUpdate(ServerQueries.updateUserRole, List(role.toString, server, user))
      Success(
        result = None,
        message = Some("User role updated.")
      )
    }

  def removeServerUser(server: Int, user: Int)(implicit connection: Connection): Result[NoRootElement] =
    if (!serverIdExists(server)) throw ApiException(FailureMessages.SERVER_NOT_FOUND)
    else if (!UserActionHandler.userIdExists(user)) throw ApiException(FailureMessages.USER_NOT_FOUND)
    else {
      runUpdate(ServerQueries.removeServerUser, List(server, user))
      Success(
        result = None,
        message = Some("User removed from server.")
      )
    }

  def deleteServer(id: Int)(implicit connection: Connection): Result[NoRootElement] =
    if (!serverIdExists(id)) throw ApiException(FailureMessages.SERVER_NOT_FOUND)
    else {
      runUpdate(ServerQueries.deleteServer, List(id, id, id))
      Success(
        result = None,
        message = Some("Server deleted.")
      )
    }
}
