package authentication

import javax.crypto.{Cipher, KeyGenerator}

object Encryption {

  implicit class EncryptionString(val str: String) extends AnyVal {
    def encrypt(): Array[Byte] = {
      val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
      val secretKey = KeyGenerator
        .getInstance("AES")
        .generateKey()
      cipher.init(Cipher.ENCRYPT_MODE, secretKey)
      cipher.doFinal(str.getBytes())
    }

    def compare(password: String): Boolean = ???
  }

}
