package api.model

abstract class Result[T](success: Boolean, result: Option[T], message: Option[String])

object Result {
  def success[A, B <: Result[A]](result: Option[A], message: Option[String]): B = new B(
    true,
    result,
    message
  )
  def fail[A, B <: Result[B]](message: String): B = new B(
    false,
    None,
    Some(message)
  )
}
