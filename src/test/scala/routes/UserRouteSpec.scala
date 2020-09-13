package routes

import akka.http.scaladsl.model.StatusCodes
import spray.json.enrichAny

import model._

class UserRouteSpec extends RouteSpec {
  private def passwordInvalidTemplate(password: String): Unit = {
    val params = CreatableUser(
      username = "newben",
      password,
      passwordReset = CreatablePasswordReset(
        question = 1,
        answer = "Hi"
      )
    ).toJson.toString
    val request = this.createPostRoute("/users", params)
    request ~!> routes ~> check {
      status shouldEqual StatusCodes.BadRequest
      responseAs[Result[User]] shouldEqual Failure(
        "Your password must be at least 8 characters and contain " +
          "at least one lowercase letter, uppercase letter, and number."
      )
    }
  }

  "The user endpoints" should {
    // Create user

    "create a new user" in {
      val params = CreatableUser(
        username = "newben",
        password = "Password222",
        passwordReset = CreatablePasswordReset(
          question = 1,
          answer = "Hi"
        )
      ).toJson.toString
      val request = this.createPostRoute("/users", params)
      request ~!> routes ~> check {
        status shouldEqual StatusCodes.OK
        val res = responseAs[Result[User]]
        res.result.get.username shouldEqual "newben"
        res.message shouldEqual Some("User successfully created.")
      }
    }

    "not create a new user if the password does not have a capital letter" in {
      passwordInvalidTemplate("password222")
    }

    "not create a new user if the password does not have a number" in {
      passwordInvalidTemplate("Password")
    }

    "not create a new user if the password is less than 8 characters" in {
      passwordInvalidTemplate("Pass2")
    }

    "not create a new user if the username has already been taken" in {
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
        status shouldEqual StatusCodes.BadRequest
        responseAs[Result[User]] shouldEqual Failure(
          "A user with that username already exists."
        )
      }
    }

    // Get user

    "retrieve a user" in {
      Get("/users/1") ~!> routes ~> check {
        status shouldEqual StatusCodes.OK
        responseAs[Result[User]] shouldEqual Success(
          result = Some(
            User(
              id = 1,
              username = "ben",
              servers = List(
                UserServerRole(
                  server = ChildServer(
                    id = 1,
                    name = "Example Server",
                    address = "exampleserver"
                  ),
                  role = Role.withName("ADMIN")
                )
              ),
              status = Status.withName("OFFLINE")
            )
          ),
          message = None
        )
      }
    }

    "return error if the user is not found" in {
      Get("/users/30") ~!> routes ~> check {
        status shouldEqual StatusCodes.NotFound
        responseAs[Result[User]] shouldEqual Failure(
          message = "User not found."
        )
      }
    }

    // Update user

    "update a username" in {
      val params = UpdatableUser(
        username = Some("newben2"),
        password = None,
        status = None,
        passwordReset = None
      ).toJson.toString
      val request = this.createPutRoute("/users/1", params)
      request ~!> routes ~> check {
        status shouldEqual StatusCodes.OK
        val res = responseAs[Result[User]]
        val user = res.result.get
        user.id shouldEqual 1
        user.username shouldEqual "newben2"
        res.message shouldEqual Some("User updated successfully.")
      }
    }

    "not update a username if it is already taken" in {
      val params = UpdatableUser(
        username = Some("ben"),
        password = None,
        status = None,
        passwordReset = None
      ).toJson.toString
      val request = this.createPutRoute("/users/1", params)
      request ~!> routes ~> check {
        status shouldEqual StatusCodes.BadRequest
        responseAs[Result[User]] shouldEqual Failure(
          "A user with that username already exists."
        )
      }
    }

    "update a password" in {
      val params = UpdatableUser(
        username = None,
        password = Some("Newpassword12"),
        status = None,
        passwordReset = None
      ).toJson.toString
      val request = this.createPutRoute("/users/1", params)
      request ~!> routes ~> check {
        status shouldEqual StatusCodes.OK
        val res = responseAs[Result[User]]
        res.message shouldEqual Some("User updated successfully.")
      }
    }

    "not update a password if it is invalid" in {
      val params = UpdatableUser(
        username = None,
        password = Some("password"),
        status = None,
        passwordReset = None
      ).toJson.toString
      val request = this.createPutRoute("/users/1", params)
      request ~!> routes ~> check {
        status shouldEqual StatusCodes.BadRequest
        responseAs[Result[User]] shouldEqual Failure(
          "Your password must be at least 8 characters and contain " +
            "at least one lowercase letter, uppercase letter, and number."
        )
      }
    }

    "update a status" in {
      val params = UpdatableUser(
        username = None,
        password = None,
        status = Some(Status.ONLINE),
        passwordReset = None
      ).toJson.toString
      val request = this.createPutRoute("/users/1", params)
      request ~!> routes ~> check {
        status shouldEqual StatusCodes.OK
        val res = responseAs[Result[User]]
        res.result.get.status shouldEqual Status.ONLINE
        res.message shouldEqual Some("User updated successfully.")
      }
    }

    "update password reset information" in {
      val params = UpdatableUser(
        username = None,
        password = None,
        status = None,
        passwordReset = Some(
          CreatablePasswordReset(
            question = 1,
            answer = "Updated answer"
          )
        )
      ).toJson.toString
      val request = this.createPutRoute("/users/1", params)
      request ~!> routes ~> check {
        status shouldEqual StatusCodes.OK
        val res = responseAs[Result[User]]
        res.message shouldEqual Some("User updated successfully.")
      }
    }

    "update all user data at once" in {
      val params = UpdatableUser(
        username = Some("newben2"),
        password = Some("Newpassword12"),
        status = Some(Status.ONLINE),
        passwordReset = Some(
          CreatablePasswordReset(
            question = 1,
            answer = "Updated answer"
          )
        )
      ).toJson.toString
      val request = this.createPutRoute("/users/1", params)
      request ~!> routes ~> check {
        status shouldEqual StatusCodes.OK
        val res = responseAs[Result[User]]
        val user = res.result.get
        user.id shouldEqual 1
        user.username shouldEqual "newben2"
        user.status shouldEqual Status.ONLINE
        res.message shouldEqual Some("User updated successfully.")
      }
    }

    "not update user if not found" in {
      val params = UpdatableUser(
        username = Some("newben2"),
        password = None,
        status = None,
        passwordReset = None
      ).toJson.toString
      val request = this.createPutRoute("/users/30", params)
      request ~!> routes ~> check {
        responseAs[Result[User]] shouldEqual Failure(
          "User not found."
        )
      }
    }

    // Delete user

    "delete a user" in {
      Delete("/users/1") ~!> routes ~> check {
        status shouldEqual StatusCodes.OK
        responseAs[Result[User]] shouldEqual Success(
          result = None,
          message = Some("User deleted.")
        )
      }
    }

    "not delete a user if not found" in {
      Delete("/users/30") ~!> routes ~> check {
        status shouldEqual StatusCodes.NotFound
        responseAs[Result[User]] shouldEqual Failure(
          "User not found."
        )
      }
    }
  }
}
