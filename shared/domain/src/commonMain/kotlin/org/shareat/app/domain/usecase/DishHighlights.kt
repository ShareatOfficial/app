package org.shareat.app.domain.usecase

import org.shareat.app.domain.model.Dish
import org.shareat.app.domain.model.DishId
import org.shareat.app.domain.model.Review

private const val MaxDishHighlightsPerRestaurant = 3

internal fun List<Dish>.toDishHighlights(
    reviewsByDish: Map<DishId, List<Review>>,
): List<DishReviewHighlight> = this
    .flatMap { dish ->
        reviewsByDish[dish.id].orEmpty().filter { it.comment != null }.map { it to dish }
    }
    .sortedByDescending { (review, _) -> review.createdAt.value }
    .take(MaxDishHighlightsPerRestaurant)
    .map { (review, dish) -> DishReviewHighlight(dish, review) }
