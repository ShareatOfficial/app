package org.shareat.feature.restauranthome.ui.restauranthome

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.shareat.app.domain.model.DishCategory
import org.shareat.app.domain.model.EuAllergen
import org.shareat.app.domain.repository.RepositoryError
import org.shareat.app.domain.repository.RepositoryResult
import org.shareat.feature.restauranthome.domain.GetRestaurantHomeUseCase
import org.shareat.feature.restauranthome.ui.model.DishEditFormUiState
import org.shareat.feature.restauranthome.ui.model.RestaurantAddressUiState
import org.shareat.feature.restauranthome.ui.model.RestaurantDish
import org.shareat.feature.restauranthome.ui.model.RestaurantHomeContent
import org.shareat.feature.restauranthome.ui.model.RestaurantHomeError
import org.shareat.feature.restauranthome.ui.model.toEditablePrice
import org.shareat.feature.restauranthome.ui.model.toRestaurantHomeData

enum class RestaurantHomeBottomSheet {
    VIEW_DISH,
    EDIT_DISH,
    VIEW_RATING,
    VIEW_ADDRESS,
    EDIT_ADDRESS,
    EDIT_MAIN_INFO,
    EDIT_CATEGORIES,
}

data class RestaurantMainInfoDraft(
    val name: String,
    val description: String,
    val imageUrl: String,
)

data class RestaurantAddressDraft(
    val streetLine: String,
    val locality: String,
    val postalCode: String,
    val region: String,
    val countryCode: String,
)

data class RestaurantHomeUiStateByTone(
    val content: RestaurantHomeContent = RestaurantHomeContent.Loading,
    val isEditMode: Boolean = true,
    val activeBottomSheet: RestaurantHomeBottomSheet? = null,
    val selectedDishId: String? = null,
    val mainInfoDraft: RestaurantMainInfoDraft? = null,
    val dishEditForm: DishEditFormUiState? = null,
    val addressDraft: RestaurantAddressDraft? = null,
    val categoriesDraft: Set<DishCategory>? = null,
) {
    val selectedDish: RestaurantDish?
        get() = (content as? RestaurantHomeContent.Loaded)
            ?.restaurant
            ?.dishes
            ?.firstOrNull { it.id == selectedDishId }
}

