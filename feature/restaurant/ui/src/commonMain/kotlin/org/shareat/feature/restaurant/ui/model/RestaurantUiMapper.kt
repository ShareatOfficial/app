package org.shareat.feature.restaurant.ui.model

import org.shareat.app.domain.model.Dish
import org.shareat.app.domain.model.DishCategory
import org.shareat.app.domain.model.EuAllergen
import org.shareat.app.domain.model.Money
import org.shareat.app.domain.model.Review
import org.shareat.app.domain.usecase.RatedMenuDish
import org.shareat.app.domain.usecase.RestaurantDetails

fun RestaurantDetails.toArgs(): RestaurantArgs = RestaurantArgs(
    id = restaurant.id.value,
    name = restaurant.name,
    address = "${restaurant.address.streetLine}, ${restaurant.address.locality}",
    isOpen = isOpen,
    heroImageUrl = restaurant.heroImage?.url,
    heroImageDescription = restaurant.heroImage?.alternativeText,
    description = restaurant.description,
    ratingLabel = ratingSummary.averageTenths?.toRatingLabel(),
    reviewCount = ratingSummary.ratingCount,
    dishes = menu?.dishes.orEmpty().map(RatedMenuDish::toArgs),
)

private fun RatedMenuDish.toArgs(): DishArgs = DishArgs(
    id = menuDish.dish.id.value,
    name = menuDish.dish.name,
    priceLabel = menuDish.price.toPriceLabel(),
    description = menuDish.dish.description,
    imageUrl = menuDish.dish.image?.url,
    reviews = reviews.map(Review::toArgs),
    category = menuDish.category,
    allergens = menuDish.dish.declaredAllergens(),
    declaresAllergens = menuDish.dish.allergenDeclaration != null,
)

private fun Review.toArgs(): DishReviewArgs = DishReviewArgs(
    id = id.value,
    rating = rating.value,
    comment = comment,
)

private fun Dish.declaredAllergens(): List<EuAllergen> =
    allergenDeclaration?.allergens.orEmpty().sortedBy(EuAllergen::ordinal)

fun RestaurantArgs.toUiState(
    selection: RestaurantSelection = RestaurantSelection(),
    isRefreshing: Boolean = false,
    errorMessage: String? = null,
    dishMatchesFilters: (DishArgs) -> Boolean = { true },
): RestaurantUiState = RestaurantUiState(
    header = toHeaderUiState(),
    categories = toCategoryChips(selection.category),
    allergenFilter = AllergenFilterUiState(
        allergens = declaredAllergens().map { allergen ->
            AllergenChipUiState(allergen, isExcluded = allergen in selection.excludedAllergens)
        },
        isExpanded = selection.isAllergenFilterExpanded,
    ),
    dishes = dishes
        .filter(dishMatchesFilters)
        .map { dish ->
            dish.toCardUiState(
                isExpanded = dish.id in selection.expandedDishIds,
                selectedRating = selection.dishRatings[dish.id],
            )
        },
    hasPublishedMenu = dishes.isNotEmpty(),
    isRefreshing = isRefreshing,
    errorMessage = errorMessage,
)

fun RestaurantArgs.declaredAllergens(): List<EuAllergen> = dishes
    .flatMap(DishArgs::allergens)
    .distinct()
    .sortedBy(EuAllergen::ordinal)

private fun RestaurantArgs.toHeaderUiState(): RestaurantHeaderUiState = RestaurantHeaderUiState(
    name = name,
    address = address,
    heroImageUrl = heroImageUrl,
    heroImageDescription = heroImageDescription,
    description = description,
    cuisineLabel = cuisineLabel,
    priceRangeLabel = priceRangeLabel,
    isVerified = isVerified,
    ratingLabel = ratingLabel,
    reviewCount = reviewCount,
)

private fun RestaurantArgs.toCategoryChips(selected: DishCategory?): List<CategoryChipUiState> {
    val categories = dishes.mapNotNull(DishArgs::category).distinct().sortedBy(DishCategory::ordinal)
    if (categories.isEmpty()) return emptyList()
    return (listOf(null) + categories).map { category ->
        CategoryChipUiState(category = category, isSelected = category == selected)
    }
}

private fun DishArgs.toCardUiState(
    isExpanded: Boolean,
    selectedRating: Int?,
): DishCardUiState = DishCardUiState(
    id = id,
    name = name,
    priceLabel = priceLabel,
    description = description,
    imageUrl = imageUrl,
    reviews = reviews.map(DishReviewArgs::toUiState),
    allergens = allergens,
    selectedRating = selectedRating,
    isExpanded = isExpanded,
)

private fun DishReviewArgs.toUiState(): DishReviewUiState = DishReviewUiState(
    id = id,
    rating = rating,
    comment = comment,
)

internal fun Money.toPriceLabel(): String {
    val units = minorUnits / 100
    val cents = minorUnits % 100
    return if (cents == 0L) "$units€" else "$units,${cents.toString().padStart(2, '0')}€"
}

internal fun Int.toRatingLabel(): String = "${this / 10},${this % 10}"
