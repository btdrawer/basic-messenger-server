package database.actions

import java.sql.Connection

import model.resources.{Message, RootReadableMessage}
import database.queries.{Message => MessageQueries}
import model.converters.ReadableConverters.ToReadable
import model.results.{MessageFailure, MessageSuccess, Result}

object Message {
  def createMessage(message: Message)(implicit connection: Connection): Result[RootReadableMessage] = {
    val statement = connection.prepareStatement(MessageQueries.createMessage)
    statement.setString(1, message.content)
    statement.setString(2, message.sender.id)
    statement.setString(3, message.server.id)
    statement.setString(4, message.createdAt.toString)

    val resultSet = statement.executeQuery()
    resultSet.last()

    if (resultSet.getRow <= 0) MessageFailure("An error occurred.")
    else MessageSuccess(
      result = RootReadableMessage(
        id = resultSet.getString(1),
        content = message.content,
        sender = message.sender.toChildReadable,
        server = message.server.toChildReadable,
        createdAt = message.createdAt
      ),
      message = None
    )
  }
}
