package routes

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.http.scaladsl.server._
import Directives._
import model.{Result, RootUser, Success}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import routes.{User => UserRoutes}

/*
object UserRouteSpec extends AnyWordSpec with Matchers with ScalatestRouteTest {
  lazy val userRoutes: Route = UserRoutes()

  "The service" should {
    "create a new user" in {
      Post("/users") ~> userRoutes ~> check {
        responseAs[Result[RootUser]] shouldEqual Success(
          result = ???,
          message = Some("")
        )
      }
    }
  }
}
*/