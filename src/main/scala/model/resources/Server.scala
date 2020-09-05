package model.resources

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
