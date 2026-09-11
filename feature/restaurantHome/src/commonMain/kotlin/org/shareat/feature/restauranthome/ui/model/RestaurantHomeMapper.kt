package org.shareat.feature.restauranthome.ui.model

import org.shareat.app.domain.model.AllergenDeclaration
import org.shareat.app.domain.model.AllergenInformationSource
import org.shareat.app.domain.model.Money
import org.shareat.app.domain.model.RatingSummary
import org.shareat.feature.restauranthome.domain.model.OwnerRatedMenuDish
import org.shareat.feature.restauranthome.domain.model.OwnerRestaurantHome

internal fun OwnerRestaurantHome.toRestaurantHomeData(): RestaurantHomeData = RestaurantHomeData(
    id = restaurant.id.value,
    name = restaurant.name,
    description = restaurant.description,
    imageUrl = restaurant.heroImage?.url,
    imageDescription = restaurant.heroImage?.alternativeText,
    address = RestaurantAddressUiState(
        streetLine = restaurant.address?.streetLine.orEmpty(),
        locality = restaurant.address?.locality.orEmpty(),
        postalCode = restaurant.address?.postalCode.orEmpty(),
        region = restaurant.address?.region,
        countryCode = restaurant.address?.countryCode ?: "ES",
    ),
    ratingLabel = restaurantRatingSummary.toRatingLabel(),
    reviewCount = restaurantRatingSummary.ratingCount,
    publicationState = restaurant.publicationState,
    categories = dishCategories.sortedBy { it.ordinal },
    allergens = menu?.dishes
        ?.flatMap { it.menuDish.dish.allergenDeclaration?.allergens.orEmpty() }
        ?.distinct()
        ?.sortedBy { it.ordinal }
        .orEmpty(),
    dishes = menu?.dishes.orEmpty().map(OwnerRatedMenuDish::toDishUiState),
)

internal fun OwnerRatedMenuDish.toDishUiState(): RestaurantDishUiState = RestaurantDishUiState(
    id = menuDish.dish.id.value,
    name = menuDish.dish.name,
    description = menuDish.dish.description,
    imageUrl = menuDish.dish.image?.url,
    imageDescription = menuDish.dish.image?.alternativeText,
    priceMinorUnits = menuDish.price.minorUnits,
    ratingLabel = ratingSummary.toRatingLabel(),
    reviewCount = ratingSummary.ratingCount,
    allergens = menuDish.dish.allergenDeclaration?.allergens.orEmpty(),
    category = menuDish.category,
    isEnabled = menuDish.dish.isEnabled,
    isMenuItemEnabled = menuDish.isEnabled,
)

internal fun RatingSummary.toRatingLabel(): String? = averageTenths?.let { "${it / 10},${it % 10}" }

internal fun Long.toEditablePrice(): String = buildString {
    append(this@toEditablePrice / 100)
    val cents = this@toEditablePrice % 100
    if (cents != 0L) append(',').append(cents.toString().padStart(2, '0'))
}

internal fun String.toMoneyOrNull(): Money? {
    val normalized = trim().replace(',', '.')
    if (!Regex("^\\d+(?:\\.\\d{1,2})?$").matches(normalized)) return null
    val parts = normalized.split('.')
    val euros = parts[0].toLongOrNull() ?: return null
    val cents = parts.getOrNull(1)?.padEnd(2, '0')?.toLongOrNull() ?: 0L
    return runCatching { Money(minorUnits = euros * 100 + cents) }.getOrNull()
}

internal fun Set<org.shareat.app.domain.model.EuAllergen>.toAllergenDeclaration(): AllergenDeclaration? =
    takeIf { it.isNotEmpty() }?.let { allergens ->
        AllergenDeclaration(allergens = allergens, source = AllergenInformationSource.Restaurant)
    }
