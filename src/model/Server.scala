package model

case class Server(
  name: String,
  address: String,
  users: Map[User, Role.Value],
  messages: List[Message]
)

object Role extends Enumeration {
  type Role = Value
  val Admin, Moderator, Member = Value
}
