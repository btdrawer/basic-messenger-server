package database.actions

import java.sql.Connection

import model.resources.MessageResult
import database.queries.{Message => MessageQueries}
import model.resources.Message

object Message {
  def createMessage(message: Message)(implicit connection: Connection): MessageResult = {
    val statement = connection.prepareStatement(MessageQueries.createMessage)
    statement.setString(1, message.content)
    statement.setString(2, message.sender.id)
    statement.setString(3, message.server.id)
    statement.setString(4, message.createdAt.toString)

    val resultSet = statement.executeQuery()
    resultSet.last()

    if (resultSet.getRow <= 0) MessageResult.fail("An error occurred.")
    else MessageResult.success(
      result = Some(
        ReadableMessage(
          id = resultSet.getString(1),
          content = message.content,
          sender = message.sender.toReadable,
          server = Some(message.server.toReadable),
          createdAt = message.createdAt
        )
      ),
      message = None
    )
  }
}
