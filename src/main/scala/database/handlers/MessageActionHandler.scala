package database.handlers

import java.sql.{Connection, Timestamp}
import java.time.Instant

import model._
import database.queries.MessageQueries

object MessageActionHandler extends ActionHandler {
  def createMessage(message: CreatableMessage, sender: Int)(implicit connection: Connection): Result[Message] = {
    val server = ServerActionHandler.getServerAsChildElement(message.server)
    val senderDetails = ServerActionHandler.getServerUser(message.server, sender)
    val timestamp = Timestamp.from(Instant.EPOCH)
    runAndGetFirst(
      MessageQueries.createMessage,
      List(
        message.content,
        sender,
        message.server,
        timestamp
      )
    ) match {
      case Some(rs) => Success(
        result = Some(
          Message(
            id = rs.getInt(1),
            content = message.content,
            server,
            sender = senderDetails,
            createdAt = timestamp
          )
        ),
        message = Some("Message sent.")
      )
      case None => throw ApiException(FailureMessages.GENERIC)
    }
  }
}
