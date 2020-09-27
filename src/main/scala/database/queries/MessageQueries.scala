package database.queries

object MessageQueries {
  def createServerMessage: String =
    """
      |INSERT INTO server_messages (content, server, sender, "createdAt")
      | VALUES (?, ?, ?, ?)
      | RETURNING id, content
      |""".stripMargin

  def createDirectMessage: String =
    """
      |INSERT INTO direct_messages (content, recipient, sender, "createdAt")
      | VALUES (?, ?, ?, ?)
      | RETURNING id, content
      |""".stripMargin

  def getServerMessages: String =
    """
      |SELECT
      |   server_messages.id AS messageid,
      |   content,
      |   users.id AS userid,
      |   users.username,
      |   users.status,
      |   server_messages."createdAt"
      | FROM server_messages
      | JOIN users ON users.id = server_messages.sender
      | WHERE server_messages.server = ?
      | LIMIT ? OFFSET ?
      |""".stripMargin

  def getDirectMessages: String =
    """
      |SELECT
      |   direct_messages.id AS messageid,
      |   content,
      |   users.id AS userid,
      |   users.username,
      |   users.status,
      |   direct_messages."createdAt"
      | FROM direct_messages
      | JOIN users ON users.id = direct_messages.sender
      | WHERE
      |   (direct_messages.recipient = ? AND direct_messages.sender = ?)
      | OR
      |   (direct_messages.recipient = ? AND direct_messages.sender = ?)
      | LIMIT ? OFFSET ?
      |""".stripMargin
}
