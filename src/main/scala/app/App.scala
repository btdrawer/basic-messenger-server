package app

import java.sql.{Connection, DriverManager}

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.{Directives, ExceptionHandler, Route}

import model.{ApiException, Failure, JsonConverters}
import routes._

import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.io.StdIn

object App extends Directives with JsonConverters {
  implicit def system: ActorSystem[Nothing] = ActorSystem(Behaviors.empty, "my-system")

  implicit def executionContext: ExecutionContextExecutor = system.executionContext

  implicit def connection: Connection = {
    val host = System.getenv("DB_HOST")
    val username = System.getenv("DB_USERNAME")
    val password = System.getenv("DB_PASSWORD")
    val url = s"jdbc:postgresql://$host"
    DriverManager.getConnection(url, username, password)
  }

  private def exceptionHandler: ExceptionHandler = ExceptionHandler {
    case ApiException(err) =>
      complete(err.statusCode -> Failure(err.message))
    case err =>
      err.printStackTrace()
      complete(StatusCodes.InternalServerError -> "Sorry, an error occurred.")
  }

  def routes: Route = handleExceptions(exceptionHandler) {
    concat(
      ServerRoutes(),
      UserRoutes(),
      MessageRoutes()
    )
  }

  private def getServer: Future[Http.ServerBinding] = {
    val host = System.getenv("SERVER_HOST")
    val port = System.getenv("SERVER_PORT")
    Http().newServerAt(host, port.toInt).bind(routes)
  }

  def main(args: Array[String]): Unit = {
    val server = getServer
    println(s"Server online, press RETURN to stop...")
    StdIn.readLine()
    server
      .flatMap(_.unbind())
      .onComplete(_ => system.terminate())
  }
}
