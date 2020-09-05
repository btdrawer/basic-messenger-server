package model.results

import model.resources.RootReadable

abstract class Result[T <: RootReadable](
  val success: Boolean,
  val res: Option[T],
  val msg: Option[String]
)

class Success[T <: RootReadable](
  val result: T,
  val message: Option[String]
) extends Result[T](true, Some(result), message)

class Failure[T <: RootReadable](
  val message: String
) extends Result[T](false, None, Some(message))
