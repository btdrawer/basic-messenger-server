package api.model

import model.{Role, Status}

trait ApiUser
case class BasicApiUser(
  id: String,
  username: String,
  servers: Map[ApiServer, Role.Value],
  status: Status
) extends ApiUser

case class UserResult(success: Boolean, user: Option[BasicApiUser], message: Option[String])
