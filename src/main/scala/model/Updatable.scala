package model

import com.zaxxer.hikari.HikariDataSource
import authentication.HashPassword
import database.handlers.UserActionHandler.{checkPasswordIsValid, usernameIsNotTaken}

import scala.concurrent.{Await, ExecutionContext}
import scala.concurrent.duration._

sealed trait Updatable {
  def toParameterList(implicit connectionPool: HikariDataSource, executionContext: ExecutionContext): List[Any]
}

case class UpdatableUser(
  username: Option[String],
  password: Option[String],
  status: Option[Status.Value],
  passwordReset: Option[CreatablePasswordReset]
) extends Updatable {
  override def toParameterList(
    implicit connectionPool: HikariDataSource,
    executionContext: ExecutionContext
  ): List[Any] = {
    val username = this.username.map(u => {
      val usernameFuture = for {
        _ <- usernameIsNotTaken(u)
      } yield u
      Await.result(usernameFuture, 2 seconds)
    }).orNull
    val hashedPassword = this.password.flatMap(p =>
      if (!checkPasswordIsValid(p)) throw ApiException(FailureMessages.PASSWORD_INVALID)
      else Some(HashPassword(p))
    )
    val password = hashedPassword.flatMap(p => Some(p.password)).orNull
    val salt = hashedPassword.flatMap(p => Some(p.salt)).orNull
    val status = this.status.flatMap(
      s => Some(s.toString)
    ).orNull
    val passwordResetQuestion = this.passwordReset.flatMap(
      p => Some(p.question)
    ).getOrElse(null)
    val passwordResetAnswer = this.passwordReset.flatMap(
      p => Some(p.answer)
    ).orNull

    List(
      username,
      password,
      salt,
      status,
      passwordResetQuestion,
      passwordResetAnswer
    )
  }
}

case class UpdatableServer(name: Option[String]) extends Updatable {
  override def toParameterList(
    implicit connectionPool: HikariDataSource,
    executionContext: ExecutionContext
  ): List[Any] = List(name.orNull)
}
