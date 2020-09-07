package routes

import java.sql.Connection

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.{Directives, ExceptionHandler, Route}

import model.converters.JsonConverters
import model.{ApiException, Failure}

import scala.concurrent.ExecutionContext

object Main extends Directives with JsonConverters {
  def exceptionHandler: ExceptionHandler = ExceptionHandler {
    case ApiException(err) =>
      complete(err.statusCode -> Failure(err.message))
    case err =>
      err.printStackTrace()
      complete(StatusCodes.InternalServerError -> "Sorry, an error occurred.")
  }

  def apply()(implicit connection: Connection, executionContext: ExecutionContext): Route = {
    handleExceptions(exceptionHandler) {
      concat(
        User()
      )
    }
  }
}
