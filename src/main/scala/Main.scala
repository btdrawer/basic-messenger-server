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
    val host = args(0)
    val port = args(1)

    implicit val system: ActorSystem[Nothing] = ActorSystem(Behaviors.empty, "my-system")
    implicit val executionContext: ExecutionContextExecutor = system.executionContext
    implicit val connection: Connection = GetConnection(
      host = args(2),
      username = args(3),
      password = args(4)
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
