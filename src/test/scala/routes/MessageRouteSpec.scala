package routes

import java.sql.Timestamp
import java.time.Instant

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
        message.sender.user.id shouldEqual 1
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
  }
}
