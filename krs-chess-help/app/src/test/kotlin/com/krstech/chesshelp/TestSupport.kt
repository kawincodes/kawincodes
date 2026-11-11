package com.krstech.chesshelp

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import com.krstech.chesshelp.settings.SealedBytes
import com.krstech.chesshelp.settings.SecretCipher
import java.io.File
import java.security.SecureRandom
import java.security.UnrecoverableKeyException
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec
import kotlinx.coroutines.flow.MutableStateFlow

/** AES-GCM with an in-memory key: stands in for the Android Keystore, which Robolectric does not have. */
internal class InMemoryCipher : SecretCipher {
    private var key: ByteArray? = null
    private val random = SecureRandom()

    override fun encrypt(plain: ByteArray): SealedBytes {
        val k = key ?: ByteArray(32).also(random::nextBytes).also { key = it }
        val iv = ByteArray(12).also(random::nextBytes)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, SecretKeySpec(k, "AES"), GCMParameterSpec(128, iv))
        return SealedBytes(iv, cipher.doFinal(plain))
    }

    override fun decrypt(sealed: SealedBytes): ByteArray {
        val k = key ?: throw UnrecoverableKeyException("missing")
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, SecretKeySpec(k, "AES"), GCMParameterSpec(128, sealed.iv))
        return cipher.doFinal(sealed.ciphertext)
    }

    override fun reset() {
        key = null
    }
}

class RecordingSession : SessionController {
    override val running = MutableStateFlow(false)
    var started: Triple<Context, Int, Intent>? = null
    var stops = 0
    var testCalls = ArrayList<Triple<String, String, String?>>()
    var testResult: Result<String> = Result.success("claude-opus-5-5")

    override fun start(context: Context, resultCode: Int, data: Intent) {
        started = Triple(context, resultCode, data)
        running.value = true
    }

    override fun stop(context: Context) {
        stops++
        running.value = false
    }

    override suspend fun testConnection(apiKey: String, model: String, baseUrl: String?): Result<String> {
        testCalls.add(Triple(apiKey, model, baseUrl))
        return testResult
    }
}

/** Lays [view] out at the given size (px) and saves a PNG under build/ui-snapshots for a human to look at. */
fun snapshot(view: View, name: String, width: Int, height: Int? = null): Bitmap {
    val widthSpec = View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY)
    val heightSpec = if (height != null) {
        View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY)
    } else {
        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
    }
    view.measure(widthSpec, heightSpec)
    view.layout(0, 0, view.measuredWidth, view.measuredHeight)
    return snapshotAsIs(view, name)
}

/** Saves the view as it is currently laid out. */
fun snapshotAsIs(view: View, name: String): Bitmap {
    val bitmap = Bitmap.createBitmap(maxOf(1, view.width), maxOf(1, view.height), Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    canvas.drawColor(0xFF30343A.toInt())
    view.draw(canvas)
    val dir = File("build/ui-snapshots").apply { mkdirs() }
    File(dir, "$name.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
    return bitmap
}

/** Root views of every window this process has added (works on releases without WindowInspector). */
fun windowRoots(): List<View> {
    val global = Class.forName("android.view.WindowManagerGlobal").getMethod("getInstance").invoke(null)
    return ArrayList(org.robolectric.util.ReflectionHelpers.getField<List<View>>(global, "mViews"))
}
