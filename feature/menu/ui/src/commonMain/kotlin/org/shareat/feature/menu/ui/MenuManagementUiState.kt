package org.shareat.feature.menu.ui

import org.shareat.app.domain.model.Menu

data class MenuManagementUiState(
    val isLoading: Boolean = true,
    val name: String = "",
    val description: String = "",
    val existingMenu: Menu? = null,
    val isSaving: Boolean = false,
    val nameError: String? = null,
    val errorMessage: String? = null,
)
