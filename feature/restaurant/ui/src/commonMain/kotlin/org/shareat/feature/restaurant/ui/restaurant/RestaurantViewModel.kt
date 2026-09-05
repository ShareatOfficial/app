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
import org.shareat.app.domain.usecase.GetRestaurantUseCase
import org.shareat.feature.restaurant.domain.model.DishFilterSubject
import org.shareat.feature.restaurant.domain.model.DishFilters
import org.shareat.feature.restaurant.domain.DishMatchesFiltersUseCase
import org.shareat.feature.restaurant.ui.model.DishArgs
import org.shareat.feature.restaurant.ui.model.RestaurantArgs
import org.shareat.feature.restaurant.ui.model.RestaurantSelection
import org.shareat.feature.restaurant.ui.model.RestaurantUiState
import org.shareat.feature.restaurant.ui.model.declaredAllergens
import org.shareat.feature.restaurant.ui.model.toArgs
import org.shareat.feature.restaurant.ui.model.toUiState

@Stable
class RestaurantViewModel(
    args: RestaurantArgs,
    private val getRestaurant: GetRestaurantUseCase,
    private val dishMatchesFilters: DishMatchesFiltersUseCase,
) : ViewModel() {
    private var restaurant: RestaurantArgs = args
    private var selection = RestaurantSelection()

    private val _uiState = MutableStateFlow(currentUiState())
    val uiState: StateFlow<RestaurantUiState> = _uiState.asStateFlow()

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
                    _uiState.value = currentUiState(errorMessage = result.error.toUserMessage())
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
        isRefreshing: Boolean = false,
        errorMessage: String? = null,
    ): RestaurantUiState = restaurant.toUiState(
        selection = selection,
        isRefreshing = isRefreshing,
        errorMessage = errorMessage,
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

private fun RepositoryError.toUserMessage(): String = when (this) {
    RepositoryError.InvalidCredentials -> "Tus credenciales ya no son válidas."
    RepositoryError.Offline -> "Parece que no tienes conexión. Inténtalo de nuevo más tarde."
    RepositoryError.Unauthenticated -> "Tu sesión ha caducado. Vuelve a iniciar sesión."
    RepositoryError.Forbidden -> "Esta cuenta no puede realizar esa acción."
    is RepositoryError.Unavailable -> "El servicio no está disponible temporalmente."
    is RepositoryError.AlreadyExists -> "$entity ya existe."
    is RepositoryError.Conflict -> reason
    is RepositoryError.NotFound -> "No hemos encontrado $entity."
    is RepositoryError.Validation -> reason
}
