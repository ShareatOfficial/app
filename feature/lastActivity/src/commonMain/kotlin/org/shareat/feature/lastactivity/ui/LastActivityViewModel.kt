package org.shareat.feature.lastactivity.ui

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel
import org.shareat.app.domain.model.AuthSessionState
import org.shareat.app.domain.repository.AuthRepository
import org.shareat.app.domain.repository.RepositoryError
import org.shareat.app.domain.repository.RepositoryResult
import org.shareat.feature.lastactivity.domain.GetLastActivityUseCase
import org.shareat.feature.lastactivity.domain.ReviewedTarget

@Stable
@KoinViewModel
class LastActivityViewModel(
    private val authRepository: AuthRepository,
    private val getLastActivityUseCase: GetLastActivityUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow<LastActivityUiState>(LastActivityUiState.Initializing)
    val uiState: StateFlow<LastActivityUiState> = _uiState.asStateFlow()
    private var accountId: org.shareat.app.domain.model.AccountId? = null
    private var retryJob: Job? = null

    init {
        viewModelScope.launch {
            authRepository.observeSession().collectLatest { session ->
                when (session) {
                    AuthSessionState.Initializing -> _uiState.value = LastActivityUiState.Initializing
                    AuthSessionState.Unauthenticated -> {
                        retryJob?.cancel()
                        accountId = null
                        _uiState.value = LastActivityUiState.Guest
                    }
                    AuthSessionState.RefreshUnavailable -> {
                        retryJob?.cancel()
                        accountId = null
                        _uiState.value = LastActivityUiState.Guest
                    }
                    is AuthSessionState.Authenticated -> {
                        accountId = session.session.accountId
                        load(session.session.accountId)
                    }
                }
            }
        }
    }

    fun retry() {
        val id = accountId ?: return
        retryJob?.cancel()
        retryJob = viewModelScope.launch { load(id) }
    }

    private suspend fun load(id: org.shareat.app.domain.model.AccountId) {
        _uiState.value = LastActivityUiState.Loading
        when (val result = getLastActivityUseCase(id)) {
            is RepositoryResult.Success -> _uiState.value = if (result.value.isEmpty()) {
                LastActivityUiState.Empty
            } else {
                LastActivityUiState.Content(result.value.map { it.toUiState() })
            }
            is RepositoryResult.Failure -> _uiState.value = LastActivityUiState.Error(result.error.message())
        }
    }
}

private fun org.shareat.feature.lastactivity.domain.LastActivityItem.toUiState() = when (val target = target) {
    is ReviewedTarget.Dish -> LastActivityReviewUiState(
        id = review.id, type = "Plato", imageUrl = target.dish.image?.url,
        imageDescription = target.dish.image?.alternativeText, name = target.dish.name,
        description = target.dish.description, rating = review.rating.value, comment = review.comment,
    )
    is ReviewedTarget.Restaurant -> LastActivityReviewUiState(
        id = review.id, type = "Restaurante", imageUrl = target.restaurant.heroImage?.url,
        imageDescription = target.restaurant.heroImage?.alternativeText, name = target.restaurant.name,
        description = target.restaurant.description, rating = review.rating.value, comment = review.comment,
    )
}

private fun RepositoryError.message() = when (this) {
    RepositoryError.Offline -> "Parece que no tienes conexión. Inténtalo de nuevo."
    RepositoryError.Unauthenticated -> "Tu sesión ha caducado. Inicia sesión de nuevo."
    is RepositoryError.Unavailable -> "El servicio no está disponible ahora mismo."
    else -> "No pudimos cargar tu actividad. Inténtalo de nuevo."
}
