package database.model

import model.{Role, User}

case class UserResult(success: Boolean, user: Option[User], message: Option[String])

case class ServerUsersResult(success: Boolean, user: List[Map[User, Role.Value]])
