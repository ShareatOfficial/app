package org.shareat.feature.restauranthome.ui.restauranthome

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.shareat.app.domain.model.DishCategory
import org.shareat.feature.restauranthome.ui.model.RestaurantHomeMode
import org.shareat.feature.restauranthome.ui.model.RestaurantHomeUiState

class RestaurantHomeViewModelV2ByTone : ViewModel() {

    private val _uiState = MutableStateFlow(RestaurantHomeUiState())
    val uiState = _uiState.asStateFlow()

    init {

    }

}

data class RestaurantHomeUiStateByTone(
    val visionMode: RestaurantHomeMode = RestaurantHomeMode.MANAGEMENT,
    val dishCategory: DishCategory,

    )