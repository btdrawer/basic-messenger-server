import java.sql.Connection

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.Http
import api.routes.{Main => GetRoutes}
import database.Connection

import scala.concurrent.ExecutionContextExecutor
import scala.io.StdIn

object Main {
  private def launchServer(host: String, port: Int, routes: Route): Unit = {
    implicit val system: ActorSystem[Nothing] = ActorSystem(Behaviors.empty, "my-system")
    implicit val executionContext: ExecutionContextExecutor = system.executionContext

    val bindingFuture = Http().newServerAt(host, port).bind(routes)

    println(s"Server online at http://$host:$port/\nPress RETURN to stop...")
    StdIn.readLine()
    bindingFuture
      .flatMap(_.unbind())
      .onComplete(_ => system.terminate())
  }

  def main(args: Array[String]): Unit = {
    val host = args(0)
    val port = args(1)

    implicit val connection: Connection = Connection(
      host = args(2),
      username = args(3),
      password = args(4)
    )

    val routes = GetRoutes()

    launchServer(host, port.toInt, routes)
  }
}
