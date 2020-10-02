package model

sealed trait Creatable

case class CreatableServer(name: String, address: String) extends Creatable
case class CreatableUser(username: String, password: String, passwordReset: CreatablePasswordReset)
  extends Creatable
case class CreatablePasswordReset(question: Int, answer: String) extends Creatable
case class CreatableMessage(content: String) extends Creatable
