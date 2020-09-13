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
              username = "ben",
              status = Status.withName("OFFLINE")
            ),
            role = Role.withName("ADMIN")
          ),
          ServerUserRole(
            user = ChildUser(
              id = 2,
              username = "ben2",
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

  "The server endpoints" should {
    "create a new server" in {
      val params = CreatableServer(
        name = "New server",
        address = "newserver",
        creator = 1
      ).toJson.toString
      val request = this.createPostRoute("/servers", params)
      request ~!> routes ~> check {
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
        address = "exampleserver",
        creator = 1
      ).toJson.toString
      val request = this.createPostRoute("/servers", params)
      request ~!> routes ~> check {
        status shouldEqual StatusCodes.BadRequest
        responseAs[Result[Server]] shouldEqual Failure(
          "That server address is already taken."
        )
      }
    }

    "should find servers that match a name" in {
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

    "should retrieve a server by its address" in {
      Get("/servers/address/exampleserver") ~!> routes ~> check {
        status shouldEqual StatusCodes.OK
        responseAs[Result[Server]] shouldEqual exampleServerResponseModel
      }
    }

    "should return an error if the server address does not exist" in {
      Get("/servers/address/fakeserver") ~!> routes ~> check {
        status shouldEqual StatusCodes.NotFound
        responseAs[Result[Server]] shouldEqual Failure(
          "Server not found."
        )
      }
    }

    "should retrieve a server by its ID" in {
      Get("/servers/id/1") ~!> routes ~> check {
        status shouldEqual StatusCodes.OK
        responseAs[Result[Server]] shouldEqual exampleServerResponseModel
      }
    }

    "should return an error if the server ID does not exist" in {
      Get("/servers/id/30") ~!> routes ~> check {
        status shouldEqual StatusCodes.NotFound
        responseAs[Result[Server]] shouldEqual Failure(
          "Server not found."
        )
      }
    }
  }
}
