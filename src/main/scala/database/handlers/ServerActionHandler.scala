package database.handlers

import java.sql.ResultSet

import com.zaxxer.hikari.HikariDataSource
import model._
import database.queries.ServerQueries

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration._

object ServerActionHandler extends ActionHandler {
  def serverIdExists(id: Int)
      (implicit connectionPool: HikariDataSource, executionContext: ExecutionContext): Future[Boolean] =
    runAndGetFirst(ServerQueries.getServerById, List(id)) { server =>
      val serverExists = server.nonEmpty
      if (!serverExists) throw ApiException(FailureMessages.SERVER_NOT_FOUND)
      else serverExists
    }

  private def addressIsNotTaken(address: String)
      (implicit connectionPool: HikariDataSource, executionContext: ExecutionContext): Future[Boolean] =
    runAndGetFirst(ServerQueries.getServerByAddress, List(address)) { server =>
      val serverExists = server.nonEmpty
      if (serverExists) throw ApiException(FailureMessages.SERVER_ADDRESS_TAKEN)
      else serverExists
    }

  def createServer(server: CreatableServer, creator: Int)
      (implicit connectionPool: HikariDataSource, executionContext: ExecutionContext): Future[Result[Server]] =
    for {
      _ <- addressIsNotTaken(server.address)
      result <- runAndGetFirst(ServerQueries.createServer, List(server.name, server.address)) {
        case Some(resultSet) =>
          val id = resultSet.getInt(1)
          val resultFuture = for {
            _ <- addServerUser(id, creator, Role.ADMIN)
            user <- getServerUser(id, creator)
          } yield Success(
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
          Await.result(resultFuture, 5 seconds)
        case None => throw ApiException(FailureMessages.GENERIC)
      }
    } yield result

  def findServers(name: String)
      (implicit connectionPool: HikariDataSource, executionContext: ExecutionContext): Future[List[ChildServer]] =
    runAndIterate(
      ServerQueries.findServersByName,
      List(s"%$name%"),
      resultSet => ChildServer(
        id = resultSet.getInt(1),
        name = resultSet.getString(2),
        address = resultSet.getString(3)
      )
    )

  private def getServerUsers(id: Int)
      (implicit connectionPool: HikariDataSource, executionContext: ExecutionContext): Future[List[ServerUserRole]] =
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

  private def getServer(resultSetOption: Option[ResultSet])
      (implicit connectionPool: HikariDataSource, executionContext: ExecutionContext): Result[Server] =
    resultSetOption match {
      case Some(rs) =>
        val id = rs.getInt(1)
        val resultFuture = for {
          users <- getServerUsers(id)
          messages <- MessageActionHandler.getServerMessages(id, 100, 0)
        } yield Success(
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
        Await.result(resultFuture, 5 seconds)
      case None => throw ApiException(FailureMessages.SERVER_NOT_FOUND)
    }

  def getServerById(id: Int)
      (implicit connectionPool: HikariDataSource, executionContext: ExecutionContext): Future[Result[Server]] =
    runAndGetFirst(ServerQueries.getServerById, List(id))(getServer)

  def getServerByAddress(address: String)
      (implicit connectionPool: HikariDataSource, executionContext: ExecutionContext): Future[Result[Server]] =
    runAndGetFirst(ServerQueries.getServerByAddress, List(address))(getServer)

  def getServerUser(serverId: Int, userId: Int)
      (implicit connectionPool: HikariDataSource, executionContext: ExecutionContext): Future[ServerUserRole] =
    runAndGetFirst(ServerQueries.getServerUser, List(serverId, userId)) {
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

  def getServerAsChildElement(id: Int)
      (implicit connectionPool: HikariDataSource, executionContext: ExecutionContext): Future[ChildServer] =
    runAndGetFirst(ServerQueries.getServerById, List(id)) {
      case Some(rs) => ChildServer(
        id,
        name = rs.getString(2),
        address = rs.getString(3)
      )
      case None => throw ApiException(FailureMessages.SERVER_NOT_FOUND)
    }

  def addServerUser(server: Int, member: Int, role: Role.Value = Role.MEMBER)
      (implicit connectionPool: HikariDataSource, executionContext: ExecutionContext): Future[Result[NoRootElement]] =
    for {
      _ <- serverIdExists(server)
      _ <- UserActionHandler.userIdExists(member)
      _ <- runUpdate(ServerQueries.addServerUser, List(server, member, role.toString))
    } yield Success(
      result = None,
      message = Some("User added to server.")
    )

  def updateServer(id: Int, server: UpdatableServer)
      (implicit connectionPool: HikariDataSource, executionContext: ExecutionContext): Future[Result[NoRootElement]] =
    for {
      _ <- serverIdExists(id)
      _ <- runUpdate(ServerQueries.updateServer, server.toParameterList :+ id)
    } yield Success(
      result = None,
      message = Some("Server updated.")
    )

  def updateUserRole(server: Int, user: Int, role: Role.Value)
      (implicit connectionPool: HikariDataSource, executionContext: ExecutionContext): Future[Result[NoRootElement]] =
    for {
      _ <- serverIdExists(server)
      _ <- UserActionHandler.userIdExists(user)
      _ <- runUpdate(ServerQueries.updateUserRole, List(role.toString, server, user))
    } yield Success(
      result = None,
      message = Some("User role updated.")
    )

  def removeServerUser(server: Int, user: Int)
      (implicit connectionPool: HikariDataSource, executionContext: ExecutionContext): Future[Result[NoRootElement]] =
    for {
      _ <- serverIdExists(server)
      _ <- UserActionHandler.userIdExists(user)
      _ <- runUpdate(ServerQueries.removeServerUser, List(server, user))
    } yield Success(
      result = None,
      message = Some("User removed from server.")
    )

  def deleteServer(id: Int)
      (implicit connectionPool: HikariDataSource, executionContext: ExecutionContext): Future[Result[NoRootElement]] =
    for {
      _ <- serverIdExists(id)
      _ <- runUpdate(ServerQueries.deleteServer, List(id, id, id))
    } yield Success(
      result = None,
      message = Some("Server deleted.")
    )
}
