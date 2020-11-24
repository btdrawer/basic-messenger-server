package database.handlers

import java.sql.{Connection, PreparedStatement, ResultSet}

import com.zaxxer.hikari.HikariDataSource
import model.JsonConverters

import scala.annotation.tailrec
import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}
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
  )(implicit connectionPool: HikariDataSource, executionContext: ExecutionContext): Future[ResultSet] =
    Future(run(query, parameters, _.executeQuery()))

  def runUpdate(
    query: String,
    parameters: List[Any]
  )(implicit connectionPool: HikariDataSource, executionContext: ExecutionContext): Future[Int] =
    Future(run(query, parameters, _.executeUpdate()))

  def runAndGetFirst[T](query: String, parameters: List[Any])
      (processor: Option[ResultSet] => T)
      (implicit connectionPool: HikariDataSource, executionContext: ExecutionContext): Future[T] =
    Future {
      withUsingManager { use =>
        val resultSetFuture = for {
          resultSet <- runQuery(query, parameters)
        } yield {
          val usedResultSet = use(resultSet)
          usedResultSet.first()
          val resultSetOption =
            if (resultSet.getRow < 1) None
            else Some(resultSet)
          processor(resultSetOption)
        }
        Await.result(resultSetFuture, 5 seconds)
      }
    }

  @tailrec
  private def iterateResultSet[T](acc: List[T], resultSet: ResultSet, iterator: ResultSet => T): List[T] =
    if (!resultSet.next()) acc
    else iterateResultSet(acc :+ iterator(resultSet), resultSet, iterator)

  def runAndIterate[T](query: String, parameters: List[Any], iterator: ResultSet => T)
      (implicit connectionPool: HikariDataSource, executionContext: ExecutionContext): Future[List[T]] = Future {
    withUsingManager { use =>
      val resultSetFuture = for {
        resultSet <- runQuery(query, parameters)
      } yield {
        val usedResultSet = use(resultSet)
        iterateResultSet(List[T](), usedResultSet, iterator)
      }
      Await.result(resultSetFuture, 5 seconds)
    }
  }
}
