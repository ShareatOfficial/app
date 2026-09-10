package org.shareat.feature.restauranthome.ui.restauranthome

import org.shareat.app.domain.model.DishCategory
import org.shareat.app.domain.model.EuAllergen
import org.shareat.app.domain.model.RestaurantPublicationState
import org.shareat.feature.restauranthome.ui.model.RestaurantAddressUiState
import org.shareat.feature.restauranthome.ui.model.RestaurantDishUiState
import org.shareat.feature.restauranthome.ui.model.RestaurantHomeContent
import org.shareat.feature.restauranthome.ui.model.RestaurantHomeError
import org.shareat.feature.restauranthome.ui.model.RestaurantHomeMode
import org.shareat.feature.restauranthome.ui.model.RestaurantHomeUiState
import org.shareat.feature.restauranthome.ui.model.RestaurantHomeData
import org.shareat.feature.restauranthome.ui.model.RestaurantHomeEditor
import org.shareat.feature.restauranthome.ui.model.RestaurantEditFormUiState
import org.shareat.feature.restauranthome.ui.model.DishEditFormUiState

internal object RestaurantHomePreviewData {
    private val restaurant = RestaurantHomeData(
        id = "casa-naranja",
        name = "Casa Naranja",
        description = "Cocina de temporada, producto local y platos pensados para compartir.",
        imageUrl = null,
        imageDescription = null,
        address = RestaurantAddressUiState("Calle del Olmo, 18", "Madrid", "28012", null, "ES"),
        ratingLabel = "4,8",
        reviewCount = 128,
        publicationState = RestaurantPublicationState.Published,
        categories = DishCategory.entries.toList(),
        allergens = listOf(EuAllergen.CerealsContainingGluten, EuAllergen.Milk, EuAllergen.Nuts),
        dishes = listOf(
            RestaurantDishUiState(
                id = "pizza-naranja", name = "Pizza Naranja", description = "Mozzarella, calabaza asada y romero.",
                imageUrl = null, imageDescription = null, priceMinorUnits = 1450,
                ratingLabel = "4,7", reviewCount = 32,
                allergens = setOf(EuAllergen.CerealsContainingGluten, EuAllergen.Milk),
                category = DishCategory.MainCourses, isEnabled = true, isMenuItemEnabled = true,
            ),
            RestaurantDishUiState(
                id = "tarta", name = "Tarta de almendra", description = "Con naranja amarga y crema ligera.",
                imageUrl = null, imageDescription = null, priceMinorUnits = 750,
                ratingLabel = null, reviewCount = 0,
                allergens = setOf(EuAllergen.Nuts), category = DishCategory.Desserts,
                isEnabled = false, isMenuItemEnabled = false,
            ),
        ),
    )

    val loaded = RestaurantHomeUiState(content = RestaurantHomeContent.Loaded(restaurant))
    val preview = loaded.copy(mode = RestaurantHomeMode.CUSTOMER_PREVIEW)
    val error = RestaurantHomeUiState(content = RestaurantHomeContent.Error(RestaurantHomeError.OFFLINE))
    val empty = RestaurantHomeUiState(content = RestaurantHomeContent.Empty)
    val restaurantEditor = loaded.copy(
        editor = RestaurantHomeEditor.Restaurant(
            RestaurantEditFormUiState(
                name = restaurant.name,
                streetLine = restaurant.address.streetLine,
                locality = restaurant.address.locality,
                postalCode = restaurant.address.postalCode,
                region = restaurant.address.region.orEmpty(),
                description = restaurant.description.orEmpty(),
                imageUrl = restaurant.imageUrl,
            ),
        ),
    )
    val newDishEditor = loaded.copy(
        editor = RestaurantHomeEditor.Dish(
            DishEditFormUiState(
                name = "",
                description = "",
                price = "",
                allergens = emptySet(),
                imageUrl = null,
                isPublished = false,
            ),
        ),
    )
    val editDishEditor = loaded.copy(
        editor = RestaurantHomeEditor.Dish(
            DishEditFormUiState(
                dishId = restaurant.dishes.first().id,
                name = restaurant.dishes.first().name,
                description = restaurant.dishes.first().description.orEmpty(),
                price = "14,50",
                allergens = restaurant.dishes.first().allergens,
                imageUrl = restaurant.dishes.first().imageUrl,
                isPublished = true,
            ),
        ),
    )
}
