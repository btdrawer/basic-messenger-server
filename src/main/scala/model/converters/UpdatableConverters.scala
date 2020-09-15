package model.converters

import java.sql.Connection

import authentication.HashPassword
import database.actions.UserActions.{checkPasswordIsValid, usernameExists}
import model.{ApiException, FailureMessages, UpdatableUser}

trait UpdatableConverters {

  trait UpdatableConverter[A] {
    def convert(value: A)(implicit connection: Connection): List[Any]
  }

  implicit class ToParameterList[A](value: A) {
    def toParameterList(implicit converter: UpdatableConverter[A], connection: Connection): List[Any] =
      converter convert value
  }

  implicit object UpdatableUserConverter extends UpdatableConverter[UpdatableUser] {
    override def convert(user: UpdatableUser)(implicit connection: Connection): List[Any] = {
      val username = user.username.flatMap(u =>
        if (usernameExists(u)) throw ApiException(FailureMessages.USERNAME_EXISTS)
        else Some(u)
      ).orNull
      val hashedPassword = user.password.flatMap(p =>
        if (!checkPasswordIsValid(p)) throw ApiException(FailureMessages.PASSWORD_INVALID)
        else Some(HashPassword(p))
      )
      val password = hashedPassword.flatMap(p => Some(p.password)).orNull
      val salt = hashedPassword.flatMap(p => Some(p.salt)).orNull
      val status = user.status.flatMap(
        s => Some(s.toString)
      ).orNull
      val passwordResetQuestion = user.passwordReset.flatMap(
        p => Some(p.question)
      ).getOrElse(null)
      val passwordResetAnswer = user.passwordReset.flatMap(
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
}
