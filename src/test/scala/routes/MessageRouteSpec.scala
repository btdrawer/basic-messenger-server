package routes

import akka.http.scaladsl.model.StatusCodes
import spray.json.enrichAny

import model._

class MessageRouteSpec extends RouteSpec {
  private val serverMessages: List[ChildMessage] = servers.head.messages

  "The message routes" should {
    "create a new server message" in {
      val params = CreatableMessage("Hello world").toJson.toString
      val request = this.createPostRoute("/messages/server/1", params)
      request ~> addCredentials(testLogins("admin")) ~!> routes ~> check {
        val res = responseAs[Result[ServerMessage]]
        val message = res.result.get
        message.content shouldEqual "Hello world"
        message.server.id shouldEqual 1
        message.sender.id shouldEqual 1
        res.message shouldEqual Some("Message sent.")
      }
    }

    "return error if server not found" in {
      val params = CreatableMessage("Hello world").toJson.toString
      val request = this.createPostRoute("/messages/server/30", params)
      request ~> addCredentials(testLogins("admin")) ~!> routes ~> check {
        responseAs[Result[ServerMessage]] shouldEqual Failure(
          "Server not found."
        )
      }
    }

    "return error if user is not a member of the server" in {
      val params = CreatableMessage("Hello world").toJson.toString
      val request = this.createPostRoute("/messages/server/2", params)
      request ~> addCredentials(testLogins("moderator")) ~!> routes ~> check {
        responseAs[Result[ServerMessage]] shouldEqual Failure(
          "User not found."
        )
      }
    }

    "create a new direct message" in {
      val params = CreatableMessage("Hello!").toJson.toString
      val request = this.createPostRoute("/messages/direct/2", params)
      request ~> addCredentials(testLogins("admin")) ~!> routes ~> check {
        val res = responseAs[Result[DirectMessage]]
        val message = res.result.get
        message.content shouldEqual "Hello!"
        message.recipient.id shouldEqual 2
        message.sender.id shouldEqual 1
        res.message shouldEqual Some("Message sent.")
      }
    }

    "return error if recipient not found" in {
      val params = CreatableMessage("Hello!").toJson.toString
      val request = this.createPostRoute("/messages/direct/30", params)
      request ~> addCredentials(testLogins("admin")) ~!> routes ~> check {
        responseAs[Result[DirectMessage]] shouldEqual Failure(
          "User not found."
        )
      }
    }

    "retrieve a list of messages from a server with default limit and offset parameters" in {
      Get("/messages/server/1") ~> addCredentials(testLogins("member")) ~!> routes ~> check {
        status shouldEqual StatusCodes.OK
        responseAs[List[ChildMessage]] shouldEqual serverMessages
      }
    }

    "retrieve a list of messages from a server with custom limit parameter" in {
      Get("/messages/server/1?limit=2") ~> addCredentials(testLogins("member")) ~!> routes ~> check {
        status shouldEqual StatusCodes.OK
        responseAs[List[ChildMessage]] shouldEqual List(
          serverMessages.head,
          serverMessages(1)
        )
      }
    }

    "retrieve a list of messages from a server with custom offset parameter" in {
      Get("/messages/server/1?offset=1") ~> addCredentials(testLogins("member")) ~!> routes ~> check {
        status shouldEqual StatusCodes.OK
        responseAs[List[ChildMessage]] shouldEqual List(
          serverMessages(1),
          serverMessages(2)
        )
      }
    }

    "retrieve a list of messages from a server with custom limit and offset parameter" in {
      Get("/messages/server/1?limit=1&offset=1") ~> addCredentials(testLogins("member")) ~!> routes ~> check {
        status shouldEqual StatusCodes.OK
        responseAs[List[ChildMessage]] shouldEqual List(
          serverMessages(1)
        )
      }
    }

    "throw error if limit exceeds 1000" in {
      Get("/messages/server/1?limit=1001") ~> addCredentials(testLogins("member")) ~!> routes ~> check {
        status shouldEqual StatusCodes.BadRequest
        responseAs[Result[NoRootElement]] shouldEqual Failure(
          "The limit must have a value between 0 and 1000, or be left blank."
        )
      }
    }

    "throw error if limit is less than 0" in {
      Get("/messages/server/1?limit=-1") ~> addCredentials(testLogins("member")) ~!> routes ~> check {
        status shouldEqual StatusCodes.BadRequest
        responseAs[Result[NoRootElement]] shouldEqual Failure(
          "The limit must have a value between 0 and 1000, or be left blank."
        )
      }
    }

    "throw error if offset is less than 0" in {
      Get("/messages/server/1?offset=-1") ~> addCredentials(testLogins("member")) ~!> routes ~> check {
        status shouldEqual StatusCodes.BadRequest
        responseAs[Result[NoRootElement]] shouldEqual Failure(
          "The offset must be at least 0, or left blank."
        )
      }
    }
  }

  "retrieve a list of direct messages with default limit and offset parameters" in {
    Get("/messages/direct/2") ~> addCredentials(testLogins("admin")) ~!> routes ~> check {
      status shouldEqual StatusCodes.OK
      responseAs[List[ChildMessage]] shouldEqual directMessages
    }
  }

  "retrieve a list of direct messages with custom limit parameter" in {
    Get("/messages/direct/2?limit=2") ~> addCredentials(testLogins("admin")) ~!> routes ~> check {
      status shouldEqual StatusCodes.OK
      responseAs[List[ChildMessage]] shouldEqual List(
        directMessages.head,
        directMessages(1)
      )
    }
  }

  "retrieve a list of direct messages with custom offset parameter" in {
    Get("/messages/direct/2?offset=1") ~> addCredentials(testLogins("admin")) ~!> routes ~> check {
      status shouldEqual StatusCodes.OK
      responseAs[List[ChildMessage]] shouldEqual List(
        directMessages(1),
        directMessages(2)
      )
    }
  }

  "retrieve a list of messages from a server with custom limit and offset parameter" in {
    Get("/messages/direct/2?limit=1&offset=1") ~> addCredentials(testLogins("admin")) ~!> routes ~> check {
      status shouldEqual StatusCodes.OK
      responseAs[List[ChildMessage]] shouldEqual List(
        directMessages(1)
      )
    }
  }

  "throw error if limit exceeds 1000" in {
    Get("/messages/direct/2?limit=1001") ~> addCredentials(testLogins("member")) ~!> routes ~> check {
      status shouldEqual StatusCodes.BadRequest
      responseAs[Result[NoRootElement]] shouldEqual Failure(
        "The limit must have a value between 0 and 1000, or be left blank."
      )
    }
  }

  "throw error if limit is less than 0" in {
    Get("/messages/direct/2?limit=-1") ~> addCredentials(testLogins("member")) ~!> routes ~> check {
      status shouldEqual StatusCodes.BadRequest
      responseAs[Result[NoRootElement]] shouldEqual Failure(
        "The limit must have a value between 0 and 1000, or be left blank."
      )
    }
  }

  "throw error if offset is less than 0" in {
    Get("/messages/direct/2?offset=-1") ~> addCredentials(testLogins("member")) ~!> routes ~> check {
      status shouldEqual StatusCodes.BadRequest
      responseAs[Result[NoRootElement]] shouldEqual Failure(
        "The offset must be at least 0, or left blank."
      )
    }
  }
}
