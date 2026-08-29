package org.shareat.feature.menu.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.shareat.app.domain.repository.RepositoryResult
import org.shareat.feature.menu.domain.CreateDraftMenuParams
import org.shareat.feature.menu.domain.CreateDraftMenuUseCase
import org.shareat.feature.menu.domain.LoadOwnedRestaurantMenuUseCase
import org.shareat.feature.menu.domain.toMenuMessage

class MenuManagementViewModel(
    private val loadOwnedRestaurantMenu: LoadOwnedRestaurantMenuUseCase,
    private val createDraftMenu: CreateDraftMenuUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(MenuManagementUiState())
    val uiState: StateFlow<MenuManagementUiState> = _uiState.asStateFlow()

    init { load() }

    fun onNameChanged(value: String) {
        _uiState.update { it.copy(name = value, nameError = null, errorMessage = null) }
    }

    fun onDescriptionChanged(value: String) {
        _uiState.update { it.copy(description = value, errorMessage = null) }
    }

    fun retry() = load()

    fun create() {
        val state = _uiState.value
        if (state.isLoading || state.isSaving || state.existingMenu != null) return
        val name = state.name.trim()
        val nameError = when {
            name.isEmpty() -> "Introduce el nombre de la carta."
            name.length > 120 -> "El nombre no puede superar 120 caracteres."
            else -> null
        }
        if (nameError != null) {
            _uiState.update { it.copy(nameError = nameError, errorMessage = null) }
            return
        }
        _uiState.update { it.copy(isSaving = true, errorMessage = null) }
        viewModelScope.launch {
            when (val result = createDraftMenu(
                CreateDraftMenuParams(name, state.description.trim().ifEmpty { null }),
            )) {
                is RepositoryResult.Success -> _uiState.update {
                    it.copy(isSaving = false, existingMenu = result.value, nameError = null)
                }
                is RepositoryResult.Failure -> _uiState.update {
                    it.copy(isSaving = false, errorMessage = result.error.toMenuMessage())
                }
            }
        }
    }

    private fun load() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            when (val result = loadOwnedRestaurantMenu()) {
                is RepositoryResult.Success -> _uiState.update {
                    it.copy(isLoading = false, existingMenu = result.value.menu)
                }
                is RepositoryResult.Failure -> _uiState.update {
                    it.copy(isLoading = false, errorMessage = result.error.toMenuMessage())
                }
            }
        }
    }
}
