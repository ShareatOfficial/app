package org.shareat.feature.home.domain

import org.shareat.app.domain.model.AccountId
import org.shareat.app.domain.model.Dish
import org.shareat.app.domain.model.DishId
import org.shareat.app.domain.model.ImageRef
import org.shareat.app.domain.model.IsoTimestamp
import org.shareat.app.domain.model.PostalAddress
import org.shareat.app.domain.model.Rating
import org.shareat.app.domain.model.RatingSummary
import org.shareat.app.domain.model.Restaurant
import org.shareat.app.domain.model.RestaurantId
import org.shareat.app.domain.model.RestaurantPublicationState
import org.shareat.app.domain.model.Review
import org.shareat.app.domain.model.ReviewId
import org.shareat.app.domain.model.ReviewModerationStatus
import org.shareat.app.domain.model.ReviewTarget
import org.shareat.app.domain.model.ReviewVisibility
import org.shareat.app.domain.model.WeeklyOpeningHours
import org.shareat.app.domain.repository.RepositoryResult

private data class RestaurantTemplate(val name: String, val locality: String, val streetLine: String)

private data class DishTemplate(val dishName: String, val comment: String, val rating: Int)

private val restaurantTemplates = listOf(
    RestaurantTemplate("Casa Naranja", "Madrid", "Calle del Olmo, 18"),
    RestaurantTemplate("Bodega del Puerto", "Barcelona", "Passeig Marítim, 4"),
    RestaurantTemplate("La Taberna Azul", "Sevilla", "Calle Sierpes, 22"),
    RestaurantTemplate("El Rincón Manchego", "Toledo", "Plaza Zocodover, 7"),
    RestaurantTemplate("Mar y Sol", "Valencia", "Avenida del Puerto, 55"),
    RestaurantTemplate("Fuego Norte", "Bilbao", "Gran Vía, 12"),
    RestaurantTemplate("Huerta Verde", "Zaragoza", "Paseo Independencia, 30"),
    RestaurantTemplate("Sabores del Sur", "Málaga", "Calle Larios, 9"),
    RestaurantTemplate("La Vieja Estación", "Córdoba", "Avenida América, 3"),
    RestaurantTemplate("Puerta Dorada", "Granada", "Calle Reyes Católicos, 15"),
    RestaurantTemplate("Almazara", "Jaén", "Paseo de la Estación, 21"),
    RestaurantTemplate("Costa Brava", "Girona", "Rambla de la Llibertat, 6"),
    RestaurantTemplate("El Fogón Criollo", "Alicante", "Explanada de España, 2"),
    RestaurantTemplate("La Marisquería", "Santander", "Paseo Pereda, 11"),
    RestaurantTemplate("Trigo y Sal", "Salamanca", "Plaza Mayor, 5"),
    RestaurantTemplate("Brasa Vieja", "Oviedo", "Calle Uría, 27"),
    RestaurantTemplate("Del Monte", "Cáceres", "Plaza Mayor, 1"),
    RestaurantTemplate("Raíces", "Pamplona", "Calle Estafeta, 14"),
    RestaurantTemplate("El Timón", "Cádiz", "Calle Ancha, 8"),
    RestaurantTemplate("La Cazuela", "Vigo", "Calle Príncipe, 19"),
)

private val dishTemplates = listOf(
    DishTemplate("Pulpo a la brasa", "Tiernísimo y con el punto justo de humo.", 5),
    DishTemplate("Croquetas de jamón ibérico", "Cremosas y recién hechas.", 4),
    DishTemplate("Arroz de marisco", "Meloso y con sabor a mar de verdad.", 5),
)

/** Stand-in Home feed until the backend supports paginated, enriched restaurant listing. */
internal object FakeHomeRestaurantCatalog {
    val result: RepositoryResult<List<RestaurantWithHighlights>> = RepositoryResult.Success(
        restaurantTemplates.mapIndexed { index, template -> template.toRestaurantWithHighlights(index) },
    )
}

private fun RestaurantTemplate.toRestaurantWithHighlights(index: Int): RestaurantWithHighlights {
    val restaurant = Restaurant(
        id = RestaurantId("mock-restaurant-$index"),
        ownerAccountId = AccountId("mock-owner-$index"),
        name = name,
        heroImage = ImageRef(
            url = "https://images.example.com/restaurants/mock-$index.jpg",
            alternativeText = "Interior de $name",
        ),
        address = PostalAddress(
            streetLine = streetLine,
            locality = locality,
            postalCode = "28${(index % 100).toString().padStart(3, '0')}",
        ),
        openingHours = WeeklyOpeningHours(emptyList()),
        publicationState = RestaurantPublicationState.Published,
    )
    return RestaurantWithHighlights(
        restaurant = restaurant,
        ratingSummary = RatingSummary(averageTenths = 40 + index % 11, ratingCount = 10 + index),
        dishHighlights = dishTemplates.mapIndexed { dishIndex, dish -> dish.toHighlight(restaurant.id, index, dishIndex) },
        isOpen = index % 3 != 0,
    )
}

private fun DishTemplate.toHighlight(
    restaurantId: RestaurantId,
    restaurantIndex: Int,
    dishIndex: Int,
): DishReviewHighlight {
    val dishId = DishId("mock-dish-$restaurantIndex-$dishIndex")
    val dish = Dish(id = dishId, restaurantId = restaurantId, name = dishName, isEnabled = true)
    val review = Review(
        id = ReviewId("mock-review-$restaurantIndex-$dishIndex"),
        authorAccountId = AccountId("mock-author-$restaurantIndex-$dishIndex"),
        target = ReviewTarget.Dish(dishId),
        rating = Rating(rating),
        comment = comment,
        visibility = ReviewVisibility.Public,
        moderationStatus = ReviewModerationStatus.Visible,
        createdAt = IsoTimestamp("2026-08-10T08:35:00Z"),
        updatedAt = IsoTimestamp("2026-08-10T08:35:00Z"),
    )
    return DishReviewHighlight(dish = dish, review = review)
}
