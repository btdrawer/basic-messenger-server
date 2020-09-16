package routes

import akka.http.scaladsl.model.StatusCodes
import spray.json.enrichAny

import model._

class ServerRouteSpec extends RouteSpec {
  private def exampleServerResponseModel: Result[Server] = Success(
    result = Some(
      Server(
        id = 1,
        name = "Example Server",
        address = "exampleserver",
        users = List(
          ServerUserRole(
            user = ChildUser(
              id = 1,
              username = "admin",
              status = Status.withName("OFFLINE")
            ),
            role = Role.withName("ADMIN")
          ),
          ServerUserRole(
            user = ChildUser(
              id = 2,
              username = "moderator",
              status = Status.withName("OFFLINE")
            ),
            role = Role.withName("MODERATOR")
          ),
          ServerUserRole(
            user = ChildUser(
              id = 3,
              username = "member",
              status = Status.withName("OFFLINE")
            ),
            role = Role.withName("MEMBER")
          ),
        ),
        messages = List()
      )
    ),
    message = None
  )

  "The server routes" should {
    "create a new server" in {
      val params = CreatableServer(
        name = "New server",
        address = "newserver"
      ).toJson.toString
      val request = this.createPostRoute("/servers", params)
      request ~> addCredentials(testLogins("admin")) ~!> routes ~> check {
        status shouldEqual StatusCodes.OK
        val res = responseAs[Result[Server]]
        val createdServer = res.result.get
        createdServer.name shouldEqual "New server"
        createdServer.address shouldEqual "newserver"
        createdServer.users.head.user.id shouldEqual 1
        createdServer.users.head.role shouldEqual Role.withName("ADMIN")
        res.message shouldEqual Some("Server successfully created.")
      }
    }

    "not create a new server if the address has already been taken" in {
      val params = CreatableServer(
        name = "New server",
        address = "exampleserver"
      ).toJson.toString
      val request = this.createPostRoute("/servers", params)
      request ~> addCredentials(testLogins("admin")) ~!> routes ~> check {
        status shouldEqual StatusCodes.BadRequest
        responseAs[Result[Server]] shouldEqual Failure(
          "That server address is already taken."
        )
      }
    }

    "find servers that match a name" in {
      Get("/servers/search/ple%20ser") ~!> routes ~> check {
        status shouldEqual StatusCodes.OK
        responseAs[List[ChildServer]] shouldEqual List(
          ChildServer(
            id = 1,
            name = "Example Server",
            address = "exampleserver"
          )
        )
      }
    }

    "retrieve a server by its address" in {
      Get("/servers/address/exampleserver") ~!> routes ~> check {
        status shouldEqual StatusCodes.OK
        responseAs[Result[Server]] shouldEqual exampleServerResponseModel
      }
    }

    "return an error if the server address does not exist" in {
      Get("/servers/address/fakeserver") ~!> routes ~> check {
        status shouldEqual StatusCodes.NotFound
        responseAs[Result[Server]] shouldEqual Failure(
          "Server not found."
        )
      }
    }

    "retrieve a server by its ID" in {
      Get("/servers/id/1") ~!> routes ~> check {
        status shouldEqual StatusCodes.OK
        responseAs[Result[Server]] shouldEqual exampleServerResponseModel
      }
    }

    "return an error if the server ID does not exist" in {
      Get("/servers/id/30") ~!> routes ~> check {
        status shouldEqual StatusCodes.NotFound
        responseAs[Result[Server]] shouldEqual Failure(
          "Server not found."
        )
      }
    }

    "update server if admin" in {
      val params = UpdatableServer(
        name = Some("Updated name")
      ).toJson.toString
      val request = this.createPutRoute("/servers/1", params)
      request ~> addCredentials(testLogins("admin")) ~!> routes ~> check {
        status shouldEqual StatusCodes.OK
        responseAs[Result[NoRootElement]] shouldEqual Success(
          result = None,
          message = Some("Server updated.")
        )
      }
    }

    "not update server if moderator" in {
      val params = UpdatableServer(
        name = Some("Updated name")
      ).toJson.toString
      val request = this.createPutRoute("/servers/1", params)
      request ~> addCredentials(testLogins("moderator")) ~!> routes ~> check {
        status shouldEqual StatusCodes.Forbidden
        responseAs[Result[NoRootElement]] shouldEqual Failure(
          "You do not have sufficient permission to complete this action."
        )
      }
    }

    "not update server if member" in {
      val params = UpdatableServer(
        name = Some("Updated name")
      ).toJson.toString
      val request = this.createPutRoute("/servers/1", params)
      request ~> addCredentials(testLogins("member")) ~!> routes ~> check {
        status shouldEqual StatusCodes.Forbidden
        responseAs[Result[NoRootElement]] shouldEqual Failure(
          "You do not have sufficient permission to complete this action."
        )
      }
    }

    "add a new member to a server" in {
      Put("/servers/2/users/2") ~> addCredentials(testLogins("admin")) ~!> routes ~> check {
        status shouldEqual StatusCodes.OK
        responseAs[Result[NoRootElement]] shouldEqual Success(
          result = None,
          message = Some("User added to server.")
        )
      }
    }

    "not add a new member if server does not exist" in {
      Put("/servers/30/users/2") ~> addCredentials(testLogins("admin")) ~!> routes ~> check {
        status shouldEqual StatusCodes.NotFound
        responseAs[Result[Server]] shouldEqual Failure(
          "Server not found."
        )
      }
    }

    "not add a new member if member does not exist" in {
      Put("/servers/2/users/30") ~> addCredentials(testLogins("admin")) ~!> routes ~> check {
        status shouldEqual StatusCodes.NotFound
        responseAs[Result[Server]] shouldEqual Failure(
          "User not found."
        )
      }
    }

    "not add a new member if a member" in {
      Put("/servers/1/users/4") ~> addCredentials(testLogins("member")) ~!> routes ~> check {
        status shouldEqual StatusCodes.Forbidden
        responseAs[Result[Server]] shouldEqual Failure(
          "You do not have sufficient permission to complete this action."
        )
      }
    }

    "update user role to MODERATOR" in {
      Put("/servers/2/users/2/roles/MODERATOR") ~> addCredentials(testLogins("admin")) ~!> routes ~> check {
        status shouldEqual StatusCodes.OK
        responseAs[Result[NoRootElement]] shouldEqual Success(
          result = None,
          message = Some("User role updated.")
        )
      }
    }

    "update user role to ADMIN" in {
      Put("/servers/2/users/2/roles/ADMIN") ~> addCredentials(testLogins("admin")) ~!> routes ~> check {
        status shouldEqual StatusCodes.OK
        responseAs[Result[NoRootElement]] shouldEqual Success(
          result = None,
          message = Some("User role updated.")
        )
      }
    }

    "not update user role if server does not exist" in {
      Put("/servers/30/users/2/roles/MODERATOR") ~> addCredentials(testLogins("admin")) ~!> routes ~> check {
        status shouldEqual StatusCodes.NotFound
        responseAs[Result[Server]] shouldEqual Failure(
          "Server not found."
        )
      }
    }

    "not update user role if user does not exist" in {
      Put("/servers/2/users/30/roles/MODERATOR") ~> addCredentials(testLogins("admin")) ~!> routes ~> check {
        status shouldEqual StatusCodes.NotFound
        responseAs[Result[Server]] shouldEqual Failure(
          "User not found."
        )
      }
    }

    "not update user role if moderator" in {
      Put("/servers/1/users/3/roles/ADMIN") ~> addCredentials(testLogins("moderator")) ~!> routes ~> check {
        status shouldEqual StatusCodes.Forbidden
        responseAs[Result[Server]] shouldEqual Failure(
          "You do not have sufficient permission to complete this action."
        )
      }
    }

    "not update user role if member" in {
      Put("/servers/1/users/2/roles/ADMIN") ~> addCredentials(testLogins("member")) ~!> routes ~> check {
        status shouldEqual StatusCodes.Forbidden
        responseAs[Result[Server]] shouldEqual Failure(
          "You do not have sufficient permission to complete this action."
        )
      }
    }

    "remove user from server" in {
      Delete("/servers/1/users/2") ~> addCredentials(testLogins("admin")) ~!> routes ~> check {
        status shouldEqual StatusCodes.OK
        responseAs[Result[NoRootElement]] shouldEqual Success(
          result = None,
          message = Some("User removed from server.")
        )
      }
    }

    "not remove user from server if not a moderator" in {
      Delete("/servers/1/users/2") ~> addCredentials(testLogins("member")) ~!> routes ~> check {
        status shouldEqual StatusCodes.Forbidden
        responseAs[Result[Server]] shouldEqual Failure(
          "You do not have sufficient permission to complete this action."
        )
      }
    }

    "return error if server does not exist" in {
      Delete("/servers/30/users/1") ~> addCredentials(testLogins("admin")) ~!> routes ~> check {
        status shouldEqual StatusCodes.NotFound
        responseAs[Result[Server]] shouldEqual Failure(
          "Server not found."
        )
      }
    }

    "return error if user does not exist" in {
      Delete("/servers/2/users/30") ~> addCredentials(testLogins("admin")) ~!> routes ~> check {
        status shouldEqual StatusCodes.NotFound
        responseAs[Result[Server]] shouldEqual Failure(
          "User not found."
        )
      }
    }

    "delete server" in {
      Delete("/servers/1") ~> addCredentials(testLogins("admin")) ~!> routes ~> check {
        status shouldEqual StatusCodes.OK
        responseAs[Result[NoRootElement]] shouldEqual Success(
          result = None,
          message = Some("Server deleted.")
        )
      }
    }

    "not delete server if moderator" in {
      Delete("/servers/1") ~> addCredentials(testLogins("moderator")) ~!> routes ~> check {
        status shouldEqual StatusCodes.Forbidden
        responseAs[Result[Server]] shouldEqual Failure(
          "You do not have sufficient permission to complete this action."
        )
      }
    }

    "not delete server if member" in {
      Delete("/servers/1") ~> addCredentials(testLogins("member")) ~!> routes ~> check {
        status shouldEqual StatusCodes.Forbidden
        responseAs[Result[Server]] shouldEqual Failure(
          "You do not have sufficient permission to complete this action."
        )
      }
    }

    "not delete server if it doesn't exist" in {
      Delete("/servers/30") ~> addCredentials(testLogins("admin")) ~!> routes ~> check {
        status shouldEqual StatusCodes.NotFound
        responseAs[Result[NoRootElement]] shouldEqual Failure(
          "Server not found."
        )
      }
    }
  }
}
