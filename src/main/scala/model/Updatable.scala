package model

sealed trait Updatable

case class UpdatableUser(
  username: Option[String],
  password: Option[String],
  status: Option[Status.Value],
  passwordReset: Option[CreatablePasswordReset]
) extends Updatable

case class UpdatableServer(name: Option[String]) extends Updatable
