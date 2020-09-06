package model

import java.time.Instant

sealed trait Resource

case class Server(
  id: Int,
  name: String,
  address: String,
  users: List[ServerUserRole],
  messages: List[Message]
) extends Resource

case class ServerUserRole(
  user: User,
  role: Role.Value
) extends Resource

object Role extends Enumeration {
  val ADMIN: Role.Value = Value("ADMIN")
  val MODERATOR: Role.Value = Value("MODERATOR")
  val MEMBER: Role.Value = Value("MEMBER")
}

case class User(
  id: Int,
  username: String,
  password: String,
  servers: List[UserServerRole],
  status: Status.Value,
  passwordReset: PasswordReset
) extends Resource

case class UserServerRole(
  server: Server,
  role: Role.Value
) extends Resource

object Status extends Enumeration {
  val ONLINE: Status.Value = Value("ONLINE")
  val BUSY: Status.Value = Value("BUSY")
  val OFFLINE: Status.Value = Value("OFFLINE")
}

case class PasswordReset(
  question: PasswordResetQuestion,
  answer: String
) extends Resource

case class PasswordResetQuestion(
  id: Int,
  content: String
) extends Resource

case class Message(
  id: Int,
  content: String,
  server: Server,
  sender: User,
  createdAt: Instant
) extends Resource
