package org.shareat.app.domain.repository

import kotlinx.coroutines.flow.Flow
import org.shareat.app.domain.model.AuthSession
import org.shareat.app.domain.model.AuthSessionState
import org.shareat.app.domain.model.EmailAddress
import org.shareat.app.domain.model.RegistrationCredentials

interface AuthRepository {
    fun observeSession(): Flow<AuthSessionState>
    suspend fun currentSession(): RepositoryResult<AuthSession?>
    suspend fun register(credentials: RegistrationCredentials): RepositoryResult<AuthSession>
    suspend fun signIn(email: EmailAddress, password: String): RepositoryResult<AuthSession>
    suspend fun signOut(): RepositoryResult<Unit>
    suspend fun requestPasswordReset(email: EmailAddress): RepositoryResult<Unit>
    suspend fun updatePassword(password: String): RepositoryResult<Unit>
}
