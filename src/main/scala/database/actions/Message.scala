package database.actions

import java.sql.Connection

import model._
import database.queries.{Message => MessageQueries}
import model.{Failure, Result, RootMessage, Success}
import converters.ElementConverters.ToReadable

object Message {
  def createMessage(message: Message)(implicit connection: Connection): Result[RootMessage] = {
    val statement = connection.prepareStatement(MessageQueries.createMessage)
    statement.setString(1, message.content)
    statement.setInt(2, message.sender.id)
    statement.setInt(3, message.server.id)
    statement.setString(4, message.createdAt.toString)

    val resultSet = statement.executeQuery()
    resultSet.last()

    if (resultSet.getRow <= 0) Failure("An error occurred.")
    else Success(
      result = Some(
        RootMessage(
          id = resultSet.getInt(1),
          content = message.content,
          sender = message.sender.toChildReadable,
          server = message.server.toChildReadable,
          createdAt = message.createdAt
        )
      ),
      message = None
    )
  }
}
