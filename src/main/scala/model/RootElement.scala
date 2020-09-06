package model

import java.time.Instant

sealed trait RootElement

case class NoRootElement() extends RootElement

case class RootServer(
  id: Int,
  name: String,
  address: String,
  users: List[ChildServerUserRole],
  messages: List[ChildMessage]
) extends RootElement

case class RootUser(
  id: Int,
  username: String,
  servers: List[ChildUserServerRole],
  status: Status.Value
) extends RootElement

case class RootMessage(
  id: Int,
  content: String,
  server: ChildServer,
  sender: ChildUser,
  createdAt: Instant
) extends RootElement
