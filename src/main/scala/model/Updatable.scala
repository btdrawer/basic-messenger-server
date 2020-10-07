package model

import com.zaxxer.hikari.HikariDataSource

import authentication.HashPassword
import database.handlers.UserActionHandler.{checkPasswordIsValid, usernameExists}

sealed trait Updatable {
  def toParameterList(implicit connectionPool: HikariDataSource): List[Any]
}

case class UpdatableUser(
  username: Option[String],
  password: Option[String],
  status: Option[Status.Value],
  passwordReset: Option[CreatablePasswordReset]
) extends Updatable {
  override def toParameterList(implicit connectionPool: HikariDataSource): List[Any] = {
    val username = this.username.flatMap(u =>
      if (usernameExists(u)) throw ApiException(FailureMessages.USERNAME_EXISTS)
      else Some(u)
    ).orNull
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
  override def toParameterList(implicit connectionPool: HikariDataSource): List[Any] = List(name.orNull)
}
