package database.handlers

import java.sql.Timestamp
import java.time.Instant

import com.zaxxer.hikari.HikariDataSource
import model._
import database.queries.MessageQueries

import scala.concurrent.{ExecutionContext, Future}

object MessageActionHandler extends ActionHandler {
  def createServerMessage(message: CreatableMessage, server: Int, sender: Int)
      (implicit connectionPool: HikariDataSource, executionContext: ExecutionContext): Future[Result[ServerMessage]] =
    for {
      serverDetails <- ServerActionHandler.getServerAsChildElement(server)
      senderDetails <- UserActionHandler.getUserAsChildElement(sender)
      timestamp = Timestamp.from(Instant.EPOCH)
      result <- runAndGetFirst(
        MessageQueries.createServerMessage,
        List(message.content, sender, server, timestamp)
      ) {
        case Some(resultSet) => Success(
          result = Some(
            ServerMessage(
              id = resultSet.getInt(1),
              content = message.content,
              server = serverDetails,
              sender = senderDetails,
              createdAt = timestamp
            )
          ),
          message = Some("Message sent.")
        )
        case None => throw ApiException(FailureMessages.GENERIC)
      }
    } yield result

  def createDirectMessage(message: CreatableMessage, recipient: Int, sender: Int)
      (implicit connectionPool: HikariDataSource, executionContext: ExecutionContext): Future[Result[DirectMessage]] =
    for {
      recipientDetails <- UserActionHandler.getUserAsChildElement(recipient)
      senderDetails <- UserActionHandler.getUserAsChildElement(sender)
      timestamp = Timestamp.from(Instant.EPOCH)
      result <- runAndGetFirst(
        MessageQueries.createDirectMessage,
        List(message.content, recipient, sender, timestamp)
      ) {
        case Some(resultSet) => Success(
          result = Some(
            DirectMessage(
              id = resultSet.getInt(1),
              content = message.content,
              recipient = recipientDetails,
              sender = senderDetails,
              createdAt = timestamp
            )
          ),
          message = Some("Message sent.")
        )
        case None => throw ApiException(FailureMessages.GENERIC)
      }
    } yield result

  private def getMessages(query: String, parameters: List[Any], limit: Int, offset: Int)
      (implicit connectionPool: HikariDataSource, executionContext: ExecutionContext): Future[List[ChildMessage]] =
    if (limit < 0 || limit > 1000)
      throw ApiException(FailureMessages.BAD_LIMIT)
    else if (offset < 0)
      throw ApiException(FailureMessages.BAD_OFFSET)
    else runAndIterate(
      query,
      parameters,
      resultSet =>
        ChildMessage(
          id = resultSet.getInt(1),
          content = resultSet.getString(2),
          sender = ChildUser(
            id = resultSet.getInt(3),
            username = resultSet.getString(4),
            status = Status.withName(resultSet.getString(5))
          ),
          createdAt = resultSet.getTimestamp(6)
        )
    )

  def getServerMessages(id: Int, limit: Int = 100, offset: Int = 0)
      (implicit connectionPool: HikariDataSource, executionContext: ExecutionContext): Future[List[ChildMessage]] =
    getMessages(
      MessageQueries.getServerMessages,
      List(id, limit, offset),
      limit,
      offset
    )

  def getDirectMessages(user1: Int, user2: Int, limit: Int = 100, offset: Int = 0)
      (implicit connectionPool: HikariDataSource, executionContext: ExecutionContext): Future[List[ChildMessage]] =
    getMessages(
      MessageQueries.getDirectMessages,
      List(user1, user2, user2, user1, limit, offset),
      limit,
      offset
    )
}
