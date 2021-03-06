package model

import akka.http.scaladsl.model.{StatusCode, StatusCodes}

case class ApiException(message: FailureMessage) extends Exception

case class FailureMessage(
  code: String,
  message: String,
  statusCode: StatusCode
)

object FailureMessages {
  final val GENERIC: FailureMessage = FailureMessage(
    code = "GENERIC",
    message = "Sorry, an error occurred.",
    statusCode = StatusCodes.InternalServerError
  )

  final val SERVER_NAME_TAKEN: FailureMessage = FailureMessage(
    code = "SERVER_NAME_TAKEN",
    message = "That server name is already taken.",
    statusCode = StatusCodes.BadRequest
  )
  final val SERVER_ADDRESS_TAKEN: FailureMessage = FailureMessage(
    code = "SERVER_ADDRESS_TAKEN",
    message = "That server address is already taken.",
    statusCode = StatusCodes.BadRequest
  )
  final val SERVER_NOT_FOUND: FailureMessage = FailureMessage(
    code = "SERVER_NOT_FOUND",
    message = "Server not found.",
    statusCode = StatusCodes.NotFound
  )

  final val USERNAME_EXISTS: FailureMessage = FailureMessage(
    code = "USERNAME_EXISTS",
    message = "A user with that username already exists.",
    statusCode = StatusCodes.BadRequest
  )
  final val PASSWORD_INVALID: FailureMessage = FailureMessage(
    code = "PASSWORD_INVALID",
    message = "Your password must be at least 8 characters and contain " +
      "at least one lowercase letter, uppercase letter, and number.",
    statusCode = StatusCodes.BadRequest
  )
  final val USER_NOT_FOUND: FailureMessage = FailureMessage(
    code = "USER_NOT_FOUND",
    message = "User not found.",
    statusCode = StatusCodes.NotFound
  )
  final val LOGIN_INCORRECT: FailureMessage = FailureMessage(
    code = "LOGIN_INCORRECT",
    message = "Your username or password were incorrect.",
    statusCode = StatusCodes.Forbidden
  )
  final val INSUFFICIENT_PERMISSIONS: FailureMessage = FailureMessage(
    code = "INSUFFICIENT_PERMISSIONS",
    message = "You do not have sufficient permission to complete this action.",
    statusCode = StatusCodes.Forbidden
  )

  final val BAD_LIMIT: FailureMessage = FailureMessage(
    code = "BAD_LIMIT",
    message = "The limit must have a value between 0 and 1000, or be left blank.",
    statusCode = StatusCodes.BadRequest
  )
  final val BAD_OFFSET: FailureMessage = FailureMessage(
    code = "BAD_OFFSET",
    message = "The offset must be at least 0, or left blank.",
    statusCode = StatusCodes.BadRequest
  )
}
