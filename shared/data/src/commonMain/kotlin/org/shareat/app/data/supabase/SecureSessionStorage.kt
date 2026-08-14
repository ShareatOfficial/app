package org.shareat.app.data.supabase

import io.github.jan.supabase.auth.SessionManager
import io.github.jan.supabase.auth.user.UserSession
import kotlinx.serialization.json.Json

/** Platform storage for the serialized Supabase session. Native implementations use Keystore/Keychain. */
interface SecureSessionStorage {
    suspend fun save(value: String)
    suspend fun load(): String?
    suspend fun delete()
}

internal class SecureSessionManager(
    private val storage: SecureSessionStorage,
    private val json: Json = Json { ignoreUnknownKeys = true },
) : SessionManager {
    override suspend fun saveSession(session: UserSession) {
        storage.save(json.encodeToString(session))
    }

    override suspend fun loadSession(): UserSession = storage.load()
        ?.let { json.decodeFromString<UserSession>(it) }
        ?: throw NoSuchElementException("No persisted Supabase session")

    override suspend fun deleteSession() {
        storage.delete()
    }
}
