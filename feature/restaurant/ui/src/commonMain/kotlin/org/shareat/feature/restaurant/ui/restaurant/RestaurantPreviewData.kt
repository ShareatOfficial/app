package org.shareat.feature.restaurant.ui.restaurant

import org.shareat.app.domain.model.DishCategory
import org.shareat.app.domain.model.EuAllergen
import org.shareat.feature.restaurant.ui.model.DishArgs
import org.shareat.feature.restaurant.ui.model.DishReviewArgs
import org.shareat.feature.restaurant.ui.model.RestaurantArgs
import org.shareat.feature.restaurant.ui.model.RestaurantSelection
import org.shareat.feature.restaurant.ui.model.toUiState

internal object RestaurantPreviewData {

    val args = RestaurantArgs(
        id = "restaurant-casa-naranja",
        name = "The Rustic Spoon",
        address = "Calle del Olmo, 18, Madrid",
        isOpen = true,
        heroImageUrl = "https://images.example.com/restaurants/casa-naranja.jpg",
        heroImageDescription = "Interior del restaurante",
        description = "Texto descriptivo del restaurante con producto local, cocina de temporada " +
            "y platos pensados para compartir.",
        cuisineLabel = "Modern European",
        priceRangeLabel = "$$ · Moderate",
        isVerified = true,
        ratingLabel = "4,8",
        reviewCount = 1_284,
        dishes = dishes(),
    )

    val loaded = args.toUiState()

    val refreshing = args.toUiState(isRefreshing = true)

    val filteredEmpty = args.toUiState(
        selection = RestaurantSelection(
            category = DishCategory.Desserts,
            excludedAllergens = setOf(EuAllergen.CerealsContainingGluten),
            isAllergenFilterExpanded = true,
        ),
        dishMatchesFilters = { false },
    )

    val withoutMenu = args.copy(dishes = emptyList()).toUiState()

    private fun dishes(): List<DishArgs> = List(4) { index ->
        DishArgs(
            id = "dish-$index",
            name = "Margherita Verace",
            priceLabel = "18€",
            description = "San Marzano tomato sauce, fresh mozzarella di bufala, albahaca fresca.",
            imageUrl = "https://images.example.com/dishes/margherita.jpg",
            reviews = reviews(index),
            category = DishCategory.entries[index % DishCategory.entries.size],
            allergens = listOf(EuAllergen.CerealsContainingGluten, EuAllergen.Fish, EuAllergen.Soybeans),
            declaresAllergens = true,
        )
    }

    private fun reviews(dishIndex: Int): List<DishReviewArgs> = when (dishIndex) {
        0 -> emptyList()
        1 -> List(3) { DishReviewArgs(id = "review-$dishIndex-$it", rating = 4) }
        else -> listOf(
            DishReviewArgs("review-$dishIndex-0", rating = 5, comment = "La mejor pizza del barrio."),
            DishReviewArgs("review-$dishIndex-1", rating = 4, comment = "Masa perfecta, un poco justa de sal."),
            DishReviewArgs("review-$dishIndex-2", rating = 4),
        )
    }
}
