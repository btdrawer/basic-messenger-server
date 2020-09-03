package api.model

import java.time.Instant

case class CreatableMessage(
  content: String,
  server: ReadableServer,
  sender: ReadableUser,
  createdAt: Instant
)

case class ReadableMessage(
  id: String,
  content: String,
  server: Option[ReadableServer],
  sender: ReadableUser,
  createdAt: Instant
)

case class MessageResult(
  success: Boolean,
  result: Option[ReadableMessage],
  message: Option[String]
) extends Result[ReadableMessage](success, result, message)

object MessageResult {
  def success(result: Option[ReadableMessage], message: Option[String]): MessageResult = new MessageResult(
    true,
    result,
    message
  )
  def fail(message: String): MessageResult = new MessageResult(
    false,
    None,
    Some(message)
  )
}
