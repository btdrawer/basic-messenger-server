package database.handlers

import java.sql.ResultSet

import com.zaxxer.hikari.HikariDataSource

import model._
import database.queries.ServerQueries

object ServerActionHandler extends ActionHandler {
  def serverIdExists(id: Int)(implicit connectionPool: HikariDataSource): Boolean =
    runAndGetFirst(
      ServerQueries.getServerById,
      List(id)
    )(_.nonEmpty)

  private def addressExists(address: String)(implicit connectionPool: HikariDataSource): Boolean =
    runAndGetFirst(
      ServerQueries.getServerByAddress,
      List(address)
    )(_.nonEmpty)

  def createServer(server: CreatableServer, creator: Int)(implicit connectionPool: HikariDataSource): Result[Server] =
    if (addressExists(server.address))
      throw ApiException(FailureMessages.SERVER_ADDRESS_TAKEN)
    else runAndGetFirst(
      ServerQueries.createServer,
      List(
        server.name,
        server.address
      )
    ) {
      case Some(rs) =>
        val id = rs.getInt(1)
        addServerUser(id, creator, Role.ADMIN)
        val user = getServerUser(id, creator)
        Success(
          result = Some(
            Server(
              id,
              name = server.name,
              address = server.address,
              users = List[ServerUserRole](user),
              messages = List[ChildMessage]()
            )
          ),
          message = Some("Server successfully created.")
        )
      case None => throw ApiException(FailureMessages.GENERIC)
    }

  def findServers(name: String)(implicit connectionPool: HikariDataSource): List[ChildServer] =
    runAndIterate(
      ServerQueries.findServersByName,
      List(s"%$name%"),
      resultSet => ChildServer(
        id = resultSet.getInt(1),
        name = resultSet.getString(2),
        address = resultSet.getString(3)
      )
    )

  private def getServerUsers(id: Int)(implicit connectionPool: HikariDataSource): List[ServerUserRole] =
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

  private def getServer(
    resultSetOption: Option[ResultSet]
  )(implicit connectionPool: HikariDataSource): Result[Server] =
    resultSetOption match {
      case Some(rs) =>
        val id = rs.getInt(1)
        val users = getServerUsers(id)
        val messages = MessageActionHandler.getServerMessages(id, 100, 0)
        Success(
          result = Some(
            Server(
              id,
              name = rs.getString(2),
              address = rs.getString(3),
              users,
              messages
            )
          ),
          message = None
        )
      case None => throw ApiException(FailureMessages.SERVER_NOT_FOUND)
    }

  def getServerById(id: Int)(implicit connectionPool: HikariDataSource): Result[Server] =
    runAndGetFirst(
      ServerQueries.getServerById,
      List(id)
    )(getServer)

  def getServerByAddress(address: String)(implicit connectionPool: HikariDataSource): Result[Server] =
    runAndGetFirst(
      ServerQueries.getServerByAddress,
      List(address)
    )(getServer)

  def getServerUser(serverId: Int, userId: Int)(implicit connectionPool: HikariDataSource): ServerUserRole =
    runAndGetFirst(
      ServerQueries.getServerUser,
      List(
        serverId,
        userId
      )
    ) {
      case Some(rs) => ServerUserRole(
        user = ChildUser(
          id = rs.getInt(1),
          username = rs.getString(2),
          status = Status.withName(rs.getString(3))
        ),
        role = Role.withName(rs.getString(4))
      )
      case None => throw ApiException(FailureMessages.USER_NOT_FOUND)
    }

  def getServerAsChildElement(id: Int)(implicit connectionPool: HikariDataSource): ChildServer =
    runAndGetFirst(
      ServerQueries.getServerById,
      List(id)
    ) {
      case Some(rs) => ChildServer(
        id,
        name = rs.getString(2),
        address = rs.getString(3)
      )
      case None => throw ApiException(FailureMessages.SERVER_NOT_FOUND)
    }

  def addServerUser(
    server: Int,
    member: Int,
    role: Role.Value = Role.MEMBER
  )(implicit connectionPool: HikariDataSource): Result[NoRootElement] =
    if (!serverIdExists(server))
      throw ApiException(FailureMessages.SERVER_NOT_FOUND)
    else if (!UserActionHandler.userIdExists(member))
      throw ApiException(FailureMessages.USER_NOT_FOUND)
    else {
      runUpdate(ServerQueries.addServerUser, List(server, member, role.toString))
      Success(
        result = None,
        message = Some("User added to server.")
      )
    }

  def updateServer(
    id: Int,
    server: UpdatableServer
  )(implicit connectionPool: HikariDataSource): Result[NoRootElement] =
    if (!serverIdExists(id))
      throw ApiException(FailureMessages.SERVER_NOT_FOUND)
    else {
      runUpdate(ServerQueries.updateServer, server.toParameterList :+ id)
      Success(
        result = None,
        message = Some("Server updated.")
      )
    }

  def updateUserRole(
    server: Int,
    user: Int,
    role: Role.Value
  )(implicit connectionPool: HikariDataSource): Result[NoRootElement] =
    if (!serverIdExists(server))
      throw ApiException(FailureMessages.SERVER_NOT_FOUND)
    else if (!UserActionHandler.userIdExists(user))
      throw ApiException(FailureMessages.USER_NOT_FOUND)
    else {
      runUpdate(ServerQueries.updateUserRole, List(role.toString, server, user))
      Success(
        result = None,
        message = Some("User role updated.")
      )
    }

  def removeServerUser(
    server: Int,
    user: Int
  )(implicit connectionPool: HikariDataSource): Result[NoRootElement] =
    if (!serverIdExists(server))
      throw ApiException(FailureMessages.SERVER_NOT_FOUND)
    else if (!UserActionHandler.userIdExists(user))
      throw ApiException(FailureMessages.USER_NOT_FOUND)
    else {
      runUpdate(ServerQueries.removeServerUser, List(server, user))
      Success(
        result = None,
        message = Some("User removed from server.")
      )
    }

  def deleteServer(id: Int)(implicit connectionPool: HikariDataSource): Result[NoRootElement] =
    if (!serverIdExists(id))
      throw ApiException(FailureMessages.SERVER_NOT_FOUND)
    else {
      runUpdate(ServerQueries.deleteServer, List(id, id, id))
      Success(
        result = None,
        message = Some("Server deleted.")
      )
    }
}
