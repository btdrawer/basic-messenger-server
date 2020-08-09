package api.model

import model.{Message, Role}

trait ReadableServer
case class BasicReadableServer(id: String, name: String, address: String) extends ReadableServer
case class FullReadableServer(
  id: String,
  name: String,
  address: String,
  users: Map[ReadableUser, Role.Value],
  messages: List[Message]
) extends ReadableServer

case class ServerResult(success: Boolean, server: ReadableServer, message: Option[Message])
case class ServerUsersResult(success: Boolean, user: Map[ReadableUser, Role.Value])
