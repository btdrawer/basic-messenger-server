package api.model

import model.{Message, Role}

case class CreatableServer(
  name: String,
  address: String,
  creator: ReadableUser,
)

case class ReadableServer(
  id: String,
  name: String,
  address: String,
  users: Option[Map[ReadableUser, Role.Value]],
  messages: Option[List[Message]]
)

case class ServerResult(
  success: Boolean,
  result: Option[ReadableServer],
  message: Option[String]
) extends Result[ReadableServer](success, result, message)

object ServerResult {
  def success(result: Option[ReadableServer], message: Option[String]): ServerResult = new ServerResult(
    true,
    result,
    message
  )
  def fail(message: String): ServerResult = new ServerResult(
    false,
    None,
    Some(message)
  )
}

case class ServerUsersResult(success: Boolean, result: Map[ReadableUser, Role.Value])
  extends Result[Map[ReadableUser, Role.Value]](success, Some(result), None)
