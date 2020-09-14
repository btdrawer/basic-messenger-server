package database.queries

object MessageQueries {
  def createMessage: String =
    """
      |INSERT INTO messages (content, server, sender, createdAt)
      | VALUES (?, ?, ?, ?)
      | RETURNING id, content
      |""".stripMargin
}
