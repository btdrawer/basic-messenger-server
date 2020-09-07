package database.actions

import java.sql.Connection

import model._
import model.converters.ElementConverters.ToReadable
import database.RunQuery
import database.queries.{Message => MessageQueries}

object Message {
  def createMessage(message: Message)(implicit connection: Connection): Result[RootMessage] = {
    val resultSet = RunQuery(
      MessageQueries.createMessage,
      List(
        message.content,
        message.sender.id,
        message.server.id,
        message.createdAt.toString
      )
    )
    resultSet.first()
    if (resultSet.getRow <= 0) throw ApiException(FailureMessages.GENERIC)
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
