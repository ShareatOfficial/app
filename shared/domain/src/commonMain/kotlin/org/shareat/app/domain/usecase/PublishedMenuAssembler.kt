package org.shareat.app.domain.usecase

import org.shareat.app.domain.model.MenuDetails
import org.shareat.app.domain.model.RestaurantId
import org.shareat.app.domain.repository.MenuRepository
import org.shareat.app.domain.repository.RepositoryError
import org.shareat.app.domain.repository.RepositoryResult
import org.shareat.app.domain.repository.ReviewRepository

class PublishedMenuAssembler(
    private val menuRepository: MenuRepository,
    private val reviewRepository: ReviewRepository,
) {
    suspend fun assemble(restaurantId: RestaurantId): RepositoryResult<RestaurantMenu?> =
        when (val result = menuRepository.getPublishedMenu(restaurantId)) {
            is RepositoryResult.Success -> RepositoryResult.Success(result.value.rated())
            is RepositoryResult.Failure ->
                if (result.error is RepositoryError.NotFound) RepositoryResult.Success(null) else result
        }

    private suspend fun MenuDetails.rated(): RestaurantMenu {
        val sellableDishes = items
            .filter { it.isEnabled && it.dish.isEnabled }
            .sortedBy { it.position }
        val reviewsByDish = when (
            val result = reviewRepository.getPublicDishReviews(sellableDishes.mapTo(mutableSetOf()) { it.dish.id })
        ) {
            is RepositoryResult.Success -> result.value
            is RepositoryResult.Failure -> emptyMap()
        }
        return RestaurantMenu(
            menu = menu,
            dishes = sellableDishes.map { menuDish ->
                RatedMenuDish(
                    menuDish = menuDish,
                    reviews = reviewsByDish[menuDish.dish.id].orEmpty(),
                )
            },
        )
    }
}
