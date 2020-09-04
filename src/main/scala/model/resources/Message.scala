package model.resources

import java.time.Instant

import model.interfaces.{ChildReadable, Creatable, Result, RootReadable}

case class Message(
  id: String,
  content: String,
  server: Option[Server],
  sender: User,
  createdAt: Instant
)

case class MessageResult(
  success: Boolean,
  result: Option[RootReadableMessage],
  message: Option[String]
) extends Result[RootReadableMessage](success, result, message)

case class RootReadableMessage(
  override val id: String,
  override val content: String,
  override val server: Option[ChildReadableServer],
  override val sender: ChildReadableUser,
  override val createdAt: Instant
) extends Message(id, content, server, sender, createdAt) with RootReadable

case class ChildReadableMessage(
  override val id: String,
  override val content: String,
  override val sender: ChildReadableUser,
  override val createdAt: Instant
) extends Message(id, content, None, sender, createdAt) with ChildReadable

case class CreatableMessage(content: String, sender: String) extends Creatable
