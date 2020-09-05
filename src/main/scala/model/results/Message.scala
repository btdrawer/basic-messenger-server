package model.results

import model.resources.RootReadableMessage

case class MessageSuccess(
  override val result: RootReadableMessage,
  override val message: Option[String]
) extends Success[RootReadableMessage](result, message)

case class MessageFailure(
  override val message: String
) extends Failure[RootReadableMessage](message)
