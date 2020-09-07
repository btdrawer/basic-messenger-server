package database

import java.sql.{Connection, DriverManager}

object GetConnection {
  def apply(host: String, username: String, password: String): Connection = {
    val url = s"jdbc:postgresql://$host/basic-messenger"
    DriverManager.getConnection(url, username, password)
  }
}
