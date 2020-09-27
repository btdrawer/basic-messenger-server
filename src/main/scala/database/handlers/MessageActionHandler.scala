package database.handlers

import java.sql.{Connection, Timestamp}
import java.time.Instant

import model._
import database.queries.MessageQueries

object MessageActionHandler extends ActionHandler {
  def createServerMessage(message: CreatableMessage, server: Int, sender: Int)
                         (implicit connection: Connection): Result[ServerMessage] = {
    val serverDetails = ServerActionHandler.getServerAsChildElement(server)
    val senderDetails = UserActionHandler.getUserAsChildElement(sender)
    val timestamp = Timestamp.from(Instant.EPOCH)
    runAndGetFirst(
      MessageQueries.createServerMessage,
      List(
        message.content,
        sender,
        server,
        timestamp
      )
    ) match {
      case Some(rs) => Success(
        result = Some(
          ServerMessage(
            id = rs.getInt(1),
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
  }

  def createDirectMessage(message: CreatableMessage, recipient: Int, sender: Int)
                         (implicit connection: Connection): Result[DirectMessage] = {
    val recipientDetails = UserActionHandler.getUserAsChildElement(recipient)
    val senderDetails = UserActionHandler.getUserAsChildElement(sender)
    val timestamp = Timestamp.from(Instant.EPOCH)
    runAndGetFirst(
      MessageQueries.createDirectMessage,
      List(
        message.content,
        recipient,
        sender,
        timestamp
      )
    ) match {
      case Some(rs) => Success(
        result = Some(
          DirectMessage(
            id = rs.getInt(1),
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
  }

  def getServerMessages(id: Int, limit: Int = 100, offset: Int = 0)
                       (implicit connection: Connection): List[ChildMessage] =
    if (limit < 0 || limit > 1000) throw ApiException(FailureMessages.BAD_LIMIT)
    else if (offset < 0) throw ApiException(FailureMessages.BAD_OFFSET)
    else {
      runAndIterate(
        MessageQueries.getServerMessages,
        List(id, limit, offset),
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
    }

  def getDirectMessages(user1: Int, user2: Int, limit: Int = 100, offset: Int = 0)
                       (implicit connection: Connection): List[ChildMessage] =
    if (limit < 0 || limit > 1000) throw ApiException(FailureMessages.BAD_LIMIT)
    else if (offset < 0) throw ApiException(FailureMessages.BAD_OFFSET)
    else 
      runAndIterate(
        MessageQueries.getDirectMessages,
        List(user1, user2, user2, user1, limit, offset),
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
}
