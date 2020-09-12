package routes

import java.sql.Connection

import scala.io.Source
import org.scalatest.BeforeAndAfterEach
import org.scalatest.wordspec.AnyWordSpec

import database.GetConnection

trait SeedDatabase extends AnyWordSpec with BeforeAndAfterEach {
  implicit def connection: Connection = GetConnection(
    host = System.getenv("TEST_DB_HOST"),
    username = System.getenv("TEST_DB_USERNAME"),
    password = System.getenv("TEST_DB_PASSWORD")
  )

  override protected def beforeEach(): Unit = {
    val sqlScript = Source.fromResource("seed_database.sql")
    val sqlScriptString = sqlScript.mkString
    val statement = connection.prepareStatement(sqlScriptString)
    statement.executeUpdate()
    sqlScript.close()
  }
}
