package org.shareat.feature.restaurant.ui.restaurant

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.shareat.app.domain.model.DishCategory
import org.shareat.app.domain.model.EuAllergen
import org.shareat.app.domain.model.RestaurantId
import org.shareat.app.domain.repository.RepositoryError
import org.shareat.app.domain.repository.RepositoryResult
import org.shareat.app.domain.usecase.GetRestaurantMenuUseCase
import org.shareat.app.domain.usecase.GetRestaurantUseCase
import org.shareat.feature.restaurant.domain.model.DishFilterSubject
import org.shareat.feature.restaurant.domain.model.DishFilters
import org.shareat.feature.restaurant.domain.DishMatchesFiltersUseCase
import org.shareat.feature.restaurant.ui.model.DishArgs
import org.shareat.feature.restaurant.ui.model.RestaurantArgs
import org.shareat.feature.restaurant.ui.model.RestaurantSelection
import org.shareat.feature.restaurant.ui.model.RestaurantError
import org.shareat.feature.restaurant.ui.model.RestaurantUiState
import org.shareat.feature.restaurant.ui.model.declaredAllergens
import org.shareat.feature.restaurant.ui.model.toArgs
import org.shareat.feature.restaurant.ui.model.withDishes
import org.shareat.feature.restaurant.ui.model.toUiState

@Stable
class RestaurantViewModel(
    args: RestaurantArgs,
    private val getRestaurantMenu: GetRestaurantMenuUseCase,
    private val getRestaurant: GetRestaurantUseCase,
    private val dishMatchesFilters: DishMatchesFiltersUseCase,
) : ViewModel() {
    private var restaurant: RestaurantArgs = args
    private var selection = RestaurantSelection()

    private val _uiState = MutableStateFlow(currentUiState())
    val uiState: StateFlow<RestaurantUiState> = _uiState.asStateFlow()

    init {
        loadDishes()
    }

    fun onCategoryClick(category: DishCategory?) = updateSelection { copy(category = category) }

    fun onAllergenClick(allergen: EuAllergen) = updateSelection {
        copy(
            excludedAllergens = if (allergen in excludedAllergens) {
                excludedAllergens - allergen
            } else {
                excludedAllergens + allergen
            },
        )
    }

    fun onDishRatingClick(dishId: String, rating: Int) = updateSelection {
        copy(dishRatings = dishRatings + (dishId to rating))
    }

    fun onRefresh() {
        if (_uiState.value.isRefreshing) return
        _uiState.value = currentUiState(isRefreshing = true)
        viewModelScope.launch {
            when (val result = getRestaurant(RestaurantId(restaurant.id))) {
                is RepositoryResult.Success -> {
                    restaurant = result.value.toArgs()
                    selection = selection.retainedFor(restaurant)
                    _uiState.value = currentUiState()
                }

                is RepositoryResult.Failure -> {
                    _uiState.value = currentUiState(error = result.error.toRestaurantError())
                }
            }
        }
    }

    private fun loadDishes() {
        _uiState.value = currentUiState(isLoadingDishes = true)
        viewModelScope.launch {
            when (val result = getRestaurantMenu(RestaurantId(restaurant.id))) {
                is RepositoryResult.Success -> {
                    restaurant = restaurant.withDishes(result.value)
                    selection = selection.retainedFor(restaurant)
                    _uiState.value = currentUiState()
                }

                is RepositoryResult.Failure -> {
                    _uiState.value = currentUiState(error = result.error.toRestaurantError())
                }
            }
        }
    }

    fun onErrorShown() {
        _uiState.value = currentUiState()
    }

    private fun updateSelection(change: RestaurantSelection.() -> RestaurantSelection) {
        selection = selection.change()
        _uiState.value = currentUiState()
    }

    private fun currentUiState(
        isLoadingDishes: Boolean = false,
        isRefreshing: Boolean = false,
        error: RestaurantError? = null,
    ): RestaurantUiState = restaurant.toUiState(
        selection = selection,
        isLoadingDishes = isLoadingDishes,
        isRefreshing = isRefreshing,
        error = error,
        dishMatchesFilters = ::matchesFilters,
    )

    private fun matchesFilters(dish: DishArgs): Boolean = dishMatchesFilters(
        DishFilterSubject(
            category = dish.category,
            declaredAllergens = dish.allergens.toSet(),
            declaresAllergens = dish.declaresAllergens,
        ),
        DishFilters(
            category = selection.category,
            excludedAllergens = selection.excludedAllergens,
        ),
    )
}

private fun RestaurantSelection.retainedFor(restaurant: RestaurantArgs): RestaurantSelection {
    val dishIds = restaurant.dishes.map(DishArgs::id).toSet()
    return copy(
        category = category?.takeIf {
            it in restaurant.dishes.mapNotNull(DishArgs::category).toSet()
        },
        excludedAllergens = excludedAllergens intersect restaurant.declaredAllergens().toSet(),
        dishRatings = dishRatings.filterKeys { it in dishIds },
    )
}

private fun RepositoryError.toRestaurantError(): RestaurantError = when (this) {
    RepositoryError.InvalidCredentials -> RestaurantError.INVALID_CREDENTIALS
    RepositoryError.Offline -> RestaurantError.OFFLINE
    RepositoryError.Unauthenticated -> RestaurantError.UNAUTHENTICATED
    RepositoryError.Forbidden -> RestaurantError.FORBIDDEN
    is RepositoryError.Unavailable -> RestaurantError.TEMPORARILY_UNAVAILABLE
    is RepositoryError.AlreadyExists -> RestaurantError.ALREADY_EXISTS
    is RepositoryError.NotFound -> RestaurantError.NOT_FOUND
    is RepositoryError.Conflict, is RepositoryError.Validation -> RestaurantError.UNKNOWN
}
