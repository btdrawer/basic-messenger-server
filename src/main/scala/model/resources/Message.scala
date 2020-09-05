package model.resources

import java.time.Instant

case class Message(
  id: String,
  content: String,
  server: Server,
  sender: User,
  createdAt: Instant
)

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

case class CreatableMessage(
  content: String,
  sender: String
) extends Creatable
