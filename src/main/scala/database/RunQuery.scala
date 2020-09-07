package database

import java.sql.{Connection, PreparedStatement, ResultSet}

object RunQuery {
  private def prepareStatement(query: String, parameters: List[Any])
                              (implicit connection: Connection): PreparedStatement = {
    val statement = connection.prepareStatement(
      query,
      ResultSet.TYPE_SCROLL_SENSITIVE,
      ResultSet.CONCUR_READ_ONLY
    )
    (1 to parameters.length).foreach(i => statement.setObject(i, parameters(i - 1)))
    statement
  }

  def apply(query: String, parameters: List[Any])(implicit connection: Connection): ResultSet = {
    val statement = prepareStatement(query, parameters)
    statement.executeQuery()
  }
}
