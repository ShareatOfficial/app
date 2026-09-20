package org.shareat.feature.review

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.stringResource
import shareat.feature.review.generated.resources.Res
import shareat.feature.review.generated.resources.dish_review_error_already_exists
import shareat.feature.review.generated.resources.dish_review_error_credentials
import shareat.feature.review.generated.resources.dish_review_error_forbidden
import shareat.feature.review.generated.resources.dish_review_error_generic
import shareat.feature.review.generated.resources.dish_review_error_not_found
import shareat.feature.review.generated.resources.dish_review_error_offline
import shareat.feature.review.generated.resources.dish_review_error_session
import shareat.feature.review.generated.resources.dish_review_error_unavailable

@Composable
internal fun DishReviewError.label(): String = stringResource(
    when (this) {
        DishReviewError.INVALID_CREDENTIALS -> Res.string.dish_review_error_credentials
        DishReviewError.OFFLINE -> Res.string.dish_review_error_offline
        DishReviewError.UNAUTHENTICATED -> Res.string.dish_review_error_session
        DishReviewError.FORBIDDEN -> Res.string.dish_review_error_forbidden
        DishReviewError.TEMPORARILY_UNAVAILABLE -> Res.string.dish_review_error_unavailable
        DishReviewError.ALREADY_EXISTS -> Res.string.dish_review_error_already_exists
        DishReviewError.NOT_FOUND -> Res.string.dish_review_error_not_found
        DishReviewError.UNKNOWN -> Res.string.dish_review_error_generic
    },
)
