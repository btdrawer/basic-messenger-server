package database.model

import model.Role

case class BasicServer(id: String, name: String, address: String)

case class ServerUsersResult(success: Boolean, user: List[Map[BasicUser, Role.Value]])
