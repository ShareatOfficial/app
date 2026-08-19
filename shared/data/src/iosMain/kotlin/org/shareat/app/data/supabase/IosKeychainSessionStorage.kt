package org.shareat.app.data.supabase

import com.liftric.kvault.KVault

class IosKeychainSessionStorage : SecureSessionStorage {
    private val keychain = KVault(serviceName = "org.shareat.app.auth")

    override suspend fun save(value: String) {
        check(keychain.set(SESSION_KEY, value)) { "Unable to persist the Supabase session in Keychain" }
    }

    override suspend fun load(): String? = keychain.string(SESSION_KEY)

    override suspend fun delete() {
        if (keychain.existsObject(SESSION_KEY)) {
            check(keychain.deleteObject(SESSION_KEY)) { "Unable to delete the Supabase session from Keychain" }
        }
    }

    private companion object {
        const val SESSION_KEY = "supabase-session"
    }
}
