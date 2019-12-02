package com.example.loggertest.custom

import android.content.Context
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.math.BigInteger
import java.security.Key
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.util.Calendar
import java.util.GregorianCalendar
import javax.crypto.KeyGenerator
import javax.security.auth.x500.X500Principal


class KeyStoreUtil {

    companion object {
        const val KEYSTORE_PROVIDER_NAME = "AndroidKeyStore"
        const val KEY_ALGORITHM_RSA = "RSA"
    }

    @Suppress("DEPRECATION")
    fun generateKey(context: Context, name: String): Key {
        val keyFromKeyStore: Key
        val keyStore = getKeyStore()

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            val start = GregorianCalendar()
            val end = GregorianCalendar()
            end.add(Calendar.YEAR, 99)

            val spec = android.security.KeyPairGeneratorSpec.Builder(context)
                    .setAlias(name)
                    .setSubject(X500Principal("CN=$name"))
                    .setSerialNumber(BigInteger.valueOf(901)) // A random integer for self-signed certificate
                    .setStartDate(start.time)
                    .setEndDate(end.time)
                    .build()

            val keyPairGenerator = KeyPairGenerator.getInstance(KEY_ALGORITHM_RSA, KEYSTORE_PROVIDER_NAME)
            keyPairGenerator.initialize(spec)
            keyPairGenerator.generateKeyPair()

            val privateKeyEntry = keyStore.getEntry(name, null) as KeyStore.PrivateKeyEntry
            keyFromKeyStore = privateKeyEntry.certificate.publicKey
        } else {
            val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, KEYSTORE_PROVIDER_NAME)

            val keySpec = KeyGenParameterSpec.Builder(name, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                    .setUserAuthenticationRequired(false)
                    .build()

            keyGenerator.init(keySpec)
            keyFromKeyStore = keyGenerator.generateKey()

//            keyFromKeyStore = keyStore.getKey(name, null)
        }

        return keyFromKeyStore
    }

    /**
     * Returns true if a key with given name exists in [KeyStore], else returns false
     *
     * @param name - Name of the key
     */
    fun containsAlias(name: String): Boolean {
        val keyStore = getKeyStore()

        return keyStore.containsAlias(name)
    }

    /**
     * Retrieve and return the key reference from [KeyStore]
     *
     * @param name - Name of the key to retrieve
     */
    fun getKey(name: String): Key {
        val keyStore = getKeyStore()



        return keyStore.getKey(name, null)
    }

    /**
     * Delete the key reference from [KeyStore]
     *
     * @param name - Name of the key to delete
     */
    fun deleteKey(name: String) {
        val keyStore = getKeyStore()

        return keyStore.deleteEntry(name)
    }

    /**
     * Returns the [KeyStore] instance
     *
     * Refer below for keystore provider name:
     * https://developer.android.com/training/articles/keystore#WorkingWithKeyStoreEntries
     */
    private fun getKeyStore(): KeyStore {
        val keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER_NAME)
        keyStore.load(null)

        return keyStore
    }
}
