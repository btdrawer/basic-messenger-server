package routes

import java.sql.Timestamp

import akka.http.scaladsl.model.StatusCodes
import spray.json.enrichAny

import model._

class MessageRouteSpec extends RouteSpec {
  "The message routes" should {
    "create a new message" in {
      val params = CreatableMessage(
        content = "Hello world",
        server = 1
      ).toJson.toString
      val request = this.createPostRoute("/messages", params)
      request ~> addCredentials(testLogins("admin")) ~!> routes ~> check {
        val res = responseAs[Result[Message]]
        val message = res.result.get
        message.content shouldEqual "Hello world"
        message.server.id shouldEqual 1
        message.sender.id shouldEqual 1
        res.message shouldEqual Some("Message sent.")
      }
    }

    "return error if server not found" in {
      val params = CreatableMessage(
        content = "Hello world",
        server = 30
      ).toJson.toString
      val request = this.createPostRoute("/messages", params)
      request ~> addCredentials(testLogins("admin")) ~!> routes ~> check {
        responseAs[Result[Message]] shouldEqual Failure(
          "Server not found."
        )
      }
    }

    "return error if user is not a member of the server" in {
      val params = CreatableMessage(
        content = "Hello world",
        server = 2
      ).toJson.toString
      val request = this.createPostRoute("/messages", params)
      request ~> addCredentials(testLogins("moderator")) ~!> routes ~> check {
        responseAs[Result[Message]] shouldEqual Failure(
          "User not found."
        )
      }
    }

    "retrieve a list of messages from a server with default limit and offset parameters" in {
      Get("/messages?server=1") ~> addCredentials(testLogins("member")) ~!> routes ~> check {
        status shouldEqual StatusCodes.OK
        responseAs[List[ChildMessage]] shouldEqual List(
          ChildMessage(
            id = 1,
            content = "Hello1",
            sender = ChildUser(
              id = 1,
              username = "admin",
              status = Status.withName("OFFLINE")
            ),
            createdAt = Timestamp.valueOf("2020-09-27 11:28:00")
          ),
          ChildMessage(
            id = 2,
            content = "Hello2",
            sender = ChildUser(
              id = 2,
              username = "moderator",
              status = Status.withName("OFFLINE")
            ),
            createdAt = Timestamp.valueOf("2020-09-27 11:28:00")
          ),
          ChildMessage(
            id = 3,
            content = "Hello3",
            sender = ChildUser(
              id = 3,
              username = "member",
              status = Status.withName("OFFLINE")
            ),
            createdAt = Timestamp.valueOf("2020-09-27 11:28:00")
          )
        )
      }
    }

    "retrieve a list of messages from a server with custom limit parameter" in {
      Get("/messages?server=1&limit=2") ~> addCredentials(testLogins("member")) ~!> routes ~> check {
        status shouldEqual StatusCodes.OK
        responseAs[List[ChildMessage]] shouldEqual List(
          ChildMessage(
            id = 1,
            content = "Hello1",
            sender = ChildUser(
              id = 1,
              username = "admin",
              status = Status.withName("OFFLINE")
            ),
            createdAt = Timestamp.valueOf("2020-09-27 11:28:00")
          ),
          ChildMessage(
            id = 2,
            content = "Hello2",
            sender = ChildUser(
              id = 2,
              username = "moderator",
              status = Status.withName("OFFLINE")
            ),
            createdAt = Timestamp.valueOf("2020-09-27 11:28:00")
          )
        )
      }
    }

    "retrieve a list of messages from a server with custom offset parameter" in {
      Get("/messages?server=1&offset=1") ~> addCredentials(testLogins("member")) ~!> routes ~> check {
        status shouldEqual StatusCodes.OK
        responseAs[List[ChildMessage]] shouldEqual List(
          ChildMessage(
            id = 2,
            content = "Hello2",
            sender = ChildUser(
              id = 2,
              username = "moderator",
              status = Status.withName("OFFLINE")
            ),
            createdAt = Timestamp.valueOf("2020-09-27 11:28:00")
          ),
          ChildMessage(
            id = 3,
            content = "Hello3",
            sender = ChildUser(
              id = 3,
              username = "member",
              status = Status.withName("OFFLINE")
            ),
            createdAt = Timestamp.valueOf("2020-09-27 11:28:00")
          )
        )
      }
    }

    "retrieve a list of messages from a server with custom limit and offset parameter" in {
      Get("/messages?server=1&limit=1&offset=1") ~> addCredentials(testLogins("member")) ~!> routes ~> check {
        status shouldEqual StatusCodes.OK
        responseAs[List[ChildMessage]] shouldEqual List(
          ChildMessage(
            id = 2,
            content = "Hello2",
            sender = ChildUser(
              id = 2,
              username = "moderator",
              status = Status.withName("OFFLINE")
            ),
            createdAt = Timestamp.valueOf("2020-09-27 11:28:00")
          )
        )
      }
    }

    "throw error if limit exceeds 1000" in {
      Get("/messages?server=1&limit=1001") ~> addCredentials(testLogins("member")) ~!> routes ~> check {
        status shouldEqual StatusCodes.BadRequest
        responseAs[Result[NoRootElement]] shouldEqual Failure(
          "The limit must have a value between 0 and 1000, or be left blank."
        )
      }
    }

    "throw error if limit is less than 0" in {
      Get("/messages?server=1&limit=-1") ~> addCredentials(testLogins("member")) ~!> routes ~> check {
        status shouldEqual StatusCodes.BadRequest
        responseAs[Result[NoRootElement]] shouldEqual Failure(
          "The limit must have a value between 0 and 1000, or be left blank."
        )
      }
    }

    "throw error if offset is less than 0" in {
      Get("/messages?server=1&offset=-1") ~> addCredentials(testLogins("member")) ~!> routes ~> check {
        status shouldEqual StatusCodes.BadRequest
        responseAs[Result[NoRootElement]] shouldEqual Failure(
          "The offset must be at least 0, or left blank."
        )
      }
    }
  }
}
