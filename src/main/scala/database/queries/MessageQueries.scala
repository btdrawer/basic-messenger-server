package database.queries

object MessageQueries {
  def createMessage: String =
    "INSERT INTO messages (content, server, sender, createdAt) VALUES (?, ?, ?, ?);\n" +
    "SELECT SCOPE_IDENTITY();"
}
