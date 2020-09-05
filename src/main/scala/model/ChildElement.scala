package model

import java.time.Instant

sealed trait ChildElement

case class ChildServer(
  id: String,
  name: String,
  address: String
) extends ChildElement

case class ChildUser(
  id: String,
  username: String,
  status: Status.Value
) extends ChildElement

case class ChildMessage(
  id: String,
  content: String,
  sender: ChildUser,
  createdAt: Instant
) extends ChildElement
