package org.shareat.feature.restaurant.ui.model

import kotlinx.serialization.Serializable
import org.shareat.app.domain.model.DishCategory
import org.shareat.app.domain.model.EuAllergen

@Serializable
data class RestaurantArgs(
    val id: String,
    val name: String,
    val address: String,
    val isOpen: Boolean,
    val heroImageUrl: String? = "https://www.aragondigital.es/articulo/zaragoza/pizzeria-que-sigue-conquistando-zaragoza-abre-cuarto-local-celebra-comidas-gratis/202608271013371004086.html",
    val heroImageDescription: String? = null,
    val description: String? = null,
    val cuisineLabel: String? = null,
    val priceRangeLabel: String? = null,
    val isVerified: Boolean = false,
    val ratingLabel: String? = null,
    val reviewCount: Int = 0,
    val dishes: List<DishArgs> = emptyList(),
)

@Serializable
data class DishArgs(
    val id: String,
    val name: String,
    val priceLabel: String,
    val description: String? = null,
    val imageUrl: String? = null,
    val reviews: List<DishReviewArgs> = emptyList(),
    val category: DishCategory? = null,
    val allergens: List<EuAllergen> = emptyList(),
    val declaresAllergens: Boolean = false,
)

@Serializable
data class DishReviewArgs(
    val id: String,
    val rating: Int,
    val comment: String? = null,
)
