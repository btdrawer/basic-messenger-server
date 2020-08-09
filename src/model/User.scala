package model

case class User(
  id: String,
  username: String,
  password: String,
  servers: Map[Server, Role.Value],
  status: Status,
  passwordReset: PasswordReset
)

case class Status(id: String, content: String)

case class PasswordReset(question: PasswordResetQuestion, answer: String)

case class PasswordResetQuestion(id: String, content: String)
