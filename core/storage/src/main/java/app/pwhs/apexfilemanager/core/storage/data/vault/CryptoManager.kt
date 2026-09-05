package app.pwhs.apexfilemanager.core.storage.data.vault

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.CipherInputStream
import javax.crypto.CipherOutputStream
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

object CryptoManager {
    private const val ALGORITHM = "AES"
    private const val TRANSFORMATION = "AES/GCM/NoPadding"
    private const val GCM_TAG_LENGTH = 128
    private const val GCM_IV_LENGTH = 12
    private const val PBKDF2_ITERATIONS = 10000
    private const val KEY_LENGTH_BITS = 256
    private const val BUFFER_SIZE = 8192

    fun generateSalt(): ByteArray {
        val salt = ByteArray(16)
        SecureRandom().nextBytes(salt)
        return salt
    }

    fun deriveKeyFromPin(pin: String, salt: ByteArray): SecretKey {
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val spec = PBEKeySpec(pin.toCharArray(), salt, PBKDF2_ITERATIONS, KEY_LENGTH_BITS)
        val tmp = factory.generateSecret(spec)
        return SecretKeySpec(tmp.encoded, ALGORITHM)
    }

    fun generateMasterKey(): SecretKey {
        val keyBytes = ByteArray(32) // 256 bits
        SecureRandom().nextBytes(keyBytes)
        return SecretKeySpec(keyBytes, ALGORITHM)
    }

    fun encryptMasterKey(masterKey: SecretKey, pinKey: SecretKey): ByteArray {
        val iv = ByteArray(GCM_IV_LENGTH)
        SecureRandom().nextBytes(iv)
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, pinKey, GCMParameterSpec(GCM_TAG_LENGTH, iv))
        val encrypted = cipher.doFinal(masterKey.encoded)
        return iv + encrypted
    }

    fun decryptMasterKey(encryptedData: ByteArray, pinKey: SecretKey): SecretKey? {
        if (encryptedData.size < GCM_IV_LENGTH) return null
        return try {
            val iv = encryptedData.copyOfRange(0, GCM_IV_LENGTH)
            val ciphertext = encryptedData.copyOfRange(GCM_IV_LENGTH, encryptedData.size)
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.DECRYPT_MODE, pinKey, GCMParameterSpec(GCM_TAG_LENGTH, iv))
            val decryptedBytes = cipher.doFinal(ciphertext)
            SecretKeySpec(decryptedBytes, ALGORITHM)
        } catch (_: Exception) {
            null
        }
    }

    fun encryptFile(sourceFile: File, destEncryptedFile: File, masterKey: SecretKey): Boolean {
        return try {
            val iv = ByteArray(GCM_IV_LENGTH)
            SecureRandom().nextBytes(iv)
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, masterKey, GCMParameterSpec(GCM_TAG_LENGTH, iv))

            FileOutputStream(destEncryptedFile).use { fos ->
                fos.write(iv)
                CipherOutputStream(fos, cipher).use { cos ->
                    FileInputStream(sourceFile).use { fis ->
                        val buffer = ByteArray(BUFFER_SIZE)
                        var bytesRead: Int
                        while (fis.read(buffer).also { bytesRead = it } != -1) {
                            cos.write(buffer, 0, bytesRead)
                        }
                    }
                }
            }
            true
        } catch (e: Exception) {
            destEncryptedFile.delete()
            false
        }
    }

    fun decryptFile(sourceEncryptedFile: File, destDecryptedFile: File, masterKey: SecretKey): Boolean {
        return try {
            FileInputStream(sourceEncryptedFile).use { fis ->
                val iv = ByteArray(GCM_IV_LENGTH)
                val readIv = fis.read(iv)
                if (readIv != GCM_IV_LENGTH) return false

                val cipher = Cipher.getInstance(TRANSFORMATION)
                cipher.init(Cipher.DECRYPT_MODE, masterKey, GCMParameterSpec(GCM_TAG_LENGTH, iv))

                CipherInputStream(fis, cipher).use { cis ->
                    FileOutputStream(destDecryptedFile).use { fos ->
                        val buffer = ByteArray(BUFFER_SIZE)
                        var bytesRead: Int
                        while (cis.read(buffer).also { bytesRead = it } != -1) {
                            fos.write(buffer, 0, bytesRead)
                        }
                    }
                }
            }
            true
        } catch (e: Exception) {
            destDecryptedFile.delete()
            false
        }
    }
}
