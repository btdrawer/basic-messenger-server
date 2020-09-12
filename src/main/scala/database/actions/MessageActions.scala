package database.actions

import java.sql.Connection

import model._
import database.Query
import database.queries.MessageQueries

object MessageActions {
  def createMessage(message: Message)(implicit connection: Connection): Result[Message] = {
    val resultSet = Query.runQuery(
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
        Message(
          id = resultSet.getInt(1),
          content = message.content,
          sender = message.sender,
          server = message.server,
          createdAt = message.createdAt
        )
      ),
      message = None
    )
  }
}
