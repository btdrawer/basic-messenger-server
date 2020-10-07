package routes

import java.sql.{Connection, DriverManager}

import akka.http.scaladsl.model.headers.BasicHttpCredentials
import akka.http.scaladsl.model.{ContentType, HttpEntity, HttpRequest, MediaTypes}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.BeforeAndAfterEach
import com.zaxxer.hikari.HikariDataSource

import app.App
import authentication.HashPassword
import model.JsonConverters

import scala.io.Source

class RouteSpec extends AnyWordSpec
  with Matchers
  with ScalatestRouteTest
  with JsonConverters
  with DatabaseSeeder
  with Model {
  lazy val routes: Route = App.routes

  private def createRoute(requestBuilder: RequestBuilder, route: String, params: String): HttpRequest = {
    val entity = HttpEntity(ContentType(MediaTypes.`application/json`), params)
    requestBuilder(route).withEntity(entity)
  }

  def createPostRoute(route: String, params: String): HttpRequest =
    createRoute(requestBuilder = Post, route, params)

  def createPutRoute(route: String, params: String): HttpRequest =
    createRoute(requestBuilder = Put, route, params)
}

trait DatabaseSeeder extends AnyWordSpec with BeforeAndAfterEach {
  implicit def connection: Connection = {
    val host = System.getenv("DB_HOST")
    val url = s"jdbc:postgresql://$host"
    val username = System.getenv("DB_USERNAME")
    val password = System.getenv("DB_PASSWORD")
    DriverManager.getConnection(url, username, password)
  }

  def testLogins: Map[String, BasicHttpCredentials] = Map[String, BasicHttpCredentials](
    "admin" -> BasicHttpCredentials("admin", "Password222"),
    "moderator" -> BasicHttpCredentials("moderator", "Password223"),
    "member" -> BasicHttpCredentials("member", "Password224"),
    "extramember" -> BasicHttpCredentials("extramember", "Password225")
  )

  override protected def beforeEach(): Unit = {
    val sqlScript = Source.fromResource("seed_database.sql")
    val sqlScriptString = sqlScript.mkString
    val statement = connection.prepareStatement(sqlScriptString)
    val passwords = testLogins.map(elem => HashPassword(elem._2.password)).toList
    (1 to passwords.length).foreach(i => {
      val index = i * 2
      statement.setString(index - 1, passwords(i - 1).password)
      statement.setString(index, passwords(i - 1).salt)
    })
    statement.executeUpdate()
    sqlScript.close()
  }
}
