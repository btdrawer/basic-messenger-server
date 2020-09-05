package model.results

import model.resources.{ChildReadableUser, Role, RootReadableServer}

case class ServerSuccess(
  override val result: RootReadableServer,
  override val message: Option[String]
) extends Success[RootReadableServer](result, message)

case class ServerFailure(
  override val message: String
) extends Failure[RootReadableServer](message)

case class ServerUsersResult(
  override val success: Boolean,
  result: Map[ChildReadableUser, Role.Value]
) extends Result[Map[ChildReadableUser, Role.Value]](success, Some(result), None)
