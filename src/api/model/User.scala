package api.model

import model.{PasswordReset, Role, Status}

case class CreatableUser(
  username: String,
  password: String,
  status: Status,
  passwordReset: PasswordReset
)

trait ReadableUser
case class BasicReadableUser(
  id: String,
  username: String,
  servers: Map[ReadableServer, Role.Value],
  status: Status
) extends ReadableUser

case class UpdatableUser(
  id: String,
  username: Option[String],
  password: Option[String],
  status: Option[Status],
  passwordReset: Option[PasswordReset]
)

case class UserResult(success: Boolean, user: Option[ReadableUser], message: Option[String])
