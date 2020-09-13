package database.actions

import java.sql.{Connection, PreparedStatement, ResultSet}

import model.{JsonConverters, UpdatableConverters}

import scala.annotation.tailrec

trait Actions extends JsonConverters with UpdatableConverters {
  private def prepareStatement(
    query: String,
    parameters: List[Any]
  )(implicit connection: Connection): PreparedStatement = {
    val statement = connection.prepareStatement(
      query,
      ResultSet.TYPE_SCROLL_SENSITIVE,
      ResultSet.CONCUR_READ_ONLY
    )
    (1 to parameters.length).foreach(i => statement.setObject(i, parameters(i - 1)))
    statement
  }

  def runQuery(query: String, parameters: List[Any])(implicit connection: Connection): ResultSet = {
    val statement = prepareStatement(query, parameters)
    statement.executeQuery()
  }

  def runAndGetFirst(query: String, parameters: List[Any])(implicit connection: Connection): ResultSet = {
    val statement = prepareStatement(query, parameters)
    val resultSet = statement.executeQuery()
    resultSet.first()
    resultSet
  }

  @tailrec
  private def iterateResultSet[T](acc: List[T], resultSet: ResultSet, iterator: ResultSet => T): List[T] =
    if (!resultSet.next()) acc
    else iterateResultSet(acc :+ iterator(resultSet), resultSet, iterator)

  def runAndIterate[T](
    query: String,
    parameters: List[Any],
    iterator: ResultSet => T
  )(implicit connection: Connection): List[T] = {
    val resultSet = runQuery(query, parameters)
    iterateResultSet(List[T](), resultSet, iterator)
  }

  def runUpdate(query: String, parameters: List[Any])(implicit connection: Connection): Int = {
    val statement = prepareStatement(query, parameters)
    statement.executeUpdate()
  }
}
