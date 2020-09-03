package api.model

abstract class Result[T](success: Boolean, result: Option[T], message: Option[String])
