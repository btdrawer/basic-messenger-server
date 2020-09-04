package model.interfaces

abstract class Result[T <: RootReadable](
  success: Boolean,
  result: Option[T],
  message: Option[String]
)

trait Readable
trait RootReadable extends Readable
trait ChildReadable extends Readable

trait Creatable
trait Updatable
