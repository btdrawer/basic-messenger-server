package routes

import java.sql.Connection

import app.App

import scala.io.Source
import org.scalatest.BeforeAndAfterEach
import org.scalatest.wordspec.AnyWordSpec

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
