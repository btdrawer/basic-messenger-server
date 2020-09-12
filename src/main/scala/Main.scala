import java.sql.Connection

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route

import routes.{Main => GetRoutes}
import database.GetConnection

import scala.concurrent.ExecutionContextExecutor
import scala.io.StdIn

object Main {
  def main(args: Array[String]): Unit = {
    val host = System.getenv("SERVER_HOST")
    val port = System.getenv("SERVER_PORT")

    implicit val system: ActorSystem[Nothing] = ActorSystem(Behaviors.empty, "my-system")
    implicit val executionContext: ExecutionContextExecutor = system.executionContext
    implicit val connection: Connection = GetConnection(
      host = System.getenv("DB_HOST"),
      username = System.getenv("DB_USERNAME"),
      password = System.getenv("DB_PASSWORD")
    )

    val routes: Route = GetRoutes()
    val bindingFuture = Http().newServerAt(host, port.toInt).bind(routes)

    println(s"Server online at http://$host:$port/\nPress RETURN to stop...")
    StdIn.readLine()
    bindingFuture
      .flatMap(_.unbind())
      .onComplete(_ => system.terminate())
  }
}
