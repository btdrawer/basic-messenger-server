package model

trait User

case class DatabaseUser(
  id: String,
  username: String,
  password: String,
  servers: Map[Server, Role.Value],
  status: String,
  passwordReset: PasswordReset
) extends User

case class PasswordReset(question: String, answer: String)

case class SimplifiedUser(
  id: String,
  username: String,
  servers: Map[Server, Role.Value],
  status: String,
) extends User
