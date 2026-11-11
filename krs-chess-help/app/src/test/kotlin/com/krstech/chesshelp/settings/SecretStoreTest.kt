package com.krstech.chesshelp.settings

import com.krstech.chesshelp.FakeSharedPreferences
import java.security.GeneralSecurityException
import java.security.ProviderException
import java.security.SecureRandom
import java.security.UnrecoverableKeyException
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** Real AES-GCM with an in-memory key standing in for the Android Keystore. */
private class FakeKeystoreCipher : SecretCipher {
    var key: ByteArray? = null
    var failEncrypt: RuntimeException? = null
    var failDecrypt: RuntimeException? = null
    var resets = 0
    var decrypts = 0
    private val random = SecureRandom()

    fun loseKey() { key = null }
    fun replaceKey() { key = ByteArray(32).also(random::nextBytes) }

    override fun encrypt(plain: ByteArray): SealedBytes {
        failEncrypt?.let { throw it }
        val k = key ?: ByteArray(32).also(random::nextBytes).also { key = it }
        val iv = ByteArray(12).also(random::nextBytes)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, SecretKeySpec(k, "AES"), GCMParameterSpec(128, iv))
        return SealedBytes(iv, cipher.doFinal(plain))
    }

    override fun decrypt(sealed: SealedBytes): ByteArray {
        decrypts++
        failDecrypt?.let { throw it }
        val k = key ?: throw UnrecoverableKeyException("missing")
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, SecretKeySpec(k, "AES"), GCMParameterSpec(128, sealed.iv))
        return cipher.doFinal(sealed.ciphertext)
    }

    override fun reset() {
        resets++
        key = null
    }
}

class SecretStoreTest {
    private val prefs = FakeSharedPreferences()
    private val cipher = FakeKeystoreCipher()
    private val store = SecretStore(prefs, cipher)
    private val secret = "sk-ant-api03-EXAMPLE-ONLY-0123456789"

    @Test
    fun `empty store has no key`() {
        assertNull(store.loadApiKey())
        assertFalse(store.hasApiKey())
    }

    @Test
    fun `key round trips and is never stored in plaintext`() {
        assertTrue(store.saveApiKey("  $secret \n"))
        assertEquals(secret, store.loadApiKey())
        assertTrue(store.hasApiKey())
        val stored = prefs.values.values.joinToString("|")
        assertFalse(stored.contains(secret))
        assertFalse(stored.contains("sk-ant"))
        assertEquals(1, prefs.values.size)
        // A second instance over the same file (e.g. the service) reads it too.
        assertEquals(secret, SecretStore(prefs, cipher).loadApiKey())
    }

    @Test
    fun `saving twice uses a fresh nonce`() {
        store.saveApiKey(secret)
        val first = prefs.values[SecretStore.KEY_API_KEY]
        store.saveApiKey(secret)
        assertNotEquals(first, prefs.values[SecretStore.KEY_API_KEY])
    }

    @Test
    fun `repeated reads are served from memory`() {
        store.saveApiKey(secret)
        val reader = SecretStore(prefs, cipher)
        repeat(5) { assertEquals(secret, reader.loadApiKey()) }
        assertEquals(1, cipher.decrypts)
    }

    @Test
    fun `a key saved through another instance replaces the cached one`() {
        store.saveApiKey(secret)
        assertEquals(secret, store.loadApiKey())
        SecretStore(prefs, cipher).saveApiKey("sk-ant-second")
        assertEquals("sk-ant-second", store.loadApiKey())
        SecretStore(prefs, cipher).clearApiKey()
        assertNull(store.loadApiKey())
    }

    @Test
    fun `blank removes the key`() {
        store.saveApiKey(secret)
        assertTrue(store.saveApiKey("   "))
        assertNull(store.loadApiKey())
        assertTrue(prefs.values.isEmpty())
    }

    @Test
    fun `a lost keystore key clears the stored value instead of crashing`() {
        store.saveApiKey(secret)
        cipher.loseKey()
        val fresh = SecretStore(prefs, cipher)
        assertNull(fresh.loadApiKey())
        assertTrue(prefs.values.isEmpty())
        assertEquals(1, cipher.resets)
        // And the store works again afterwards.
        assertTrue(fresh.saveApiKey(secret))
        assertEquals(secret, fresh.loadApiKey())
    }

    @Test
    fun `a replaced keystore key fails authentication and clears the stored value`() {
        store.saveApiKey(secret)
        cipher.replaceKey()
        assertNull(SecretStore(prefs, cipher).loadApiKey())
        assertTrue(prefs.values.isEmpty())
    }

    @Test
    fun `malformed stored data is cleared`() {
        for (garbage in listOf("garbage", "v1:!!!:###", "v2:AAAA:AAAA", "v1::", "v1:AAAA")) {
            prefs.values[SecretStore.KEY_API_KEY] = garbage
            assertNull(SecretStore(prefs, cipher).loadApiKey(), garbage)
            assertTrue(prefs.values.isEmpty(), garbage)
        }
    }

    @Test
    fun `a transient keystore failure keeps the stored value`() {
        store.saveApiKey(secret)
        cipher.failDecrypt = ProviderException("keystore busy")
        val reader = SecretStore(prefs, cipher)
        assertNull(reader.loadApiKey())
        assertEquals(1, prefs.values.size)
        cipher.failDecrypt = null
        assertEquals(secret, reader.loadApiKey())
    }

    @Test
    fun `save reports failure when the device cannot encrypt and stores nothing`() {
        cipher.failEncrypt = ProviderException("no keystore")
        assertFalse(store.saveApiKey(secret))
        assertTrue(prefs.values.isEmpty())
        assertEquals(2, cipher.resets)
        assertNull(store.loadApiKey())
    }

    @Test
    fun `save recovers from an unusable existing key by creating a new one`() {
        val flaky = object : SecretCipher {
            var calls = 0
            override fun encrypt(plain: ByteArray): SealedBytes {
                if (calls++ == 0) throw object : GeneralSecurityException("key permanently invalidated") {}
                return cipher.encrypt(plain)
            }
            override fun decrypt(sealed: SealedBytes): ByteArray = cipher.decrypt(sealed)
            override fun reset() = cipher.reset()
        }
        val recovering = SecretStore(prefs, flaky)
        assertTrue(recovering.saveApiKey(secret))
        assertEquals(secret, SecretStore(prefs, flaky).loadApiKey())
    }

    @Test
    fun `encoding round trips and rejects foreign formats`() {
        val sealed = SealedBytes(byteArrayOf(1, 2, 3), byteArrayOf(4, 5, 6, 7))
        val decoded = SecretStore.decode(SecretStore.encode(sealed))
        assertContentEquals(sealed.iv, decoded.iv)
        assertContentEquals(sealed.ciphertext, decoded.ciphertext)
        assertFailsWith<IllegalArgumentException> { SecretStore.decode("plain-text-key") }
    }
}