@Stable
class RestaurantHomeViewModelV2ByTone(
    private val loadRestaurantHome: GetRestaurantHomeUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(RestaurantHomeUiStateByTone())
    val uiState: StateFlow<RestaurantHomeUiStateByTone> = _uiState.asStateFlow()

    init {
        load()
    }

    fun onRetryClick() = load()

    fun onEditModeChange(isEditMode: Boolean) {
        _uiState.value = _uiState.value.copy(
            isEditMode = isEditMode,
            activeBottomSheet = null,
            selectedDishId = null,
            mainInfoDraft = null,
            dishEditForm = null,
            addressDraft = null,
            categoriesDraft = null,
        )
    }

    fun onDishClick(dishId: String) {
        val dish = findDish(dishId) ?: return
        val isEditMode = _uiState.value.isEditMode
        _uiState.value = _uiState.value.copy(
            activeBottomSheet = if (isEditMode) {
                RestaurantHomeBottomSheet.EDIT_DISH
            } else {
                RestaurantHomeBottomSheet.VIEW_DISH
            },
            selectedDishId = dishId,
            mainInfoDraft = null,
            dishEditForm = if (isEditMode) dish.toEditForm() else null,
            addressDraft = null,
            categoriesDraft = null,
        )
    }

    fun onMainInfoClick() {
        if (!_uiState.value.isEditMode) return
        val restaurant = (_uiState.value.content as? RestaurantHomeContent.Loaded)?.restaurant ?: return
        _uiState.value = _uiState.value.copy(
            activeBottomSheet = RestaurantHomeBottomSheet.EDIT_MAIN_INFO,
            selectedDishId = null,
            mainInfoDraft = RestaurantMainInfoDraft(
                name = restaurant.name,
                description = restaurant.description.orEmpty(),
                imageUrl = restaurant.imageUrl.orEmpty(),
            ),
            dishEditForm = null,
            addressDraft = null,
            categoriesDraft = null,
        )
    }

    fun onAddressClick() {
        val restaurant = (_uiState.value.content as? RestaurantHomeContent.Loaded)?.restaurant ?: return
        if (!_uiState.value.isEditMode) {
            openSheet(RestaurantHomeBottomSheet.VIEW_ADDRESS)
            return
        }

        _uiState.value = _uiState.value.copy(
            activeBottomSheet = RestaurantHomeBottomSheet.EDIT_ADDRESS,
            selectedDishId = null,
            mainInfoDraft = null,
            dishEditForm = null,
            addressDraft = restaurant.address.toDraft(),
            categoriesDraft = null,
        )
    }

    fun onRatingClick() {
        if (_uiState.value.content !is RestaurantHomeContent.Loaded) return
        openSheet(RestaurantHomeBottomSheet.VIEW_RATING)
    }

    fun onCategoriesEditClick() {
        if (!_uiState.value.isEditMode) return
        val restaurant = (_uiState.value.content as? RestaurantHomeContent.Loaded)?.restaurant ?: return
        _uiState.value = _uiState.value.copy(
            activeBottomSheet = RestaurantHomeBottomSheet.EDIT_CATEGORIES,
            selectedDishId = null,
            mainInfoDraft = null,
            dishEditForm = null,
            addressDraft = null,
            categoriesDraft = restaurant.categories.toSet(),
        )
    }

    fun onRestaurantNameChange(value: String) = updateMainInfoDraft { copy(name = value) }

    fun onRestaurantDescriptionChange(value: String) = updateMainInfoDraft { copy(description = value) }

    fun onDishNameChange(value: String) = updateDishEditForm {
        copy(
            name = value,
            validation = validation.copy(nameInvalid = false),
            error = null,
        )
    }

    fun onDishDescriptionChange(value: String) = updateDishEditForm {
        copy(description = value, error = null)
    }

    fun onDishPriceChange(value: String) = updateDishEditForm {
        copy(
            price = value,
            validation = validation.copy(priceInvalid = false),
            error = null,
        )
    }

    fun onDishAllergenClick(allergen: EuAllergen) = updateDishEditForm {
        copy(
            allergens = if (allergen in allergens) allergens - allergen else allergens + allergen,
            error = null,
        )
    }

    fun onAddressStreetLineChange(value: String) = updateAddressDraft { copy(streetLine = value) }

    fun onAddressLocalityChange(value: String) = updateAddressDraft { copy(locality = value) }

    fun onAddressPostalCodeChange(value: String) = updateAddressDraft { copy(postalCode = value) }

    fun onAddressRegionChange(value: String) = updateAddressDraft { copy(region = value) }

    fun onCategoryClick(category: DishCategory) {
        val categories = _uiState.value.categoriesDraft ?: return
        _uiState.value = _uiState.value.copy(
            categoriesDraft = if (category in categories) {
                categories - category
            } else {
                categories + category
            },
        )
    }

    fun onDismissBottomSheet() {
        _uiState.value = _uiState.value.copy(
            activeBottomSheet = null,
            selectedDishId = null,
            mainInfoDraft = null,
            dishEditForm = null,
            addressDraft = null,
            categoriesDraft = null,
        )
    }

    private fun load() {
        _uiState.value = _uiState.value.copy(
            content = RestaurantHomeContent.Loading,
            activeBottomSheet = null,
            selectedDishId = null,
            mainInfoDraft = null,
            dishEditForm = null,
            addressDraft = null,
            categoriesDraft = null,
        )
        viewModelScope.launch {
            when (val result = loadRestaurantHome()) {
                is RepositoryResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        content = RestaurantHomeContent.Loaded(result.value.toRestaurantHomeData()),
                    )
                }

                is RepositoryResult.Failure -> {
                    _uiState.value = _uiState.value.copy(
                        content = RestaurantHomeContent.Error(result.error.toUiError()),
                    )
                }
            }
        }
    }

    private fun openSheet(sheet: RestaurantHomeBottomSheet) {
        _uiState.value = _uiState.value.copy(
            activeBottomSheet = sheet,
            selectedDishId = null,
            mainInfoDraft = null,
            dishEditForm = null,
            addressDraft = null,
            categoriesDraft = null,
        )
    }

    private fun findDish(dishId: String): RestaurantDish? =
        (_uiState.value.content as? RestaurantHomeContent.Loaded)
            ?.restaurant
            ?.dishes
            ?.firstOrNull { it.id == dishId }

    private fun updateMainInfoDraft(change: RestaurantMainInfoDraft.() -> RestaurantMainInfoDraft) {
        val draft = _uiState.value.mainInfoDraft ?: return
        _uiState.value = _uiState.value.copy(mainInfoDraft = draft.change())
    }

    private fun updateDishEditForm(change: DishEditFormUiState.() -> DishEditFormUiState) {
        val form = _uiState.value.dishEditForm ?: return
        _uiState.value = _uiState.value.copy(dishEditForm = form.change())
    }

    private fun updateAddressDraft(change: RestaurantAddressDraft.() -> RestaurantAddressDraft) {
        val draft = _uiState.value.addressDraft ?: return
        _uiState.value = _uiState.value.copy(addressDraft = draft.change())
    }
}

private fun RestaurantAddressUiState.toDraft() = RestaurantAddressDraft(
    streetLine = streetLine,
    locality = locality,
    postalCode = postalCode,
    region = region.orEmpty(),
    countryCode = countryCode,
)

private fun RestaurantDish.toEditForm() = DishEditFormUiState(
    dishId = id,
    name = name,
    description = description.orEmpty(),
    price = priceMinorUnits.toEditablePrice(),
    allergens = allergens,
    imageUrl = imageUrl,
    isPublished = isPublished,
)

private fun RepositoryError.toUiError(): RestaurantHomeError = when (this) {
    RepositoryError.Offline -> RestaurantHomeError.OFFLINE
    RepositoryError.Unauthenticated -> RestaurantHomeError.UNAUTHENTICATED
    RepositoryError.Forbidden -> RestaurantHomeError.FORBIDDEN
    is RepositoryError.NotFound -> RestaurantHomeError.NOT_FOUND
    is RepositoryError.Unavailable -> RestaurantHomeError.TEMPORARILY_UNAVAILABLE
    is RepositoryError.Validation -> RestaurantHomeError.VALIDATION
    else -> RestaurantHomeError.UNKNOWN
}
