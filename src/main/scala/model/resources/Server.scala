package model.resources

import model.interfaces.{ChildReadable, Creatable, Result, RootReadable}

case class Server(
  id: String,
  name: String,
  address: String,
  users: Map[User, Role.Value],
  messages: List[Message]
)

object Role extends Enumeration {
  type Role = Value
  val Admin, Moderator, Member = Value
}

case class ServerResult(
  success: Boolean,
  result: Option[RootReadableServer],
  message: Option[String]
) extends Result[RootReadableServer](success, result, message)

case class ServerUsersResult(success: Boolean, result: Map[ChildReadableUser, Role.Value])
  extends Result[Map[ChildReadableUser, Role.Value]](success, Some(result), None)

case class RootReadableServer(
  id: String,
  name: String,
  address: String,
  users: Map[ChildReadableUser, Role.Value],
  messages: List[ChildReadableMessage]
) extends RootReadable

case class ChildReadableServer(
  id: String,
  name: String,
  address: String
) extends ChildReadable

case class CreatableServer(
  name: String,
  address: String,
  creator: String
) extends Creatable
