package database.queries

object ServerQueries {
  def createServer: String =
    """
      |INSERT INTO servers (name, address)
      | VALUES (?, ?)
      | RETURNING id, name, address
      |""".stripMargin

  def findServersByName: String = "SELECT id, name, address FROM servers WHERE name ILIKE ?"

  def getServerById: String = "SELECT * FROM servers WHERE id = ?"
  def getServerByName: String = "SELECT id, name, address FROM servers WHERE name = ?"
  def getServerByAddress: String = "SELECT id, name, address FROM servers WHERE address = ?"

  def getServerUsers: String =
    """
      |SELECT users.id AS userid, username, status, role
      | FROM users
      | JOIN server_users ON server_users.user = users.id
      | WHERE server_users.server = ?
      |""".stripMargin
  def getServerUser: String =
    """
      |SELECT users.id AS userid, username, status, role
      | FROM users
      | JOIN server_users ON server_users.user = users.id
      | WHERE server_users.server = ? AND users.id = ?
      |""".stripMargin

  def updateServer: String =
    """
      |UPDATE servers
      | SET name = ?
      | WHERE id = ?
      |""".stripMargin

  def addServerUser: String =
    """
      |INSERT INTO server_users (server, "user", role)
      | VALUES (?, ?, ?)
      |""".stripMargin
  def updateUserRole: String =
    """
      |UPDATE server_users
      | SET role = ?
      | WHERE server = ? AND "user" = ?
      |""".stripMargin
  def removeServerUser: String =
    """
      |DELETE FROM server_users
      | WHERE server = ? AND "user" = ?
      |""".stripMargin

  def deleteServer: String =
    """
      |DELETE FROM server_messages WHERE server = ?;
      |DELETE FROM server_users WHERE server = ?;
      |DELETE FROM servers WHERE id = ?
      |""".stripMargin
}
