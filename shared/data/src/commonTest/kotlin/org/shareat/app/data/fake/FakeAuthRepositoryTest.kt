package org.shareat.app.data.fake

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import org.shareat.app.domain.model.AuthSessionState
import org.shareat.app.domain.model.EmailAddress
import org.shareat.app.domain.repository.RepositoryError
import org.shareat.app.domain.repository.RepositoryResult

class FakeAuthRepositoryTest {
    @Test
    fun signInAndSignOutFollowTheAuthContract() = runSuspend {
        val repository = FakeAuthRepository()
        assertIs<AuthSessionState.Unauthenticated>(repository.observeSession().let { it as kotlinx.coroutines.flow.StateFlow }.value)

        val signedIn = repository.signIn(EmailAddress("preview@example.com"), "password")
        val session = assertIs<RepositoryResult.Success<*>>(signedIn).value
        assertEquals("preview@example.com", (session as org.shareat.app.domain.model.AuthSession).email.value)

        repository.signOut()
        assertEquals(null, assertIs<RepositoryResult.Success<*>>(repository.currentSession()).value)
    }

    @Test
    fun shortPasswordIsRejected() = runSuspend {
        val result = FakeAuthRepository().signIn(EmailAddress("preview@example.com"), "short")
        assertEquals(RepositoryError.InvalidCredentials, assertIs<RepositoryResult.Failure>(result).error)
    }
}
