package api.model

import java.time.Instant

case class ReadableMessage(
  id: String,
  content: String,
  server: Option[ReadableServer],
  sender: ReadableUser,
  createdAt: Instant
)

case class MessageResult(success: Boolean, message: ReadableMessage)
