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
        server = 1,
        sender = 1,
        createdAt = new Timestamp(Instant.EPOCH.getEpochSecond)
      ).toJson.toString
      val request = this.createPostRoute("/messages", params)
      request ~!> routes ~> check {
        val res = responseAs[Result[Message]]
        val message = res.result.get
        message.content shouldEqual "Hello world"
        message.server.id shouldEqual 1
        message.sender.user.id shouldEqual 1
        res.message shouldEqual Some("Message sent.")
      }
    }

    "return error if user not found" in {
      val params = CreatableMessage(
        content = "Hello world",
        server = 1,
        sender = 30,
        createdAt = new Timestamp(Instant.EPOCH.getEpochSecond)
      ).toJson.toString
      val request = this.createPostRoute("/messages", params)
      request ~!> routes ~> check {
        responseAs[Result[Message]] shouldEqual Failure(
          "User not found."
        )
      }
    }

    "return error if server not found" in {
      val params = CreatableMessage(
        content = "Hello world",
        server = 30,
        sender = 1,
        createdAt = new Timestamp(Instant.EPOCH.getEpochSecond)
      ).toJson.toString
      val request = this.createPostRoute("/messages", params)
      request ~!> routes ~> check {
        responseAs[Result[Message]] shouldEqual Failure(
          "Server not found."
        )
      }
    }

    "return error if user is not a member of the server" in {
      val params = CreatableMessage(
        content = "Hello world",
        server = 2,
        sender = 2,
        createdAt = new Timestamp(Instant.EPOCH.getEpochSecond)
      ).toJson.toString
      val request = this.createPostRoute("/messages", params)
      request ~!> routes ~> check {
        responseAs[Result[Message]] shouldEqual Failure(
          "User not found."
        )
      }
    }
  }
}
