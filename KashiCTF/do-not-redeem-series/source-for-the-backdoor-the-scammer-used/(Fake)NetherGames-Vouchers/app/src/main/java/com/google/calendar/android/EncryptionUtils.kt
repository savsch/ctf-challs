package com.google.calendar.android
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

fun hexStringToByteArray(hexString: String): ByteArray {
    val byteArray = ByteArray(hexString.length / 2)
    for (i in 0 until hexString.length step 2) {
        byteArray[i / 2] = ((hexString[i].digitToInt(16) shl 4) + hexString[i + 1].digitToInt(16)).toByte()
    }
    return byteArray
}

fun aesEncrypt(data: ByteArray, hexKey: String): ByteArray {
    val secretKey = SecretKeySpec(hexStringToByteArray(hexKey), "AES")
    val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
    cipher.init(Cipher.ENCRYPT_MODE, secretKey)
    return cipher.doFinal(data)
}

fun aesDecrypt(encryptedData: ByteArray, hexKey: String): ByteArray {
    val secretKey = SecretKeySpec(hexStringToByteArray(hexKey), "AES")
    val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
    cipher.init(Cipher.DECRYPT_MODE, secretKey)
    return cipher.doFinal(encryptedData)
}