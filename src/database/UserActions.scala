package database

import java.sql.Connection

import model.User

case class UserResult(user: User, success: Boolean, message: String)

object UserActions {
  def createUser(user: User)(implicit connection: Connection): UserResult = ???
}
