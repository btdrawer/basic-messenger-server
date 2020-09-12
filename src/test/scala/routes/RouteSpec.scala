package routes

import akka.http.scaladsl.model.{ContentType, HttpEntity, HttpRequest, MediaTypes}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import app.App
import model.JsonConverters

class RouteSpec extends AnyWordSpec
  with Matchers
  with ScalatestRouteTest
  with JsonConverters
  with DatabaseSeeder {
  lazy val routes: Route = App.routes

  def createPostRoute(route: String, params: String): HttpRequest = {
    val entity = HttpEntity(ContentType(MediaTypes.`application/json`), params)
    Post(route).withEntity(entity)
  }
}
