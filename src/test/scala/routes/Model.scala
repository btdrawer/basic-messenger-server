package routes

import java.sql.Timestamp

import model._

trait Model {
  def servers = List(
    Server(
      id = 1,
      name = "Example Server",
      address = "exampleserver",
      users = List(
        ServerUserRole(
          user = ChildUser(
            id = 1,
            username = "admin",
            status = Status.withName("OFFLINE")
          ),
          role = Role.withName("ADMIN")
        ),
        ServerUserRole(
          user = ChildUser(
            id = 2,
            username = "moderator",
            status = Status.withName("OFFLINE")
          ),
          role = Role.withName("MODERATOR")
        ),
        ServerUserRole(
          user = ChildUser(
            id = 3,
            username = "member",
            status = Status.withName("OFFLINE")
          ),
          role = Role.withName("MEMBER")
        ),
      ),
      messages = List(
        ChildMessage(
          id = 1,
          content = "Hello1",
          sender = ChildUser(
            id = 1,
            username = "admin",
            status = Status.withName("OFFLINE")
          ),
          createdAt = Timestamp.valueOf("2020-09-27 11:28:00")
        ),
        ChildMessage(
          id = 2,
          content = "Hello2",
          sender = ChildUser(
            id = 2,
            username = "moderator",
            status = Status.withName("OFFLINE")
          ),
          createdAt = Timestamp.valueOf("2020-09-27 11:28:00")
        ),
        ChildMessage(
          id = 3,
          content = "Hello3",
          sender = ChildUser(
            id = 3,
            username = "member",
            status = Status.withName("OFFLINE")
          ),
          createdAt = Timestamp.valueOf("2020-09-27 11:28:00")
        )
      )
    ),
    Server(
      id = 2,
      name = "Another Server",
      address = "anotherserver",
      users = List(
        ServerUserRole(
          user = ChildUser(
            id = 1,
            username = "admin",
            status = Status.withName("OFFLINE")
          ),
          role = Role.withName("ADMIN")
        ),
        ServerUserRole(
          user = ChildUser(
            id = 3,
            username = "member",
            status = Status.withName("OFFLINE")
          ),
          role = Role.withName("MEMBER")
        ),
      ),
      messages = List()
    ),
  )

  def directMessages = List(
    ChildMessage(
      id = 1,
      content = "Hello1",
      sender = ChildUser(
        id = 1,
        username = "admin",
        status = Status.withName("OFFLINE")
      ),
      createdAt = Timestamp.valueOf("2020-09-27 11:28:00")
    ),
    ChildMessage(
      id = 2,
      content = "Hello2",
      sender = ChildUser(
        id = 2,
        username = "moderator",
        status = Status.withName("OFFLINE")
      ),
      createdAt = Timestamp.valueOf("2020-09-27 11:29:00")
    ),
    ChildMessage(
      id = 3,
      content = "Hello3",
      sender = ChildUser(
        id = 1,
        username = "admin",
        status = Status.withName("OFFLINE")
      ),
      createdAt = Timestamp.valueOf("2020-09-27 11:31:00")
    )
  )
}
