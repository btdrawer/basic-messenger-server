package api.converters

import api.model._
import spray.json.DefaultJsonProtocol._
import spray.json._

object JsonConverters {
  implicit val creatableServerFormat: RootJsonFormat[CreatableServer] = jsonFormat3(CreatableServer)
  implicit val readableServerFormat: RootJsonFormat[ReadableServer] = rootFormat(
    lazyFormat(
      jsonFormat(ReadableServer, "id", "name", "address", "users", "messages")
    )
  )
  implicit val serverResultFormat: RootJsonFormat[Nothing] = jsonFormat3(ServerResult)

  implicit val creatableUserFormat: RootJsonFormat[CreatableUser] = jsonFormat4(CreatableUser)
  implicit val readableUserFormat: RootJsonFormat[ReadableUser] = jsonFormat4(ReadableUser)
  implicit val updatableUserFormat: RootJsonFormat[UpdatableUser] = jsonFormat5(UpdatableUser)
  implicit val userResultFormat: RootJsonFormat[Nothing] = jsonFormat3(UserResult)

  implicit val creatableMessageFormat: RootJsonFormat[CreatableMessage] = jsonFormat4(CreatableMessage)
  implicit val readableMessageFormat: RootJsonFormat[ReadableMessage] = jsonFormat5(ReadableMessage)
  implicit val messageResultFormat: RootJsonFormat[Nothing] = jsonFormat3(ReadableMessage)
}
