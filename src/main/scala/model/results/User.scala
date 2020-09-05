package model.results

import model.resources.RootReadableUser

case class UserSuccess(
  override val result: RootReadableUser,
  override val message: Option[String]
) extends Success[RootReadableUser](result, message)

case class UserFailure(
  override val message: String
) extends Failure[RootReadableUser](message)
