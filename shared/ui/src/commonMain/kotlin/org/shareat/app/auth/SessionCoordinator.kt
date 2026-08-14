package org.shareat.app.auth

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import org.shareat.app.domain.model.AuthSessionState
import org.shareat.app.domain.repository.AuthRepository

class SessionCoordinator(
    authRepository: AuthRepository,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    val state: StateFlow<AuthSessionState> = authRepository.observeSession().stateIn(
        scope = scope,
        started = SharingStarted.Eagerly,
        initialValue = AuthSessionState.Initializing,
    )

    val isAuthenticated: Boolean
        get() = state.value is AuthSessionState.Authenticated
}
