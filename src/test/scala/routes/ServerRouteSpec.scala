package routes

import spray.json.enrichAny

import model._

class ServerRouteSpec extends RouteSpec {
  "The server endpoints" should {
    "create a new server" in {
      val params = CreatableServer(
        name = "New server",
        address = "newserver",
        creator = 1
      ).toJson.toString
      val request = this.createPostRoute("/servers", params)
      request ~!> routes ~> check {
        val res = responseAs[Result[Server]]
        val createdServer = res.result.get
        createdServer.name shouldEqual "New server"
        createdServer.address shouldEqual "newserver"
        createdServer.users.head.user.id shouldEqual 1
        createdServer.users.head.role shouldEqual "ADMIN"
        res.message shouldEqual Some("Server successfully created.")
      }
    }

    "should retrieve a server by its address" in {
      Get("/servers/address/exampleserver") ~!> routes ~> check {
        responseAs[Result[Server]] shouldEqual Success(
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
      }
    }
  }
}
