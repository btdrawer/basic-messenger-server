package routes

import model._

class UserRouteSpec extends RouteSpec {
  "The service" should {
    "retrieve a user" in {
      Get("/users/1") ~> routes ~> check {
        responseAs[Result[User]] shouldEqual Success(
          result = Some(
            User(
              id = 1,
              username = "ben",
              servers = List(),
              status = Status.withName("OFFLINE")
            )
          ),
          message = None
        )
      }
    }
  }
}
