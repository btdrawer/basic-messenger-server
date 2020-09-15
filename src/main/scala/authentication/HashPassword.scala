package authentication

import java.security.SecureRandom
import java.util.Base64

import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

case class SaltedPassword(password: String, salt: String)

object HashPassword {
  private def encoder: Base64.Encoder = Base64.getEncoder
  private def decoder: Base64.Decoder = Base64.getDecoder

  private def SALT_SIZE: Int = 16
  private def ITERATION_COUNT: Int = 65536
  private def KEY_LENGTH = 128

  private def generateSalt(): Array[Byte] = {
    val secureRandom: SecureRandom = new SecureRandom()
    val salt = new Array[Byte](SALT_SIZE)
    secureRandom.nextBytes(salt)
    salt
  }

  def hashPassword(password: String, salt: Array[Byte]): SaltedPassword = {
    val spec = new PBEKeySpec(password.toCharArray, salt, ITERATION_COUNT, KEY_LENGTH)
    val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
    val hash = factory.generateSecret(spec).getEncoded
    SaltedPassword(
      password = encoder.encodeToString(hash),
      salt = encoder.encodeToString(salt)
    )
  }

  def verify(salt: String)(password: String): String =
    hashPassword(password, decoder.decode(salt)).password

  def apply(password: String): SaltedPassword =
    hashPassword(password, generateSalt())
}
