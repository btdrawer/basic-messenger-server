package model

import java.sql.Timestamp

sealed trait RootElement

case class NoRootElement() extends RootElement

case class Server(
  id: Int,
  name: String,
  address: String,
  users: List[ServerUserRole],
  messages: List[ChildMessage]
) extends RootElement

case class ServerUserRole(user: ChildUser, role: Role.Value)

object Role extends Enumeration {
  val ADMIN: Role.Value = Value("ADMIN")
  val MODERATOR: Role.Value = Value("MODERATOR")
  val MEMBER: Role.Value = Value("MEMBER")
}

case class User(
  id: Int,
  username: String,
  servers: List[UserServerRole],
  status: Status.Value
) extends RootElement

case class UserServerRole(server: ChildServer, role: Role.Value)

object Status extends Enumeration {
  val ONLINE: Status.Value = Value("ONLINE")
  val BUSY: Status.Value = Value("BUSY")
  val OFFLINE: Status.Value = Value("OFFLINE")
}

case class PasswordReset(question: PasswordResetQuestion, answer: String)

case class PasswordResetQuestion(id: Int, content: String)

class Message(
  val id: Int,
  val content: String,
  val sender: ChildUser,
  val createdAt: Timestamp
) extends RootElement

case class ServerMessage(
  override val id: Int,
  override val content: String,
  server: ChildServer,
  override val sender: ChildUser,
  override val createdAt: Timestamp
) extends Message(id, content, sender, createdAt)

case class DirectMessage(
  override val id: Int,
  override val content: String,
  recipient: ChildUser,
  override val sender: ChildUser,
  override val createdAt: Timestamp
) extends Message(id, content, sender, createdAt)
