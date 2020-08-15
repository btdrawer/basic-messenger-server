package database.queries

object Server {
  def findServersByName: String = "SELECT id, name, address FROM servers WHERE name LIKE ?"
  def findServersByAddress: String = "SELECT id, name, address FROM servers WHERE address LIKE ?"

  def getServerUsers: String = "SELECT users.id, username, users.status, status.content FROM users " +
    "JOIN server_users ON server_users.user = users.id " +
    "JOIN statuses ON users.status = statuses.id " +
    "WHERE server_users.server = ?"
  def getServer: String = "SELECT * FROM servers WHERE id = ?"
  def getServerMessages: String = "SELECT messages.id, content, " +
    "messages.sender, users.username, users.status, statuses.content, createdAt FROM messages " +
    "JOIN users ON users.id = messages.sender " +
    "JOIN statuses ON users.status = status.id " +
    "WHERE messages.server = ? " +
    "LIMIT ? OFFSET ?"
}
