package database.queries

object ServerQueries {
  def createServer: String = "INSERT INTO servers (name, address) VALUES (?, ?) " +
    "RETURNING id, name, address;"
  def addUserToServer: String = "INSERT INTO server_users (\"user\", server, role) VALUES (?, ?, ?);"

  def findServersByName: String = "SELECT id, name, address FROM servers WHERE name ILIKE ?"

  def getServerById: String = "SELECT * FROM servers WHERE id = ?"
  def getServerByName: String = "SELECT id, name, address FROM servers WHERE name = ?"
  def getServerByAddress: String = "SELECT id, name, address FROM servers WHERE address = ?"

  def getServerUsers: String = "SELECT users.id AS userid, username, status, role " +
    "FROM users JOIN server_users ON server_users.user = users.id " +
    "WHERE server_users.server = ?"
  def getServerUser: String = "SELECT users.id AS userid, username, status, role " +
    "FROM users JOIN server_users ON server_users.user = users.id " +
    "WHERE server_users.server = ? AND users.id = ?"

  def getServerMessages: String = "SELECT messages.id AS messageid, content, " +
    "sender, users.username, users.status, messages.\"createdAt\" FROM messages " +
    "JOIN users ON users.id = messages.sender " +
    "WHERE messages.server = ? " +
    "LIMIT ? OFFSET ?"
}
