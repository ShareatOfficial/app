package org.shareat.app.data.supabase

import io.github.jan.supabase.auth.user.UserSession
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import org.shareat.app.data.fake.runSuspend

class SecureSessionManagerTest {
    @Test
    fun sessionRoundTripsThroughPlatformStorageContract() {
        runSuspend {
            val storage = MemorySecureSessionStorage()
            val manager = SecureSessionManager(storage)
            val session = UserSession(
                accessToken = "access",
                refreshToken = "refresh",
                expiresIn = 3600,
                tokenType = "bearer",
                user = null,
            )

            manager.saveSession(session)
            assertEquals("refresh", manager.loadSession().refreshToken)
            manager.deleteSession()
            assertFailsWith<NoSuchElementException> { runSuspend { manager.loadSession() } }
        }
    }
}

private class MemorySecureSessionStorage : SecureSessionStorage {
    private var value: String? = null
    override suspend fun save(value: String) { this.value = value }
    override suspend fun load(): String? = value
    override suspend fun delete() { value = null }
}
