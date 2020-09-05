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
  passwordReset: CreatablePasswordReset
) extends Creatable

case class CreatablePasswordReset(
  question: String,
  answer: String
) extends Creatable

case class CreatableMessage(
  content: String,
  sender: String
) extends Creatable
