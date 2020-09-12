package routes

import spray.json.enrichAny

import model._

class UserRouteSpec extends RouteSpec {
  "The user endpoints" should {
    // Create user

    "create a new user" in {
      val params = CreatableUser(
        username = "ben2",
        password = "Password222",
        passwordReset = CreatablePasswordReset(
          question = 1,
          answer = "Hi"
        )
      ).toJson.toString
      val request = this.createPostRoute("/users", params)
      request ~!> routes ~> check {
        val res = responseAs[Result[User]]
        res.result.get.username shouldEqual "ben2"
        res.message shouldEqual Some("User successfully created.")
      }
    }

    "throw an error if the password is not valid" in {
      val params = CreatableUser(
        username = "ben2",
        password = "password",
        passwordReset = CreatablePasswordReset(
          question = 1,
          answer = "Hi"
        )
      ).toJson.toString
      val request = this.createPostRoute("/users", params)
      request ~!> routes ~> check {
        responseAs[Result[User]] shouldEqual Failure(
          "Your password must be at least 8 characters and contain " +
            "at least one lowercase letter, uppercase letter, and number."
        )
      }
    }

    "throw an error if the username has already been taken" in {
      val params = CreatableUser(
        username = "ben",
        password = "Password222",
        passwordReset = CreatablePasswordReset(
          question = 1,
          answer = "Hi"
        )
      ).toJson.toString
      val request = this.createPostRoute("/users", params)
      request ~!> routes ~> check {
        responseAs[Result[User]] shouldEqual Failure(
          "A user with that username already exists."
        )
      }
    }

    // Get user

    "retrieve a user" in {
      Get("/users/1") ~!> routes ~> check {
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

    "return error if user is not found" in {
      Get("/users/30") ~!> routes ~> check {
        responseAs[Result[User]] shouldEqual Failure(
          message = "User not found."
        )
      }
    }

    // Delete user

    "delete a user" in {
      Delete("/users/1") ~!> routes ~> check {
        responseAs[Result[User]] shouldEqual Success(
          result = None,
          message = Some("User deleted.")
        )
      }
    }
  }
}
