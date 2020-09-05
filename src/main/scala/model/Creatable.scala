package model

sealed trait Creatable

case class CreatableServer(
  name: String,
  address: String,
  creator: String
) extends Creatable

case class CreatableUser(
  username: String,
  password: String,
  status: Status.Value,
  passwordReset: PasswordReset
) extends Creatable

case class CreatableMessage(
  content: String,
  sender: String
) extends Creatable
