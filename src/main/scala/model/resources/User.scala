package model.resources

import model.interfaces.{ChildReadable, Creatable, Result, RootReadable}

case class User(
  id: String,
  username: String,
  password: String,
  servers: Map[Server, Role.Value],
  status: Status.Value,
  passwordReset: PasswordReset
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
  id: String,
  username: String,
  servers: Map[ChildReadableServer, Role.Value],
  status: Status.Value
) extends RootReadable

case class ChildReadableUser(
  id: String,
  username: String,
  status: Status.Value
) extends ChildReadable

case class CreatableUser(
  username: String,
  password: String,
  status: Status.Value,
  passwordReset: PasswordReset
) extends Creatable
