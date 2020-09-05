package model

import java.time.Instant

sealed trait RootElement

case class RootServer(
  id: String,
  name: String,
  address: String,
  users: Map[ChildUser, Role.Value],
  messages: List[ChildMessage]
) extends RootElement

case class RootUser(
  id: String,
  username: String,
  servers: Map[ChildServer, Role.Value],
  status: Status.Value
) extends RootElement

case class RootMessage(
  id: String,
  content: String,
  server: ChildServer,
  sender: ChildUser,
  createdAt: Instant
) extends RootElement
