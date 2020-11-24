package database.handlers

import java.sql.{Connection, PreparedStatement, ResultSet}

import com.zaxxer.hikari.HikariDataSource
import model.JsonConverters

import scala.annotation.tailrec
import scala.util.Using

trait ActionHandler extends JsonConverters {
  private def prepareStatement(
    query: String,
    parameters: List[Any]
  )(connection: Connection): PreparedStatement = {
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

  private def withUsingManager[T](fn: Using.Manager => T)(implicit connectionPool: HikariDataSource): T =
    Using.Manager(fn)
      .recover {
        case exception: Exception => throw exception
      }
      .get

  private def run[T](
    query: String,
    parameters: List[Any],
    executeStatement: PreparedStatement => T
  )(implicit connectionPool: HikariDataSource): T =
    withUsingManager { use =>
      val connection = use(connectionPool.getConnection())
      val statement = use(prepareStatement(query, parameters)(connection))
      executeStatement(statement)
    }

  private def runQuery(
    query: String,
    parameters: List[Any]
  )(implicit connectionPool: HikariDataSource): ResultSet =
    run(query, parameters, _.executeQuery())

  def runUpdate(
    query: String,
    parameters: List[Any]
  )(implicit connectionPool: HikariDataSource): Int =
    run(query, parameters, _.executeUpdate())

  def runAndGetFirst[T](
    query: String,
    parameters: List[Any]
  )(processor: Option[ResultSet] => T)(implicit connectionPool: HikariDataSource): T =
    withUsingManager { use =>
      val resultSet = use(runQuery(query, parameters))
      resultSet.first()
      val resultSetOption = {
        if (resultSet.getRow < 1) None
        else Some(resultSet)
      }
      processor(resultSetOption)
    }

  @tailrec
  private def iterateResultSet[T](acc: List[T], resultSet: ResultSet, iterator: ResultSet => T): List[T] =
    if (!resultSet.next()) acc
    else iterateResultSet(acc :+ iterator(resultSet), resultSet, iterator)

  def runAndIterate[T](
    query: String,
    parameters: List[Any],
    iterator: ResultSet => T
  )(implicit connectionPool: HikariDataSource): List[T] =
    withUsingManager { use =>
      val resultSet = use(runQuery(query, parameters))
      iterateResultSet(List[T](), resultSet, iterator)
    }
}
