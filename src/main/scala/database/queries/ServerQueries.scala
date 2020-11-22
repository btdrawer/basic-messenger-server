package database.queries

object ServerQueries {
  val createServer: String =
    """
      |INSERT INTO servers (name, address)
      | VALUES (?, ?)
      | RETURNING id, name, address
      |""".stripMargin

  val findServersByName: String = "SELECT id, name, address FROM servers WHERE name ILIKE ?"

  val getServerById: String = "SELECT * FROM servers WHERE id = ?"
  val getServerByName: String = "SELECT id, name, address FROM servers WHERE name = ?"
  val getServerByAddress: String = "SELECT id, name, address FROM servers WHERE address = ?"

  val getServerUsers: String =
    """
      |SELECT users.id AS userid, username, status, role
      | FROM users
      | JOIN server_users ON server_users.user = users.id
      | WHERE server_users.server = ?
      |""".stripMargin
  val getServerUser: String =
    """
      |SELECT users.id AS userid, username, status, role
      | FROM users
      | JOIN server_users ON server_users.user = users.id
      | WHERE server_users.server = ? AND users.id = ?
      |""".stripMargin

  val updateServer: String =
    """
      |UPDATE servers
      | SET name = ?
      | WHERE id = ?
      |""".stripMargin

  val addServerUser: String =
    """
      |INSERT INTO server_users (server, "user", role)
      | VALUES (?, ?, ?)
      |""".stripMargin
  val updateUserRole: String =
    """
      |UPDATE server_users
      | SET role = ?
      | WHERE server = ? AND "user" = ?
      |""".stripMargin
  val removeServerUser: String =
    """
      |DELETE FROM server_users
      | WHERE server = ? AND "user" = ?
      |""".stripMargin

  val deleteServer: String =
    """
      |DELETE FROM server_messages WHERE server = ?;
      |DELETE FROM server_users WHERE server = ?;
      |DELETE FROM servers WHERE id = ?
      |""".stripMargin
}
