package model.resources

import java.time.Instant

import model.interfaces.{ChildReadable, Creatable, Result, RootReadable}

case class Message(
  id: String,
  content: String,
  server: Server,
  sender: User,
  createdAt: Instant
)

case class MessageResult(
  success: Boolean,
  result: Option[RootReadableMessage],
  message: Option[String]
) extends Result[RootReadableMessage](success, result, message)

case class RootReadableMessage(
  id: String,
  content: String,
  server: ChildReadableServer,
  sender: ChildReadableUser,
  createdAt: Instant
) extends RootReadable

case class ChildReadableMessage(
  id: String,
  content: String,
  sender: ChildReadableUser,
  createdAt: Instant
) extends ChildReadable

case class CreatableMessage(content: String, sender: String) extends Creatable
