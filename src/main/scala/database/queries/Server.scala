package database.queries

object Server {
  def createServer: String = "INSERT INTO servers (name, address) VALUES (?, ?);\n" +
    "SET @id = SELECT SCOPE_IDENTITY()\n" +
    "INSERT INTO server_users (user, server, role) VALUES (?, @id, " +
    "(SELECT id FROM roles WHERE name = \"admin\")" +
    ");\n" +
    "SELECT @id;"

  def findServersByName: String = "SELECT id, name, address FROM servers WHERE name LIKE ?"
  def findServersByAddress: String = "SELECT id, name, address FROM servers WHERE address LIKE ?"

  def getServerById: String = "SELECT * FROM servers WHERE id = ?"
  def getServerByName: String = "SELECT id, name, address FROM servers WHERE name = ?"
  def getServerByAddress: String = "SELECT id, name, address FROM servers WHERE address = ?"

  def getServerUsers: String = "SELECT users.id, username, users.status, status.content, roles.name FROM users " +
    "JOIN server_users ON server_users.user = users.id " +
    "JOIN statuses ON users.status = statuses.id " +
    "JOIN roles ON users.role = roles.id " +
    "WHERE server_users.server = ?"

  def getServerMessages: String = "SELECT messages.id, content, " +
    "messages.sender, users.username, users.status, statuses.content, createdAt FROM messages " +
    "JOIN users ON users.id = messages.sender " +
    "JOIN statuses ON users.status = status.id " +
    "WHERE messages.server = ? " +
    "LIMIT ? OFFSET ?"
}
