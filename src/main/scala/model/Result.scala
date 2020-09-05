package model

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

object Failure {
  def apply[T <: RootElement](message: String): Result[T] = Result[T](
    success = false,
    result = None,
    message = Some(message)
  )
}
