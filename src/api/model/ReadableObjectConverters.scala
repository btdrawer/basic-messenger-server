package api.model

import model.{Message, Role, Server, User}

object ReadableObjectConverters {
  trait ReadableObjectConverter[A, B] {
    def convert(value: A): B
  }

  implicit class ToReadable[A, B](value: A) {
    def toReadable(implicit converter: ReadableObjectConverter[A, B]): B = converter convert value
  }

  implicit object ReadableServerMapConverter
    extends ReadableObjectConverter[Map[Server, Role.Value], Map[ReadableServer, Role.Value]] {
    override def convert(users: Map[Server, Role.Value]): Map[ReadableServer, Role.Value] =
      users.map(elem => (elem._1.toReadable, elem._2))
  }

  implicit object ReadableUserMapConverter
    extends ReadableObjectConverter[Map[User, Role.Value], Map[ReadableUser, Role.Value]] {
    override def convert(users: Map[User, Role.Value]): Map[ReadableUser, Role.Value] =
      users.map(elem => (elem._1.toReadable, elem._2))
  }

  implicit object ReadableServerConverter extends ReadableObjectConverter[Server, ReadableServer] {
    override def convert(server: Server): ReadableServer = ReadableServer(
      id = server.id,
      name = server.name,
      address = server.address,
      users = Some(server.users.toReadable),
      messages = Some(server.messages)
    )
  }

  implicit object ReadableUserConverter extends ReadableObjectConverter[User, ReadableUser] {
    override def convert(user: User): ReadableUser = ReadableUser(
      id = user.id,
      username = user.username,
      servers = Some(user.servers.toReadable),
      status = user.status
    )
  }

  implicit object ReadableMessageConverter extends ReadableObjectConverter[Message, ReadableMessage] {
    override def convert(message: Message): ReadableMessage = ReadableMessage(
      id = message.id,
      content = message.content,
      server = message.server.toReadable,
      sender = message.sender.toReadable,
      createdAt = message.createdAt
    )
  }
}
