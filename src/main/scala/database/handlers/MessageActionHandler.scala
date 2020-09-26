package database.handlers

import java.sql.Connection

import model._
import database.queries.MessageQueries

object MessageActionHandler extends ActionHandler {
  def createMessage(message: CreatableMessage, sender: Int)(implicit connection: Connection): Result[Message] = {
    val server = ServerActionHandler.getServerAsChildElement(message.server)
    val senderDetails = ServerActionHandler.getServerUser(message.server, sender)
    runAndGetFirst(
      MessageQueries.createMessage,
      List(
        message.content,
        sender,
        message.server,
        message.createdAt
      )
    ) match {
      case Some(rs) => Success(
        result = Some(
          Message(
            id = rs.getInt(1),
            content = message.content,
            server,
            sender = senderDetails,
            createdAt = message.createdAt
          )
        ),
        message = Some("Message sent.")
      )
      case None => throw ApiException(FailureMessages.GENERIC)
    }
  }
}
