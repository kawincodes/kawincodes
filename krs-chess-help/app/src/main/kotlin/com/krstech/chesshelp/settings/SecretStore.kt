package com.krstech.chesshelp.settings

import android.content.Context
import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Log
import androidx.core.content.edit
import java.security.GeneralSecurityException
import java.security.KeyStore
import java.security.UnrecoverableKeyException
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/** An AES-GCM result: the random nonce the cipher chose plus ciphertext-with-tag. */
internal class SealedBytes(val iv: ByteArray, val ciphertext: ByteArray)

/** The encryption primitive behind [SecretStore]; the real one keeps its key in the Android Keystore. */
internal interface SecretCipher {
    @Throws(GeneralSecurityException::class)
    fun encrypt(plain: ByteArray): SealedBytes

    @Throws(GeneralSecurityException::class)
    fun decrypt(sealed: SealedBytes): ByteArray

    /** Forgets the key, so the next [encrypt] creates a fresh one. Must not throw. */
    fun reset()
}

/**
 * Stores the Anthropic API key encrypted with an AES-GCM key that never leaves the Android Keystore.
 * Nothing is kept in plaintext at rest, and the file is excluded from backup (a restored ciphertext
 * would be useless anyway: Keystore keys do not transfer between devices).
 *
 * The key is never logged. Safe to call from any thread; reads after the first are served from memory.
 */
class SecretStore internal constructor(
    private val prefs: SharedPreferences,
    private val cipher: SecretCipher,
) {
    constructor(context: Context) : this(
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE),
        cipherFactory(),
    )

    // Keyed on the stored text, so a key saved through another instance is picked up here too.
    private var cachedStored: String? = null
    private var cachedKey: String? = null

    /** Encrypts and stores [apiKey] (blank removes it). False when the device could not encrypt it. */
    fun saveApiKey(apiKey: String): Boolean = synchronized(LOCK) {
        val trimmed = apiKey.trim()
        if (trimmed.isEmpty()) {
            clearApiKey()
            return true
        }
        val sealed = encryptWithRecovery(trimmed.toByteArray(Charsets.UTF_8)) ?: return false
        val stored = encode(sealed)
        prefs.edit { putString(KEY_API_KEY, stored) }
        cachedStored = stored
        cachedKey = trimmed
        true
    }

    /** The stored key, or null when there is none or it can no longer be decrypted. */
    fun loadApiKey(): String? = synchronized(LOCK) {
        val stored = prefs.getString(KEY_API_KEY, null)
        if (stored.isNullOrEmpty()) {
            forgetCache()
            return null
        }
        if (stored == cachedStored) return cachedKey
        val key = try {
            String(cipher.decrypt(decode(stored)), Charsets.UTF_8).ifEmpty { null }
        } catch (e: GeneralSecurityException) {
            // The Keystore key is gone or was invalidated (restore onto another device, lock-screen
            // reset, keystore wipe): the ciphertext can never be read again, so drop it.
            Log.w(TAG, "Stored API key is unreadable (${e.javaClass.simpleName}); clearing it")
            discardUnreadable()
            return null
        } catch (e: IllegalArgumentException) {
            Log.w(TAG, "Stored API key is malformed; clearing it")
            discardUnreadable()
            return null
        } catch (e: RuntimeException) {
            // Keystore daemon trouble (ProviderException and friends) may be temporary: report "no key"
            // now without destroying data that might decrypt next time.
            Log.w(TAG, "Keystore failure while reading the API key (${e.javaClass.simpleName})")
            return null
        }
        cachedStored = stored
        cachedKey = key
        key
    }

    fun hasApiKey(): Boolean = loadApiKey() != null

    fun clearApiKey() = synchronized(LOCK) {
        prefs.edit { remove(KEY_API_KEY) }
        forgetCache()
    }

    private fun discardUnreadable() {
        prefs.edit { remove(KEY_API_KEY) }
        forgetCache()
        cipher.reset()
    }

    private fun forgetCache() {
        cachedStored = null
        cachedKey = null
    }

    /** One retry with a fresh Keystore key covers an existing key that has become unusable. */
    private fun encryptWithRecovery(plain: ByteArray): SealedBytes? {
        repeat(2) { attempt ->
            try {
                return cipher.encrypt(plain)
            } catch (e: GeneralSecurityException) {
                Log.w(TAG, "Encrypting the API key failed (${e.javaClass.simpleName}), attempt ${attempt + 1}")
            } catch (e: RuntimeException) {
                Log.w(TAG, "Keystore failure while saving the API key (${e.javaClass.simpleName}), attempt ${attempt + 1}")
            }
            cipher.reset()
        }
        return null
    }

    internal companion object {
        const val PREFS_NAME = "krs_secrets"
        const val KEY_API_KEY = "api_key"
        private const val KEY_ALIAS = "krs_api_key_v1"
        private const val FORMAT_PREFIX = "v1"
        private const val TAG = "KrsSecretStore"

        // Process-wide: Keystore key creation and deletion must not interleave between instances.
        private val LOCK = Any()

        /** Replaced in tests, where there is no Android Keystore. */
        @Volatile
        var cipherFactory: () -> SecretCipher = { KeystoreCipher(KEY_ALIAS) }

        fun encode(sealed: SealedBytes): String {
            val encoder = Base64.getEncoder()
            return "$FORMAT_PREFIX:${encoder.encodeToString(sealed.iv)}:${encoder.encodeToString(sealed.ciphertext)}"
        }

        /** @throws IllegalArgumentException when [stored] is not something [encode] produced. */
        fun decode(stored: String): SealedBytes {
            val parts = stored.split(':')
            require(parts.size == 3 && parts[0] == FORMAT_PREFIX) { "Unknown secret format" }
            val decoder = Base64.getDecoder()
            val iv = decoder.decode(parts[1])
            val ciphertext = decoder.decode(parts[2])
            require(iv.isNotEmpty() && ciphertext.isNotEmpty()) { "Empty secret" }
            return SealedBytes(iv, ciphertext)
        }
    }
}

/** AES-256-GCM with a non-exportable key in the Android Keystore; no user authentication required. */
internal class KeystoreCipher(private val alias: String) : SecretCipher {

    override fun encrypt(plain: ByteArray): SealedBytes {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        // The Keystore insists on choosing the nonce itself (randomized encryption).
        cipher.init(Cipher.ENCRYPT_MODE, existingKey() ?: createKey())
        val ciphertext = cipher.doFinal(plain)
        return SealedBytes(cipher.iv, ciphertext)
    }

    override fun decrypt(sealed: SealedBytes): ByteArray {
        val key = existingKey() ?: throw UnrecoverableKeyException("The Keystore key is missing")
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(TAG_BITS, sealed.iv))
        return cipher.doFinal(sealed.ciphertext)
    }

    override fun reset() {
        try {
            keyStore().deleteEntry(alias)
        } catch (e: Exception) {
            Log.w(TAG, "Could not delete the Keystore key (${e.javaClass.simpleName})")
        }
    }

    private fun keyStore(): KeyStore = KeyStore.getInstance(PROVIDER).apply { load(null) }

    private fun existingKey(): SecretKey? = keyStore().getKey(alias, null) as? SecretKey

    private fun createKey(): SecretKey {
        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, PROVIDER)
        generator.init(
            KeyGenParameterSpec.Builder(alias, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build(),
        )
        return generator.generateKey()
    }

    private companion object {
        const val PROVIDER = "AndroidKeyStore"
        const val TRANSFORMATION = "AES/GCM/NoPadding"
        const val TAG_BITS = 128
        const val TAG = "KrsSecretStore"
    }
}
