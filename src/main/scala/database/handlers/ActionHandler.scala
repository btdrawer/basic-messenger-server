package database.handlers

import java.sql.{Connection, PreparedStatement, ResultSet}

import com.zaxxer.hikari.HikariDataSource
import model.JsonConverters

import scala.annotation.tailrec

trait ActionHandler extends JsonConverters {
  private def prepareStatement(
    query: String,
    parameters: List[Any]
  )(implicit connection: Connection): PreparedStatement = {
    val statement = connection.prepareStatement(
      query,
      ResultSet.TYPE_SCROLL_SENSITIVE,
      ResultSet.CONCUR_READ_ONLY
    )
    (1 to parameters.length).foreach(
      i => statement.setObject(i, parameters(i - 1))
    )
    statement
  }

  private def runQuery(query: String, parameters: List[Any])
                      (implicit connectionPool: HikariDataSource): ResultSet = {
    implicit val connection: Connection = connectionPool.getConnection()
    val statement = prepareStatement(query, parameters)
    val resultSet = statement.executeQuery()
    connection.close()
    resultSet
  }

  def runAndGetFirst(query: String, parameters: List[Any])
                    (implicit connectionPool: HikariDataSource): Option[ResultSet] = {
    val resultSet = runQuery(query, parameters)
    resultSet.first()
    if (resultSet.getRow < 1) None
    else Some(resultSet)
  }

  @tailrec
  private def iterateResultSet[T](acc: List[T], resultSet: ResultSet, iterator: ResultSet => T): List[T] =
    if (!resultSet.next()) acc
    else iterateResultSet(acc :+ iterator(resultSet), resultSet, iterator)

  def runAndIterate[T](
    query: String,
    parameters: List[Any],
    iterator: ResultSet => T
  )(implicit connectionPool: HikariDataSource): List[T] = {
    val resultSet = runQuery(query, parameters)
    iterateResultSet(List[T](), resultSet, iterator)
  }

  def runUpdate(query: String, parameters: List[Any])(implicit connectionPool: HikariDataSource): Int = {
    implicit val connection: Connection = connectionPool.getConnection()
    val statement = prepareStatement(query, parameters)
    val rowsUpdated = statement.executeUpdate()
    connection.close()
    rowsUpdated
  }
}
