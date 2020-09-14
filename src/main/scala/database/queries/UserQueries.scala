package database.queries

object UserQueries {
  def checkUsernameExists: String = "SELECT * FROM users WHERE username = ?"

  def createUser: String =
    """
      |INSERT INTO users (username, password, status, password_reset_question, password_reset_answer)
      | VALUES (?, ?, 'OFFLINE', ?, ?)
      | RETURNING id, username, status
      |""".stripMargin

  def getUser: String = "SELECT username, status FROM \"users\" WHERE id = ?"
  def getUserId: String = "SELECT id FROM user WHERE username = ?"

  def getUserServers: String =
    """
      |SELECT servers.id AS serverid, name, address, role
      | FROM servers
      | JOIN server_users ON servers.id = server_users.server
      | WHERE server_users.user = ?
      |""".stripMargin
  def getUserServer: String =
    """
      |SELECT servers.id AS serverid, name, address, role
      | FROM servers
      | JOIN server_users ON servers.id = server_users.server
      | WHERE servers.id = ?
      |""".stripMargin

  def updateUsername: String = "UPDATE users SET username = ? WHERE id = ?"
  def updateStatus: String = "UPDATE users SET status = ? WHERE id = ?"

  def updateUser: String =
    """
      |UPDATE users
      | SET username = COALESCE(?, username),
      |   password = COALESCE(?, password),
      |   status = COALESCE(?, status),
      |   password_reset_question = COALESCE(?, password_reset_question),
      |   password_reset_answer = COALESCE(?, password_reset_answer)
      | WHERE id = ?
      |""".stripMargin

  def deleteUser: String = "DELETE FROM messages WHERE sender = ?;" +
    "DELETE FROM server_users WHERE \"user\" = ?;" +
    "DELETE FROM users WHERE id = ?;"
}
