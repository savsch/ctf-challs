package com.secure.notes.feature_note.domain.util

import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

object Encryption {
    fun aesEncrypt(data: ByteArray, secretKey: ByteArray): ByteArray {
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        val secureRandom = SecureRandom()
        val iv = ByteArray(16)
        secureRandom.nextBytes(iv)
        val ivParameterSpec = IvParameterSpec(iv)
        cipher.init(Cipher.ENCRYPT_MODE, SecretKeySpec(secretKey, 0, 16, "AES"), ivParameterSpec)
        return iv + cipher.doFinal(data)
    }
    fun aesDecrypt(encryptedData: ByteArray, secretKey: ByteArray): ByteArray {
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        val ivParameterSpec = IvParameterSpec(encryptedData.sliceArray(0 until 16))
        cipher.init(Cipher.DECRYPT_MODE, SecretKeySpec(secretKey, 0, 16, "AES"), ivParameterSpec)
        return cipher.doFinal(encryptedData.sliceArray(16 until encryptedData.size))
    }
}
