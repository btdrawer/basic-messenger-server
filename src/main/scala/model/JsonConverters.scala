package model

import java.time.Instant

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json._

/*
  EnumJsonConverter class
  Source: https://groups.google.com/forum/#!topic/spray-user/RkIwRIXzDDc
  via https://github.com/spray/spray-json/issues/200
 */
class EnumJsonConverter[T <: scala.Enumeration](enu: T) extends RootJsonFormat[T#Value] {
  override def write(obj: T#Value): JsValue = JsString(obj.toString)

  override def read(json: JsValue): T#Value = json match {
    case JsString(txt) => enu.withName(txt)
    case somethingElse =>
      throw DeserializationException(s"Expected a value from enum $enu instead of $somethingElse")
  }
}

class InstantJsonConverter extends RootJsonFormat[Instant] {
  override def write(v: Instant): JsValue = JsNumber(v.toEpochMilli)

  override def read(json: JsValue): Instant = json match {
    case JsNumber(v) => Instant.ofEpochMilli(v.toLongExact)
    case _ => throw DeserializationException("Serialization failed")
  }
}

trait JsonConverters extends DefaultJsonProtocol with SprayJsonSupport {
  // Resources

  implicit val instantFormat: RootJsonFormat[Instant] = new InstantJsonConverter

  implicit val roleFormat: EnumJsonConverter[Role.type] = new EnumJsonConverter[Role.type](Role)
  implicit val statusFormat: EnumJsonConverter[Status.type] = new EnumJsonConverter[Status.type](Status)

  implicit val passwordResetQuestionFormat: RootJsonFormat[PasswordResetQuestion] =
    jsonFormat2(PasswordResetQuestion)
  implicit val passwordResetFormat: RootJsonFormat[PasswordReset] = jsonFormat2(PasswordReset)

  implicit val creatableMessageFormat: RootJsonFormat[CreatableMessage] = jsonFormat4(CreatableMessage)
  implicit val messageFormat: RootJsonFormat[Message] = jsonFormat5(Message)
  implicit val childMessageFormat: RootJsonFormat[ChildMessage] = jsonFormat4(ChildMessage)

  implicit val creatableServerFormat: RootJsonFormat[CreatableServer] = jsonFormat3(CreatableServer)
  implicit val serverUserRoleFormat: RootJsonFormat[ServerUserRole] = jsonFormat2(ServerUserRole)
  implicit val serverFormat: RootJsonFormat[Server] = jsonFormat5(Server)
  implicit val childServerFormat: RootJsonFormat[ChildServer] = jsonFormat3(ChildServer)

  implicit val creatablePasswordResetFormat: RootJsonFormat[CreatablePasswordReset] =
    jsonFormat2(CreatablePasswordReset)
  implicit val creatableUserFormat: RootJsonFormat[CreatableUser] = jsonFormat3(CreatableUser)
  implicit val userServerRoleFormat: RootJsonFormat[UserServerRole] = jsonFormat2(UserServerRole)
  implicit val userFormat: RootJsonFormat[User] = jsonFormat4(User)
  implicit val childUserFormat: RootJsonFormat[ChildUser] = jsonFormat3(ChildUser)

  // Results

  implicit val messageResultFormat: RootJsonFormat[Result[Message]] = jsonFormat3(Result[Message])
  implicit val serverResultFormat: RootJsonFormat[Result[Server]] = jsonFormat3(Result[Server])
  implicit val userResultFormat: RootJsonFormat[Result[User]] = jsonFormat3(Result[User])

  implicit val noRootElementFormat: RootJsonFormat[NoRootElement] = jsonFormat0(NoRootElement)
  implicit val failureResultFormat: RootJsonFormat[Result[NoRootElement]] = jsonFormat3(Result[NoRootElement])
}
