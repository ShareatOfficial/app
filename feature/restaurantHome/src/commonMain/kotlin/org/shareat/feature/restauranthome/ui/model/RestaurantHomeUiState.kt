package org.shareat.feature.restauranthome.ui.model

import org.shareat.app.domain.model.DishCategory
import org.shareat.app.domain.model.EuAllergen
import org.shareat.app.domain.model.ImageUpload
import org.shareat.app.domain.model.RestaurantPublicationState

/** The owner can switch between managing the catalogue and its public-facing simulation. */
enum class RestaurantHomeMode { MANAGEMENT, CUSTOMER_PREVIEW }

data class RestaurantHomeUiState(
    val content: RestaurantHomeContent = RestaurantHomeContent.Loading,
    val mode: RestaurantHomeMode = RestaurantHomeMode.MANAGEMENT,
    val selectedCategory: DishCategory? = null,
    val excludedAllergens: Set<EuAllergen> = emptySet(),
    val editor: RestaurantHomeEditor? = null,
)

sealed interface RestaurantHomeContent {
    data object Loading : RestaurantHomeContent
    data class Error(val reason: RestaurantHomeError) : RestaurantHomeContent
    data object Empty : RestaurantHomeContent
    data class Loaded(val restaurant: RestaurantHomeData) : RestaurantHomeContent
}

enum class RestaurantHomeError {
    OFFLINE,
    UNAUTHENTICATED,
    FORBIDDEN,
    NOT_FOUND,
    TEMPORARILY_UNAVAILABLE,
    VALIDATION,
    IMAGE_FORMAT_UNSUPPORTED,
    IMAGE_TOO_LARGE,
    IMAGE_READ_FAILED,
    UNKNOWN,
}

data class RestaurantHomeData(
    val id: String,
    val name: String,
    val description: String?,
    val imageUrl: String?,
    val imageDescription: String?,
    val address: RestaurantAddressUiState,
    val ratingLabel: String?,
    val reviewCount: Int,
    val publicationState: RestaurantPublicationState,
    val categories: List<DishCategory>,
    val allergens: List<EuAllergen>,
    val dishes: List<RestaurantDishUiState>,
) {
    val isPublished: Boolean get() = publicationState == RestaurantPublicationState.Published
}

data class RestaurantAddressUiState(
    val streetLine: String,
    val locality: String,
    val postalCode: String,
    val region: String?,
    val countryCode: String,
)

data class RestaurantDishUiState(
    val id: String,
    val name: String,
    val description: String?,
    val imageUrl: String?,
    val imageDescription: String?,
    val priceMinorUnits: Long,
    val ratingLabel: String?,
    val reviewCount: Int,
    val allergens: Set<EuAllergen>,
    val category: DishCategory?,
    val isEnabled: Boolean,
    val isMenuItemEnabled: Boolean,
) {
    val isPublished: Boolean get() = isEnabled && isMenuItemEnabled
}

sealed interface RestaurantHomeEditor {
    data class Restaurant(val form: RestaurantEditFormUiState) : RestaurantHomeEditor
    data class Dish(val form: DishEditFormUiState) : RestaurantHomeEditor
}

data class RestaurantEditFormUiState(
    val name: String,
    val streetLine: String,
    val locality: String,
    val postalCode: String,
    val region: String,
    val description: String,
    val imageUrl: String?,
    val pendingImageUpload: ImageUpload? = null,
    val isSaving: Boolean = false,
    val validation: RestaurantFormValidation = RestaurantFormValidation(),
    val error: RestaurantHomeError? = null,
)

data class RestaurantFormValidation(
    val nameInvalid: Boolean = false,
    val streetInvalid: Boolean = false,
    val localityInvalid: Boolean = false,
    val postalCodeInvalid: Boolean = false,
)

data class DishEditFormUiState(
    val dishId: String? = null,
    val name: String,
    val description: String,
    val price: String,
    val allergens: Set<EuAllergen>,
    val imageUrl: String?,
    val pendingImageUpload: ImageUpload? = null,
    val isPublished: Boolean,
    val isSaving: Boolean = false,
    val validation: DishFormValidation = DishFormValidation(),
    val error: RestaurantHomeError? = null,
)

data class DishFormValidation(
    val nameInvalid: Boolean = false,
    val priceInvalid: Boolean = false,
)
