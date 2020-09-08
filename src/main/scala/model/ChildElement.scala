package model

import java.time.Instant

sealed trait ChildElement

case class ChildServer(
  id: Int,
  name: String,
  address: String
) extends ChildElement

case class ChildUser(
  id: Int,
  username: String,
  status: Status.Value
) extends ChildElement

case class ChildMessage(
  id: Int,
  content: String,
  sender: ChildUser,
  createdAt: Instant
) extends ChildElement
