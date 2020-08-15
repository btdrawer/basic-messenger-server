package database.actions

import java.sql.Connection

import model.Message

object Message {
  def createMessage(message: Message)(implicit connection: Connection):
}
