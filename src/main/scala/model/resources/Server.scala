package model.resources

import model.interfaces.{ChildReadable, Creatable, Result, RootReadable}

case class Server(
  id: String,
  name: String,
  address: String,
  users: Option[Map[User, Role.Value]],
  messages: Option[List[Message]]
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
  override val id: String,
  override val name: String,
  override val address: String,
  override val users: Option[Map[User, Role.Value]],
  override val messages: Option[List[Message]]
) extends Server(id, name, address, users, messages) with RootReadable

case class ChildReadableServer(
  override val id: String,
  override val name: String,
  override val address: String
) extends Server(id, name, address, None, None) with ChildReadable

case class CreatableServer(name: String, address: String, creator: String) extends Creatable