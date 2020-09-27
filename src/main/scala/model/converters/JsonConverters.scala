package model.converters

import java.sql.Timestamp

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json._

import model._

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

class TimestampJsonConverter extends RootJsonFormat[Timestamp] {
  override def write(v: Timestamp): JsValue = JsNumber(v.getTime)

  override def read(json: JsValue): Timestamp = json match {
    case JsNumber(v) => new Timestamp(v.toLongExact)
    case _ => throw DeserializationException("Serialization failed")
  }
}

trait JsonConverters extends DefaultJsonProtocol with SprayJsonSupport {
  implicit val timestampFormat: RootJsonFormat[Timestamp] = new TimestampJsonConverter

  // Enumeration

  implicit val roleFormat: EnumJsonConverter[Role.type] = new EnumJsonConverter[Role.type](Role)
  implicit val statusFormat: EnumJsonConverter[Status.type] = new EnumJsonConverter[Status.type](Status)

  // Creatable

  implicit val creatableMessageFormat: RootJsonFormat[CreatableMessage] = jsonFormat2(CreatableMessage)
  implicit val creatableServerFormat: RootJsonFormat[CreatableServer] = jsonFormat2(CreatableServer)
  implicit val creatablePasswordResetFormat: RootJsonFormat[CreatablePasswordReset] =
    jsonFormat2(CreatablePasswordReset)
  implicit val creatableUserFormat: RootJsonFormat[CreatableUser] = jsonFormat3(CreatableUser)

  // ChildElement

  implicit val childMessageFormat: RootJsonFormat[ChildMessage] = jsonFormat4(ChildMessage)
  implicit val childServerFormat: RootJsonFormat[ChildServer] = jsonFormat3(ChildServer)
  implicit val childUserFormat: RootJsonFormat[ChildUser] = jsonFormat3(ChildUser)

  // RootElement

  implicit val passwordResetQuestionFormat: RootJsonFormat[PasswordResetQuestion] =
    jsonFormat2(PasswordResetQuestion)
  implicit val passwordResetFormat: RootJsonFormat[PasswordReset] = jsonFormat2(PasswordReset)

  implicit val userServerRoleFormat: RootJsonFormat[UserServerRole] = jsonFormat2(UserServerRole)
  implicit val serverUserRoleFormat: RootJsonFormat[ServerUserRole] = jsonFormat2(ServerUserRole)

  implicit val messageFormat: RootJsonFormat[Message] = jsonFormat5(Message)
  implicit val serverFormat: RootJsonFormat[Server] = jsonFormat5(Server)
  implicit val userFormat: RootJsonFormat[User] = jsonFormat4(User)

  // Updatable

  implicit val updatableUserFormat: RootJsonFormat[UpdatableUser] = jsonFormat4(UpdatableUser)
  implicit val updatableServerFormat: RootJsonFormat[UpdatableServer] = jsonFormat1(UpdatableServer)

  // Result

  implicit val messageResultFormat: RootJsonFormat[Result[Message]] = jsonFormat3(Result[Message])
  implicit val serverResultFormat: RootJsonFormat[Result[Server]] = jsonFormat3(Result[Server])
  implicit val userResultFormat: RootJsonFormat[Result[User]] = jsonFormat3(Result[User])

  implicit val noRootElementFormat: RootJsonFormat[NoRootElement] = jsonFormat0(NoRootElement)
  implicit val failureResultFormat: RootJsonFormat[Result[NoRootElement]] = jsonFormat3(Result[NoRootElement])
}
