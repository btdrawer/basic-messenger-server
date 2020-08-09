package api.model

import model.{Message, Role}

trait ApiServer
case class BasicApiServer(id: String, name: String, address: String) extends ApiServer
case class FullApiServer(
  id: String,
  name: String,
  address: String,
  users: Map[BasicApiUser, Role.Value],
  messages: List[Message]
) extends ApiServer

case class ServerResult(success: Boolean, server: ApiServer, message: Option[Message])
case class ServerUsersResult(success: Boolean, user: Map[ApiUser, Role.Value])
