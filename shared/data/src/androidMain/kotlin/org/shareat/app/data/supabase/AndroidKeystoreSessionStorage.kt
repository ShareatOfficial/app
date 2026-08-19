package org.shareat.app.data.supabase

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class AndroidKeystoreSessionStorage(context: Context) : SecureSessionStorage {
    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    override suspend fun save(value: String) {
        val cipher = Cipher.getInstance(TRANSFORMATION).apply {
            init(Cipher.ENCRYPT_MODE, getOrCreateKey())
        }
        val encrypted = cipher.doFinal(value.encodeToByteArray())
        val payload = Base64.encodeToString(cipher.iv, Base64.NO_WRAP) + ":" +
            Base64.encodeToString(encrypted, Base64.NO_WRAP)
        check(preferences.edit().putString(SESSION_KEY, payload).commit())
    }

    override suspend fun load(): String? {
        val payload = preferences.getString(SESSION_KEY, null) ?: return null
        return runCatching {
            val (iv, encrypted) = payload.split(':', limit = 2)
            val cipher = Cipher.getInstance(TRANSFORMATION).apply {
                init(
                    Cipher.DECRYPT_MODE,
                    getOrCreateKey(),
                    GCMParameterSpec(128, Base64.decode(iv, Base64.NO_WRAP)),
                )
            }
            cipher.doFinal(Base64.decode(encrypted, Base64.NO_WRAP)).decodeToString()
        }.getOrElse {
            preferences.edit().remove(SESSION_KEY).commit()
            null
        }
    }

    override suspend fun delete() {
        check(preferences.edit().remove(SESSION_KEY).commit())
    }

    private fun getOrCreateKey(): SecretKey {
        val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (keyStore.getKey(KEY_ALIAS, null) as? SecretKey)?.let { return it }
        return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore").run {
            init(
                KeyGenParameterSpec.Builder(
                    KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setRandomizedEncryptionRequired(true)
                    .build(),
            )
            generateKey()
        }
    }

    private companion object {
        const val PREFERENCES_NAME = "shareat_secure_auth"
        const val SESSION_KEY = "supabase_session"
        const val KEY_ALIAS = "org.shareat.app.supabase.session"
        const val TRANSFORMATION = "AES/GCM/NoPadding"
    }
}
