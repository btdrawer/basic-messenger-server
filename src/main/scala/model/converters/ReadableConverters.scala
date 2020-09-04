package model.converters

import model.interfaces.{ChildReadable, RootReadable}
import model.resources._

object ReadableConverters {
  trait ReadableObjectConverter[A, B] {
    def convert(value: A): B
  }

  implicit class ToReadable[A](value: A) {
    def toRootReadable[B <: RootReadable](implicit converter: ReadableObjectConverter[A, B]): B =
      converter convert value
    def toChildReadable[C <: ChildReadable](implicit converter: ReadableObjectConverter[A, C]): C =
      converter convert value
  }

  // ChildReadable converters

  implicit object ChildServerConverter extends ReadableObjectConverter[Server, ChildReadableServer] {
    override def convert(server: Server): ChildReadableServer = ChildReadableServer(
      id = server.id,
      name = server.name,
      address = server.address
    )
  }

  implicit object ChildUserConverter extends ReadableObjectConverter[User, ChildReadableUser] {
    override def convert(user: User): ChildReadableUser = ChildReadableUser(
      id = user.id,
      username = user.username,
      status = user.status
    )
  }

  implicit object ChildMessageConverter extends ReadableObjectConverter[Message, ChildReadableMessage] {
    override def convert(message: Message): ChildReadableMessage = ChildReadableMessage(
      id = message.id,
      content = message.content,
      sender = message.sender.toChildReadable,
      createdAt = message.createdAt
    )
  }

  // RootReadable converters

  implicit object RootServerConverter extends ReadableObjectConverter[Server, RootReadableServer] {
    override def convert(server: Server): RootReadableServer = RootReadableServer(
      id = server.id,
      name = server.name,
      address = server.address,
      users = Some(
        server.users.flatMap(
          _.map(
            user => (user._1.toChildReadable, user._2)
          )
        ).toMap
      ),
      messages = Some(
        server.messages.flatMap(
          _.map(
            _.toChildReadable
          )
        ).toList
      )
    )
  }

  implicit object RootUserConverter extends ReadableObjectConverter[User, RootReadableUser] {
    override def convert(user: User): RootReadableUser = RootReadableUser(
      id = user.id,
      username = user.username,
      servers = Some(
        user.servers.flatMap(
          _.map(
            server => (server._1.toChildReadable, server._2)
          )
        ).toMap
      ),
      status = user.status
    )
  }

  implicit object RootMessageConverter extends ReadableObjectConverter[Message, RootReadableMessage] {
    override def convert(message: Message): RootReadableMessage = RootReadableMessage(
      id = message.id,
      content = message.content,
      server = message.server.flatMap(
        s => Some(s.toChildReadable)
      ),
      sender = message.sender.toChildReadable,
      createdAt = message.createdAt
    )
  }
}
