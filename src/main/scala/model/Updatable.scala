package model

sealed trait Updatable

case class UpdatableUser(
  password: Option[String],
  status: Option[Status.Value],
  passwordReset: Option[CreatablePasswordReset]
) extends Updatable
