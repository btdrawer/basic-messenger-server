package database.queries

object Message {
  def createMessage: String =
    "INSERT INTO messages (content, server, sender, createdAt) VALUES (?, ?, ?, ?);\n" +
    "SELECT SCOPE_IDENTITY();"
}
