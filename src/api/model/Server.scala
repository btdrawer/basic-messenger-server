package api.model

import model.{Message, Role}

case class ReadableServer(
  id: String,
  name: String,
  address: String,
  users: Option[Map[ReadableUser, Role.Value]],
  messages: Option[List[Message]]
)

case class ServerResult(success: Boolean, server: Option[ReadableServer], message: Option[String])
object ServerResult {
  def createFailedServerResult(message: String): ServerResult = ServerResult(
    success = false,
    server = None,
    message = Some(message)
  )
}

case class ServerUsersResult(success: Boolean, user: Map[ReadableUser, Role.Value])
