package model

import java.time.Instant

case class Message(
  id: String,
  content: String,
  server: Server,
  sender: User,
  createdAt: Instant
)
