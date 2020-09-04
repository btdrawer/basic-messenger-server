package model.resources

import model.Creatable
import model.interfaces.{ChildReadable, Creatable, Result, RootReadable}

case class User(
  id: String,
  username: String,
  password: Option[String],
  servers: Option[Map[Server, Role.Value]],
  status: Status.Value,
  passwordReset: Option[PasswordReset]
)

object Status extends Enumeration {
  type Status = Value
  val Online, Busy, Offline = Status
}

case class PasswordReset(question: PasswordResetQuestion, answer: String)

case class PasswordResetQuestion(id: String, content: String)

case class UserResult(
  success: Boolean,
  result: Option[RootReadableUser],
  message: Option[String]
) extends Result[RootReadableUser](success, result, message)

case class RootReadableUser(
  override val id: String,
  override val username: String,
  override val servers: Option[Map[ChildReadableServer, Role.Value]],
  override val status: Status.Value
) extends User(id, username, None, servers, status, None) with RootReadable

case class ChildReadableUser(
  override val id: String,
  override val username: String,
  override val status: Status.Value
) extends User(id, username, None, None, status, None) with ChildReadable

case class CreatableUser(
  username: String,
  password: String,
  status: Status.Value,
  passwordReset: PasswordReset
) extends Creatable
