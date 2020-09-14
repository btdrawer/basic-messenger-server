package routes

import java.sql.Connection

import akka.http.scaladsl.model.{ContentType, HttpEntity, HttpRequest, MediaTypes}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.BeforeAndAfterEach
import app.App
import model.converters.JsonConverters

import scala.io.Source

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

  def createPutRoute(route: String, params: String): HttpRequest = {
    val entity = HttpEntity(ContentType(MediaTypes.`application/json`), params)
    Put(route).withEntity(entity)
  }
}

trait DatabaseSeeder extends AnyWordSpec with BeforeAndAfterEach {
  implicit def connection: Connection = App.connection

  override protected def beforeEach(): Unit = {
    val sqlScript = Source.fromResource("seed_database.sql")
    val sqlScriptString = sqlScript.mkString
    val statement = connection.prepareStatement(sqlScriptString)
    statement.executeUpdate()
    sqlScript.close()
  }
}
