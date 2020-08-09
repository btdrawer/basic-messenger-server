package model

case class User(
  id: String,
  username: String,
  password: String,
  servers: Map[Server, Role.Value],
  status: String,
  passwordReset: PasswordReset
)

case class PasswordReset(question: String, answer: String)
