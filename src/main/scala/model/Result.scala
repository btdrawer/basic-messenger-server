package model

import akka.http.scaladsl.model.{StatusCode, StatusCodes}

sealed case class Result[T <: RootElement](
  success: Boolean,
  result: Option[T],
  message: Option[String]
)

object Success {
  def apply[T <: RootElement](result: Option[T], message: Option[String]): Result[T] = Result[T](
    success = true,
    result = result,
    message = message
  )
}

case class ApiException(message: FailureMessage) extends Exception

object Failure {
  def apply(message: String): Result[NoRootElement] = Result[NoRootElement](
    success = false,
    result = None,
    message = Some(message)
  )
}

case class FailureMessage(
  code: String,
  message: String,
  statusCode: StatusCode
)

object FailureMessages {
  final def GENERIC: FailureMessage = FailureMessage(
    code = "GENERIC",
    message = "Sorry, an error occurred.",
    statusCode = StatusCodes.InternalServerError
  )

  final def SERVER_NAME_TAKEN: FailureMessage = FailureMessage(
    code = "SERVER_NAME_TAKEN",
    message = "That server name is already taken.",
    statusCode = StatusCodes.BadRequest
  )
  final def SERVER_ADDRESS_TAKEN: FailureMessage = FailureMessage(
    code = "SERVER_ADDRESS_TAKEN",
    message = "That server address is already taken.",
    statusCode = StatusCodes.BadRequest
  )
  final def SERVER_NOT_FOUND: FailureMessage = FailureMessage(
    code = "SERVER_NOT_FOUND",
    message = "Server not found.",
    statusCode = StatusCodes.NotFound
  )

  final def USERNAME_EXISTS: FailureMessage = FailureMessage(
    code = "USERNAME_EXISTS",
    message = "A user with that username already exists.",
    statusCode = StatusCodes.BadRequest
  )
  final def PASSWORD_INVALID: FailureMessage = FailureMessage(
    code = "PASSWORD_INVALID",
    message = "Your password must be at least 8 characters and contain " +
      "at least one lowercase letter, uppercase letter, and number.",
    statusCode = StatusCodes.BadRequest
  )
  final def USER_NOT_FOUND: FailureMessage = FailureMessage(
    code = "USER_NOT_FOUND",
    message = "User not found.",
    statusCode = StatusCodes.NotFound
  )
}
