package routes

import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import model.JsonConverters

class RouteSpec extends AnyWordSpec
  with Matchers with ScalatestRouteTest with JsonConverters with SeedDatabase {
  lazy val routes: Route = Main()
}
