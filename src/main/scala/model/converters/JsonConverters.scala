package model.converters

import java.time.Instant

import model.resources.Status.Status
import model.resources._
import spray.json.DefaultJsonProtocol._
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
    case somethingElse => throw DeserializationException(s"Expected a value from enum $enu instead of $somethingElse")
  }
}

class InstantJsonConverter extends RootJsonFormat[Instant] {
  override def write(v: Instant): JsValue = JsNumber(v.toEpochMilli)

  override def read(json: JsValue): Instant = json match {
    case JsNumber(v) => new Instant(v.toLongExact)
    case _ => throw DeserializationException("Serialization failed")
  }
}

object JsonConverters {
  implicit val instantFormat: RootJsonFormat[Instant] = new InstantJsonConverter

  implicit val roleFormat: EnumJsonConverter[Role.type] = new EnumJsonConverter[Role.type](Role)
  implicit val statusFormat: RootJsonFormat[Status] = new EnumJsonConverter[Status.type](Status)

  implicit val passwordResetQuestionFormat: RootJsonFormat[PasswordResetQuestion] = jsonFormat2(PasswordResetQuestion)
  implicit val passwordResetFormat: RootJsonFormat[PasswordReset] = jsonFormat2(PasswordReset)

  implicit val childReadableUserFormat: RootJsonFormat[ChildReadableUser] = jsonFormat3(ChildReadableUser)
  implicit val childReadableServerFormat: RootJsonFormat[ChildReadableServer] = jsonFormat3(ChildReadableServer)

  implicit val creatableMessageFormat: RootJsonFormat[CreatableMessage] = jsonFormat4(CreatableMessage)
  implicit val rootReadableMessageFormat: RootJsonFormat[RootReadableMessage] = jsonFormat5(RootReadableMessage)
  implicit val messageResultFormat: RootJsonFormat[MessageResult] = jsonFormat3(MessageResult.apply)

  implicit val creatableServerFormat: RootJsonFormat[CreatableServer] = jsonFormat3(CreatableServer)
  implicit val readableServerFormat: RootJsonFormat[RootReadableServer] = jsonFormat5(RootReadableServer)
  implicit val serverResultFormat: RootJsonFormat[ServerResult] = jsonFormat3(ServerResult.apply)

  implicit val creatableUserFormat: RootJsonFormat[CreatableUser] = jsonFormat4(CreatableUser)
  implicit val readableUserFormat: RootJsonFormat[RootReadableUser] = jsonFormat4(RootReadableUser)
  implicit val userResultFormat: RootJsonFormat[UserResult] = jsonFormat3(UserResult.apply)
}
