package database.queries

object UserQueries {
  def checkUsernameExists: String = "SELECT * FROM users WHERE username = ?"

  def createUser: String = "INSERT INTO users " +
    "(username, password, status, password_reset_question, password_reset_answer) " +
    "VALUES (?, ?, 'OFFLINE', ?, ?) " +
    "RETURNING id, username, status"

  def getUser: String = "SELECT username, statuses.id AS statusid FROM users " +
    "JOIN statuses ON users.status = statuses.id " +
    "WHERE users.id = ?"
  def getUserId: String = "SELECT id FROM user WHERE username = ?"
  def getUserServers: String = "SELECT servers.id AS id, name, address, role FROM servers " +
    "JOIN server_users ON servers.id = server_users.server " +
    "WHERE server_users.user = ?"

  def updateUsername: String = "UPDATE users SET username = ? WHERE id = ?" +
    "RETURNING id, username, status"
  def updateStatus: String = "UPDATE users SET status = ? WHERE id = ?"

  def deleteUser: String = "DELETE FROM users WHERE id = ?"
}
