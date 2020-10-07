package app

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.{Directives, ExceptionHandler, Route}
import com.zaxxer.hikari.HikariDataSource

import model.{ApiException, Failure, JsonConverters}
import routes._

import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.io.StdIn

object App extends Directives with JsonConverters {
  implicit def system: ActorSystem[Nothing] = ActorSystem(Behaviors.empty, "my-system")

  implicit def executionContext: ExecutionContextExecutor = system.executionContext

  implicit def launchConnectionPool(): HikariDataSource = {
    val host = System.getenv("DB_HOST")
    val url = s"jdbc:postgresql://$host"
    val username = System.getenv("DB_USERNAME")
    val password = System.getenv("DB_PASSWORD")
    val maximumPoolSize = System.getenv("DB_CONNECTION_MAX_POOL_SIZE").toInt
    val dataSource: HikariDataSource = new HikariDataSource()
    dataSource.setJdbcUrl(url)
    dataSource.setUsername(username)
    dataSource.setPassword(password)
    dataSource.setMaximumPoolSize(maximumPoolSize)
    dataSource
  }

  private def exceptionHandler: ExceptionHandler = ExceptionHandler {
    case ApiException(err) =>
      complete(err.statusCode -> Failure(err.message))
    case err =>
      err.printStackTrace()
      complete(StatusCodes.InternalServerError -> "Sorry, an error occurred.")
  }

  def routes(implicit connectionPool: HikariDataSource): Route = handleExceptions(exceptionHandler) {
    concat(
      ServerRouteHandler().routes,
      UserRouteHandler().routes,
      MessageRouteHandler().routes
    )
  }

  private def createServer(): Future[Http.ServerBinding] = {
    implicit val connectionPool: HikariDataSource = launchConnectionPool()
    val host = System.getenv("SERVER_HOST")
    val port = System.getenv("SERVER_PORT")
    Http().newServerAt(host, port.toInt).bind(routes)
  }

  private def terminateServer(server: Future[Http.ServerBinding]): Unit =
    server
      .flatMap(_.unbind())
      .onComplete(_ => system.terminate())

  def main(args: Array[String]): Unit = {
    val server = createServer()
    println(s"Server online, press RETURN to stop...")
    StdIn.readLine()
    terminateServer(server)
  }
}
