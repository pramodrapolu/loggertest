package com.example.loggertest.custom

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.security.keystore.KeyProperties
import android.util.Base64
import java.io.InputStream
import java.io.OutputStream
import java.security.Key
import javax.crypto.Cipher
import javax.crypto.CipherInputStream
import javax.crypto.CipherOutputStream
import javax.crypto.spec.IvParameterSpec

class LogProcessor constructor(private val context: Context) {

    companion object {
        const val LOGS_ENCRYPTION_KEY = "logs_encryption_key"
        const val KEY_RSA_ECB_PKCS1 = "RSA/ECB/PKCS1Padding"

        const val STORAGE_NAME = "com.example.loggertest.KEY_STORAGE"
    }

    val keyFromKeystore: Key
    val sharedPrefs: SharedPreferences

    init {
        sharedPrefs = context.getSharedPreferences(STORAGE_NAME, Context.MODE_PRIVATE)
        val keyStoreUtil = KeyStoreUtil()

        if (keyStoreUtil.containsAlias(LOGS_ENCRYPTION_KEY)) {
            keyFromKeystore = keyStoreUtil.getKey(LOGS_ENCRYPTION_KEY)
        } else {
            keyFromKeystore = keyStoreUtil.generateKey(context, LOGS_ENCRYPTION_KEY)
        }
    }

    fun encrypt(outputStream: OutputStream): CipherOutputStream {
        val cipher = getCipher()
        cipher.init(Cipher.ENCRYPT_MODE, keyFromKeystore)

        val initVector = cipher.iv
        val storeIV = Base64.encodeToString(initVector, Base64.DEFAULT)
        sharedPrefs.edit().putString("iv_key", storeIV).apply()
        return CipherOutputStream(outputStream, cipher)
    }

    fun decrypt(inputStream: InputStream): InputStream {

        val storeIV = sharedPrefs.getString("iv_key", null)
        val iv = Base64.decode(storeIV, Base64.DEFAULT)

        val cipher = getCipher()
        if (iv != null) {
            cipher.init(Cipher.DECRYPT_MODE, keyFromKeystore, IvParameterSpec(iv))
        } else {
            cipher.init(Cipher.DECRYPT_MODE, keyFromKeystore)
        }
        return CipherInputStream(inputStream, cipher)
    }

    private fun getCipher(): Cipher {
        return Cipher.getInstance(
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                KEY_RSA_ECB_PKCS1
            } else {
                KeyProperties.KEY_ALGORITHM_AES + "/" +
                        KeyProperties.BLOCK_MODE_CBC + "/" +
                        KeyProperties.ENCRYPTION_PADDING_PKCS7
            })
    }
}
