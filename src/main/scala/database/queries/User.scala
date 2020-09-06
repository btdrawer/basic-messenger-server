package database.queries

object User {
  def checkUsernameExists: String = "SELECT * FROM users WHERE username = ?"

  def createUser: String = "INSERT INTO users " +
    "(username, password, status, password_reset_question, password_reset_answer) " +
    "VALUES (?, ?, 'OFFLINE', ?, ?) " +
    "RETURNING id, username, status"

  def getUser: String = "SELECT username, statuses.id AS statusId, statuses.content AS statusContent FROM users " +
    "JOIN statuses ON users.status = statuses.id " +
    "WHERE users.id = ?"
  def getUserId: String = "SELECT id FROM user WHERE username = ?"
  def getUserServers: String = "SELECT id, name, address, role FROM servers " +
    "JOIN server_members ON servers.id = server_members.server " +
    "WHERE server_members.user = ?"

  def updateUsername: String = "UPDATE users SET username = ? WHERE id = ?"
  def updateStatus: String = "UPDATE users SET status = ? WHERE id = ?"
}
