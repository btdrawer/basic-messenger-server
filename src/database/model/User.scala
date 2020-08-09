package database.model

import model.{Role, Status}

case class BasicUser(
  id: String,
  username: String,
  servers: Map[BasicServer, Role.Value],
  status: Status
)

case class UserResult(success: Boolean, user: Option[BasicUser], message: Option[String])
