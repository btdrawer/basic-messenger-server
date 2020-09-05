package model

import java.time.Instant

sealed trait Resource

case class Server(
  id: String,
  name: String,
  address: String,
  users: Map[User, Role.Value],
  messages: List[Message]
) extends Resource

object Role extends Enumeration {
  type Role = Value
  val Admin, Moderator, Member = Value
}

case class User(
  id: String,
  username: String,
  password: String,
  servers: Map[Server, Role.Value],
  status: Status.Value,
  passwordReset: PasswordReset
) extends Resource

object Status extends Enumeration {
  type Status = Value
  val Online, Busy, Offline = Status
}

case class PasswordReset(
  question: PasswordResetQuestion,
  answer: String
) extends Resource

case class PasswordResetQuestion(
  id: String,
  content: String
) extends Resource

case class Message(
  id: String,
  content: String,
  server: Server,
  sender: User,
  createdAt: Instant
) extends Resource
