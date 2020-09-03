package api.converters

import api.model.{ReadableMessage, ReadableServer, ReadableUser}
import model.{Message, Server, User}

object ReadableObjectConverters {
  trait ReadableObjectConverter[A, B] {
    def convert(value: A): B
  }

  implicit class ToReadable[A, B](value: A) {
    def toReadable(implicit converter: ReadableObjectConverter[A, B]): B = converter convert value
  }

  implicit object ReadableServerConverter extends ReadableObjectConverter[Server, ReadableServer] {
    override def convert(server: Server): ReadableServer = ReadableServer(
      id = server.id,
      name = server.name,
      address = server.address,
      users = Some(
        server.users.map(elem => (
          ReadableUser(
            id = elem._1.id,
            username = elem._1.username,
            servers = None,
            status = elem._1.status
          ), elem._2)
        )
      ),
      messages = Some(server.messages)
    )
  }

  implicit object ReadableUserConverter extends ReadableObjectConverter[User, ReadableUser] {
    override def convert(user: User): ReadableUser = ReadableUser(
      id = user.id,
      username = user.username,
      servers = Some(
        user.servers.map(elem => (
          ReadableServer(
            id = elem._1.id,
            name = elem._1.name,
            address = elem._1.address,
            users = None,
            messages = None
          ), elem._2)
        )
      ),
      status = user.status
    )
  }

  implicit object ReadableMessageConverter extends ReadableObjectConverter[Message, ReadableMessage] {
    override def convert(message: Message): ReadableMessage = ReadableMessage(
      id = message.id,
      content = message.content,
      server = Some(message.server.toReadable),
      sender = message.sender.toReadable,
      createdAt = message.createdAt
    )
  }

}
