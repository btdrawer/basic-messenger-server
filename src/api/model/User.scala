package api.model

import model.{PasswordReset, Role, Status}

case class CreatableUser(
  username: String,
  password: String,
  status: Status,
  passwordReset: PasswordReset
)

case class ReadableUser(
  id: String,
  username: String,
  servers: Option[Map[ReadableServer, Role.Value]],
  status: Status
)

case class UpdatableUser(
  id: String,
  username: Option[String],
  password: Option[String],
  status: Option[Status],
  passwordReset: Option[PasswordReset]
)

case class UserResult(success: Boolean, user: Option[ReadableUser], message: Option[String])
object UserResult {
  def createFailedUserResult(message: String): UserResult = UserResult(
    success = false,
    user = None,
    message = Some(message)
  )
}
