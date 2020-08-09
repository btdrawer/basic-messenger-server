package database.queries

object UserQueries {
  def checkUsernameExists: String = "SELECT * FROM user WHERE username = ?"

  def createUser: String = "INSERT INTO users (username, password, status) " +
    "SELECT ?, ?, id FROM statuses WHERE status = \"online\""
  def createPasswordResetData: String = "INSERT INTO password_reset (question, answer) VALUES (?, ?)"

  def getUser: String = "SELECT username, status FROM user WHERE id = ?"
  def getUserServers: String = "SELECT id, name, address, role FROM servers " +
    "JOIN server_members ON servers.id = server_members.server " +
    "WHERE server_members.user = ?"
}
