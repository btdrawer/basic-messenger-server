package model.converters

import java.time.Instant

import spray.json._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport

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
    case somethingElse => throw DeserializationException(s"Expected a value from enum $enu instead of $somethingElse")
  }
}

class InstantJsonConverter extends RootJsonFormat[Instant] {
  override def write(v: Instant): JsValue = JsNumber(v.toEpochMilli)

  override def read(json: JsValue): Instant = json match {
    case JsNumber(v) => Instant.ofEpochMilli(v.toLongExact)
    case _ => throw DeserializationException("Serialization failed")
  }
}

trait JsonConverters extends SprayJsonSupport with DefaultJsonProtocol {
  // Resources

  implicit val instantFormat: RootJsonFormat[Instant] = new InstantJsonConverter

  implicit val roleFormat: EnumJsonConverter[Role.type] = new EnumJsonConverter[Role.type](Role)
  implicit val statusFormat: EnumJsonConverter[Status.type] = new EnumJsonConverter[Status.type](Status)

  implicit val passwordResetQuestionFormat: RootJsonFormat[PasswordResetQuestion] =
    jsonFormat2(PasswordResetQuestion)
  implicit val passwordResetFormat: RootJsonFormat[PasswordReset] = jsonFormat2(PasswordReset)

  implicit val creatableMessageFormat: RootJsonFormat[CreatableMessage] = jsonFormat2(CreatableMessage)
  implicit val rootReadableMessageFormat: RootJsonFormat[RootMessage] = jsonFormat5(RootMessage)
  implicit val childReadableMessageFormat: RootJsonFormat[ChildMessage] = jsonFormat4(ChildMessage)

  implicit val creatableServerFormat: RootJsonFormat[CreatableServer] = jsonFormat3(CreatableServer)
  implicit val childServerUserRoleFormat: RootJsonFormat[ChildServerUserRole] =
    jsonFormat2(ChildServerUserRole)
  implicit val rootServerFormat: RootJsonFormat[RootServer] = jsonFormat5(RootServer)
  implicit val childServerFormat: RootJsonFormat[ChildServer] = jsonFormat3(ChildServer)

  implicit val creatablePasswordResetFormat: RootJsonFormat[CreatablePasswordReset] =
    jsonFormat2(CreatablePasswordReset)
  implicit val creatableUserFormat: RootJsonFormat[CreatableUser] = jsonFormat3(CreatableUser)
  implicit val childUserServerRoleFormat: RootJsonFormat[ChildUserServerRole] = jsonFormat2(ChildUserServerRole)
  implicit val rootUserFormat: RootJsonFormat[RootUser] = jsonFormat4(RootUser)
  implicit val childUserFormat: RootJsonFormat[ChildUser] = jsonFormat3(ChildUser)

  // Results

  implicit val messageResultFormat: RootJsonFormat[Result[RootMessage]] = jsonFormat3(Result[RootMessage])
  implicit val serverResultFormat: RootJsonFormat[Result[RootServer]] = jsonFormat3(Result[RootServer])
  implicit val userResultFormat: RootJsonFormat[Result[RootUser]] = jsonFormat3(Result[RootUser])

  implicit val noResourceFormat: RootJsonFormat[NoRootElement] = jsonFormat0(NoRootElement)
  implicit val failureResultFormat: RootJsonFormat[Result[NoRootElement]] = jsonFormat3(Result[NoRootElement])
}
