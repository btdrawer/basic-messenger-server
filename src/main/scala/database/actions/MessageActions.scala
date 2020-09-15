package database.actions

import java.sql.Connection

import model._
import database.queries.MessageQueries

object MessageActions extends Actions {
  def createMessage(message: CreatableMessage, sender: Int)(implicit connection: Connection): Result[Message] = {
    val server = ServerActions.getServerAsChildElement(message.server)
    val senderDetails = ServerActions.getServerUser(message.server, sender)
    val resultSet = runAndGetFirst(
      MessageQueries.createMessage,
      List(
        message.content,
        sender,
        message.server,
        message.createdAt
      )
    )
    if (resultSet.getRow <= 0) throw ApiException(FailureMessages.GENERIC)
    else Success(
      result = Some(
        Message(
          id = resultSet.getInt(1),
          content = message.content,
          server,
          sender = senderDetails,
          createdAt = message.createdAt
        )
      ),
      message = Some("Message sent.")
    )
  }
}
